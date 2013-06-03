/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 *
 *  This file is part of DVB Inspector.
 *
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */

package nl.digitalekabeltelevisie.data.mpeg;

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.packet_length;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.AC3Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.AncillaryDataDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.CADescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.EnhancedAC3Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.SubtitlingDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.TeletextDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.VBIDataDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.AC3Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.EAC3Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.Audio138183Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.DVBSubtitleHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ebu.EBUTeletextHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.Video138182Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video264.Video14496Handler;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTs;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDTsection;
import nl.digitalekabeltelevisie.util.JTreeLazyList;
import nl.digitalekabeltelevisie.util.PositionPushbackInputStream;
import nl.digitalekabeltelevisie.util.Utils;


/**
 * TransportStream is responsible for parsing a file containing a transport stream, dividing it into 188 byte {@link TSPackets}, and handing them over to the correct PID.
 *
 */
public class TransportStream implements TreeNode{


	/**
	 *
	 */
	public static final int TRANSPORT_ERROR_FLAG = 0x8000;
	public static final int ADAPTATION_FIELD_FLAG = 0x2000;
	public static final int PAYLOAD_UNIT_START_FLAG = 0x4000;


	private static final Logger logger = Logger.getLogger(TransportStream.class.getName());

	/**
	 * used when building DescriptorLists, for transportStreams that do use private descriptors, but (incorrectly) do not use a PrivateDataSpecifierDescriptor
	 * @see DescriptorFactory
	 */
	private long defaultPrivateDataSpecifier = 0;

	private boolean enableTSPackets = false;

	/**
	 * File containing data of this TransportStream
	 */
	private File file;
	/**
	 * after reading a TSPAcket from the file, it is handed over to the respective PID for aggregating into larger PES or PSI sections, and further processing.
	 */
	private PID [] pids = new PID [8192];
	/**
	 * for every TSPacket read, store it's packet_id. Used for bit rate calculations, and Grid View
	 */
	private final short [] packet_pid;

	/**
	 * for every TSPacket read, store it's offset in the file
	 */
	private long [] packet_offset =null;
	/**
	 * Starting point for all the PSI information in this TransportStream
	 */
	private PSI psi = new PSI();
	/**
	 * how many TSPackets have bean read.
	 */
	private int no_packets = 0;
	/**
	 * number of TSPackets that had Transport Error Indicator set.
	 */
	private int error_packets = 0;
	/**
	 * Bitrate based on the average of all PIDs that contain a PCR. This is the most accurate way to calculate the bit rate.
	 */
	private long bitRate = -1;
	/**
	 * for streams that have no PIDS with PCRs (empty transport streams) this value is calculated based on the number of bytes between different occurences of the TDT table
	 */
	private long bitRateTDT = -1;
	/**
	 * time at which this transportStream started. Calculated by calculating backwards from first TDT, using bitrate. null if no TDT found
	 */
	private Calendar zeroTime = null;

	/**
	 * Not used
	 */
	private Bouquet bouquet = null;

	private long len;

	private int max_packets;

	/**
	 *
	 * Creates a new Transport stream based on the supplied file. After construction the TransportStream is not complete, first parseStream() has to be called!
	 * @param fileName name of the file to be read (null not permitted).
	 */
	public TransportStream(final String fileName) {
		this(new File(fileName));
	}



	/**
	 *
	 * Creates a new Transport stream based on the supplied file. After construction the TransportStream is not complete, first parseStream() has to be called!
	 * @param file the file to be read (null not permitted). Don't enable TSPackets by default.
	 */



	public TransportStream(final File file) {
		this.file = file;
		len = file.length();
		max_packets = (int)(len / packet_length);
		packet_pid = new short [max_packets];

	}

	/**
	 * read the file, and parse it. Packets are counted, bitrate calculated, etc. Used for initial construction. PES data is not analyzed.
	 * @throws IOException
	 */
	public void parseStream() throws IOException {
		final PositionPushbackInputStream fileStream = getInputStream();
		final byte [] buf = new byte[packet_length];
		long count=0;
		no_packets = 0;

		pids = new PID [8192];
		psi = new PSI();
		error_packets = 0;
		bitRate = -1;
		bitRateTDT = -1;
		if(enableTSPackets){
			packet_offset = new long [max_packets];
		}else{
			packet_offset = null;
		}

		int bytes_read =0;
		do {
			long offset = fileStream.getPosition();
			bytes_read = fileStream.read(buf, 0, MPEGConstants.packet_length);
			final int next = fileStream.read();
			if((bytes_read==MPEGConstants.packet_length)&&
					(buf[0]==MPEGConstants.sync_byte) &&
					((next==-1)||(next==MPEGConstants.sync_byte))) {
				//always push back first byte of next packet
				if((next!=-1)) {
					fileStream.unread(next);
				}

				TSPacket packet = new TSPacket(buf, count,this);
				final short pid = packet.getPID();
				short pidFlags = pid;
				if(packet.hasAdaptationField()){
					pidFlags = (short) (pidFlags | ADAPTATION_FIELD_FLAG);
				}
				if(packet.isPayloadUnitStartIndicator()){
					pidFlags = (short) (pidFlags | PAYLOAD_UNIT_START_FLAG);
				}
				if(packet.isTransportErrorIndicator()){
					pidFlags = (short) (pidFlags | TRANSPORT_ERROR_FLAG);
				}
				packet_pid[no_packets]=pidFlags;
				if(packet_offset!=null){
					packet_offset[no_packets]=offset;
				}
				no_packets++;
				if(!packet.isTransportErrorIndicator()){
					if(pids[pid]==null) {
						pids[pid] = new PID(pid,this);
					}
					pids[pid].update_packet(packet);
					packet=null;
				}else{
					error_packets++;
					logger.warning("TransportErrorIndicator set for packet "+ packet);
					packet=null;
				}
				count++;
			}else{ // something wrong, find next syncbyte. First push back the lot
				if((next!=-1)) {
					fileStream.unread(next);
					fileStream.unread(buf, 0, bytes_read);
					// now read 1 byte and restart all
					fileStream.read(); //ignore result
				}
			}
		} while (bytes_read==MPEGConstants.packet_length);
		namePIDs();
	}

	/**
	 *
	 * Read the file, and parse only the packets for which a GeneralPesHandler is present in toParsePids. Used for analyzing PESdata, like a video, teletext or subtitle stream
	 * @param toParsePids Map with an entry for each PID that should be parsed, and a handler that knows how to interpret the data
	 *
	 * TODO: optimize to read only the packets needed for these Pids, using seek(). We now have the file offset, use it!
	 *
	 * @throws IOException
	 */
	public void parseStream(final Map<Integer,GeneralPesHandler> toParsePids) throws IOException {
		if((toParsePids==null)||(toParsePids.isEmpty())){
			return;
		}
		final PositionPushbackInputStream fileStream = getInputStream();
		final byte [] buf = new byte[MPEGConstants.packet_length];
		long count=0;


		int bytes_read =0;
		do {
			bytes_read = fileStream.read(buf, 0, MPEGConstants.packet_length);
			final int next = fileStream.read();
			if((bytes_read==MPEGConstants.packet_length)&&
					(buf[0]==MPEGConstants.sync_byte) &&
					((next==-1)||(next==MPEGConstants.sync_byte))) {
				//always push back first byte of next packet
				if((next!=-1)) {
					fileStream.unread(next);
				}

				TSPacket packet = new TSPacket(buf, count,this);
				if(!packet.isTransportErrorIndicator()){
					final int pid = packet.getPID();
					final GeneralPesHandler handler = toParsePids.get(pid);
					if(handler!=null){
						handler.processTSPacket(packet);
					}

					packet=null;
				}else{
					error_packets++;
					logger.warning("TransportErrorIndicator set for packet "+ packet);
				}
				count++;
			}else{ // something wrong, find next syncbyte. First push back the lot
				if((next!=-1)) {
					fileStream.unread(next);
					fileStream.unread(buf, 0, bytes_read);
					// now read 1 byte and restart all
					fileStream.read(); //ignore result
				}
			}
		} while (bytes_read==MPEGConstants.packet_length);
	}


	private PositionPushbackInputStream getInputStream() throws IOException{
		final InputStream is = new FileInputStream(file);
		return new PositionPushbackInputStream(new BufferedInputStream(is),200);
	}



	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append("Transportstream :").append(file.getName()).append('\n');
		for (int i = 0; i < pids.length; i++) {
			final PID pid = pids[i];
			if(pid!=null)
			{
				buf.append("  PID :").append(i).append(", Label:").append(pid.getLabel()).append(", ").append(pid.toString()).append(" packets, ").append((pid.getPackets()*100)/no_packets).append("%, duplicate packets:"+pid.getDup_packets()+"\n");
			}
		}

		return buf.toString();

	}

	public File getFile() {
		return file;
	}

	public void setFile(final File file) {
		this.file = file;
	}

	/**
	 * @return the number of TSPackets read
	 */
	public int getNo_packets() {
		return no_packets;
	}

	public PID[] getPids() {
		return pids;
	}

	public PSI getPsi() {
		return psi;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Transport Stream "+psi.getPat().getTransportStreamId()));

		t.add(new DefaultMutableTreeNode(new KVP("file",file.getPath(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("size",file.length(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("modified",new Date(file.lastModified()).toString(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("TS packets",no_packets,null)));
		t.add(new DefaultMutableTreeNode(new KVP("Error packets",error_packets,null)));
		if(bitRate!=-1){
			t.add(new DefaultMutableTreeNode(new KVP("bitrate",bitRate,null)));
			t.add(new DefaultMutableTreeNode(new KVP("length (secs)",(file.length()*8)/bitRate,null)));
		}
		if(bitRateTDT!=-1){
			t.add(new DefaultMutableTreeNode(new KVP("bitrate based on TDT",bitRateTDT,null)));
			t.add(new DefaultMutableTreeNode(new KVP("length (secs)",(file.length()*8)/bitRateTDT,null)));
		}

		t.add(psi.getJTreeNode(modus));
		if(!psiOnlyModus(modus)){
			final DefaultMutableTreeNode pid = new DefaultMutableTreeNode(new KVP("PIDs"));
			t.add(pid);
			for (final PID pid2 : pids) {
				if((pid2)!=null){
					pid.add(pid2.getJTreeNode(modus));
				}

			}
		}

		if(packet_offset!=null){
			JTreeLazyList list = new JTreeLazyList(this, 100, 0, getNo_packets());
			t.add(list.getJTreeNode(modus, "Transport packets "));
		}

		return t;
	}

	private static void setLabel(final int pidNo, final PID[] pids, final String text)
	{
		if(pids[pidNo]!=null){
			pids[pidNo].setLabel(text);
			pids[pidNo].setShortLabel(text);
		}

	}

	private static void setLabel(final int pidNo, final PID[] pids, final String longText, final String shortText)
	{
		if(pids[pidNo]!=null){
			pids[pidNo].setLabel(longText);
			pids[pidNo].setShortLabel(shortText);
		}

	}

	public void namePIDs() {
		setLabel(0, pids, "PAT");
		setLabel(1, pids, "CAT");
		setLabel(2, pids, "TSDT");

		for (int i = 0x3; i <= 0xf; i++) {
			setLabel(i,pids,"reserved");
		}


		if(pids[1]!=null){
			final Iterator<Descriptor> catIter= getPsi().getCat().getDescriptorList().iterator();
			while (catIter.hasNext()) {
				final Descriptor d = catIter.next();
				if(d instanceof CADescriptor) {
					final CADescriptor cad = (CADescriptor) d;
					final int capid=cad.getCaPID();
					setLabel(capid, pids, "EMM for CA_ID:"+cad.getCaSystemID()+ " ("+Utils.getCASystemIDString(cad.getCaSystemID())+")");
				}
			}
		}

		final Iterator<PMTsection[]> it = getPsi().getPmts().getPmts().values().iterator();
		while (it.hasNext()) {
			final PMTsection[] pmt = it.next();
			PMTsection section = pmt[0];
			while(section!=null){
				final int service_id=section.getProgramNumber();
				String service_name = getPsi().getSdt().getServiceName(service_id);
				if(service_name==null){
					service_name="Service "+service_id;
				}
				final int pmt_pid=section.getParentPID().getPid();
				setLabel(pmt_pid,pids,"PMT for service:"+service_id+" ("+service_name+")","PMT "+service_name);

				final Iterator<Descriptor> i = section.getDescriptorList().iterator();
				while (i.hasNext()) {
					final Descriptor d = i.next();
					if(d instanceof CADescriptor) {
						final CADescriptor cad = (CADescriptor) d;
						final int capid=cad.getCaPID();
						setLabel(capid,pids,"ECM for CA_ID:"+cad.getCaSystemID()+" for service:"+service_id+", ("+service_name+")","ECM "+service_name);
					}
				}

				final Iterator<Component> l = section.getComponentenList().iterator();
				while(l.hasNext()){
					final Component component = l.next();
					GeneralPesHandler abstractPesHandler = null;
					final int comp_pid = component.getElementaryPID();
					final int streamType = component.getStreamtype();
					final StringBuilder compt_type = new StringBuilder(service_name).append(' ').append(getStreamTypeString(streamType));
					final StringBuilder short_compt_type = new StringBuilder(service_name).append(' ').append(getStreamTypeShortString(streamType));
					if((pids[comp_pid]!=null)&&(!pids[comp_pid].isScrambled())&&(pids[comp_pid].getType()==PID.PES)){
						if((streamType==1)||(streamType==2)){
							abstractPesHandler = new Video138182Handler();
						}else if((streamType==3)||(streamType==4)){
							// find Ancillary Data info so we can parse RDS
							int ancillaryData = 0;
							final Iterator<Descriptor> k = component.getComponentDescriptorList().iterator();
							while (k.hasNext()) {
								final Descriptor d = k.next();
								if(d instanceof AncillaryDataDescriptor) {
									final AncillaryDataDescriptor ac= (AncillaryDataDescriptor)d;
									ancillaryData = ac.getAncillaryDataIdentifier();
								}
							}

							abstractPesHandler = new Audio138183Handler(ancillaryData);
						}else if(streamType==0x1B){
							abstractPesHandler = new Video14496Handler();
						}else{
							abstractPesHandler = new GeneralPesHandler();
						}
					}

					final Iterator<Descriptor> k = component.getComponentDescriptorList().iterator();
					while (k.hasNext()) {
						final Descriptor d = k.next();
						if(d instanceof SubtitlingDescriptor) {
							compt_type.append(" DVB subtitling");
							short_compt_type.append("DVB subtitling");
							abstractPesHandler = new DVBSubtitleHandler();
						}else if(d instanceof TeletextDescriptor) {
							compt_type.append(" Teletext");
							short_compt_type.append("Teletext");
							abstractPesHandler = new EBUTeletextHandler();
						}else if(d instanceof VBIDataDescriptor) {
							compt_type.append(" VBI Data");
							short_compt_type.append("VBI Data");
							abstractPesHandler = new EBUTeletextHandler();
						}else if(d instanceof AC3Descriptor){
							compt_type.append(" Dolby Audio (AC3)");
							short_compt_type.append("Dolby Audio (AC3)");
							abstractPesHandler = new AC3Handler();
						}else if(d instanceof EnhancedAC3Descriptor){
							compt_type.append(" Enhanced Dolby Audio (AC3)");
							short_compt_type.append(" Enhanced Dolby Audio (AC3)");
							abstractPesHandler = new EAC3Handler();
						}if(d instanceof CADescriptor) {
							final CADescriptor cad = (CADescriptor) d;
							final int capid=cad.getCaPID();
							setLabel(capid,pids,"ECM for CA_ID:"+cad.getCaSystemID()+" for component(s) of service:"+service_id+", ("+service_name+")","ECM "+service_name);
						}
					}

					if(pids[comp_pid]!=null){
						if(pids[comp_pid].getLabel()==null){
							pids[comp_pid].setLabel(compt_type.toString());
						}else if(!pids[comp_pid].getLabel().contains(compt_type)){
							pids[comp_pid].setLabel(pids[comp_pid].getLabel()+ '/'+compt_type);
						}
						if(pids[comp_pid].getShortLabel()==null){
							pids[comp_pid].setShortLabel(short_compt_type.toString());
						}else if(!pids[comp_pid].getShortLabel().contains(short_compt_type)){
							pids[comp_pid].setShortLabel(pids[comp_pid].getShortLabel()+'/'+short_compt_type);
						}
						if(abstractPesHandler!=null){
							abstractPesHandler.setTransportStream(this);
							abstractPesHandler.setPID(pids[comp_pid]);
							pids[comp_pid].setPesHandler(abstractPesHandler);
						}
					}
				}
				final int PCR_pid = section.getPcrPid();
				final String pcrLabel = "PCR for "+service_id+" ("+service_name+")";
				final String pcrShortLabel = "PCR "+service_name;
				if(pids[PCR_pid]==null){
					logger.warning("PID "+PCR_pid +" does not exist, needed for "+ pcrLabel);
				}
				else if(pids[PCR_pid].getLabel()==null){
					pids[PCR_pid].setLabel(pcrLabel);
				}else if(!pids[PCR_pid].getLabel().contains(pcrLabel)){
					pids[PCR_pid].setLabel(pids[PCR_pid].getLabel()+", "+pcrLabel);
				}
				if(pids[PCR_pid]!=null){
					if(pids[PCR_pid].getShortLabel()==null){
						pids[PCR_pid].setShortLabel(pcrShortLabel);
					}else if(pids[PCR_pid].getShortLabel().contains(service_name)){
						pids[PCR_pid].setShortLabel(pids[PCR_pid].getShortLabel()+", PCR");
					}else{
						pids[PCR_pid].setShortLabel(pids[PCR_pid].getShortLabel()+", "+pcrShortLabel);
					}
				}
				section =(PMTsection)section.getNextVersion();
			}
		}

		setLabel(16,pids,"NIT");
		setLabel(17,pids,"SDT/BAT");
		setLabel(18,pids,"EIT");
		setLabel(19,pids,"RST, ST");
		setLabel(20,pids,"TOT/TDT");
		setLabel(21,pids,"network synchronization","network sync");
		setLabel(22,pids,"RNT (TS 102 323)");

		for (int i = 0x17; i <= 0x1b; i++) {
			setLabel(i,pids,"reserved for future use");
		}
		setLabel(0x1c,pids,"inband signalling");
		setLabel(0x1d,pids,"measurement");
		setLabel(0x1e,pids,"DIT");
		setLabel(0x1f,pids,"SIT");


		setLabel(8191,pids,"NULL Packets (Stuffing)");

		int teller=0;
		long totBitrate=0l;
		for(final PID pid:pids){
			if(pid!=null){
				if(pid.getLabel()==null){
					pid.setLabel("?");
				}
				if(pid.getShortLabel()==null){
					pid.setShortLabel("?");
				}
				if(pid.getBitRate()!=-1){
					teller++;
					totBitrate+=pid.getBitRate();
				}
			}
		}
		if(teller!=0){
			bitRate = totBitrate / teller;
		}

		// calculate bitrate based on TDT sections. Need at least 2
		if(getPsi().getTdt()!=null){
			final List<TDTsection> tdtSectionList  = getPsi().getTdt().getTdtSectionList();
			if(tdtSectionList.size()>=2){
				final TDTsection first = tdtSectionList.get(0);
				final TDTsection last = tdtSectionList.get(tdtSectionList.size()-1);
				final long diffPacket = last.getPacket_no() - first.getPacket_no();
				final long timeDiffMills =   getUTCmillis(last.getUTC_time())- getUTCmillis(first.getUTC_time());
				if(timeDiffMills>0){ // shit happens... capture.guangdong  has 10 with same timestamp....
					bitRateTDT = (diffPacket * packet_length * 8 * 1000)/timeDiffMills;
				}

			}

		}
		// calculate zeroTime

		if((getPsi().getTdt()!=null)&&(getBitRate()!=-1)){
			final List<TDTsection> tdtSectionList  = getPsi().getTdt().getTdtSectionList();
			if(tdtSectionList.size()>=1){
				final TDTsection first = tdtSectionList.get(0);
				final Calendar firstTime = getUTCCalender(first.getUTC_time());
				final long millsIntoStream= (first.getPacket_no() *packet_length * 8 * 1000)/getBitRate();
				firstTime.add(Calendar.MILLISECOND, (int)-millsIntoStream);
				zeroTime = firstTime;
			}
		}
	}

	public int getStreamID(){
		return psi.getPat().getTransportStreamId();
	}

	public Bouquet getBouquet() {
		return bouquet;
	}

	public void setBouquet(final Bouquet bouquet) {
		this.bouquet = bouquet;
	}

	/**
	 * @return the number of unique pids used in this stream
	 */
	public int getNoPIDS()
	{
		int t=0;

		for(final PID pid:pids){
			if(pid!=null){
				t++;
			}
		}
		return t;
	}

	public short [] getUsedPids(){
		final int no=getNoPIDS();
		final short [] r = new short[no];
		int i=0;
		for(short  pid=0; pid<8192;pid++){
			if(pids[pid]!=null){
				r[i++]=pid;
			}
		}
		return r;
	}

//	public short[] getPacket_pid() {
//		return packet_pid;
//	}

	public short getPacket_pid(final int t) {
		return (short) (0x1fff & packet_pid[t]);
	}

	public short getPacketPidFlags(final int t) {
		return  packet_pid[t];
	}

	public String getLabel(final short pid){
		return pids[pid].getLabel();
	}
	public String getShortLabel(final short pid){
		return pids[pid].getShortLabel();
	}

	/**
	 * @return the bitrate based on PCRs if available, else bitrate based on TDTs (if available). -1 if we have no idea what the bitrate could be.
	 */
	public long getBitRate() {
		if(bitRate!=-1){
			return bitRate;
		}else if(bitRateTDT!=-1){
			return bitRateTDT;
		}
		return -1;
	}

	/**
	 * @return the length of the stream in seconds, based on PCRs bitrate if available, else based on bitrate based on TDTs (if available). -1 if we have no idea what the length could be.
	 */
	public double getLength(){
		if(bitRate!=-1){
			return ((double)file.length()*8)/bitRate;
		}else if(bitRateTDT!=-1){
			return ((double)file.length()*8)/bitRateTDT;
		}else{
			return -1;
		}
	}

	public String getPacketTime(final long packetNo){
		String r = null;

		if(getBitRate()!=-1){ //can't do anything without a bitrate
			if(zeroTime==null){
				final Calendar now=new GregorianCalendar();
				now.setTimeInMillis(0);
				now.add(Calendar.MILLISECOND, (int)((packetNo * packet_length * 8 * 1000)/getBitRate()));
				// return only the hours/min,secs and millisecs. Not TS recording will last days
				r = now.get(Calendar.HOUR_OF_DAY)+"h"+now.get(Calendar.MINUTE)+"m"+now.get(Calendar.SECOND)+":"+now.get(Calendar.MILLISECOND);

			}else{
				final Calendar now=(Calendar)zeroTime.clone();
				now.add(Calendar.MILLISECOND, (int)((packetNo * packet_length * 8 * 1000)/getBitRate()));

				r = now.get(Calendar.YEAR)+"/"+ (now.get(Calendar.MONTH)+1)+"/"+now.get(Calendar.DAY_OF_MONTH)+" "+now.get(Calendar.HOUR_OF_DAY)+"h"+df2pos.format(now.get(Calendar.MINUTE))+"m"+df2pos.format(now.get(Calendar.SECOND))+":"+df3pos.format(now.get(Calendar.MILLISECOND));
			}
		}
		return r;
	}

	public String getShortPacketTime(final long packetNo){
		String r = null;

		if(getBitRate()!=-1){ //can't do anything without a bitrate
			if(zeroTime==null){
				final Calendar now=new GregorianCalendar();
				now.setTimeInMillis(0);
				now.add(Calendar.MILLISECOND, (int)((packetNo * packet_length * 8 * 1000)/getBitRate()));
				// return only the hours/min,secs and millisecs. Not TS recording will last days
				r = now.get(Calendar.HOUR_OF_DAY)+"h"+now.get(Calendar.MINUTE)+"m"+now.get(Calendar.SECOND)+":"+now.get(Calendar.MILLISECOND);

			}else{
				final Calendar now=(Calendar)zeroTime.clone();
				now.add(Calendar.MILLISECOND, (int)((packetNo * packet_length * 8 * 1000)/getBitRate()));

				r = now.get(Calendar.HOUR_OF_DAY)+"h"+df2pos.format(now.get(Calendar.MINUTE))+"m"+df2pos.format(now.get(Calendar.SECOND))+":"+df3pos.format(now.get(Calendar.MILLISECOND));
			}
		}
		return r;
	}

	/**
	 * @return the defaultPrivateDataSpecifier
	 */
	public long getDefaultPrivateDataSpecifier() {
		return defaultPrivateDataSpecifier;
	}

	/**
	 * @param defaultPrivateDataSpecifier the defaultPrivateDataSpecifier to set
	 */
	public void setDefaultPrivateDataSpecifier(final long defaultPrivateDataSpecifier) {
		this.defaultPrivateDataSpecifier = defaultPrivateDataSpecifier;
	}

	public PMTsection getPMTforPID(final int thisPID) {
		final PMTs pmts = getPsi().getPmts();
		for (final PMTsection[] pmTsections : pmts) {
			final PMTsection pmt = pmTsections[0];
			for(final Component component :pmt.getComponentenList()){
				if(component.getElementaryPID()==thisPID){
					return pmt;
				}
			}
		}
		return null;
	}

	public TSPacket getTSPacket(int packetNo){
		TSPacket packet = null;
		if(packet_offset!=null){
			if(packet_offset.length>packetNo){
				try {
					RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
					randomAccessFile.seek(packet_offset[packetNo]);
					byte [] buf = new byte[MPEGConstants.packet_length];
					int l = randomAccessFile.read(buf);
					if(l==MPEGConstants.packet_length){
						packet = new TSPacket(buf, packetNo,this);

					}else{
						logger.warning("read less then MPEGConstants.packet_length ("+MPEGConstants.packet_length+") bytes, actual read: "+l);
					}

					randomAccessFile.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.warning("FileNotFoundException");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}else{
				logger.warning("packet_offset.length ("+packet_offset.length+") < packetNo ("+packetNo+")");

			}

		}else{
			logger.warning("packet_offset[] is null");
		}
		return packet;
	}


	public boolean isEnableTSPackets() {
		return enableTSPackets;
	}


	public void setEnableTSPackets(boolean enableTSPackets) {
		this.enableTSPackets = enableTSPackets;
	}


}

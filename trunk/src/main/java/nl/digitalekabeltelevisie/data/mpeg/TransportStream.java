/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.sync_byte;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2MIDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.Audio138183Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac.Audio144963Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.DVBSubtitleHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ebu.EBUTeletextHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.Video138182Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video264.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.video265.H265Handler;
import nl.digitalekabeltelevisie.data.mpeg.pid.t2mi.T2miPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.util.*;


/**
 * TransportStream is responsible for parsing a file containing a transport stream, dividing it into 188 byte {@link TSPackets}, and handing them over to the correct PID.
 *
 */
public class TransportStream implements TreeNode{
	
	private enum ComponentType{
		AC3, E_AC3, VBI, TELETEXT, DVB_SUBTITLING, AIT, RCT, ECM, T2MI
	}


	/**
	 *
	 */
	private static final int MAX_SEARCH_BYTES =5000;
	private static final int CONSECUTIVE_PACKETS =5;
	/**
	 *
	 */
	public static final int TRANSPORT_ERROR_FLAG = 0x8000;
	public static final int ADAPTATION_FIELD_FLAG = 0x2000;
	public static final int PAYLOAD_UNIT_START_FLAG = 0x4000;


	private static final Logger logger = Logger.getLogger(TransportStream.class.getName());

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

	private OffsetHelper offsetHelper = null;
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

	private final long len;
	
	/**
	 * number of times sync was lost
	 */
	private int sync_errors = 0;

	private final int max_packets;

	private int packetLength = 188;

	private static final int [] ALLOWED_PACKET_LENGTHS = {188,192,204,208};


	/**
	 *
	 * Creates a new Transport stream based on the supplied file. After construction the TransportStream is not complete, first parseStream() has to be called!
	 * @param fileName name of the file to be read (null not permitted).
	 */
	public TransportStream(final String fileName) throws NotAnMPEGFileException,IOException {
		this(new File(fileName));
	}

	/**
	 *
	 * Creates a new Transport stream based on the supplied file. After construction the TransportStream is not complete, first parseStream() has to be called!
	 * @param file the file to be read (null not permitted). Don't enable TSPackets by default.
	 */

	public TransportStream(final File file) throws NotAnMPEGFileException,IOException{
		this.file = file;
		len = file.length();
		packetLength = determineActualPacketLength(file);
		max_packets = (int)(len / packetLength);
		packet_pid = new short [max_packets];
		offsetHelper = new OffsetHelper(max_packets,packetLength);

	}

	/**
	 * tries to find the actual packetLength of packets in this file by finding a sequence of 5 sync bytes at the same distance, starting within the first 1000 bytes of the file
	 * @param file
	 * @return
	 */
	private static int determineActualPacketLength(final File file) throws NotAnMPEGFileException,IOException{
		try(final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
			for(final int possiblePacketLength:ALLOWED_PACKET_LENGTHS){
				logger.log(Level.INFO, "Trying for packetLength {0}",possiblePacketLength);
	
				if(usesPacketLength(possiblePacketLength, randomAccessFile)){
					randomAccessFile.close();
					logger.log(Level.INFO, "Found packetLength {0}",possiblePacketLength);
					return possiblePacketLength;
				}
			}
		}
		throw new NotAnMPEGFileException();
	}

	/**
	 * @param possiblePacketLength
	 * @param randomAccessFile
	 * @return
	 * @throws IOException
	 */
	private static boolean usesPacketLength(final int possiblePacketLength, final RandomAccessFile randomAccessFile) throws IOException {
		int startPos = 0;
		do{
			logger.log(Level.INFO, "starting at position {0}",startPos);
			randomAccessFile.seek(startPos);
			int b = randomAccessFile.read();
			while ((b != sync_byte)&&(startPos<MAX_SEARCH_BYTES)){
				b = randomAccessFile.read();
				startPos++;
			}
			logger.log(Level.INFO, "found a sync byte at position {0}",startPos);
			// found a sync byte, try to find next 4 sync bytes
			boolean seqFound = true;
			for (int i = 1; (i < CONSECUTIVE_PACKETS) && seqFound; i++) {
				randomAccessFile.seek(startPos + (i * possiblePacketLength));
				logger.log(Level.INFO, "found {0} sequence syncs at pos {1}",new Object[]{i,startPos + (i * possiblePacketLength)});
				seqFound &= (randomAccessFile.read() == sync_byte);
			}
			if(seqFound){
				return true;
			}
			startPos++;
		}while(startPos <MAX_SEARCH_BYTES);
		return false;
	}

	/**
	 * read the file, and parse it. Packets are counted, bitrate calculated, etc. Used for initial construction. PES data is not analyzed.
	 * @throws IOException
	 */
	public void parseStream() throws IOException {
		parsePSITables(null);
	}


	/**
	 * read the file, and parse it. Packets are counted, bitrate calculated, etc. Used for initial construction. PES data is not analyzed.
	 * @throws IOException
	 */
	public void parsePSITables(final java.awt.Component component) throws IOException {
		final PositionPushbackInputStream fileStream = getInputStream(component);
		final byte [] buf = new byte[packetLength];
		int count=0;
		no_packets = 0;

		pids = new PID [8192];
		psi = new PSI();
		error_packets = 0;
		bitRate = -1;
		bitRateTDT = -1;

		int bytes_read =0;
		int lastHandledSyncErrorPacket = -1;
		do {
			final long offset = fileStream.getPosition();
			bytes_read = fileStream.read(buf, 0, packetLength);
			final int next = fileStream.read();
			if((bytes_read==packetLength)&&
					(buf[0]==MPEGConstants.sync_byte) &&
					((next==-1)||(next==MPEGConstants.sync_byte))) {
				//always push back first byte of next packet
				if((next!=-1)) {
					fileStream.unread(next);
				}
				offsetHelper.addPacket(no_packets,offset);
				processPacket(new TSPacket(buf, count,this));
				count++;
			}else{ // something wrong, find next syncbyte. First push back the lot
				if((next!=-1)) {
					if(lastHandledSyncErrorPacket != no_packets){
						sync_errors++;
						logger.severe("Did not find sync byte, resyncing at offset:"+offset+", packet_no:"+no_packets);
						lastHandledSyncErrorPacket = no_packets;
					}
					fileStream.unread(next);
					fileStream.unread(buf, 0, bytes_read);
					// now read 1 byte and restart all
					fileStream.read(); //ignore result
				}
			}
		} while (bytes_read==packetLength);
		namePIDs();
		calculateBitRate();
	}

	private void processPacket(TSPacket packet) {
		final short pid = packet.getPID();
		packet_pid[no_packets]=addPIDFlags(packet, pid);
		no_packets++;
		if(pids[pid]==null) {
			pids[pid] = new PID(pid,this);
		}
		pids[pid].updatePacket(packet);
		if(packet.isTransportErrorIndicator()){
			error_packets++;
			logger.warning("TransportErrorIndicator set for packet "+ packet);
		}
	}

	private static short addPIDFlags(TSPacket packet, final short pid) {
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
		return pidFlags;
	}

	/**
	 *
	 * Read the file, and parse only the packets for which a GeneralPesHandler is present in toParsePids. Used for analyzing PESdata, like a video, teletext or subtitle stream
	 * @param toParsePids Map with an entry for each PID that should be parsed, and a handler that knows how to interpret the data
	 * @throws IOException
	 */
	public void parsePidStreams(final Map<Integer,GeneralPidHandler> toParsePids) throws IOException {
		if((toParsePids==null)||(toParsePids.isEmpty())){
			return;
		}
		
		try (final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
			for(int t=0; t<no_packets;t++){
				int pid = getPacket_pid(t);
				final GeneralPidHandler handler = toParsePids.get(pid);
				if(handler!=null){
					TSPacket packet = readPacket(t, randomAccessFile);
					handler.processTSPacket(packet);
				}
			}
			for(GeneralPidHandler pidHandler: toParsePids.values()) {
				pidHandler.postProcess();
			}
		}
	}


	private PositionPushbackInputStream getInputStream(final java.awt.Component component) throws IOException{
		final InputStream is = new FileInputStream(file);
		final long expectedSize=file.length();
		if(component==null){
			return new PositionPushbackInputStream(new BufferedInputStream(is),200);
		}
		return new PositionPushbackInputStream(new BufferedInputStream(new ProgressMonitorLargeInputStream(component,
				"Reading file \"" + file.getPath() +"\"",is, expectedSize)),200);
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
		t.add(new DefaultMutableTreeNode(new KVP("packet size",packetLength,null)));
		t.add(new DefaultMutableTreeNode(new KVP("Error packets",error_packets,null)));
		t.add(new DefaultMutableTreeNode(new KVP("Sync Errors",sync_errors,null)));
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

		if(!psiOnlyModus(modus)){
			final JTreeLazyList list = new JTreeLazyList(new TSPacketGetter(this,modus));
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

	/**
	 * returns labels for the fixed PIds, like PAT, CAT, etc
	 *
	 * @param pid
	 * @return
	 */
	private static String getFixedLabel(final short pid){
		switch (pid) {
		case 0:
			return "PAT";
		case 1:
			return "CAT";
		case 2:
			return "TSDT";
		case 3:
			return "IPMP control information table "; // ISO/IEC 13818-1:2013 (E) 
		case 4:
			return "Adaptive streaming information"; // ISO/IEC 13818-1:2013/Amd.4:2014 (E) 
		case 16:
			return  "NIT";
		case 17:
			return "SDT/BAT";
		case 18:
			return "EIT";
		case 19:
			return "RST, ST";
		case 20:
			return "TOT/TDT";
		case 21:
			return "network synchronization";
		case 22:
			return "RNT (TS 102 323)";
		case 0x1c:
			return "inband signalling";
		case 0x1d:
			return "measurement";
		case 0x1e:
			return "DIT";
		case 0x1f:
			return "SIT";

		default:
			if(pid <= 0x1b){
				return "reserved for future use";
			}
			break;
		}

		return "??";
	}


	public void namePIDs() {

		// first the easy ones, the fixed values
		for (short i = 0; i <=0x1f; i++) {
			setLabel(i, pids,getFixedLabel(i));
		}

		setLabel(8191,pids,"NULL Packets (Stuffing)");

		// now the streams referenced from the CAT
		if(pids[1]!=null){
			for(CADescriptor caDescriptor:findGenericDescriptorsInList(getPsi().getCat().getDescriptorList(), CADescriptor.class)){
				setLabel(caDescriptor.getCaPID(), pids, "EMM for CA_ID:"+caDescriptor.getCaSystemID()+ " ("+Utils.getCASystemIDString(caDescriptor.getCaSystemID())+")");
			}
		}

		// now all services, starting with PMTs themselves, then referenced ES
		final Iterator<PMTsection[]> it = getPsi().getPmts().iterator();
		while (it.hasNext()) {
			final PMTsection[] pmt = it.next();
			PMTsection pmtSection = pmt[0];
			while(pmtSection!=null){
				final int service_id=pmtSection.getProgramNumber();
				String service_name = getPsi().getSdt().getServiceName(service_id);
				if(service_name==null){
					service_name="Service "+service_id;
				}
				final int pmt_pid=pmtSection.getParentPID().getPid();
				setLabel(pmt_pid,pids,"PMT for service:"+service_id+" ("+service_name+")","PMT "+service_name);

				for(CADescriptor caDescriptor:findGenericDescriptorsInList(pmtSection.getDescriptorList(), CADescriptor.class)){
					setLabel(caDescriptor.getCaPID(),pids,"ECM for CA_ID:"+caDescriptor.getCaSystemID()+" for service:"+service_id+", ("+service_name+")","ECM "+service_name);
				}

				final Iterator<Component> l = pmtSection.getComponentenList().iterator();
				while(l.hasNext()){
					final Component component = l.next();
					final int streamType = component.getStreamtype();
					final StringBuilder compt_type = new StringBuilder(service_name).append(' ').append(getStreamTypeString(streamType));
					final StringBuilder short_compt_type = new StringBuilder(service_name).append(' ').append(getStreamTypeShortString(streamType));
					GeneralPidHandler generalPidHandler = determinePesHandlerByStreamType(component,streamType);

					ComponentType componentType = determineComponentType(component.getComponentDescriptorList());
					if(componentType!=null){
						switch(componentType){
						case DVB_SUBTITLING:
								compt_type.append(" DVB subtitling");
								short_compt_type.append("DVB subtitling");
								generalPidHandler = new DVBSubtitleHandler();
								break;
						case TELETEXT:
								compt_type.append(" Teletext");
								short_compt_type.append("Teletext");
								generalPidHandler = new EBUTeletextHandler();
								break;
						case VBI:
								compt_type.append(" VBI Data");
								short_compt_type.append("VBI Data");
								generalPidHandler = new EBUTeletextHandler();
								break;
						case AC3:
								compt_type.append(" Dolby Audio (AC3)");
								short_compt_type.append("Dolby Audio (AC3)");
								generalPidHandler = new AC3Handler();
								break;
						case E_AC3:
								compt_type.append(" Enhanced Dolby Audio (AC3)");
								short_compt_type.append(" Enhanced Dolby Audio (AC3)");
								generalPidHandler = new EAC3Handler();
								break;
						case AIT:
								compt_type.append(" Application Information Table (AIT)");
								short_compt_type.append(" Application Information Table (AIT)");
								break;
						case RCT:
								compt_type.append(" Related Content Table (RCT)");
								short_compt_type.append(" Related Content Table (RCT)");
								break;
						case ECM:
								final List<CADescriptor> caDescriptorList =findGenericDescriptorsInList(component.getComponentDescriptorList(), CADescriptor.class);
									if(component.getComponentDescriptorList().size()>0){
										final CADescriptor cad = caDescriptorList.get(0);
										setLabel(cad.getCaPID(),pids,"ECM for CA_ID:"+cad.getCaSystemID()+" for component(s) of service:"+service_id+", ("+service_name+")","ECM "+service_name);
									}
								break;
						case T2MI:
							compt_type.append(" T2-MI");
							short_compt_type.append(" T2-MI");
							generalPidHandler = new T2miPidHandler();
							break;
							
						default:
								logger.warning("no componenttype found for pid "+component.getElementaryPID()+", part of service "+service_id);
						}
					}

					final PID pid = pids[component.getElementaryPID()];
					if(pid!=null){
						if(pid.getLabel()==null){
							pid.setLabel(compt_type.toString());
						}else if(!pid.getLabel().contains(compt_type)){
							pid.setLabel(pid.getLabel()+ '/'+compt_type);
						}
						if(pid.getShortLabel()==null){
							pid.setShortLabel(short_compt_type.toString());
						}else if(!pid.getShortLabel().contains(short_compt_type)){
							pid.setShortLabel(pid.getShortLabel()+'/'+short_compt_type);
						}
						if(generalPidHandler!=null){
							generalPidHandler.setTransportStream(this);
							generalPidHandler.setPID(pid);
							pid.setPidHandler(generalPidHandler);
						}
					}
				}
				final int PCR_pid = pmtSection.getPcrPid();
				if(PCR_pid!=MPEGConstants.NO_PCR_PID){ // ISO/IEC 13818-1:2013, 2.4.4.9; If no PCR is associated with a program definition for private streams, then this field shall take the value of 0x1FFF.
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
							//pids[PCR_pid].setShortLabel(pids[PCR_pid].getShortLabel()+", PCR");
						}else{
							pids[PCR_pid].setShortLabel(pids[PCR_pid].getShortLabel()+", "+pcrShortLabel);
						}
					}
				}
				pmtSection =(PMTsection)pmtSection.getNextVersion();
			}
		}


		for(final PID pid:pids){
			if(pid!=null){
				// just label PIDs that have not been labeled yet
				if(pid.getLabel()==null){
					pid.setLabel("?");
				}
				if(pid.getShortLabel()==null){
					pid.setShortLabel("?");
				}
			}
		}
	}

	private static ComponentType determineComponentType(List<Descriptor> componentDescriptorList) {
		
		for(Descriptor d:componentDescriptorList){
			if(d instanceof SubtitlingDescriptor) {
				return ComponentType.DVB_SUBTITLING;
			}else if(d instanceof TeletextDescriptor) {
				return ComponentType.TELETEXT;
			}else if(d instanceof VBIDataDescriptor) {
				return ComponentType.VBI;
			}else if(d instanceof AC3Descriptor){
				return ComponentType.AC3;
			}else if(d instanceof RegistrationDescriptor){
				byte[] formatIdentifier = ((RegistrationDescriptor)d).getFormatIdentifier();
				if(Utils.equals(formatIdentifier, 0, formatIdentifier.length,RegistrationDescriptor.AC_3,0,RegistrationDescriptor.AC_3.length)){
					return ComponentType.AC3;
				}
			}else if(d instanceof EnhancedAC3Descriptor){
				return ComponentType.E_AC3;
			}else if(d instanceof ApplicationSignallingDescriptor){
				return ComponentType.AIT;
			}else if(d instanceof RelatedContentDescriptor){
				return ComponentType.RCT;
			}else if(d instanceof T2MIDescriptor){
				return ComponentType.T2MI;
			}if(d instanceof CADescriptor) {
				return ComponentType.ECM;
			}
		}

		return null;
	}

	private GeneralPidHandler determinePesHandlerByStreamType(final Component component,
			final int streamType) {
		int comp_pid = component.getElementaryPID();
		GeneralPidHandler abstractPidHandler = null;
		if((pids[comp_pid]!=null)&&(!pids[comp_pid].isScrambled())&&(pids[comp_pid].getType()==PID.PES)){
			if((streamType==1)||(streamType==2)){
				abstractPidHandler = new Video138182Handler();
			}else if((streamType==3)||(streamType==4)){
				abstractPidHandler = new Audio138183Handler(getAncillaryDataIdentifier(component));
			}else if(streamType==0x11){
				abstractPidHandler = new Audio144963Handler();
			}else if(streamType==0x1B){
				abstractPidHandler = new Video14496Handler();
			}else if(streamType==0x20){ //MVC video sub-bitstream of an AVC video stream conforming to one or more profiles defined in Annex H of ITU-T Rec. H.264 | ISO/IEC 14496-10
				abstractPidHandler = new Video14496Handler();
			}else if(streamType==0x24){
				abstractPidHandler = new H265Handler();
			}else{
				abstractPidHandler = new GeneralPesHandler();
			}
		}
		return abstractPidHandler;
	}

	private static int getAncillaryDataIdentifier(final Component component) {
		int ancillaryData = 0;
		final List<AncillaryDataDescriptor> ancillaryDataDescriptors = findGenericDescriptorsInList(component.getComponentDescriptorList(), AncillaryDataDescriptor.class);
		if(ancillaryDataDescriptors.size()>0){
			ancillaryData = ancillaryDataDescriptors.get(0).getAncillaryDataIdentifier();
		}
		return ancillaryData;
	}



	/**
	 *
	 */
	public void calculateBitRate() {

		// now calculate bitrate of stream by averaging bitrates of PIDS with PCR

		int teller=0;
		long totBitrate=0l;
		for(final PID pid:pids){
			if((pid!=null)&&(pid.getBitRate()!=-1)){
				teller++;
				totBitrate+=pid.getBitRate();
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
				final Calendar utcCalenderLast = getUTCCalender(last.getUTC_time());
				final Calendar utcCalenderFirst = getUTCCalender(first.getUTC_time());
				// getUTCCalender might fail if not correct BCD, then will return null.
				if((utcCalenderLast!=null)&&(utcCalenderFirst!=null)){
					final long timeDiffMills =   utcCalenderLast.getTimeInMillis()- utcCalenderFirst.getTimeInMillis();
					if(timeDiffMills>0){ // shit happens... capture.guangdong  has 10 with same timestamp....
						bitRateTDT = (diffPacket * packetLength * 8 * 1000)/timeDiffMills;
					}
				}

			}

		}
		// calculate zeroTime

		if((getPsi().getTdt()!=null)&&(getBitRate()!=-1)){
			final List<TDTsection> tdtSectionList  = getPsi().getTdt().getTdtSectionList();
			if(tdtSectionList.size()>=1){
				final TDTsection first = tdtSectionList.get(0);
				final Calendar firstTime = getUTCCalender(first.getUTC_time());
				if(firstTime!=null){
					final long millsIntoStream= (first.getPacket_no() *packetLength * 8 * 1000)/getBitRate();
					firstTime.add(Calendar.MILLISECOND, (int)-millsIntoStream);
					zeroTime = firstTime;
				}
			}
		}
	}

	public int getStreamID(){
		return psi.getPat().getTransportStreamId();
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
		if(pids[pid]!=null){
			return pids[pid].getShortLabel();
		}
		return null;
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

		if(getBitRate()!=-1){ //can't calculate time without a bitrate
			if(zeroTime==null){
				final Calendar now=new GregorianCalendar();
				now.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
				now.setTimeInMillis(0);
				now.add(Calendar.MILLISECOND, (int)((packetNo * packetLength * 8 * 1000)/getBitRate()));
				// return only the hours/min,secs and millisecs. Not TS recording will last days
				r = getFormattedTime(now);

			}else{
				final Calendar now=(Calendar)zeroTime.clone();
				now.add(Calendar.MILLISECOND, (int)((packetNo * packetLength * 8 * 1000)/getBitRate()));

				r = getFormattedDate(now)+ " "+getFormattedTime(now);
			}
		}else{ // no bitrate, return packet number
			r = Long.toString(packetNo)+" (packetNo)";
		}
		return r;
	}

	private static String getFormattedDate(final Calendar now) {
		StringBuilder sb = new StringBuilder();
		sb.append(now.get(Calendar.YEAR)).append("/").
			append(now.get(Calendar.MONTH)+1).append("/").
			append(now.get(Calendar.DAY_OF_MONTH));
		return sb.toString();
	}

	private static String getFormattedTime(final Calendar now) {
		StringBuilder sb = new StringBuilder();
		
		sb.append(df2pos.format(now.get(Calendar.HOUR_OF_DAY))).append("h").
			append(df2pos.format(now.get(Calendar.MINUTE))).append("m").
			append(df2pos.format(now.get(Calendar.SECOND))).append(":").
			append(df3pos.format(now.get(Calendar.MILLISECOND)));
		return sb.toString();
	}

	public String getShortPacketTime(final long packetNo){
		String r = null;

		if(getBitRate()!=-1){ //can't calculate time  without a bitrate
			if(zeroTime==null){
				final Calendar now=new GregorianCalendar();
				now.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
				now.setTimeInMillis(0);
				now.add(Calendar.MILLISECOND, (int)((packetNo * packetLength * 8 * 1000)/getBitRate()));
				// return only the hours/min,secs and millisecs. Not TS recording will last days
				r = now.get(Calendar.HOUR_OF_DAY)+"h"+now.get(Calendar.MINUTE)+"m"+now.get(Calendar.SECOND)+":"+now.get(Calendar.MILLISECOND);

			}else{
				final Calendar now=(Calendar)zeroTime.clone();
				now.add(Calendar.MILLISECOND, (int)((packetNo * packetLength * 8 * 1000)/getBitRate()));

				r = now.get(Calendar.HOUR_OF_DAY)+"h"+df2pos.format(now.get(Calendar.MINUTE))+"m"+df2pos.format(now.get(Calendar.SECOND))+":"+df3pos.format(now.get(Calendar.MILLISECOND));
			}
		}else{ // no bitrate
			r = Long.toString(packetNo)+" (packetNo)";
		}
		return r;
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

	public TSPacket getTSPacket(final int packetNo){
		TSPacket packet = null;
		if(offsetHelper.getMaxPacket()>packetNo){
			try (final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
				packet = readPacket(packetNo,randomAccessFile);
			} catch (final IOException e) {
				logger.warning("IOException:"+e);
			}
		}else{
			logger.warning("offsetHelper.getMaxPacket() ("+offsetHelper.getMaxPacket()+") < packetNo ("+packetNo+")");
		}
		return packet;
	}

	private TSPacket readPacket(final int packetNo, final RandomAccessFile randomAccessFile)
			throws IOException {
		TSPacket packet = null;
		final long offset = offsetHelper.getOffset(packetNo);
		randomAccessFile.seek(offset);
		final byte [] buf = new byte[packetLength];
		final int bytesRead = randomAccessFile.read(buf);
		if(bytesRead==packetLength){
			packet = new TSPacket(buf, packetNo,this);
			packet.setPacketOffset(offset);
		}else{
			logger.warning("read less then packetLenghth ("+packetLength+") bytes, actual read: "+bytesRead);
		}
		return packet;
	}


	public Iterator<TSPacket> getTSPacketsIterator(final int pid, final int flags){
		return  getTSPacketsIterator(pid, flags, 0, getNo_packets());
	}

	public Iterator<TSPacket> getTSPacketsIterator(final int pid, final int flags, final int start, final int end){

		final Iterator<TSPacket> iter = new Iterator<TSPacket>(){

			int pos = findNext(start-1);

			private int findNext(final int pos){
				int p = pos+1;
				while((p<end)&&(!match(p))){
					p++;
				}
				return p;

			}

			private boolean match(final int p) {
				return (packet_pid[p]&(0x1fff | flags))==( pid | flags) ;
			}

			@Override
			public boolean hasNext() {
				return pos<end;
			}

			@Override
			public TSPacket next() {
				final TSPacket packet = getTSPacket(pos);
				pos = findNext(pos);
				return packet;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
		return iter;

	}

	public PID getPID(final int p){
		return pids[p];
	}

	public int getPacketLenghth() {
		return packetLength;
	}

	public long getLen() {
		return len;
	}

	public int getSync_errors() {
		return sync_errors;
	}

	public void setSync_errors(int sync_errors) {
		this.sync_errors = sync_errors;
	}

	public int getError_packets() {
		return error_packets;
	}

	public void setError_packets(int error_packets) {
		this.error_packets = error_packets;
	}
	
}

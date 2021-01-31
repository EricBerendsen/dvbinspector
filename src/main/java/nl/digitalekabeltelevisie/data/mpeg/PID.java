/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.system_clock_frequency;
import static nl.digitalekabeltelevisie.util.Utils.printPCRTime;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors.TimelineDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.PesHeader;
import nl.digitalekabeltelevisie.data.mpeg.psi.GeneralPSITable;
import nl.digitalekabeltelevisie.data.mpeg.psi.MegaFrameInitializationPacket;
import nl.digitalekabeltelevisie.gui.DVBtree;
import nl.digitalekabeltelevisie.util.JTreeLazyList;
import nl.digitalekabeltelevisie.util.PIDPacketGetter;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * Collects all {@link TSPacket}s with same packet_id, groups them together, and interprets them depending on type. For PSI packets tables are built, PES packets are (initially) only counted.
 * Does not store all data packets for this PID
 */
public class PID implements TreeNode{
	
	
	private static final Logger logger = Logger.getLogger(PID.class.getName());

	public static final int PES = 1;
	public static final int PSI = 2;
	private int type=0;
	private boolean scrambled = false;

	private long bitRate = -1;

	/**
	 * if this PID is of type PSI, this table is used as a general representation of its data.
	 */
	private GeneralPSITable psi ;


	/**
	 * generalPesHandler that is able to interpret the PES_packet_data_byte, and turn it into something we can display
	 */
	private GeneralPidHandler generalPidHandler=null;

	/**
	 * number of TS packets in this PID
	 */
	private int packets = 0;
	/**
	 * number of different duplicate packets
	 */
	private int dup_packets = 0;
	/**
	 *  number of continuity_errors
	 */
	private long continuity_errors = 0;
	private int last_continuity_counter = -1;
	private int pid = -1;
	private long last_packet_no=-1;
	private TSPacket last_packet = null;
	private int dup_found=0; // number of times current packet is duplicated. 
	private PCR lastPCR;
	private PCR firstPCR;
	private long lastPCRpacketNo = -1;
	private long firstPCRpacketNo =-1;
	private long pcr_count =-1;
	protected TransportStream parentTransportStream = null;

	private final GatherPIDData gatherer = new GatherPIDData();
	
	private final ArrayList<TimeStamp> pcrList = new ArrayList<>();
	private final ArrayList<TimeStamp> ptsList = new ArrayList<>();
	private final ArrayList<TimeStamp> dtsList = new ArrayList<>();
	
	private final HashMap<Integer, ArrayList<TemiTimeStamp>> temiList = new HashMap<>();

	private final LabelMaker labelMaker = new LabelMaker();
	
	/**
	 *
	 * This inner class is a helper that collects and groups TSPackets for the containing PID into PsiSectionData's . If this PID contains PES data, the bytes are ignored.
	 * @author Eric Berendsen
	 *
	 */
	public class GatherPIDData {


		private PsiSectionData lastPSISection;

		public void reset(){
			lastPSISection = null;

		}

		private void processPayload(final TSPacket packet, final TransportStream ts, final PID parentPID)
		{
			parentTransportStream = ts;
			final byte []data = packet.getData();
			final int adaptationFieldControl = packet.getAdaptationFieldControl();
			final boolean packetHasPayload = (adaptationFieldControl==1)||(adaptationFieldControl==3);
			if((lastPSISection==null)){ // nothing started
				// sometimes PayloadUnitStartIndicator is 1, and there is no payload, so check
				// AdaptationFieldControl
				if (packet.isPayloadUnitStartIndicator() && (data.length > 1) && packetHasPayload) {
					startNewSection(packet, parentPID, data);
				}
				//	something started
			}else if(packetHasPayload){
				// are we in a PSI PID??
				if(type==PSI){
					int start;
					if(packet.isPayloadUnitStartIndicator()){ //first byte is pointer, skip pointer and continue with what we already got from previous TSPacket
						start = 1;
					}else{
						start = 0;
					}
					int available = data.length -start;
					if(!lastPSISection.isComplete()){
						final int bytes_read=lastPSISection.readBytes(data, start, available);
						start+=bytes_read;
						available-=bytes_read;
					}
					if (lastPSISection != null && lastPSISection.isComplete()) {
						lastPSISection = null;
					}
					if(packet.isPayloadUnitStartIndicator()){
						while ((available > 0) && (toUnsignedInt(data[start]) != 0xFF)) {
							lastPSISection = new PsiSectionData(parentPID, packet.getPacketNo(), parentTransportStream);
							final int bytes_read = lastPSISection.readBytes(data, start, available);
							start += bytes_read;
							available -= bytes_read;
						}
					}
				}
			}
		}

		private void startNewSection(final TSPacket packet, final PID parentPID, final byte[] data) {
			{ // start something
				// at least one byte plus pointer available
				int start;
				int available;
				if ((data[0] != 0) 
					|| ((getPid() == 0) 
					|| (data[1] != 0))) { // starting PSI section after offset
					// this is just an educated guess, it might still be private data of unspecified
					// format
					type = PSI;

					start = 1 + toUnsignedInt(data[0]);
					available = data.length - start;
					while ((available > 0) && (toUnsignedInt(data[start]) != 0xFF)) {
						lastPSISection = new PsiSectionData(parentPID, packet.getPacketNo(), parentTransportStream);
						final int bytes_read = lastPSISection.readBytes(data, start, available);
						start += bytes_read;
						available -= bytes_read;
					}
					if (lastPSISection != null && lastPSISection.isComplete()) {
						lastPSISection = null;
					}
				// could be starting PES stream, make sure it really is, Should start with
				// packet_start_code_prefix -'0000 0000 0000 0000 0000 0001' (0x000001)
				} else if ((data.length > 2) && (data[0] == 0) && (data[1] == 0) && (data[2] == 1)) {
					startPesPacket(packet, parentPID);
				}
			}
		}

		private void startPesPacket(final TSPacket packet, final PID parentPID) {
			type = PES;

			if (PreferencesManager.isEnablePcrPtsView()) {
				try {
					// insert into PTS /DTS List
					PesHeader pesHeader = packet.getPesHeader();
					if ((pesHeader != null)
							&& (pesHeader.isValidPesHeader() && pesHeader.hasExtendedHeader())) {

						final int pts_dts_flags = pesHeader.getPts_dts_flags();
						if ((pts_dts_flags == 2) || (pts_dts_flags == 3)) { // PTS present,
							ptsList.add(new TimeStamp(packet.getPacketNo(), pesHeader.getPts()));
						}
						if (pts_dts_flags == 3) { // DTS present,
							dtsList.add(new TimeStamp(packet.getPacketNo(), pesHeader.getDts()));
						}
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Error getting PTS/DTS from PESHeader in packet:"
							+ packet.getPacketNo() + " from PID:" + parentPID.getPid(), e);
				}
			}
		}
		
	}


	class LabelMaker{
		
		private String base;
		private LinkedHashMap <String, LinkedHashSet<String>> components = new LinkedHashMap<>();
		
		void setBase(String base) {
			this.base = base;
		}
		
		void addComponent(String type, String serviceName) {
			components.computeIfAbsent(type, k -> new LinkedHashSet<>()).add(serviceName);
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(base!=null) {
				sb.append(base);
				if(!components.isEmpty()) {
					sb.append(" / ");
				}
			}
			StringJoiner sj = new StringJoiner(" / ");
			for(String type:components.keySet()) {
				sj.add(getTypeServices(type));
			}
			sb.append(sj.toString());
			if(sb.length()==0) {
				return "?";
			}
			return sb.toString();
		}

		private String getTypeServices(String type) {
			StringBuilder sb = new StringBuilder();
			sb.append(type).append(" - ");
			LinkedHashSet<String> servicesList = components.get(type);
			
			sb.append(servicesList.
					stream().
					collect(Collectors.joining(", ")));
			return sb.toString();
		}
	}

	/**
	 * @param pid packet_id of packets in this PID
	 * @param ts
	 */
	public PID(final int pid, final TransportStream ts) {
		this.pid=pid;
		parentTransportStream = ts;
		psi = new GeneralPSITable(ts.getPsi());

	}

	public void updatePacket(final TSPacket packet) {
		
		if(!packet.isTransportErrorIndicator()){
			updateNonErrorPacket(packet);
		}
		packets++;
	}

	
	/**
	 * calculate value of pcr based on this pid for packet packetNo
	 * @param packetNo
	 * @return pcr based on 27 Mhz clock
	 */
	public Long getPacketPcrTime(long packetNo) {
		
		if((firstPCR != null) && 
			(lastPCR != null) && 
			!firstPCR.equals(lastPCR)){
			
			long diffPCR = lastPCR.getProgram_clock_reference() - firstPCR.getProgram_clock_reference();
			long diffPackets = lastPCRpacketNo - firstPCRpacketNo;
			
			return firstPCR.getProgram_clock_reference() + (packetNo - firstPCRpacketNo) * diffPCR /  diffPackets;
				
		}
		
		return null;
		
	}
	
	private void updateNonErrorPacket(final TSPacket packet) {
		if (pid == 0x015) {
			updateMegaFrameInitializationPacket(packet);
		} else {

			AdaptationField adaptationField = null;
			if (packet.hasAdaptationField()) {
				adaptationField = handleAdaptationField(packet);
			}
			if (isNormalPacket(packet, adaptationField)) {
				handleNormalPacket(packet);
			} else if (packet.hasPayload() && (last_continuity_counter == packet.getContinuityCounter())) {
				handleDuplicatePacket(packet);
			} else if (packet.hasPayload() || // not dup, and not consecutive, so error
					(last_continuity_counter != packet.getContinuityCounter()) // if no payload, counter should not
																				// increment
			) {
				handleContinuityError(packet);
			} // else{ // no payload, only adaptation. Don't Increase continuity_counter
		}
	}

	/**
	 * @param packet
	 * @param adaptationField
	 * @return true if this packet is expected here, i.e. first packet for this PID, or null packet, or has next continuityCounter, or disContinuity indicator has been set.
	 */
	private boolean isNormalPacket(final TSPacket packet, AdaptationField adaptationField) {
		return ((last_continuity_counter==-1)|| // first packet
				(pid==0x1fff)|| // null packet
				((((last_continuity_counter+1)%16)==packet.getContinuityCounter()))&&packet.hasPayload()) || // counter ok
				(adaptationField!=null && adaptationField.isDiscontinuity_indicator()) // discontinuity_indicator true
;
	}

	private void handleDuplicatePacket(final TSPacket packet) {
		if(dup_found>=1){ // third or more dup packet (third total), illegal
			dup_found++;
			logger.warning("multiple dup packet ("+dup_found+"th total), illegal, PID="+pid+", last="+last_continuity_counter+", new="+packet.getContinuityCounter()+", last_no="+last_packet_no +", packet_no="+packet.getPacketNo());
		}else{ // just a dup, count it and ignore
			dup_found = 1;
			dup_packets++;
		}
	}

	private void handleNormalPacket(final TSPacket packet) {
		last_continuity_counter = packet.getContinuityCounter();
		last_packet_no = packet.getPacketNo();
		last_packet = packet;
		dup_found = 0;

		if(packet.getTransportScramblingControl()==0){ // not scrambled, or else payload is of no use
			gatherer.processPayload(packet,parentTransportStream,this);
		}else{
			scrambled=true;
		}
	}

	private AdaptationField handleAdaptationField(final TSPacket packet) {
		AdaptationField adaptationField;
		try{
			adaptationField = packet.getAdaptationField();
			processAdaptationField(adaptationField,packet.getPacketNo());
		}catch(final RuntimeException re){ // might be some error in adaptation field, it is not well protected
			logger.log(Level.WARNING, "Error getting adaptationField", re);
			adaptationField = null;
		}
		return adaptationField;
	}

	private void handleContinuityError(final TSPacket packet) {
		//logger.warning("continuity error, PID="+pid+", last="+last_continuity_counter+", new="+packet.getContinuityCounter()+", last_no="+last_packet_no +", packet_no="+packet.getPacketNo()+", adaptation_field_control="+packet.getAdaptationFieldControl());
		last_continuity_counter=packet.getContinuityCounter();
		last_packet_no = packet.getPacketNo();
		last_packet = packet;
		continuity_errors++;
		gatherer.reset();
	}

	private void updateMegaFrameInitializationPacket(final TSPacket packet) {
		// MIP has only TSPackets, no structure with PSISectionData
		if((packet.getData()!=null)&&(packet.getData().length>=14)){
			try {
				final MegaFrameInitializationPacket mip= new MegaFrameInitializationPacket(packet);
				parentTransportStream.getPsi().getNetworkSync().update(mip);
			} catch (Exception exception) {
				logger.log(Level.WARNING, "Exception trying to create MegaFrameInitializationPacket. ", exception);
			}
		}
	}


	private void processAdaptationField(AdaptationField adaptationField, int packetNo) {
		processTEMI(adaptationField, temiList, packetNo);
		if (adaptationField.isPCR_flag()) {
			final PCR newPCR = adaptationField.getProgram_clock_reference();
			if(PreferencesManager.isEnablePcrPtsView()) {
				pcrList.add(new TimeStamp(packetNo, newPCR.getProgram_clock_reference_base()));
			}
			if ((firstPCR != null) && !adaptationField.isDiscontinuity_indicator()) {
				final long packetsDiff = packetNo - firstPCRpacketNo;

				// This will ignore single PCR packets that have lower values than previous.
				// when PCR wraps around we only use first part till wrap around for bitrate calculation
				// (unless PCR reaches value of firstPCR again, this would mean stream of > 24 hours)
				if ((newPCR.getProgram_clock_reference() - firstPCR.getProgram_clock_reference()) > 0) {
					bitRate = ((packetsDiff * parentTransportStream.getPacketLenghth() * system_clock_frequency * 8))
							/ (newPCR.getProgram_clock_reference() - firstPCR.getProgram_clock_reference());
				}
				lastPCR = newPCR;
				lastPCRpacketNo = packetNo;
				pcr_count++;

			} else { // start, or restart of discontinuity
				firstPCR = newPCR;
				firstPCRpacketNo = packetNo;
				lastPCR = null;
				lastPCRpacketNo = -1;
				pcr_count = 1;
			}
		}
	}

	private static void processTEMI(AdaptationField adaptationField, HashMap<Integer, ArrayList<TemiTimeStamp>> temiList, int packetNo) {
		if(adaptationField.isAdaptation_field_extension_flag()){
			if(!adaptationField.isAf_descriptor_not_present_flag()){
				List<Descriptor> afDescriptorList = adaptationField.getAfDescriptorList();
				for (Descriptor descriptor : afDescriptorList) {
					if(descriptor instanceof TimelineDescriptor){
						TimelineDescriptor timelineDescriptor = (TimelineDescriptor) descriptor;
						if((timelineDescriptor.getHas_timestamp()==1)||
							(timelineDescriptor.getHas_timestamp()==2)){
							ArrayList<TemiTimeStamp> tl = temiList.get(timelineDescriptor.getTimeline_id());
							if(tl==null){
								tl = new ArrayList<>();
								temiList.put(timelineDescriptor.getTimeline_id(), tl);
							}
							tl.add(new TemiTimeStamp(packetNo, timelineDescriptor.getMedia_timestamp(),timelineDescriptor.getTimescale(),timelineDescriptor.getDiscontinuity(),timelineDescriptor.getPaused()));
						}
					}
				}
			}
		}
		
	}

	/**
	 * @return number of TS packets found for this PID
	 */
	public int getPackets() {
		return packets;
	}
	@Override
	public String toString() {
		return "PID:"+pid+", packets:"+packets;
	}

	public int getPid() {
		return pid;
	}

	public boolean isDup_found() {
		return dup_found>1;
	}

	public int getDup_packets() {
		return dup_packets;
	}

	public int getLast_continuity_counter() {
		return last_continuity_counter;
	}

	public TSPacket getLast_packet() {
		return last_packet;
	}

	public long getLast_packet_no() {
		return last_packet_no;
	}

	public TransportStream getParentTransportStream() {
		return parentTransportStream;
	}

	public void setParentTransportStream(final TransportStream parentTransportStream) {
		this.parentTransportStream = parentTransportStream;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final KVP kvp=new KVP("pid",getPid(),getLabelMaker().toString());
		if((generalPidHandler!=null)&&(!scrambled)){
			final JMenuItem pesMenu = new JMenuItem("Parse data");
			pesMenu.setActionCommand(DVBtree.PARSE);
			kvp.setSubMenuAndOwner(pesMenu,this);
		}
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);

		t.add(new DefaultMutableTreeNode(new KVP("packets",getPackets(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("duplicate packets",dup_packets,null)));
		t.add(new DefaultMutableTreeNode(new KVP("continuity errors",continuity_errors,null)));


		t.add(new DefaultMutableTreeNode(new KVP("transport_scrambling_control",Boolean.toString(scrambled),null)));
		if(!scrambled){
			t.add(new DefaultMutableTreeNode(new KVP("type",getTypeString(),null)));
		}
		if(firstPCR!=null){
			t.add(new DefaultMutableTreeNode(new KVP("First PCR",firstPCR.getProgram_clock_reference(),printPCRTime(firstPCR.getProgram_clock_reference()))));
			t.add(new DefaultMutableTreeNode(new KVP("First PCR packet",firstPCRpacketNo,getParentTransportStream().getPacketTime(firstPCRpacketNo))));
		}
		if(lastPCR!=null){
			t.add(new DefaultMutableTreeNode(new KVP("Last PCR",lastPCR.getProgram_clock_reference(),printPCRTime(lastPCR.getProgram_clock_reference()))));
			t.add(new DefaultMutableTreeNode(new KVP("Last PCR packet",lastPCRpacketNo, getParentTransportStream().getPacketTime(lastPCRpacketNo))));
			t.add(new DefaultMutableTreeNode(new KVP("PCR_count",pcr_count ,getRepetitionRate(pcr_count,lastPCRpacketNo,firstPCRpacketNo))));
		}
		if(bitRate!= -1){
			t.add(new DefaultMutableTreeNode(new KVP("TS bitrate based on PCR",bitRate,null)));
		}
		if(type==PSI){
			t.add(psi.getJTreeNode(modus));
		}
		final JTreeLazyList list = new JTreeLazyList(new PIDPacketGetter(parentTransportStream,pid,modus));
		t.add(list.getJTreeNode(modus, "Transport packets "));

		if((generalPidHandler!=null)&&(generalPidHandler.isInitialized())) {
			t.add(((TreeNode)generalPidHandler).getJTreeNode(modus));
		}

		return t;
	}

	public String getTypeString() {
		return (type==PSI)?"PSI":((type==PES)?"PES":"-");
	}

	public GeneralPSITable getPsi() {
		return psi;
	}

	private String getRepetitionRate(final long count,final long last, final long  first) {
		final long bitrate=getParentTransportStream().getBitRate();
		if((bitrate>0)&&(count>=2)){
			@SuppressWarnings("resource")
			final Formatter formatter = new Formatter();
			final float repRate=((float)(last-first)*parentTransportStream.getPacketLenghth()*8)/((count-1)*bitrate);
			return "repetition rate: "+formatter.format("%3.3f seconds",repRate);
		}
		return null;
	}

	public void setPsi(final GeneralPSITable psi) {
		this.psi = psi;
	}

	public long getBitRate() {
		return bitRate;
	}


	/**
	 * @return the generalPesHandler
	 */
	public GeneralPidHandler getPidHandler() {
		return generalPidHandler;
	}

	/**
	 * @param generalPidHandler the generalPesHandler to set
	 */
	public void setPidHandler(final GeneralPidHandler generalPidHandler) {
		this.generalPidHandler = generalPidHandler;
	}

	public int getType() {
		return type;
	}

	public boolean isScrambled() {
		return scrambled;
	}

	public long getContinuity_errors() {
		return continuity_errors;
	}

	public PCR getLastPCR() {
		return lastPCR;
	}

	public PCR getFirstPCR() {
		return firstPCR;
	}

	public long getLastPCRpacketNo() {
		return lastPCRpacketNo;
	}

	public long getFirstPCRpacketNo() {
		return firstPCRpacketNo;
	}

	public long getPcr_count() {
		return pcr_count;
	}

	public GatherPIDData getGatherer() {
		return gatherer;
	}

	public static Logger getLogger() {
		return logger;
	}

	public static int getPes() {
		return PES;
	}

	public ArrayList<TimeStamp> getPcrList() {
		return pcrList;
	}

	public ArrayList<TimeStamp> getPtsList() {
		return ptsList;
	}

	public ArrayList<TimeStamp> getDtsList() {
		return dtsList;
	}

	public HashMap<Integer, ArrayList<TemiTimeStamp>> getTemiList() {
		return temiList;
	}

	public LabelMaker getLabelMaker() {
		return labelMaker;
	}

}

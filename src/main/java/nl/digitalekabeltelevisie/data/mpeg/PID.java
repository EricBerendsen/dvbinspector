/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors.AFDescriptor;
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
	
	private record ContinuityError(int lastPacketNo, int lastCCounter, int newPacketNo,int newCCounter) {}
	
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
	private long continuity_errors_count = 0;
	private List<ContinuityError> continuityErrors = new ArrayList<>();

	private int last_continuity_counter = -1;
	private int pid = -1;
	private int last_packet_no=-1;
	private TSPacket last_packet = null;
	private int dup_found=0; // number of times current packet is duplicated. 
	private PCR lastPCR;
	private PCR firstPCR;
	private int lastPCRpacketNo = -1;
	private int firstPCRpacketNo =-1;
	private long pcr_count =-1;
	protected TransportStream parentTransportStream = null;

	private final GatherPIDData gatherer = new GatherPIDData();
	
	private final ArrayList<TimeStamp> pcrList = new ArrayList<>();
	private final ArrayList<TimeStamp> ptsList = new ArrayList<>();
	private final ArrayList<TimeStamp> dtsList = new ArrayList<>();
	

	/**
	 * Map &lt;time_line_id, List&lt;TemiTimeStamp&gt;&gt;
	 */
	private final Map<Integer, ArrayList<TemiTimeStamp>> temiMap = new HashMap<>();

	private final LabelMaker labelMaker = new LabelMaker();

	private long lastPts = -1;
	
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
			if(data.length==0) {
				logger.info("packet pretends to have payload, but data is empty, packetNo;"+packet.getPacketNo());
				return;
			}
			final int adaptationFieldControl = packet.getAdaptationFieldControl();
			final boolean packetHasPayload = (adaptationFieldControl==1)||(adaptationFieldControl==3);
			if((lastPSISection==null)){ // nothing started
				// sometimes PayloadUnitStartIndicator is 1, and there is no payload, so check
				// AdaptationFieldControl
				if (packet.isPayloadUnitStartIndicator() && (data.length > 1) && packetHasPayload) {
					startNewSection(packet, parentPID, data);
				}
				//	something started
			} else if (packetHasPayload && type==PSI) {
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
							ptsList.add(new TimeStamp(packet.getTimeBase(), pesHeader.getPts()));
						}
						if (pts_dts_flags == 3) { // DTS present,
							dtsList.add(new TimeStamp(packet.getTimeBase(), pesHeader.getDts()));
						}
					}
				} catch (Exception e) {
					logger.log(Level.WARNING, "Error getting PTS/DTS from PESHeader in packet:"
							+ packet.getPacketNo() + " from PID:" + parentPID.getPid(), e);
				}
			}
		}
		
	}


	static class LabelMaker{
		
		private String base;
		private final LinkedHashMap <String, LinkedHashSet<String>> components = new LinkedHashMap<>();
		
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
			
			sb.append(String.join(", ", servicesList));
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
				
				if(packet.isPayloadUnitStartIndicator() && type != PSI) {
					PesHeader pesHeader = packet.getPesHeader();
					if(pesHeader != null) {
						if(pesHeader.hasPTS()) {
							lastPts = pesHeader.getPts();
						} else {
							lastPts  = -1L;
						}
					}
				}
				if (packet.hasAdaptationField()) {
					processTEMI(adaptationField, temiMap, packet.getPacketNo(), lastPts);
				}

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
			processAdaptationField(adaptationField,packet.getPacketNo(),packet.getTimeBase());
		}catch(final RuntimeException re){ // might be some error in adaptation field, it is not well protected
			logger.log(Level.WARNING, "Error getting adaptationField", re);
			adaptationField = null;
		}
		return adaptationField;
	}

	private void handleContinuityError(final TSPacket packet) {
		//logger.warning("continuity error, PID="+pid+", last="+last_continuity_counter+", new="+packet.getContinuityCounter()+", last_no="+last_packet_no +", packet_no="+packet.getPacketNo()+", adaptation_field_control="+packet.getAdaptationFieldControl());
		continuityErrors.add(new ContinuityError(last_packet_no, last_continuity_counter, packet.getPacketNo(), packet.getContinuityCounter()));
		last_continuity_counter=packet.getContinuityCounter();
		last_packet_no = packet.getPacketNo();
		last_packet = packet;
		continuity_errors_count++;
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


	private void processAdaptationField(AdaptationField adaptationField, int packetNo, long timeBase) {
		if (adaptationField.isPCR_flag()) {
			final PCR newPCR = adaptationField.getProgram_clock_reference();
			if(PreferencesManager.isEnablePcrPtsView()) {
				pcrList.add(new TimeStamp(timeBase, newPCR.getProgram_clock_reference_base()));
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

	private static void processTEMI(AdaptationField adaptationField, Map<Integer, ArrayList<TemiTimeStamp>> temiList, int packetNo, long pts) {
		if (adaptationField.isAdaptation_field_extension_flag()) {
			if (!adaptationField.isAf_descriptor_not_present_flag()) {
				List<AFDescriptor> afDescriptorList = adaptationField.getAfDescriptorList();
				for (AFDescriptor descriptor : afDescriptorList) {
					if (descriptor instanceof TimelineDescriptor timelineDescriptor) {
						if ((timelineDescriptor.getHas_timestamp() == 1) || (timelineDescriptor.getHas_timestamp() == 2)) {
							ArrayList<TemiTimeStamp> tl = temiList.computeIfAbsent(timelineDescriptor.getTimeline_id(), k -> new ArrayList<>());
							tl.add(new TemiTimeStamp(packetNo, pts, timelineDescriptor.getMedia_timestamp(), timelineDescriptor.getTimescale(),
									timelineDescriptor.getDiscontinuity(), timelineDescriptor.getPaused()));
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
			final JMenuItem pesMenu = new JMenuItem(generalPidHandler.getMenuDescription());
			pesMenu.setActionCommand(DVBtree.PARSE);
			kvp.setSubMenuAndOwner(pesMenu,this);
		}
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);

		t.add(new DefaultMutableTreeNode(new KVP("packets",getPackets(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("duplicate packets",dup_packets,null)));
		final KVP continuityErrorsKvp = new KVP("continuity errors",continuity_errors_count,null);
		continuityErrorsKvp.addHTMLSource(()->createHtmlList(continuityErrors),"Continuity Errors");
		t.add(new DefaultMutableTreeNode(continuityErrorsKvp));


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
			t.add(generalPidHandler.getJTreeNode(modus));
		}

		return t;
	}

	/**
	 * @param continuityErrors2
	 * @return
	 */
	private  String createHtmlList(List<ContinuityError> continuityErrorsList) {
		if(continuityErrorsList.isEmpty()) {
			return "No Continuity Errors in this PID";
		}
		StringBuilder sb = new StringBuilder("""
			<table><tr>\
			<th>Last<br>Packet<br>No</th>\
			<th>Last<br>Continuity<br>Counter</th>\
			<th>Next<br>Packet<br>No</th>\
			<th>Next<br>Continuity<br>Counter</th>\
			</tr>""");
		for(ContinuityError error:continuityErrorsList) {
			sb.append("<tr>").
			append(getPacketColumns(error.lastPacketNo, error.lastCCounter)).
			append(getPacketColumns(error.newPacketNo, error.newCCounter)).

			append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private String getPacketColumns(int packetNo, int cCounter) {
		return new StringBuilder().
				append("<td align=\"right\">").
				append("<a href=\"").
				append(getPacketCrumbTrail(packetNo)).
				append("\">").
				append(packetNo).
				append("</a>").
				append("</td><td align=\"right\">").
				append(cCounter).
				append("</td>").
				toString();
	}
	
	private String getPacketCrumbTrail(int packetNo) {
		return new StringBuilder().
		append("root/pids/pid:").
		append(pid).
		append("/transport packets/").
		append(packetNo).
		toString();

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
			final float repRate=((float)(last-first)*parentTransportStream.getPacketLenghth()*8)/((count-1)*bitrate);
			try (Formatter formatter = new Formatter()){
				return "repetition rate: "+formatter.format("%3.3f seconds",repRate);
			}
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

	public long getContinuity_errors_count() {
		return continuity_errors_count;
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

	public ArrayList<TimeStamp> getPcrList() {
		return pcrList;
	}

	public ArrayList<TimeStamp> getPtsList() {
		return ptsList;
	}

	public ArrayList<TimeStamp> getDtsList() {
		return dtsList;
	}

	public Map<Integer, ArrayList<TemiTimeStamp>> getTemiMap() {
		return temiMap;
	}

	public LabelMaker getLabelMaker() {
		return labelMaker;
	}

}

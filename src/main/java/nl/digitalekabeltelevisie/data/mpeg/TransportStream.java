/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.AVCHD_PACKET_LENGTH;
import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.MAX_PIDS;
import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.sync_byte;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.io.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.AC4Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2MIDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.TtmlSubtitlingDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.AC3Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.EAC3Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.Audio138183Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac.Audio144963Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.ac4.AC4Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.DVBSubtitleHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ebu.EBUTeletextHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.smpte.Smpte2038Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.temi.TEMIPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ttml.TtmlPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.Video138182Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.jpegxs.JpegXsHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video264.Video14496Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video265.H265Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video266.H266Handler;
import nl.digitalekabeltelevisie.data.mpeg.pid.t2mi.T2miPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.data.mpeg.psi.handler.GeneralPsiTableHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.M7Fastscan;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.ONTSection;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.OperatorFastscan;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.util.*;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;


/**
 * TransportStream is responsible for parsing a file containing a transport stream, dividing it into 188 byte TSPackets, and handing them over to the correct PID.
 *
 */
public class TransportStream implements TreeNode{


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
	private final File file;
	/**
	 * after reading a TSPAcket from the file, it is handed over to the respective PID for aggregating into larger PES or PSI sections, and further processing.
	 */
	private PID [] pids = new PID [MAX_PIDS];
	/**
	 * for every TSPacket read, store it's packet_id. Used for bit rate calculations, and Grid View
	 */
	private final short [] packet_pid;
	private int [] packetATS;

	private OffsetHelper offsetHelper;
	private RollOverHelper rollOverHelper;
	
	/**
	 * Get value once at creation of TS, because getting it for every call to getAVCHDPacketTime is a bit expensive
	 */
	private boolean enabledHumaxAtsFix;
	/**
	 * Starting point for all the PSI information in this TransportStream
	 */
	private PSI psi = new PSI();
	/**
	 * how many TSPackets have bean read.
	 */
	private int no_packets;
	/**
	 * number of TSPackets that had Transport Error Indicator set.
	 */
	private int error_packets;
	/**
	 * Bitrate based on the average of all PIDs that contain a PCR. This is the most accurate way to calculate the bit rate.
	 */
	private long bitRate = -1L;
	/**
	 * for streams that have no PIDS with PCRs (empty transport streams) this value is calculated based on the number of bytes between different occurences of the TDT table
	 */
	private long bitRateTDT = -1L;
	/**
	 * time at which this transportStream started. Calculated by calculating backwards from first TDT, using bitrate. null if no TDT found
	 */
	private LocalDateTime zeroTime;

	private final long len;
	
	/**
	 * number of times sync was lost
	 */
	private int sync_errors;

	private int packetLength = 188;

	public static final int [] ALLOWED_PACKET_LENGTHS = {188,AVCHD_PACKET_LENGTH,204,208};
	

	/**
	 *
	 * Creates a new Transport stream based on the supplied file. After construction the TransportStream is not complete, first parseStream() has to be called!
	 * @param fileName name of the file to be read (null not permitted).
	 */
	public TransportStream(String fileName) throws NotAnMPEGFileException,IOException {
		this(new File(fileName));
	}

	/**
	 *
	 * Creates a new Transport stream based on the supplied file. After construction the TransportStream is not complete, first parseStream() has to be called!
	 * @param file the file to be read (null not permitted). Don't enable TSPackets by default.
	 */

	public TransportStream(File file) throws NotAnMPEGFileException,IOException{
		this.file = file;
		len = file.length();
		packetLength = determinePacketLengthToUse(file);
		int max_packets = (int) (len / packetLength);
		packet_pid = new short [max_packets];
		if(isAVCHD()) {
			packetATS = new int [max_packets];
			enabledHumaxAtsFix = PreferencesManager.isEnableHumaxAtsFix();
			rollOverHelper = new RollOverHelper(max_packets);
		}
		offsetHelper = new OffsetHelper(max_packets,packetLength);

	}
	
	private static int determinePacketLengthToUse(File file) throws NotAnMPEGFileException, IOException {
		int packetLengthModus = PreferencesManager.getPacketLengthModus();
		if(packetLengthModus == 0) { // auto
			return determineActualPacketLength(file);
		}
		return packetLengthModus;
	}

	/**
	 * tries to find the actual packetLength of packets in this file by finding a sequence of 5 sync bytes at the same distance, starting within the first 1000 bytes of the file
	 * @param file
	 * @return
	 */
	private static int determineActualPacketLength(File file) throws NotAnMPEGFileException,IOException{
		if(file.length() < 752L) {
			throw new NotAnMPEGFileException("File too short to determine packet length automatic. File should have at least 5 consecutive packets.\n\nTry setting packet length manual.");
		}
		try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
			for(int possiblePacketLength:ALLOWED_PACKET_LENGTHS){
				logger.log(Level.INFO, "Trying for packetLength {0}",possiblePacketLength);
	
				if(usesPacketLength(possiblePacketLength, randomAccessFile)){
					logger.log(Level.INFO, "Found packetLength {0}",possiblePacketLength);
					return possiblePacketLength;
				}
			}
		}
		throw new NotAnMPEGFileException("""
                DVB Inspector could not determine packetsize for this file.\s
                DVB Inspector supports packet sizes of 188, 192, 204 and 208 bytes.

                 \
                Are you sure this file contains a valid MPEG Transport Stream?

                \s""");
	}

	/**
	 * @param possiblePacketLength
	 * @param randomAccessFile
	 * @return
	 * @throws IOException
	 */
	private static boolean usesPacketLength(int possiblePacketLength, RandomAccessFile randomAccessFile) throws IOException {
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
				randomAccessFile.seek(startPos + ((long)i * possiblePacketLength));
				logger.log(Level.INFO, "found {0} sequence syncs at pos {1}",new Object[]{i,startPos + (i * possiblePacketLength)});
				seqFound = (randomAccessFile.read() == sync_byte);
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
	public void parseStream(java.awt.Component component) throws IOException {
		try (InputStream is = new FileInputStream(file);
				PositionPushbackInputStream fileStream = (component==null) ? 
						new PositionPushbackInputStream(new BufferedInputStream(is),300): 
						new PositionPushbackInputStream(new BufferedInputStream(new ProgressMonitorLargeInputStream(component,
						"Reading file \"" + file.getPath() +"\"",is, file.length())),300)
	
				) {
			no_packets = 0;

			pids = new PID[MAX_PIDS];
			psi = new PSI();
			error_packets = 0;
			bitRate = -1L;
			bitRateTDT = -1L;

			if(isAVCHD()) {
				readAVCHDPackets(fileStream);
			} else {
				readPackets(fileStream);
			}
		}
		postProcess();
	}

	private void readPackets(PositionPushbackInputStream fileStream) throws IOException {
		int count = 0;
		int bytes_read = 0;
		int lastHandledSyncErrorPacket = -1;
		byte[] buf = new byte[packetLength];
		do {
			long offset = fileStream.getPosition();
			bytes_read = fileStream.read(buf, 0, packetLength);
			int next = fileStream.read();
			if ((bytes_read == packetLength) && (buf[0] == sync_byte)
					&& ((next == -1) || (next == sync_byte))) {
				// always push back first byte of next packet
				if ((next != -1)) {
					fileStream.unread(next);
				}
				offsetHelper.addPacket(no_packets, offset);
				processPacket(new TSPacket(buf, count, this));
				count++;
			} else { // something wrong, find next syncbyte. First push back the lot
				if ((next != -1)) {
					if (lastHandledSyncErrorPacket != no_packets) {
						sync_errors++;
						logger.severe(String.format("Did not find sync byte, resyncing at offset:%d, packet_no:%d", offset,
								no_packets));
						lastHandledSyncErrorPacket = no_packets;
					}
					fileStream.unread(next);
					fileStream.unread(buf, 0, bytes_read);
					// now read 1 byte and restart all
					fileStream.read(); // ignore result
				}
			}
		} while (bytes_read == packetLength);
	}

	private void readAVCHDPackets(PositionPushbackInputStream fileStream) throws IOException {
		int count = 0;
		int bytes_read = 0;
		int lastHandledSyncErrorPacket = -1;
		byte[] buf = new byte[AVCHD_PACKET_LENGTH];
		
		int lastArrivalTimeStamp = Integer.MAX_VALUE;
		long currentRollOver = -1L;
		do {
			long offset = fileStream.getPosition();
			bytes_read = fileStream.read(buf, 0, AVCHD_PACKET_LENGTH);
			byte[] nextBytes =new byte[5];
			int next = fileStream.read(nextBytes,0,5);
			if ((bytes_read == packetLength) && (buf[4] == sync_byte)
					&& ((next != 5) || (nextBytes[4] == sync_byte))) {
				// always push back first byte of next packet
				if ((next != -1)) {
					fileStream.unread(nextBytes,0,next);
				}
				offsetHelper.addPacket(no_packets, offset);
				AVCHDPacket packet = new AVCHDPacket(buf, count, this);
				int arrivalTimestamp = packet.getArrivalTimestamp();
				if (arrivalTimestamp < lastArrivalTimeStamp) {
					currentRollOver++;
					rollOverHelper.addPacket(count, currentRollOver);
				}
				lastArrivalTimeStamp = arrivalTimestamp;
				packetATS[count] = arrivalTimestamp;
				processPacket(packet);
				count++;
			} else { // something wrong, find next syncbyte. First push back the lot
				if ((next != -1)) {
					if (lastHandledSyncErrorPacket != no_packets) {
						sync_errors++;
						logger.severe(String.format("Did not find sync byte, resyncing at offset:%d, packet_no:%d", offset,
								no_packets));
						lastHandledSyncErrorPacket = no_packets;
					}
					fileStream.unread(nextBytes,0,next);
					fileStream.unread(buf, 0, bytes_read);
					// now read 1 byte and restart all
					fileStream.read(); // ignore result
				}
			}
		} while (bytes_read == packetLength);
	}

	public void postProcess() {
		namePIDs();
		setGeneralPsiTableHandlers();
		calculateBitRate();
		calculateBitrateTDT();
		calculateZeroTime();
	}

	private void processPacket(TSPacket packet) {
		short pid = packet.getPID();
		packet_pid[no_packets]=addPIDFlags(packet, pid);
		no_packets++;
		if(pids[pid]==null) {
			pids[pid] = new PID(pid,this);
		}
		pids[pid].updatePacket(packet);
		if(packet.isTransportErrorIndicator()){
			error_packets++;
			logger.warning(String.format("TransportErrorIndicator set for packet %s", packet));
		}
	}

	private static short addPIDFlags(TSPacket packet, short pid) {
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
	public void parsePidStreams(Map<Integer,GeneralPidHandler> toParsePids) throws IOException {
		if((toParsePids==null)||(toParsePids.isEmpty())){
			return;
		}
		
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
			for(int t=0; t<no_packets;t++){
				int pid = getPacket_pid(t);
				GeneralPidHandler handler = toParsePids.get(pid);
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


	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Transportstream :").append(file.getName()).append('\n');
		for (int i = 0; i < pids.length; i++) {
			PID pid = pids[i];
			if(pid!=null)
			{
				buf.append("  PID :").append(i).append(", ").append(pid).append(" packets, ").append((pid.getPackets() * 100) / no_packets).append("%, duplicate packets:").append(pid.getDup_packets()).append("\n");
			}
		}

		return buf.toString();

	}

	public File getFile() {
		return file;
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

	@Override
	public KVP getJTreeNode(int modus){

		KVP t = new KVP("Transport Stream "+psi.getPat().getTransportStreamId()).setCrumb("root");
		
		t.addHTMLSource(() ->getSummary(modus), "Summary");

		t.add(new KVP("file",file.getPath()));
		t.add(new KVP("size",file.length()));

		t.add(new KVP("modified",String.format("%1$tc", file.lastModified())));
		t.add(new KVP("TS packets",no_packets));
		t.add(new KVP("packet size",packetLength).setDescription(PreferencesManager.getPacketLengthModus()==0?"(detected)":"(forced)"));
		t.add(new KVP("Error packets",error_packets));
		t.add(new KVP("Sync Errors",sync_errors));
		if(bitRate!= -1L){
			t.add(new KVP("bitrate",bitRate));
			t.add(new KVP("length (secs)",(file.length()* 8L)/bitRate));
		}
		if(bitRateTDT!= -1L){
			t.add(new KVP("bitrate based on TDT",bitRateTDT));
			t.add(new KVP("length (secs)",(file.length()* 8L)/bitRateTDT));
		}

		t.add(psi.getJTreeNode(modus));
		if(!psiOnlyModus(modus)){
			KVP pidTreeNode = new KVP("PIDs").addTableSource(this::getTableModel,"PIDs");
			t.add(pidTreeNode);
			for (PID pid : pids) {
				if((pid)!=null){
					pidTreeNode.add(pid.getJTreeNode(modus));
				}

			}
			// TSPackets
            if (no_packets == 0) {
                t.add(new KVP("Transport packets "));
            } else {
                JTreeLazyList list = new JTreeLazyList(new TSPacketGetter(this, modus));
                t.add(list.getJTreeNode(modus, "Transport packets "));
            }
		}

		return t;
	}

	
	private String getSummary(int modus) {
		StringBuilder sb = new StringBuilder();
		int transportStreamId = psi.getPat().getTransportStreamId();
		if(transportStreamId != -1) {
			sb.append("Transport Stream: ").append(transportStreamId).append("<br/>");
		}
		
		Map<Integer, PMTsection[]> pmts = psi.getPmts().getPmts();
		Iterable<Integer> serviceIds = new TreeSet<>(pmts.keySet());

		sb.append("<ol>");

		for (Integer programNumber : serviceIds) {
			PMTsection[] sections = pmts.get(programNumber);
			
			sb.append("<li>program: ")
			.append("<a href=\"root/psi/pmts/program:")
			.append(programNumber)
			.append("\">")
			.append(programNumber)
			.append("</a>");
			psi.getSdt().getServiceNameForActualTransportStreamOptional(programNumber).ifPresent(s -> sb.append(" (").append(s).append(')'));
			sb.append("<br/>");
			
			PMTsection pmtSection = sections[0];
			sb.append("<ol>");
			for(Component component:pmtSection.getComponentenList()) {
				sb.append("<li>Pid: ");
				if(!Utils.psiOnlyModus(modus)) {
					sb.append("<a href=\"root/pids/pid:")
						.append(component.getElementaryPID())
						.append("\">");
				}
				sb.append(component.getElementaryPID());
				if(!Utils.psiOnlyModus(modus)) {
					sb.append("</a>");
				}
				sb.append(" Stream type: ")
				.append(component.getStreamtype())
				.append(" (")
				.append(determineComponentType(component.getComponentDescriptorList()).
						map(ComponentType::getDescription).
						orElse(getStreamTypeShortString(component.getStreamtype())))
				.append(")</li>");
			}
			sb.append("</ol><br/>");

			
			EITsection[] pf = psi.getEit().getActualTransportStreamEitPF(programNumber);
			if(pf.length >0) {
				
				sb.append("EIT p/f:<br/>");
				for(EITsection section:pf) {
					int sectionNumber = section.getSectionNumber();
					for(Event event : section.getEventList()) {
						sb.append(Utils.escapeHTML(getEITStartTimeAsString(event.getStartTime())))
						.append("&nbsp;")
						.append(formatDuration(event.getDuration()))
						.append("&nbsp;")
						
						.append("<a href=\"root/psi/eit/original_network_id:")
						.append(section.getOriginalNetworkID())
						.append("/transport_stream_id:")
						.append(section.getTransportStreamID())
						.append("/service_id:")
						.append(programNumber)
						.append("/tableid:78");
						if(!Utils.simpleModus(modus)) {
							sb.append("/tablesection:")
								.append(sectionNumber)
								.append("/events");
							
						}
						sb.append("/event:")
						.append(event.getEventID())
						.append("\">")
						.append(event.getEventName())
						.append("</a><br/>");
					}
				}
			}
			
			
			sb.append("<br/></li>");
		}
		sb.append("</ol>");
		
		return sb.toString();
	}
	
	static TableHeader<TransportStream,PID> buildPidTableHeader() {

		return new TableHeaderBuilder<TransportStream,PID>().
				addOptionalRowColumn("pid", PID::getPid, Integer.class).
				addOptionalRowColumn("label", p->p.getLabelMaker().toString(), String.class).
				addOptionalRowColumn("pid type", PID::getTypeString, String.class).
				addOptionalRowColumn("packets", PID::getPackets, Integer.class).
				addOptionalRowColumn("duplicate packets", PID::getDup_packets, Integer.class).
				addOptionalRowColumn("continuity errors", PID::getContinuity_errors_count, Integer.class).
				addOptionalRowColumn("scrambled", PID::isScrambled, Boolean.class).
				build();
	}
		

	public TableModel getTableModel() {
		FlexTableModel<TransportStream,PID> tableModel =  new FlexTableModel<>(buildPidTableHeader());
		List<PID> pidsList = Arrays.asList(pids);
		
		tableModel.addData(this, pidsList);

		tableModel.process();
		return tableModel;
	}

	private void setLabelMakerBase(int pidNo, String base)
	{
		if(pids[pidNo]!=null){
			pids[pidNo].getLabelMaker().setBase(base);
		}

	}

	private void addLabelMakerComponent(int pidNo, String type, String serviceName)
	{
		if(pids[pidNo]!=null){
			pids[pidNo].getLabelMaker().addComponent(type, serviceName);
		}

	}

	/**
	 * returns labels for the fixed PIds, like PAT, CAT, etc
	 *
	 * @param pid
	 * @return
	 */
	private static String getFixedLabel(short pid){
		switch (pid) {
		case 0:
			return "PAT";
		case 0x1:
			return "CAT";
		case 0x2:
			return "TSDT";
		case 0x3:
			return "IPMP control information table "; // ISO/IEC 13818-1:2013 (E) 
		case 0x4:
			return "Adaptive streaming information"; // ISO/IEC 13818-1:2013/Amd.4:2014 (E) 
		case 0x10:
			return  "NIT";
		case 0x11:
			return "SDT/BAT";
		case 0x12:
			return "EIT";
		case 0x13:
			return "RST, ST";
		case 0x14:
			return "TOT/TDT";
		case 0x15:
			return "network synchronization";
		case 0x16:
			return "RNT (TS 102 323)";
		case 0x1b:
			return "SAT";
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


	private void namePIDs() {

		// first the easy ones, the fixed values
		for (short i = 0; i <=0x1f; i++) {
			setLabelMakerBase(i, getFixedLabel(i));
		}

		setLabelMakerBase(8191,"NULL Packets (Stuffing)");

		// now the streams referenced from the CAT
		if(pids[1]!=null){
			for(CADescriptor caDescriptor:findGenericDescriptorsInList(psi.getCat().getDescriptorList(), CADescriptor.class)){
				addLabelMakerComponent(caDescriptor.getCaPID(), "EMM", "CA_ID:"+ caDescriptor.getCaSystemID()+ " ("+ getCASystemIDString(caDescriptor.getCaSystemID())+")");
			}
		}

		// now all services, starting with PMTs themselves, then referenced ES
		for(PMTsection[] pmt: psi.getPmts()){
			PMTsection pmtSection = pmt[0];
			while(pmtSection!=null){
				int service_id=pmtSection.getProgramNumber();
				String service_name = psi.getSdt().getServiceNameForActualTransportStreamOptional(service_id).orElse("Service "+service_id);

				labelPmtForProgram(pmtSection, service_name);
				labelEcmForProgram(pmtSection, service_name);
				labelComponentsForProgram(pmtSection, service_name);
				labelPcrForProgram(pmtSection, service_name);

				pmtSection =(PMTsection)pmtSection.getNextVersion();
			}
		}
		
		// Tables like AIT, DSM-CC, UNT, INT< etc, are all referenced from at least one PMT, so have been given a label
		// However, there is an exception;
		if(PreferencesManager.isEnableM7Fastscan()) {
			labelM7FastscanTables();
		}
		

	}

	private void setGeneralPsiTableHandlers() {
		if(PreferencesManager.isEnableGenericPSI()) {
			for (PID pid : pids) {
				if((pid!=null)&&(pid.getType()==PID.PSI)) {
						GeneralPSITable psiData = pid.getPsi();
						if (((!psiData.getLongSections().isEmpty())|| 
								(!psiData.getSimpleSectionsd().isEmpty())) && pid.getPidHandler()==null) {
							GeneralPsiTableHandler generalPsiTableHandler = new GeneralPsiTableHandler();
							generalPsiTableHandler.setPID(pid);
							generalPsiTableHandler.setTransportStream(this);
							pid.setPidHandler(generalPsiTableHandler);
						}
					}
				}
			}
	}

	/**
	 * 
	 */
	private void labelM7FastscanTables() {
		
		M7Fastscan fastScan = psi.getM7fastscan();
		labelM7FastscanONT(fastScan);
		labelM7FastscanFstFnt(fastScan);
	}

	private void labelM7FastscanFstFnt(M7Fastscan fastScan) {
		Map<Integer, Map<Integer, OperatorFastscan>> operators = fastScan.getOperators();
		for (Integer operatorId : new TreeSet<>(operators.keySet())) {
			Map<Integer, OperatorFastscan> operatorsInPid = operators.get(operatorId);
			for (Integer pid : new TreeSet<>(operatorsInPid.keySet())) {
				StringBuilder name = new StringBuilder("M7 FastScan operator ").append(fastScan.getOperatorName(operatorId));
				
				OperatorFastscan operatorFastscan = operatorsInPid.get(pid);
				if(operatorFastscan.getOperatorSubListName()!=null) {
					name.append(" ").append(operatorFastscan.getOperatorSubListName());
				}
				if(operatorFastscan.getFntSections() != null){
					name.append(" FNT");
				}
				if(operatorFastscan.getFstSections() != null){
					name.append(" FST");
				}
				if(pids[pid]!=null){
					pids[pid].getLabelMaker().setBase(name.toString());
				}
			}
		}
	}

	private static void labelM7FastscanONT(M7Fastscan fastScan) {
		ONTSection[] sections = fastScan.getOntSections();
		if(sections != null) {
			for(ONTSection section:sections) {
				if(section != null) {
					section.getParentPID().getLabelMaker().setBase("M7 FastScan ONT");
					return;
				}
			}
		}
	}

	private void labelPmtForProgram(PMTsection pmtSection, String service_name) {
		addLabelMakerComponent(pmtSection.getParentPID().getPid(),"PMT",service_name);
	}

	private void labelEcmForProgram(PMTsection pmtSection, String service_name) {
		for(CADescriptor caDescriptor:findGenericDescriptorsInList(pmtSection.getDescriptorList(), CADescriptor.class)){
			addLabelMakerComponent(caDescriptor.getCaPID(), "ECM", "CA_ID:"+ caDescriptor.getCaSystemID()+ " ("+service_name+")");

		}
	}

	private void labelPcrForProgram(PMTsection pmtSection, String service_name) {
		int PCR_pid = pmtSection.getPcrPid();
		boolean pcrInComponent = false;
		for(Component component:pmtSection.getComponentenList()) {
			if (PCR_pid == component.getElementaryPID()) {
				pcrInComponent = true;
				break;
			}
		}
		if(!pcrInComponent && PCR_pid!=MPEGConstants.NO_PCR_PID) {// ISO/IEC 13818-1:2013, 2.4.4.9; If no PCR is associated with a program definition for private streams, then this field shall take the value of 0x1FFF.
			addLabelMakerComponent(PCR_pid,"PCR",service_name);
		}

	}

	private void labelComponentsForProgram(PMTsection pmtSection, String service_name) {
		for(Component component:pmtSection.getComponentenList()) {
			int streamType = component.getStreamtype();
			GeneralPidHandler generalPidHandler = determinePesHandlerByStreamType(component,streamType);

			Optional<ComponentType> componentType = determineComponentType(component.getComponentDescriptorList());
			
			addLabelMakerComponent(
					component.getElementaryPID(), 
					componentType.map(ComponentType::getDescription).orElse(getStreamTypeShortString(streamType)),
					service_name
					);
			if (componentType.isPresent()) {
				switch (componentType.get()) {
				case DVB_SUBTITLING:
					generalPidHandler = new DVBSubtitleHandler();
					break;
				case TELETEXT, VBI:
					generalPidHandler = new EBUTeletextHandler();
					break;
				case AC3:
					generalPidHandler = new AC3Handler();
					break;
				case AC4:
					generalPidHandler = new AC4Handler();
					break;
				case E_AC3:
					generalPidHandler = new EAC3Handler();
					break;
				case AIT, RCT:
					break;
                case T2MI:
					generalPidHandler = new T2miPidHandler();
					break;
				case TTML:
					generalPidHandler = new TtmlPesHandler();
					break;
				case SMPTE2038:
					generalPidHandler = new Smpte2038Handler();
					break;
				default:
					logger.warning(String.format("no componenttype found for pid %d, part of service %s", component.getElementaryPID(), service_name));
				}
			}

			PID pid = pids[component.getElementaryPID()];
			if (pid!=null && generalPidHandler!=null) {
				generalPidHandler.setTransportStream(this);
				generalPidHandler.setPID(pid);
				pid.setPidHandler(generalPidHandler);
			}
			
			List<CADescriptor> caDescriptorList =findGenericDescriptorsInList(component.getComponentDescriptorList(), CADescriptor.class);
			for(CADescriptor cad: caDescriptorList) {
				addLabelMakerComponent(cad.getCaPID(), "ECM", "CA_ID:"+ cad.getCaSystemID()+ " ("+service_name+")");

			}
		}
	}

	public static Optional<ComponentType> determineComponentType(List<Descriptor> componentDescriptorList){
		return Optional.ofNullable(findComponentType(componentDescriptorList));
	}
	
	private static ComponentType findComponentType(List<Descriptor> componentDescriptorList) {
		
		for(Descriptor descriptor:componentDescriptorList){
			switch (descriptor) {
				case SubtitlingDescriptor ignored:
					return ComponentType.DVB_SUBTITLING;
				case TeletextDescriptor ignored:
					return ComponentType.TELETEXT;
				case VBIDataDescriptor ignored:
					return ComponentType.VBI;
				case AC3Descriptor ignored:
					return ComponentType.AC3;
				case AC4Descriptor ignored:
					return ComponentType.AC4;
				case RegistrationDescriptor registrationDescriptor:
					byte[] formatIdentifier = registrationDescriptor.getFormatIdentifier();
					if (Arrays.equals(formatIdentifier, RegistrationDescriptor.AC_3)) {
						return ComponentType.AC3;
					}
					if (Arrays.equals(formatIdentifier, RegistrationDescriptor.SMPTE_2038)) {
						return ComponentType.SMPTE2038;
					}
					break;
				case EnhancedAC3Descriptor ignored:
					return ComponentType.E_AC3;
				case ApplicationSignallingDescriptor ignored:
					return ComponentType.AIT;
				case RelatedContentDescriptor ignored:
					return ComponentType.RCT;
				case T2MIDescriptor ignored:
					return ComponentType.T2MI;
				case TtmlSubtitlingDescriptor ignored:
					return ComponentType.TTML;
				default:
			}
		}

		return null;
	}

	private GeneralPidHandler determinePesHandlerByStreamType(Component component,
                                                              int streamType) {
		int componentElementaryPID = component.getElementaryPID();
		PID pid = pids[componentElementaryPID];
		if ((pid != null) && (!pid.isScrambled()) && (pid.getType() == PID.PES)){
			return switch(streamType){
				case 1,2 -> new Video138182Handler();
				case 3,4 -> new Audio138183Handler(getAncillaryDataIdentifier(component));
				case 0x11 -> new Audio144963Handler();
				case 0x1B -> new Video14496Handler();
				case 0x20 -> new Video14496Handler(); //MVC video sub-bitstream of an AVC video stream conforming to one or more profiles defined in Annex H of ITU-T Rec. H.264 | ISO/IEC 14496-10
				case 0x24 -> new H265Handler();
				case 0x27 -> new TEMIPesHandler();
				case 0x33 -> new H266Handler();
				case 0x32 -> new JpegXsHandler();
				default -> new GeneralPesHandler();
			};
		}
		return null;
	}

	private static int getAncillaryDataIdentifier(Component component) {
		List<AncillaryDataDescriptor> ancillaryDataDescriptors = findGenericDescriptorsInList(component.getComponentDescriptorList(), AncillaryDataDescriptor.class);
		if(!ancillaryDataDescriptors.isEmpty()){
			return ancillaryDataDescriptors.getFirst().getAncillaryDataIdentifier();
		}
		return 0;
	}



	/**
	 *
	 */
	private void calculateBitRate() {

		// now calculate bitrate of stream by averaging bitrates of PIDS with PCR

		int teller=0;
		long totBitrate= 0L;
		for (PID pid : pids) {
			if ((pid != null) && (pid.getBitRate() != -1L)) {
				teller++;
				totBitrate += pid.getBitRate();
			}
		}
		if (teller != 0) {
			bitRate = totBitrate / teller;
		}
	}

	private void calculateBitrateTDT() {
		// calculate bitrate based on TDT sections. Need at least 2
		if(psi.getTdt()!=null){
			List<TDTsection> tdtSectionList  = psi.getTdt().getTdtSectionList();
			if(tdtSectionList.size()>=2){
				TDTsection first = tdtSectionList.getFirst();
				TDTsection last = tdtSectionList.getLast();
				long diffPacket = (long)last.getPacket_no() - first.getPacket_no();
				LocalDateTime utcCalenderLast = getUTCLocalDateTime(last.getUTC_time());
				LocalDateTime utcCalenderFirst = getUTCLocalDateTime(first.getUTC_time());
				// getUTCCalender might fail if not correct BCD, then will return null.
				if((utcCalenderLast!=null)&&(utcCalenderFirst!=null)){
					long timeDiffMills =   utcCalenderFirst.until(utcCalenderLast, ChronoUnit.MILLIS);
					if(timeDiffMills> 0L){ // shit happens... capture.guangdong  has 10 with same timestamp....
						bitRateTDT = (diffPacket * packetLength * 8 * 1000)/timeDiffMills;
					}
				}
			}
		}
	}

	private void calculateZeroTime() {
		if ((psi.getTdt() != null) && (getBitRate() != -1L)) {
			List<TDTsection> tdtSectionList = psi.getTdt().getTdtSectionList();
			if (!tdtSectionList.isEmpty()) {
				TDTsection first = tdtSectionList.getFirst();
				LocalDateTime firstTime = getUTCLocalDateTime(first.getUTC_time());
				if (firstTime != null) {
					long millsIntoStream = ((long) first.getPacket_no() * packetLength * 8 * 1000) / getBitRate();
					zeroTime = firstTime.minus (millsIntoStream, ChronoUnit.MILLIS);
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

		for(PID pid:pids){
			if(pid!=null){
				t++;
			}
		}
		return t;
	}

	public short [] getUsedPids(){
		int no = getNoPIDS();
		short[] r = new short[no];
		int i = 0;
		for (short pid = 0; pid < MAX_PIDS; pid++) {
			if (pids[pid] != null) {
				r[i++] = pid;
			}
		}
		return r;
	}

	public short getPacket_pid(int t) {
		return (short) (0x1fff & packet_pid[t]);
	}

	public short getPacketPidFlags(int t) {
		return  packet_pid[t];
	}

	
	public String getShortLabel(short pid){
		if(pids[pid]!=null){
			return pids[pid].getLabelMaker().toString();
		}
		return null;
	}

	/**
	 * @return the bitrate based on PCRs if available, else bitrate based on TDTs (if available). -1 if we have no idea what the bitrate could be.
	 */
	public long getBitRate() {
		if(bitRate!= -1L){
			return bitRate;
		}else if(bitRateTDT!= -1L){
			return bitRateTDT;
		}
		return -1L;
	}

	/**
	 * @return the length of the stream in seconds, based on PCRs bitrate if available, else based on bitrate based on TDTs (if available). -1 if we have no idea what the length could be.
	 */
	public double getLength(){
		if(bitRate!= -1L){
			return ((double)file.length()*8)/bitRate;
		}else if(bitRateTDT!= -1L){
			return ((double)file.length()*8)/bitRateTDT;
		}else{
			return -1.0;
		}
	}

	public String getPacketTime(int packetNo){
		if(isAVCHD()) {
			return printPCRTime(getAVCHDPacketTime(packetNo));
		}

        if (getBitRate() == -1L) { // no bitrate, return packet number
           return packetNo + " (packetNo)";
        }
		if (zeroTime == null) {
			// return only the hours/min,secs and millisecs. Not TS recording will last days

			Instant instant = Instant.ofEpochMilli(getTimeFromStartInMilliSecs(packetNo));
			LocalDateTime ldt = instant.atZone(ZoneId.of("Z")).toLocalDateTime();
			return getFormattedTime(ldt);
		}
		// calculation in long, intermediate results can be > Integer.MAX_VALUE
		LocalDateTime packetTime =  zeroTime.plusNanos(1_000_000L * getTimeFromStartInMilliSecs( packetNo));

		return getFormattedDateTime(packetTime);
	}

	private int getTimeFromStartInMilliSecs(int packetNo) {
		// calculation in long, intermediate results can be > Integer.MAX_VALUE
		return (int) ((((long) packetNo) * packetLength * 8 * 1000L) / getBitRate());
	}


	private static String getFormattedDateTime(LocalDateTime calendar) {
		return(String.format("%1$tY/%1$tm/%1$td %1$tHh%1$tMm%1$tS:%1$tL", calendar));
	}

	private static String getFormattedTime(LocalDateTime calendar) {

		return(String.format("%1$tHh%1$tMm%1$tS:%1$tL", calendar));
	}


	/**
	 * TODO the parameter packetNoOrPCR has two different meaning, because BitRateChat and TimeStampChart use different X-axis for aVCHD/DVB Full stream
	 * This should be fixed somewhere else ???
	 * @param packetNoOrPCR when stream is a AVCHD stream, this is time in PCR ticks, otherwise this is packetNo (which will be converted into time.
	 *                      packetNo is always in range int, pcr value is long.
	 * @return
	 */
	public String getShortPacketTime(long packetNoOrPCR){
		
		if(isAVCHD()) {
			return printPCRTime(packetNoOrPCR);
		}

		if(getBitRate()!=-1){ //can't calculate time  without a bitrate
			if(zeroTime==null){
				Instant instant = Instant.ofEpochMilli(getTimeFromStartInMilliSecs((int)packetNoOrPCR));
				return getFormattedTime(instant.atZone(ZoneId.of("Z")).toLocalDateTime());
			}

			return getFormattedTime(zeroTime.plusNanos(1_000_000L * getTimeFromStartInMilliSecs((int) packetNoOrPCR)));

		} // no bitrate
		return packetNoOrPCR +" (packetNo)";
	}

	public final boolean isAVCHD() {
		return packetLength == AVCHD_PACKET_LENGTH;
	}

	public PMTsection getPMTforPID(int thisPID) {
		PMTs pmts = psi.getPmts();
		for (PMTsection[] pmTsections : pmts) {
			PMTsection pmt = pmTsections[0];
			for(Component component :pmt.getComponentenList()){
				if(component.getElementaryPID()==thisPID){
					return pmt;
				}
			}
		}
		return null;
	}

	// TODO handle FileNotFoundException more elegant, show some msg in GUI  
	public TSPacket getTSPacket(int packetNo){
		TSPacket packet = null;
		if(offsetHelper.getMaxPacket()>packetNo){
			try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")){
				packet = readPacket(packetNo,randomAccessFile);
			} catch (IOException e) {
				logger.warning("IOException:"+e);
			}
		}else{
			logger.warning(String.format("offsetHelper.getMaxPacket() (%d) < packetNo (%d)", offsetHelper.getMaxPacket(), packetNo));
		}
		return packet;
	}

	private TSPacket readPacket(int packetNo, RandomAccessFile randomAccessFile)
			throws IOException {
		TSPacket packet = null;
		long offset = offsetHelper.getOffset(packetNo);
		randomAccessFile.seek(offset);
		byte [] buf = new byte[packetLength];
		int bytesRead = randomAccessFile.read(buf);
		if(bytesRead==packetLength){
			if(isAVCHD()) {
				packet = new AVCHDPacket(buf, packetNo,this); 
			}else {
				packet = new TSPacket(buf, packetNo,this);
			}
			packet.setPacketOffset(offset);
		}else{
			logger.warning(String.format("read less then packetLenghth (%d) bytes, actual read: %d", packetLength, bytesRead));
		}
		return packet;
	}


	/**
	 * returns time (in system ticks) for packet, starting from 0 for begin of file, based on ATS in TP_extra_header
	 * Only to be called for an AVCHD file
	 */
	public long getAVCHDPacketTime(int packetNo) {
		if (isAVCHD() && packetATS != null) {
			if (enabledHumaxAtsFix) {
				return (rollOverHelper.getRollOver(packetNo) * ((0x4000_0000 >> 9) + 1) * 300)
						+ fixHumaxAts(packetATS[packetNo]) - fixHumaxAts(packetATS[0]);
			}
			return (rollOverHelper.getRollOver(packetNo) * 0x4000_0000) + packetATS[packetNo] - packetATS[0];
		}
		throw new RuntimeException("Not an AVCHD File!");
	}

	/**
	 * Humax PVR records partial TS with arrival time stamps in P_extra_header coded as if PCR;
	 * the last 9 bits are the extension, and have values 0 - 299. 
	 * Then it roles over into the base. 
	 * This method corrects the Humax ATS into a normal ATS
	 * 
	 * Normal AVCHD files use continuous numbering with 30 bits
	 *  
	 */
	private static int fixHumaxAts(int ats) {
		int extension = ats & 0b1_1111_1111; // 9 bits
		int base = (ats & 0x3FFF_FE00) >> 9;
		
		return base * 300 + extension;
	}
	
	public PID getPID(int p){
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

	public int getError_packets() {
		return error_packets;
	}

	/**
	 * @return
	 */
	List<LinkageDescriptor> getLinkageDescriptorsFromNitNetworkLoop() {
		NIT nit = psi.getNit();
		int actualNetworkID = nit.getActualNetworkID();
		List<Descriptor> descriptors = nit.getNetworkDescriptors(actualNetworkID);
		return findGenericDescriptorsInList(descriptors, LinkageDescriptor.class);
	}

	public boolean isONTSection(int pid) {
		NIT nit = psi.getNit();
		int actualNetworkID = nit.getActualNetworkID();
		List<LinkageDescriptor> linkageDescriptors = getLinkageDescriptorsFromNitNetworkLoop();
	
		int streamID = getStreamID();
	
		int originalNetworkID = nit.getOriginalNetworkID(actualNetworkID, streamID);
	
		for (LinkageDescriptor ld : linkageDescriptors) {
			if (ld.getLinkageType() == 0x8D 
					&& ld.getTransportStreamId() == streamID
					&& ld.getOriginalNetworkId() == originalNetworkID 
					&& ld.getServiceId() == pid
					&& M7Fastscan.isValidM7Code(ld.getM7_code())) {
				return true;
			}
		}
		return false;
	}

	public int getFirstAvchdPacketATS() {
		return packetATS[0];
	}


}

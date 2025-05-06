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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static java.lang.Byte.toUnsignedInt;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.Formatter;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

public class TableSection implements TreeNode{

	protected PsiSectionData raw_data;

	private PID parentPID;

	/* non-specific section data */
	protected int tableId; /*!< table_id */

	protected int sectionSyntaxIndicator; /*!< section_syntax_indicator */
	protected int privateIndicator; /*!< private_indicator */
	protected int sectionLength; /*!< section_length */

	/* used if b_syntax_indicator is true */
	protected int tableIdExtension; /*!< table_id_extension */

	/*!< transport_stream_id for a
	 PATsection section */
	protected int version; /*!< version_number */

	protected int currentNext; /*!< current_next_indicator */

	protected int sectionNumber; /*!< section_number */

	protected int sectionLastNumber; /*!< last_section_number */

	/* used if b_syntax_indicator is true */
	protected long crc; /*!< CRC_32 */

	protected boolean crc_error;

	protected TableSection nextVersion;

	/* non DVB fields */

	private int firstPacketNo;
	private int lastPacketNo;
	/**
	 * min distance between packets, in packets for CBR, in system_ ticks for AVCHD
	 */
	private long minPacketDistance = Long.MAX_VALUE;
	/**
	 * max distance between packets, in packets for CBR, in system_ ticks for AVCHD
	 */
	private long maxPacketDistance;
	private int occurrenceCount;
	private int packetNo;

	public TableSection(PsiSectionData raw_data, PID parent){
        this.raw_data = raw_data;
		this.packetNo= raw_data.getPacket_no();
		this.firstPacketNo= raw_data.getPacket_no();
		this.lastPacketNo= raw_data.getPacket_no();
		this.occurrenceCount = 1;

		parentPID = parent;
		byte[] bytes=raw_data.getData(); // just for convenience

		tableId = getInt(bytes, 0, 1, MASK_8BITS);

		sectionSyntaxIndicator = getInt(bytes, 1, 1, 0x80) >>7;
		privateIndicator = getInt(bytes, 1, 1, 0x40) >>6;
		sectionLength = getInt(bytes, 1, 2, MASK_12BITS);

		if(sectionSyntaxIndicator==1){ //long format
			tableIdExtension = getInt(bytes,3,2,MASK_16BITS);
			version = getInt(bytes,5,1,0x3E)>>1;
			currentNext = getInt(bytes,5,1,0x01);
			sectionNumber = getInt(bytes,6,1,MASK_8BITS);
			sectionLastNumber = getInt(bytes,7,1,MASK_8BITS);

			int startCRC = sectionLength -1 ; // +3 - 4, first 3 bytes not included, skip back 4 bytes till begin CRC
			crc = (((long)toUnsignedInt(bytes[startCRC]))<<24) |
					(((long)toUnsignedInt(bytes[startCRC+1]))<<16) |
					(((long)toUnsignedInt(bytes[startCRC+2]))<<8) |
					(toUnsignedInt(bytes[startCRC+3]));

			if (tableId != 0x72) {  //stuffing_section 
				long res = CRCcheck.crc32(bytes, sectionLength + 3);
				if (res != 0L) {
					crc_error = true;
					throw new RuntimeException("CRC Error in packet for pid:" + parent.getPid() + ",tableID:" + tableId
							+ ",tableIdExtension:" + tableIdExtension + ",CRC=" + crc + ", packetNo=" + packetNo
							+ ", privateIndicator=" + privateIndicator + ", sectionSyntaxIndicator:"
							+ sectionSyntaxIndicator);
				}
			}
		}
	}

	public long getCrc() {
		return crc;
	}

	public void setCrc(long crc) {
		this.crc = crc;
	}


	public int getTableIdExtension() {
		return tableIdExtension;
	}

	public void setTableIdExtension(int extension) {
		this.tableIdExtension = extension;
	}

	public int getSectionLastNumber() {
		return sectionLastNumber;
	}

	public void setSectionLastNumber(int lastNumber) {
		this.sectionLastNumber = lastNumber;
	}

	public int getSectionLength() {
		return sectionLength;
	}

	public void setSectionLength(int length) {
		this.sectionLength = length;
	}

	public int getSectionNumber() {
		return sectionNumber;
	}

	public void setSectionNumber(int number) {
		this.sectionNumber = number;
	}



	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public PsiSectionData getRaw_data() {
		return raw_data;
	}

	public void setRaw_data(PsiSectionData raw_data) {
		this.raw_data = raw_data;
	}

	public static String getTableType(int tableId){

		switch (tableId) {
		case 0x00: return "program_association_section";
		case 0x01: return "conditional_access_section";
		case 0x02: return "program_map_section";
		case 0x03: return "transport_stream_description_section";
		case 0x04: return "ISO_IEC_14496_scene_description_section"; // MPEG2 13818-1
		case 0x05: return "ISO_IEC_14496_object_descriptor_section"; // MPEG2 13818-1

		// DVBSnoop dvb_str.c
		case 0x06: return "Metadata Table";				// H.222.0 AMD1
		case 0x07: return "IPMP_Control_Information_section (ISO 13818-11)";  //  H.222.0 AMD1
		
		
		// ISO/IEC 13818-1:2018 (E)
		case 0x08: return "ISO_IEC_14496_section";
		case 0x09: return "ISO/IEC 23001-11 (Green access unit) section";
		case 0x0a: return "ISO/IEC 23001-10 (Quality access unit) section";

		// ISO/IEC 13818-1:2021
		case 0x0b: return "ISO/IEC 23001-13 (Media Orchestration access unit) section";

		
		case 0x38: return "ISO/IEC 13818-6 reserved";
		case 0x39: return "ISO/IEC 13818-6 reserved";
		case 0x3A: return "DSM-CC - multiprotocol encapsulated data";
		case 0x3B: return "DSM-CC - U-N messages (DSI or DII)";
		case 0x3C: return "DSM-CC - Download Data Messages (DDB)";
		case 0x3D: return "DSM-CC - stream descriptorlist";
		case 0x3E: return "DSM-CC sections with private data // DVB datagram (ISO/IEC 13818-6)";
		case 0x3F: return "DSM-CC - Addressable Sections"; // 13818-6:1998/Amd.1:2000

		case 0x40: return "network_information_section - actual_network";
		case 0x41: return "network_information_section - other_network";
		case 0x42: return "service_description_section - actual_transport_stream";

		case 0x46: return "service_description_section - other_transport_stream";

		case 0x4A: return "bouquet_association_section";
		case 0x4B: return "SSU Update Notification Table (UNT)";
		case 0x4C: return "IP/MAC Notification Table (INT)"; // EN 301 192
		case 0x4D: return "satellite_access_table_section"; 

		case 0x4E: return "event_information_section - actual_transport_stream, present/following";
		case 0x4F: return "event_information_section - other_transport_stream, present/following";

		case 0x70: return "time_date_section (TDT)";
		case 0x71: return "running_status_section (RST)";
		case 0x72: return "stuffing_section (ST)";
		case 0x73: return "time_offset_section (TOT)";
		case 0x74: return "MHP-application information section (AIT)"; // (TS 102 812 [17])
		case 0x75: return "TVA-container section (CT)"; //(TS 102 323 [15])
		case 0x76: return "TVA-related content section (RCT)";//(TS 102 323 [15])
		case 0x77: return "TVA-content identifier section (CIT)";//(TS 102 323 [15])
		case 0x78: return "MPE-FEC section (MFT)"; //(EN 301 192 [4])
		case 0x79: return "TVA-resolution notification section (RNT)";//(TS 102 323 [15])
		case 0x7A: return "MPE-IFEC section"; //  (TS 102 772 [51])
		
		case 0x7B: return "protection message section"; // (TS 102 809 [56])
		case 0x7C: return "Downloadable Font Information Table (DFIT)"; // DVB BlueBook A038r14 (December 2021)

		case 0x7E: return "discontinuity_information_section";
		case 0x7F: return "selection_information_section";

		case 0x80: return "CA_message_section, ECM 1";
		case 0x81: return "CA_message_section, ECM 2";

		case 0x91: return "user defined / Service Guide Table (SGT)";

		case 0xBC: return "user defined / M7 Fastscan Network Table (FNT) ";
		case 0xBD: return "user defined / M7 Fastscan Services Table (FST) ";
		case 0xBE: return "user defined / M7 Operator Network Table (ONT) ";


		// https://fossies.org/linux/MediaInfo_CLI/MediaInfoLib/Source/MediaInfo/Multiple/File_Mpeg_Psi.cpp
		case 0xC0 : return "user defined / ATSC - Program Information Message";
        case 0xC1 : return "user defined / ATSC - Program Name Message";
        case 0xC2 : return "user defined / ATSC/SCTE - Network Information Message";
        case 0xC3 : return "user defined / ATSC/SCTE - Network Text Table (NTT)";
        case 0xC4 : return "user defined / ATSC/SCTE - Short Form Virtual Channel Table (S-VCT)";
        case 0xC5 : return "user defined / ATSC/SCTE - System Time Table (STT)";
        case 0xC6 : return "user defined / ATSC/SCTE - Subtitle Message (SCTE-27)";
        case 0xC7 : return "user defined / ATSC - Master Guide Table (MGT)";
        case 0xC8 : return "user defined / ATSC - Terrestrial Virtual Channel Table (TVCT)";
        case 0xC9 : return "user defined / ATSC - Cable Virtual Channel Table (CVCT) / Long-form Virtual Channel Table (L-VCT)";
        case 0xCA : return "user defined / ATSC - Rating Region Table (RRT)";
        case 0xCB : return "user defined / ATSC - Event Information Table (EIT)";
        case 0xCC : return "user defined / ATSC - Extended Text Table (ETT)";
        case 0xCD : return "user defined / ATSC - System Time Table (STT)";
        case 0xCE : return "user defined / ATSC - Data Event Table (DET)";
        case 0xCF : return "user defined / ATSC - Data Service Table (DST)";
        case 0xD0 : return "user defined / ATSC - Program Identifier Table (PIT)";
        case 0xD1 : return "user defined / ATSC - Network Resource Table (NRT)";
        case 0xD2 : return "user defined / ATSC - Long-term Service Table (L-TST)";
        case 0xD3 : return "user defined / ATSC - Directed Channel Change Table (DCCT)";
        case 0xD4 : return "user defined / ATSC - DCC Selection Code Table (DCCSCT)";
        case 0xD5 : return "user defined / ATSC - Selection Information Table (SIT)";
        case 0xD6 : return "user defined / ATSC - Aggregate Event Information Table (AEIT)";
        case 0xD7 : return "user defined / ATSC - Aggregate Extended Text Table (AETT)";
        case 0xD8 : return "user defined / ATSC - Cable Emergency Alert";
        case 0xD9 : return "user defined / ATSC - Aggregate Data Event Table";
        case 0xDA : return "user defined / ATSC - Satellite VCT (SVCT)";
        case 0xFC : return "user defined / SCTE - Splice_info_section (SCTE-35)";

		case 0xFF: return "not used (illegal)";

		default:
			// Based on EN300468
			if((0x08<=tableId)&&(tableId<=0x3F)){
				return "ITU-T Rec. H.222.0|ISO/IEC13818 reserved";
			}
			if((0x43<=tableId)&&(tableId<=0x45)){
				return "reserved for future use";
			}

			if((0x47<=tableId)&&(tableId<=0x49)){
				return "reserved for future use";
			}

			if((0x50<=tableId)&&(tableId<=0x5F)){
				return "event_information_section - actual_transport_stream, schedule";
			}

			if((0x60<=tableId)&&(tableId<=0x6F)){
				return "event_information_section - other_transport_stream, schedule";
			}

			if((0x7A<=tableId)&&(tableId<=0x7D)){
				return "reserved for future use";
			}

			if((0x82<=tableId)&&(tableId<=0x8F)){
				return "CA_message_section, CA System private";
			}
			if((0x90<=tableId)&&(tableId<=0xFE)){
				return "user defined";
			}

			return "illegal value";
		}

	}



	public static String getRunningStatusString(int runningStatus) {

        return switch (runningStatus) {
            case 0 -> "undefined";
            case 1 -> "not running";
            case 2 -> "starts in a few seconds (e.g. for video recording)";
            case 3 -> "pausing";
            case 4 -> "running";
            case 5 -> "service off-air";
            case 6 -> "reserved for future use";
            case 7 -> "reserved for future use";
            default -> "Illegal value";
        };

	}

	public static String getFreeCAmodeString(int freeCAmode) {

        return switch (freeCAmode) {
            case 0 -> "clear";
            case 1 -> "one or more streams scrambled";
            default -> "Illegal value";
        };

	}

	public DefaultMutableTreeNode getJTreeNode(int modus){

		KVP t = getSectionKVP(modus);
		addTableDetails(modus, t);
		return t;
	}

	private KVP getSectionKVP(int modus) {
		KVP kvp = new KVP("TableType: "+getTableType(tableId)+" ("+ sectionNumber +"/"+ sectionLastNumber +")");
		if(showVersionModus(modus)&&(sectionSyntaxIndicator==1)){
			kvp.appendLabel(" <version "+version+">");

		}
		kvp.setCrumb("tablesection:"+ sectionNumber);
		return kvp;
	}

	protected void addTableDetails(int modus, DefaultMutableTreeNode t) {
		if (packetModus(modus)) {
			t.add(new KVP("first_packet_no", firstPacketNo).setDescription(parentPID.getParentTransportStream().getPacketTime(firstPacketNo)));
			t.add(new KVP("last_packet_no", lastPacketNo).setDescription(parentPID.getParentTransportStream().getPacketTime(lastPacketNo)));
			t.add(new KVP("occurrence_count", occurrenceCount).setDescription(getRepetitionRate(occurrenceCount, lastPacketNo, firstPacketNo)));
			if (occurrenceCount >= 2) {
				if(getParentTransportStream().isAVCHD()) {
					t.add(new KVP("min_packet_distance in ticks", minPacketDistance).setDescription(printPCRTime(minPacketDistance)));
					t.add(new KVP("max_packet_distance in ticks", maxPacketDistance).setDescription(printPCRTime(maxPacketDistance)));
					
				}else {
					t.add(new KVP("min_packet_distance in packets", minPacketDistance).setDescription(getDistanceSecs(minPacketDistance)));
					t.add(new KVP("max_packet_distance in packets", maxPacketDistance).setDescription(getDistanceSecs(maxPacketDistance)));
				}
			}
		}
		t.add(new KVP("table_id", tableId, getTableType(tableId)));
		t.add(new KVP("section_syntax_indicator", sectionSyntaxIndicator));
		t.add(new KVP("private_indicator", privateIndicator));
		t.add(new KVP("section_length", sectionLength));
		if ((sectionSyntaxIndicator == 1)&&(tableId!=0x72)) { // long format, but not stuffing
			t.add(new KVP(getTableIdExtensionLabel(), tableIdExtension).setDescription( getTableIdExtensionDescription(tableIdExtension)));
			t.add(new KVP("version", version));
			t.add(new KVP("current_next_indicator", currentNext).setDescription((currentNext == 1) ? "current" : "next"));
			t.add(new KVP("section_number", sectionNumber));
			t.add(new KVP("last_section_number", sectionLastNumber));
			t.add(new KVP("private_data", raw_data.getData(), 8, sectionLength - 5));
		} else {
			t.add(new KVP("private_data", raw_data.getData(), 3, sectionLength));
		}
	}

	/**
	 * The field table_id_extension has different meaning for different sub classes. By overriding this method
	 * the correct specific label can be displayed.
	 * @return label to be displayed in TreeView
	 */
	protected String getTableIdExtensionLabel() {
		return "table_id_extension";
	}

	/**
	 * The field table_id_extension has different meaning for different sub classes. By overriding this method
	 * the correct specific description (value dependend) can be displayed.
	 * @return description to be displayed in TreeView
	 */
	protected String getTableIdExtensionDescription(int tableIdExtension) {
		return null;
	}


	private String getRepetitionRate(int count, int last, int  first) {
		TransportStream parentTransportStream = parentPID.getParentTransportStream();
		long bitrate=parentTransportStream.getBitRate();
		if((bitrate> 0L)&&(count>=2)){
			float repRate;
			if(parentTransportStream.isAVCHD()) {
				long timeDiff = parentTransportStream.getAVCHDPacketTime(last) - parentTransportStream.getAVCHDPacketTime(first);
				repRate = ((float) timeDiff) / MPEGConstants.system_clock_frequency / (count - 1);  
			}else {
				repRate = ((float)(last-first)*parentTransportStream.getPacketLenghth()* 8.0F)/((count-1)*bitrate);
			}
			try (Formatter formatter = new Formatter()){
				return "repetition rate: "+formatter.format("%3.3f seconds",repRate);
			}
		}
		return null;
	}

	private String getDistanceSecs(long distanceInPackets) {
		TransportStream parentTransportStream = parentPID.getParentTransportStream();
		long bitrate=parentTransportStream.getBitRate();
		if(bitrate> 0L){
			float repRate=((float)(distanceInPackets)*parentTransportStream.getPacketLenghth()* 8.0F)/(bitrate);
			try (Formatter formatter = new Formatter()){
				return "interval: "+formatter.format("%3.3f seconds",repRate);
			}
		}
		return null;
	}
	
	public PID getParentPID() {
		return parentPID;
	}

	public void setParentPID(PID parentPID) {
		this.parentPID = parentPID;
	}

	public TransportStream getParentTransportStream(){
		return parentPID.getParentTransportStream();
	}

	public PSI getPSI(){
		return getParentTransportStream().getPsi();
	}

	public int getCurrentNext() {
		return currentNext;
	}

	public void setCurrentNext(int currentNext) {
		this.currentNext = currentNext;
	}

	public int getPrivateIndicator() {
		return privateIndicator;
	}

	public void setPrivateIndicator(int privateIndicator) {
		this.privateIndicator = privateIndicator;
	}

	public int getSectionSyntaxIndicator() {
		return sectionSyntaxIndicator;
	}

	public void setSectionSyntaxIndicator(int sectionSyntaxIndicator) {
		this.sectionSyntaxIndicator = sectionSyntaxIndicator;
	}

	public boolean isCrc_error() {
		return crc_error;
	}

	public void setCrc_error(boolean crc_error) {
		this.crc_error = crc_error;
	}

	public TableSection getNextVersion() {
		return nextVersion;
	}

	public void setNextVersion(TableSection next) {
		this.nextVersion = next;
	}

	public int getFirst_packet_no() {
		return firstPacketNo;
	}

	public void setFirst_packet_no(int first_packet_no) {
		this.firstPacketNo = first_packet_no;
	}

	public int getLast_packet_no() {
		return lastPacketNo;
	}

	public void setLast_packet_no(int last_packet_no) {
		this.lastPacketNo = last_packet_no;
	}

	public int getOccurrence_count() {
		return occurrenceCount;
	}

	public void setOccurrence_count(int occurrence_count) {
		this.occurrenceCount = occurrence_count;
	}

	public int getPacket_no() {
		return packetNo;
	}
	
	public void setPacket_no(int packetNo) {
		this.packetNo = packetNo;
		raw_data.setPacket_no(packetNo);
	}


	@Override
	public String toString(){
		return "TableSection"+((sectionSyntaxIndicator==1)?"tableIdExtension:"+tableIdExtension:"simple Syntax");
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = (PRIME * result) + ((raw_data == null) ? 0 : raw_data.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TableSection other = (TableSection) obj;
		if (raw_data == null) {
			return other.raw_data == null;
		}
		return raw_data.equals(other.raw_data);
	}

	public long getMinPacketDistance() {
		return minPacketDistance;
	}

	public void setMinPacketDistance(long minPacketDistance) {
		this.minPacketDistance = minPacketDistance;
	}

	public long getMaxPacketDistance() {
		return maxPacketDistance;
	}

	public void setMaxPacketDistance(long maxPacketDistance) {
		this.maxPacketDistance = maxPacketDistance;
	}




}

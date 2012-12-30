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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.packet_length;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.Formatter;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.CRCcheck;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.Utils;

public class TableSection implements TreeNode{

	protected PsiSectionData raw_data = null;

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
	private int version; /*!< version_number */

	private int currentNext; /*!< current_next_indicator */

	private int sectionNumber; /*!< section_number */

	private int sectionLastNumber; /*!< last_section_number */

	/* used if b_syntax_indicator is true */
	private long crc; /*!< CRC_32 */

	private boolean crc_error=false;

	private TableSection nextVersion = null;

	/* non DVB fields */

	private long first_packet_no=-1;
	private long last_packet_no=-1;
	private long occurrence_count=-1;
	private long packet_no=-1;

	static Formatter formatter = new Formatter();

	public TableSection(final PsiSectionData raw_data, final PID parent){
		super();
		this.raw_data = raw_data;
		this.packet_no= raw_data.getPacket_no();
		this.first_packet_no= raw_data.getPacket_no();
		this.last_packet_no= raw_data.getPacket_no();
		this.occurrence_count = 1;

		parentPID = parent;
		final byte[] bytes=raw_data.getData(); // just for convenience

		tableId = getInt(bytes, 0, 1, MASK_8BITS);

		sectionSyntaxIndicator = getInt(bytes, 1, 1, 0x80) >>7;
		privateIndicator = getInt(bytes, 1, 1, 0x40) >>6;
		sectionLength = getInt(bytes, 1, 2, MASK_12BITS);

		if(sectionSyntaxIndicator==1){ //long format
			tableIdExtension = getInt(bytes,3,2,MASK_16BITS);
			version = getInt(bytes,5,1,0x1E)>>1;
			currentNext = getInt(bytes,5,1,0x01);
			sectionNumber = getInt(bytes,6,1,MASK_8BITS);
			sectionLastNumber = getInt(bytes,7,1,MASK_8BITS);

			final int startCRC = sectionLength -1 ; // +3 - 4, first 3 bytes not included, skip back 4 bytes till begin CRC
			crc = (((long)getUnsignedByte(bytes[startCRC]))<<24) |
					(((long)getUnsignedByte(bytes[startCRC+1]))<<16) |
					(((long)getUnsignedByte(bytes[startCRC+2]))<<8) |
					(getUnsignedByte(bytes[startCRC+3]));

			final long res = CRCcheck.crc32(bytes,sectionLength+3);
			if(res!=0){
				crc_error=true;
				throw new RuntimeException("CRC Error in packet for pid:"+parent.getPid()+",CRC="+res);
			}
		}
	}

	public long getCrc() {
		return crc;
	}

	public void setCrc(final long crc) {
		this.crc = crc;
	}


	public int getTableIdExtension() {
		return tableIdExtension;
	}

	public void setTableIdExtension(final int extension) {
		this.tableIdExtension = extension;
	}

	public int getSectionLastNumber() {
		return sectionLastNumber;
	}

	public void setSectionLastNumber(final int lastNumber) {
		this.sectionLastNumber = lastNumber;
	}

	public int getSectionLength() {
		return sectionLength;
	}

	public void setSectionLength(final int length) {
		this.sectionLength = length;
	}

	public int getSectionNumber() {
		return sectionNumber;
	}

	public void setSectionNumber(final int number) {
		this.sectionNumber = number;
	}



	public int getTableId() {
		return tableId;
	}

	public void setTableId(final int tableId) {
		this.tableId = tableId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(final int version) {
		this.version = version;
	}

	public PsiSectionData getRaw_data() {
		return raw_data;
	}

	public void setRaw_data(final PsiSectionData raw_data) {
		this.raw_data = raw_data;
	}

	public static String getTableType(final int tableId){

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

		case 0x38: return "ISO/IEC 13818-6 reserved";
		case 0x39: return "ISO/IEC 13818-6 reserved";
		case 0x3A: return "DSM-CC - multiprotocol encapsulated data";
		case 0x3B: return "DSM-CC - U-N messages (DSI or DII)";
		case 0x3C: return "DSM-CC - Download Data Messages (DDB)";
		case 0x3D: return "DSM-CC - stream descriptorlist";
		case 0x3E: return "DSM-CC sections with private data // DVB datagram (ISO/IEC 13818-6)";
		case 0x3F: return "ISO/IEC 13818-6 reserved";

		case 0x40: return "network_information_section - actual_network";
		case 0x41: return "network_information_section - other_network";
		case 0x42: return "service_description_section - actual_transport_stream";

		case 0x46: return "service_description_section - other_transport_stream";

		case 0x4A: return "bouquet_association_section";
		case 0x4B: return "SSU Update Notification Table (UNT)";
		case 0x4C: return "IP/MAC Notification Table (INT)"; // EN 301 192

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

		case 0x7E: return "discontinuity_information_section";
		case 0x7F: return "selection_information_section";

		case 0x80: return "CA_message_section, ECM 1";
		case 0x81: return "CA_message_section, ECM 2";

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

			if((0x4B<=tableId)&&(tableId<=0x4D)){
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



	public static String getRunningStatusString(final int runningStatus) {

		switch (runningStatus) {
		case 0: return "undefined";
		case 1: return "not running";
		case 2: return "starts in a few seconds (e.g. for video recording)";
		case 3: return "pausing";
		case 4: return "running";
		case 5: return "service off-air";
		case 6: return "reserved for future use";
		case 7: return "reserved for future use";

		default:
			return "Illegal value";
		}

	}

	public static String getFreeCAmodeString(final int freeCAmode) {

		switch (freeCAmode) {
		case 0: return "clear";
		case 1: return "one or more streams scrambled";

		default:
			return "Illegal value";
		}

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("TableType: "+getTableType(getTableId())+" ("+getSectionNumber()+"/"+getSectionLastNumber()+")"));
		addTableDetails(modus, t);
		return t;
	}

	private void addTableDetails(final int modus, final DefaultMutableTreeNode t) {
		if(!Utils.simpleModus(modus)){
			if(Utils.packetModus(modus)){
				t.add(new DefaultMutableTreeNode(new KVP("first_packet_no",first_packet_no ,parentPID.getParentTransportStream().getPacketTime(first_packet_no))));
				t.add(new DefaultMutableTreeNode(new KVP("last_packet_no",last_packet_no ,parentPID.getParentTransportStream().getPacketTime(last_packet_no))));
				t.add(new DefaultMutableTreeNode(new KVP("occurrence_count",occurrence_count ,getRepetitionRate(occurrence_count,last_packet_no,first_packet_no))));
			}
			t.add(new DefaultMutableTreeNode(new KVP("table_id",tableId ,getTableType(tableId))));
			t.add(new DefaultMutableTreeNode(new KVP("section_syntax_indicator",sectionSyntaxIndicator,null)));
			t.add(new DefaultMutableTreeNode(new KVP("private_indicator",privateIndicator,null)));
			t.add(new DefaultMutableTreeNode(new KVP("section_length",sectionLength,null)));
			if(sectionSyntaxIndicator==1){ //long format
				t.add(new DefaultMutableTreeNode(new KVP("table_id_extension",tableIdExtension ,null)));
				t.add(new DefaultMutableTreeNode(new KVP("version",version ,null)));
				t.add(new DefaultMutableTreeNode(new KVP("current_next_indicator",currentNext ,(currentNext==1)?"current":"next")));
				t.add(new DefaultMutableTreeNode(new KVP("section_number",sectionNumber ,null)));
				t.add(new DefaultMutableTreeNode(new KVP("last_section_number",sectionLastNumber ,null)));
				t.add(new DefaultMutableTreeNode(new KVP("private_data",raw_data.getData(), 8, sectionLength -5 ,null)));
			}else{
				t.add(new DefaultMutableTreeNode(new KVP("private_data",raw_data.getData(), 3, sectionLength ,null)));
			}
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus,final HTMLSource htmlSource){
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("TableType: "+getTableType(getTableId())+" ("+getSectionNumber()+"/"+getSectionLastNumber()+")",htmlSource));
		addTableDetails(modus, t);
		return t;
	}

	/**
	 * @return
	 */
	private String getRepetitionRate(final long count,final long last, final long  first) {
		final long bitrate=getParentPID().getParentTransportStream().getBitRate();
		if(bitrate>0){
			if(count>=2){
				final float repRate=((float)(last-first)*packet_length*8)/((count-1)*bitrate);
				return "repetition rate: "+formatter.format("%3.3f seconds",repRate);
			}
		}
		return null;
	}

	public PID getParentPID() {
		return parentPID;
	}

	public void setParentPID(final PID parentPID) {
		this.parentPID = parentPID;
	}

	public TransportStream getParentTransportStream(){
		return getParentPID().getParentTransportStream();
	}

	public PSI getPSI(){
		return getParentTransportStream().getPsi();
	}

	public int getCurrentNext() {
		return currentNext;
	}

	public void setCurrentNext(final int currentNext) {
		this.currentNext = currentNext;
	}

	public int getPrivateIndicator() {
		return privateIndicator;
	}

	public void setPrivateIndicator(final int privateIndicator) {
		this.privateIndicator = privateIndicator;
	}

	public int getSectionSyntaxIndicator() {
		return sectionSyntaxIndicator;
	}

	public void setSectionSyntaxIndicator(final int sectionSyntaxIndicator) {
		this.sectionSyntaxIndicator = sectionSyntaxIndicator;
	}

	public boolean isCrc_error() {
		return crc_error;
	}

	public void setCrc_error(final boolean crc_error) {
		this.crc_error = crc_error;
	}

	public TableSection getNextVersion() {
		return nextVersion;
	}

	public void setNextVersion(final TableSection next) {
		this.nextVersion = next;
	}

	public long getFirst_packet_no() {
		return first_packet_no;
	}

	public void setFirst_packet_no(final long first_packet_no) {
		this.first_packet_no = first_packet_no;
	}

	public long getLast_packet_no() {
		return last_packet_no;
	}

	public void setLast_packet_no(final long last_packet_no) {
		this.last_packet_no = last_packet_no;
	}

	public long getOccurrence_count() {
		return occurrence_count;
	}

	public void setOccurrence_count(final long occurrence_count) {
		this.occurrence_count = occurrence_count;
	}

	public long getPacket_no() {
		return packet_no;
	}

	public void setPacket_no(final long packet_no) {
		this.packet_no = packet_no;
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TableSection other = (TableSection) obj;
		if (raw_data == null) {
			if (other.raw_data != null) {
				return false;
			}
		} else if (!raw_data.equals(other.raw_data)) {
			return false;
		}
		return true;
	}




}

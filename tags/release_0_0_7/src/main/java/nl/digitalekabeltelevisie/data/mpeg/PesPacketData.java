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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

/**
 * Represent a PES packet, which can correspond to a MPEG frame, (or a complete group of frames) or Audio packet, or DVB
 * subtitle, or teletext. Subclasses are present for DVBsubtitles, MPEG2 video, MPEG2 audio, teletext.
 *
 * based on ISO/IEC 13818-1 2.4.3.6 and 2.4.3.7
 *
 * <img src="doc-files/pespacket.png">
 *
 * @author Eric Berendsen
 *
 */
public class PesPacketData  implements TreeNode{

	/**
	 * Data including packet_start_code_prefix, stream id, PES packet length, pesHeader
	 *
	 */
	protected byte [] data ;
	protected int stream_id = 0;
	/**
	 * the number of bytes to be expected in this pes as defined by PES packet length. 0 for unbounded
	 */
	protected int noBytes=0;

	private int pts_dts_flags;
	private int pes_header_data_length;

	protected long pts;
	protected long dts;
	protected long escr;

	protected long packet_no=0;
	protected GeneralPesHandler pesHandler;

	protected int pesDataStart;
	protected int pesDataLen;

	private int bytesRead = 0;

	public static int program_stream_map = 0xBC;
	public static int private_stream_1 = 0xBD;
	public static int padding_stream = 0xBE;
	public static int private_stream_2 = 0xBF;
	public static int ECM_stream = 0xF0;
	public static int EMM_stream = 0xF1;
	public static int DSMCC_stream = 0xF2;
	public static int ISO_IEC_13522_stream = 0xF3;
	public static int ITU_T_Rec_H_222_1typeA = 0xF4;
	public static int ITU_T_Rec_H_222_1typeB = 0xF5;
	public static int ITU_T_Rec_H_222_1typeC = 0xF6;
	public static int ITU_T_Rec_H_222_1typeD = 0xF7;
	public static int ITU_T_Rec_H_222_1typeE = 0xF8;
	public static int ancillary_stream = 0xF9;
	public static int ISO_IEC14496_1_SL_packetized_stream = 0xFA;
	public static int ISO_IEC14496_1_FlexMux_stream = 0xFB;
	public static int program_stream_directory = 0xFF;

	/**
	 * Constructor used to start creating a new PesPacket. pesStreamID, pesLength and pesHandler have to be set, but the
	 * data is to be added later by calling readBytes()
	 *
	 * @param pesStreamID
	 * @param pesLength
	 * @param pesHandler
	 */
	public PesPacketData(final int pesStreamID, final int pesLength,final GeneralPesHandler pesHandler) {
		this.stream_id = pesStreamID;
		this.noBytes = pesLength;
		this.pesHandler = pesHandler;
		if(pesLength!=0){
			this.data= new byte[pesLength+6];
		}else{
			this.data= new byte[20000]; // start default for video, should be able to handle small frames.
		}

	}

	/**
	 * This constructor is only used to 'wrap' an existing PesPacketData into a specialized form, like
	 * VideoPESDataField.
	 *
	 * @param pesPacket
	 */
	protected PesPacketData(final PesPacketData pesPacket) {
		this.stream_id = pesPacket.getPesStreamID();

		this.data = pesPacket.getData();
		this.noBytes = pesPacket.getNoBytes();
		this.pesHandler = pesPacket.getPesHandler();
		this.packet_no= pesPacket.getPacket_no();
		this.pesDataStart = pesPacket.getPesDataStart();
		this.bytesRead = pesPacket.bytesRead;
		processPayload();

	}

	/**
	 * Method to (partial) fill the data[] of this PesPacket. Data from single TSPacket is appended to the already
	 * collected data
	 *
	 * @param payload data to be copied into PesPacketData
	 * @param offset where in payload[] does the actual data start
	 * @param available number of bytes available to be read
	 */
	public void readBytes(final byte [] payload, final int offset, final int available){
		if(noBytes!=0){  //fixed length PES packet, we know how much to expect
			if(bytesRead<(noBytes+6)){
				final int read1 = Math.min((noBytes - bytesRead) + 6, available); // we are going to read this number of bytes
				System.arraycopy(payload, offset, data, bytesRead, read1);
				bytesRead+=read1;
			}
		}else{ // noBytes==0, unbounded video packet, length unknown
			final int newcount = bytesRead + available;
			if (newcount > data.length) {
				final byte newbuf[] = new byte[Math.max(data.length << 1, newcount)];
				System.arraycopy(data, 0, newbuf, 0, bytesRead);
				data = newbuf;
			}
			System.arraycopy(payload, offset, data, bytesRead, available);
			bytesRead = newcount;
		}
	}

	/**
	 * @return the position of the first TSpacket of this PesPacket in the TransportStream
	 */
	public long getPacket_no() {
		return packet_no;
	}




	/**
	 * @return All Data of packet, including packet_start_code_prefix, stream id, PES packet length, pesHeader
	 */
	public byte[] getData() {
		return data;
	}



	/**
	 * @return the number of bytes to be expected in this pes as defined by PES packet length. 0 for unbounded
	 */
	public int getNoBytes() {
		return noBytes;
	}



	/**
	 * @return the stream_id
	 */
	public int getPesStreamID() {
		return stream_id;
	}


	/**
	 * Called when all data for this PesPacket has been read by the readBytes() method. Determines values for PTS and
	 * DTS (if any), and sets pesDataStart and pesDataLen. iso 13818-1 2.4.3.6 PES packet
	 */
	public void processPayload() {
		if((stream_id!=program_stream_map)
				&& (stream_id != padding_stream)
				&& (stream_id != private_stream_2)
				&& (stream_id != ECM_stream)
				&& (stream_id != EMM_stream)
				&& (stream_id != program_stream_directory)
				&& (stream_id != DSMCC_stream)
				&& (stream_id != ITU_T_Rec_H_222_1typeE))
		{

			pts_dts_flags  = getPts_dts_flags() ;
			pes_header_data_length  = getPes_header_data_length();

			int offset=9;
			if ((pts_dts_flags ==2) || (pts_dts_flags ==3)) {
				pts = getTimeStamp(data,offset);
				offset+=5;
			}
			if (pts_dts_flags ==3) {
				dts = getTimeStamp(data,offset);
				offset+=5;
			}
			pesDataStart=9+pes_header_data_length;
			pesDataLen=noBytes-pes_header_data_length-3;  // was -3

		}else{
			pesDataStart=6;
			pesDataLen=noBytes;
		}

	}

	/**
	 * @return length of Pes header
	 */
	public int getPes_header_data_length() {
		return getInt(data, 8, 1, MASK_8BITS);
	}
	/**
	 * @return
	 */
	public int getPes_extension_flag() {
		return getInt(data, 7, 1, MASK_1BIT);
	}
	/**
	 * @return
	 */
	public int getPes_crc_flag() {
		return getInt(data, 7, 1, 0x02)>>1;
	}
	/**
	 * @return
	 */
	public int getAdditional_copy_info_flag() {
		return getInt(data, 7, 1, 0x04)>>2;
	}
	/**
	 * @return
	 */
	public int getDsm_trick_mode_flag() {
		return getInt(data, 7, 1, 0x08)>>3;
	}
	/**
	 * @return
	 */
	public int getEs_rate_flag() {
		return getInt(data, 7, 1, 0x10)>>4;
	}
	/**
	 * @return
	 */
	public int getEscr_flag() {
		return getInt(data, 7, 1, 0x20)>>5;
	}
	/**
	 * @return
	 */
	public int getPts_dts_flags() {
		return getInt(data, 7, 1, 0xC0)>>6;
	}
	/**
	 * @return
	 */
	public int getOriginal_or_copy() {
		return getInt(data, 6, 1, MASK_1BIT);
	}
	/**
	 * @return
	 */
	public int getCopyright() {
		return getInt(data, 6, 1, 0x02) >>1;
	}
	/**
	 * @return
	 */
	public int getData_alignment_indicator() {
		return getInt(data, 6, 1, 0x04) >>2;
	}
	/**
	 * @return
	 */
	public int getPes_priority() {
		return getInt(data, 6, 1, 0x08) >>3;
	}
	/**
	 * @return
	 */
	public int getPes_scrambling_control() {
		return getInt(data, 6, 1, 0x30) >>4;
	}
	/**
	 * @return
	 */
	public int getMarkerBits() {
		return getInt(data, 6, 1, 0xC0) >>6;
	}

	/**
	 * @param array
	 * @param offset
	 * @return the value of the PTS/DTS as described in 2.4.3.7 of iso 13813, prefix and marker bits are ignored
	 */
	public long getTimeStamp(final byte[] array, final int offset) {

		long ts = getLong(array, offset, 1, 0x0E) << 29; // bits 32..30
		ts |= getLong(array, offset + 1, 2, 0xFFFE) << 14; // bits 29..15
		ts |= getLong(array, offset + 3, 2, 0xFFFE) >> 1; // bits 14..0

		return ts;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus){
		return 	getJTreeNode(modus,"PES Packet");
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus, String title) {

		String ptsString = "";
		if(showPtsModus(modus)){
			if ((pts_dts_flags ==2) || (pts_dts_flags ==3)){ // PTS present, so decorate top node with it
				ptsString = " [pts="+ printTimebase90kHz(pts)+"]";
			}
		}
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(title+ptsString));
		t.add(new DefaultMutableTreeNode(new KVP("stream_id",stream_id,getStreamIDDescription(stream_id))));
		t.add(new DefaultMutableTreeNode(new KVP("PES_packet_length",noBytes,null)));
		if(noBytes==0){
			t.add(new DefaultMutableTreeNode(new KVP("Actual PES length",bytesRead,null)));
			t.add(new DefaultMutableTreeNode(new KVP("data",data,0,bytesRead,null)));
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("data",data,null)));
		}

		if((stream_id!=program_stream_map)
				&& (stream_id != padding_stream)
				&& (stream_id != private_stream_2)
				&& (stream_id != ECM_stream)
				&& (stream_id != EMM_stream)
				&& (stream_id != program_stream_directory)
				&& (stream_id != DSMCC_stream)
				&& (stream_id != ITU_T_Rec_H_222_1typeE)){

			t.add(new DefaultMutableTreeNode(new KVP("markerBits",getMarkerBits(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("pes_scrambling_control",getPes_scrambling_control(),getPes_scrambling_control()==0?"Not scrambled":"User-defined")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_priority",getPes_priority(),getPes_priority()==1?"higher":"normal")));
			t.add(new DefaultMutableTreeNode(new KVP("data_alignment_indicator",getData_alignment_indicator(),getData_alignment_indicator()==1?"PES packet header is immediately followed by the video start code or audio syncword indicated in the data_stream_alignment_descriptor":"alignment not defined")));
			t.add(new DefaultMutableTreeNode(new KVP("copyright",getCopyright(),getCopyright()==1?"packet payload is protected by copyright":"not defined whether the material is protected by copyright")));
			t.add(new DefaultMutableTreeNode(new KVP("original_or_copy",getOriginal_or_copy(),getOriginal_or_copy()==1?"contents of the associated PES packet payload is an original":"contents of the associated PES packet payload is a copy")));

			t.add(new DefaultMutableTreeNode(new KVP("pts_dts_flags",getPts_dts_flags(),getPts_dts_flagsString())));
			t.add(new DefaultMutableTreeNode(new KVP("escr_flag",getEscr_flag(),getEscr_flag()==1?"ESCR base and extension fields are present":"no ESCR fields are present")));
			t.add(new DefaultMutableTreeNode(new KVP("es_rate_flag",getEs_rate_flag(),getEs_rate_flag()==1?"ES_rate field is present":"no ES_rate field is present")));
			t.add(new DefaultMutableTreeNode(new KVP("dsm_trick_mode_flag",getDsm_trick_mode_flag() ,getDsm_trick_mode_flag()==1?"8-bit trick mode field is present":"8-bit trick mode field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("additional_copy_info_flag",getAdditional_copy_info_flag(),getAdditional_copy_info_flag()==1?"additional_copy_info field is present":"additional_copy_info field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_crc_flag",getPes_crc_flag(),getPes_crc_flag()==1?"CRC field is present":"CRC field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_extension_flag",getPes_extension_flag() ,getPes_extension_flag()==1?"extension field is present":"extension field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_header_data_length",getPes_header_data_length(),null)));
			if ((pts_dts_flags ==2) || (pts_dts_flags ==3)) {
				t.add(new DefaultMutableTreeNode(new KVP("pts",pts,printTimebase90kHz(pts))));
			}
			if (pts_dts_flags ==3) {
				t.add(new DefaultMutableTreeNode(new KVP("dts",dts,printTimebase90kHz(dts))));
			}
			if(noBytes!=0){
				t.add(new DefaultMutableTreeNode(new KVP("PES_packet_data_byte2",data,9+pes_header_data_length,noBytes-pes_header_data_length-3,null)));   // was noBytes-pes_header_data_length-3
			}
		}else{
			t.add(new DefaultMutableTreeNode(new KVP("PES_packet_data_byte3",data,6,noBytes,null)));
		}
		return t;
	}

	private String getPts_dts_flagsString() {
		switch (getPts_dts_flags()) {
		case 0:
			return "no PTS or DTS fields shall be present in the PES packet header";
		case 1:
			return "forbidden value";
		case 2:
			return "PTS fields shall be present in the PES packet header";
		case 3:
			return "both the PTS fields and DTS fields shall be present in the PES packet header";

		default:
			return "illegal value (program error)";
		}
	}

	public static String getStreamIDDescription(final int streamId){

		if((0xC0<=streamId)&&(streamId<0xE0)){
			return "ISO/IEC 13818-3 or ISO/IEC 11172-3 or ISO/IEC 13818-7 or ISO/IEC 14496-3 audio stream number "+ Integer.toHexString(streamId & 0x1F);
		}
		if((0xE0<=streamId)&&(streamId<0xF0)){
			return "ITU-T Rec. H.262 | ISO/IEC 13818-2 or ISO/IEC 11172-2 or ISO/IEC 14496-2 video stream number "+ Integer.toHexString(streamId & 0x0F);
		}

		switch (streamId) {
		case 0xBC :return "program_stream_map";
		case 0xBD :return "private_stream_1";
		case 0xBE :return "padding_stream";
		case 0xBF :return "private_stream_2";
		case 0xF0 :return "ECM_stream";
		case 0xF1 :return "EMM_stream";
		case 0xF2 :return "DSMCC_stream";
		case 0xF3 :return "ISO/IEC_13522_stream";
		case 0xF4 :return "ITU-T Rec. H.222.1 type A";
		case 0xF5 :return "ITU-T Rec. H.222.1 type B";
		case 0xF6 :return "ITU-T Rec. H.222.1 type C";
		case 0xF7 :return "ITU-T Rec. H.222.1 type D";
		case 0xF8 :return "ITU-T Rec. H.222.1 type E";
		case 0xF9 :return "ancillary_stream";
		case 0xFA :return "ISO/IEC14496-1_SL-packetized_stream";
		case 0xFB :return "ISO/IEC14496-1_FlexMux_stream";
		/* ISO/IEC 13818-1:2007/FPDAM5 */
		case 0xFC :return "metadata stream";
		case 0xFD :return "extended_stream_id";
		case 0xFE :return "reserved data stream";

		case 0xFF :return "program_stream_directory";
		default:
			return "??";
		}


	}

	/**
	 * @return the pesDataLen,the actual len of the payload (without prefix, stream_id, and header)
	 */
	public int getPesDataLen() {
		return pesDataLen;
	}



	/**
	 * @return the pesDataStart, the offset into data[], where the PES packet data bytes start (start of actual payload)
	 */
	public int getPesDataStart() {
		return pesDataStart;
	}

	/**
	 * @return the pts
	 */
	public long getPts() {
		return pts;
	}



	/**
	 * @return the PesHandler that knows how to process the raw data in this type of PesPacket
	 */
	public GeneralPesHandler getPesHandler() {
		return pesHandler;
	}



}

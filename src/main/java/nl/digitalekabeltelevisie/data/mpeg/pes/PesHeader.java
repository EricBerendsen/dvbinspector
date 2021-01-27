/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_1BIT;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_4BITS;
import static nl.digitalekabeltelevisie.util.Utils.getBytes;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;
import static nl.digitalekabeltelevisie.util.Utils.indexOf;
import static nl.digitalekabeltelevisie.util.Utils.printTimebase90kHz;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/**
 * @author Eric
 * 
 * Based on ITU-T H.222.0 (03/2017) Table 2-21 – PES packet
 *
 */
public class PesHeader implements TreeNode {


	class AdDescriptor implements TreeNode{
		
		// based on TS 101 154 V2.6.1 (2019-09) Table E.1: AD_descriptor
		
		private int ad_descriptor_length;
		private byte[] ad_text_tag;
		private int version_text_tag;
		private int ad_fade_byte;
		private int ad_pan_byte;
		private int ad_gain_byte_center;
		private int ad_gain_byte_front;
		private int ad_gain_byte_surround;


		AdDescriptor(byte[] data){
			ad_descriptor_length = getInt(data, 0, 1, MASK_4BITS);
			ad_text_tag = getBytes(data, 1, 5);
			version_text_tag = getInt(data, 6, 1, MASK_8BITS);
			ad_fade_byte = getInt(data, 7, 1, MASK_8BITS);
			ad_pan_byte = getInt(data, 8, 1, MASK_8BITS);
			if (version_text_tag == 0x32) {
				ad_gain_byte_center  = getInt(data, 9, 1, MASK_8BITS);
				ad_gain_byte_front  = getInt(data, 10, 1, MASK_8BITS);
				ad_gain_byte_surround = getInt(data, 11, 1,MASK_8BITS);
			}
		}
		

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AD_descriptor"));
			t.add(new DefaultMutableTreeNode(new KVP("AD_descriptor_length", ad_descriptor_length, null)));
			t.add(new DefaultMutableTreeNode(new KVP("AD_text_tag", ad_text_tag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("version_text_tag", version_text_tag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("AD_fade_byte", ad_fade_byte, null)));
			t.add(new DefaultMutableTreeNode(new KVP("AD_pan_byte", ad_pan_byte, null)));
			if (version_text_tag == 0x32) {
				t.add(new DefaultMutableTreeNode(new KVP("AD_gain_byte center", ad_gain_byte_center, null)));
				t.add(new DefaultMutableTreeNode(new KVP("AD_gain_byte front", ad_gain_byte_front, null)));
				t.add(new DefaultMutableTreeNode(new KVP("AD_gain_byte surround", ad_gain_byte_surround, null)));
			}

			return t;
		}
		
		
	}

	private final byte[] data;
	private final int offset;
	private int stream_id;
	private int pes_packet_length;
	private int markerBits;
	private int pes_scrambling_control;
	private int pes_priority;
	private int data_alignment_indicator;
	private int copyright;
	private int original_or_copy;
	private int pts_dts_flags;
	private int escr_flag;
	private int es_rate_flag;
	private int dsm_trick_mode_flag;
	private int additional_copy_info_flag;
	private int pes_crc_flag;
	private int pes_extension_flag;
	private int pes_header_data_length;
	private long pts;
	private long dts;
	private byte[] escr;
	private int es_rate;
	private int trick_mode_control;
	private int trick_mode_detail;
	private int additional_copy_info;
	private int previous_PES_packet_CRC;
	private int pes_private_data_flag;
	private int pack_header_field_flag;
	private int program_packet_sequence_counter_flag;
	private int p_std_buffer_flag;
	private int reserved;
	private int pes_extension_flag_2;
	private byte[] pes_private_data;
	private int pack_field_length;
	private byte[] pack_header;
	private AdDescriptor adDescriptor;

	/**
	 * @param data
	 * @param offset
	 */
	public PesHeader(final byte[] data, final int offset) {
		super();
		this.data = data;
		this.offset = offset;

		stream_id = getInt(data, offset + 3, 1, MASK_8BITS);
		pes_packet_length = getInt(data, offset + 4, 2, MASK_16BITS);
		
		if (hasExtendedHeader(stream_id)) {

			markerBits = getInt(data, offset + 6, 1, 0xC0) >> 6;
			pes_scrambling_control = getInt(data, offset + 6, 1, 0x30) >> 4;
			pes_priority = getInt(data, offset + 6, 1, 0x08) >> 3;
			data_alignment_indicator = getInt(data, offset + 6, 1, 0x04) >> 2;
			copyright = getInt(data, offset + 6, 1, 0x02) >> 1;
			original_or_copy = getInt(data, offset + 6, 1, MASK_1BIT);

			pts_dts_flags = getInt(data, offset + 7, 1, 0xC0) >> 6;
			escr_flag = getInt(data, offset + 7, 1, 0x20) >> 5;
			es_rate_flag = getInt(data, offset + 7, 1, 0x10) >> 4;
			dsm_trick_mode_flag = getInt(data, offset + 7, 1, 0x08) >> 3;
			additional_copy_info_flag = getInt(data, offset + 7, 1, 0x04) >> 2;
			pes_crc_flag = getInt(data, offset + 7, 1, 0x02) >> 1;
			pes_extension_flag = getInt(data, offset + 7, 1, MASK_1BIT);
			pes_header_data_length = getInt(data, offset + 8, 1, MASK_8BITS);

			int off = offset + 9;
			if (hasPTS(pts_dts_flags)) {
				pts = getTimeStamp(data, off);
				off += 5;
			}

			if (pts_dts_flags == 3) {
				dts = getTimeStamp(data, off);
				off += 5;
			}
			if (escr_flag == 1) {
				escr = getBytes(data, off, 6);
				off += 6; // 48 bits
			}
			if (es_rate_flag == 1) {
				es_rate = getInt(data, off, 3, 0xFF_FFFE) >> 1;
				off += 3;
			}
			if (dsm_trick_mode_flag == 1) {
				trick_mode_control = getInt(data, off, 1, 0b1110_0000) >> 5;
				trick_mode_detail = getInt(data, off, 1, 0b0001_1111);
				off += 1;
			}
			if (additional_copy_info_flag == 1) {
				additional_copy_info = getInt(data, off, 1, 0b0111_1111);
				off += 1;
			}
			if (pes_crc_flag == 1) {
				previous_PES_packet_CRC = getInt(data, off, 2, MASK_16BITS);
				off += 2;
			}
			if (pes_extension_flag == 1) {
				pes_private_data_flag = getInt(data, off, 1, 0b1000_0000) >> 7;
				pack_header_field_flag = getInt(data, off, 1, 0b0100_0000) >> 6;
				program_packet_sequence_counter_flag = getInt(data, off, 1, 0b0010_0000) >> 5;
				p_std_buffer_flag = getInt(data, off, 1, 0b0001_0000) >> 4;
				reserved = getInt(data, off, 1, 0b000_1110) >> 1;
				pes_extension_flag_2 = getInt(data, off, 1, 0b000_0001);
				off += 1;

				if (pes_private_data_flag == 1) {
					pes_private_data = getBytes(data, off, 16);
					off += 16;

					if(indexOf(pes_private_data, new byte[]{0x44,0x54,0x47,0x41,0x44}, 0)==1) { // DTGAD  // AD_descriptor TS 101 154 V2.6.1 (2019-09) Table E.1
						adDescriptor = new AdDescriptor(pes_private_data);
					}
				}
				if (pack_header_field_flag == 1) {
					pack_field_length = getInt(data, off, 1, MASK_8BITS);
					off += 1;

					pack_header = getBytes(data, off, pack_field_length);
					off += pack_field_length;
				}

			}// endif (pes_extension_flag == 1) 
		}

	}

	public boolean isValidPesHeader() {
		return (data.length >= offset + 2) && (data[offset] == 0) && (data[offset + 1] == 0) && (data[offset + 2] == 1);
	}

	public int getStreamID() {
		return stream_id;
	}

	public int getPesPacketLength() {
		return pes_packet_length;
	}

	@SuppressWarnings("unused")
	public void addToJtree(final DefaultMutableTreeNode t, final int modus) {

		try {
			t.add(new DefaultMutableTreeNode(
					new KVP("stream_id", stream_id, PesHeader.getStreamIDDescription(stream_id))));
			t.add(new DefaultMutableTreeNode(new KVP("PES_packet_length", pes_packet_length, null)));
			if (hasExtendedHeader(stream_id)) {

				t.add(new DefaultMutableTreeNode(new KVP("markerBits", markerBits, null)));
				t.add(new DefaultMutableTreeNode(new KVP("pes_scrambling_control", pes_scrambling_control,
						pes_scrambling_control == 0 ? "Not scrambled" : "User-defined")));
				t.add(new DefaultMutableTreeNode(
						new KVP("pes_priority", pes_priority, pes_priority == 1 ? "higher" : "normal")));
				t.add(new DefaultMutableTreeNode(new KVP("data_alignment_indicator", data_alignment_indicator,
						data_alignment_indicator == 1
								? "PES packet header is immediately followed by the video start code or audio syncword indicated in the data_stream_alignment_descriptor"
								: "alignment not defined")));
				t.add(new DefaultMutableTreeNode(
						new KVP("copyright", copyright, copyright == 1 ? "packet payload is protected by copyright"
								: "not defined whether the material is protected by copyright")));
				t.add(new DefaultMutableTreeNode(new KVP("original_or_copy", original_or_copy,
						original_or_copy == 1 ? "contents of the associated PES packet payload is an original"
								: "contents of the associated PES packet payload is a copy")));

				t.add(new DefaultMutableTreeNode(
						new KVP("pts_dts_flags", pts_dts_flags, PesHeader.getPts_dts_flagsString(pts_dts_flags))));
				t.add(new DefaultMutableTreeNode(new KVP("escr_flag", escr_flag,
						escr_flag == 1 ? "ESCR base and extension fields are present" : "no ESCR fields are present")));
				t.add(new DefaultMutableTreeNode(new KVP("es_rate_flag", es_rate_flag,
						es_rate_flag == 1 ? "ES_rate field is present" : "no ES_rate field is present")));
				t.add(new DefaultMutableTreeNode(new KVP("dsm_trick_mode_flag", dsm_trick_mode_flag,
						dsm_trick_mode_flag == 1 ? "8-bit trick mode field is present"
								: "8-bit trick mode field is not present")));
				t.add(new DefaultMutableTreeNode(new KVP("additional_copy_info_flag", additional_copy_info_flag,
						additional_copy_info_flag == 1 ? "additional_copy_info field is present"
								: "additional_copy_info field is not present")));
				t.add(new DefaultMutableTreeNode(new KVP("pes_crc_flag", pes_crc_flag,
						pes_crc_flag == 1 ? "CRC field is present" : "CRC field is not present")));
				t.add(new DefaultMutableTreeNode(new KVP("pes_extension_flag", pes_extension_flag,
						pes_extension_flag == 1 ? "extension field is present" : "extension field is not present")));
				t.add(new DefaultMutableTreeNode(new KVP("pes_header_data_length", pes_header_data_length, null)));
				if (hasPTS(pts_dts_flags)) {
					t.add(new DefaultMutableTreeNode(new KVP("pts", pts, printTimebase90kHz(pts))));
				}
				if (pts_dts_flags == 3) {
					t.add(new DefaultMutableTreeNode(new KVP("dts", dts, printTimebase90kHz(dts))));
				}
				if (escr_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("escr", escr, null)));
				}
				if (es_rate_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("es_rate", es_rate, null)));
				}
				if (dsm_trick_mode_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("trick_mode_control", trick_mode_control, null)));
					t.add(new DefaultMutableTreeNode(new KVP("trick_mode_detail", trick_mode_detail, null)));
				}
				if (additional_copy_info_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("additional_copy_info", additional_copy_info, null)));
				}
				if (pes_crc_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("previous_PES_packet_CRC", previous_PES_packet_CRC, null)));
				}
				
				if ( pes_extension_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("PES_private_data_flag", pes_private_data_flag, null)));
					t.add(new DefaultMutableTreeNode(new KVP("pack_header_field_flag", pack_header_field_flag, null)));
					t.add(new DefaultMutableTreeNode(new KVP("program_packet_sequence_counter_flag", program_packet_sequence_counter_flag, null)));
					t.add(new DefaultMutableTreeNode(new KVP("P-STD_buffer_flag", p_std_buffer_flag, null)));
					t.add(new DefaultMutableTreeNode(new KVP("reserved", reserved, null)));
					t.add(new DefaultMutableTreeNode(new KVP("PES_extension_flag_2", pes_extension_flag_2, null)));
					if ( pes_private_data_flag == 1) {
						DefaultMutableTreeNode privateDataNode = new DefaultMutableTreeNode(new KVP("PES_private_data", pes_private_data, null));
						t.add(privateDataNode);
						if(adDescriptor != null) {
							privateDataNode.add(adDescriptor.getJTreeNode(modus));
						}
					}
					
					if (pack_header_field_flag == 1) {
						t.add(new DefaultMutableTreeNode(new KVP("pack_field_length", pack_field_length, null)));
						t.add(new DefaultMutableTreeNode(new KVP("pack_header", pack_header, null)));
					}

				}// endif ( pes_extension_flag == 1) 

			}
		} catch (Exception e) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getErrorKVP("Error parsing PESHeader")));

		}
	}

	/**
	 * @param pts_dts_flags
	 * @return
	 */
	static boolean hasPTS(final int pts_dts_flags) {
		return (pts_dts_flags == 2) || (pts_dts_flags == 3);
	}

	public boolean hasPTS() {
		return hasPTS(getPts_dts_flags());
	}

	/**
	 * @param stream_id
	 * @return
	 */
	public static boolean hasExtendedHeader(final int stream_id) {
		return (stream_id != PesPacketData.program_stream_map) && (stream_id != PesPacketData.padding_stream)
				&& (stream_id != PesPacketData.private_stream_2) && (stream_id != PesPacketData.ECM_stream)
				&& (stream_id != PesPacketData.EMM_stream) && (stream_id != PesPacketData.program_stream_directory)
				&& (stream_id != PesPacketData.DSMCC_stream) && (stream_id != PesPacketData.ITU_T_Rec_H_222_1typeE);
	}

	public boolean hasExtendedHeader() {
		return hasExtendedHeader(getStreamID());
	}

	/**
	 * @return
	 */
	public long getDts() {
		return dts;
	}

	/**
	 * @return
	 */
	public long getPts() {
		return pts;
	}

	/**
	 * @return length of Pes header
	 */
	public final int getPes_header_data_length() {
		return pes_header_data_length;
	}

	/**
	 * @return
	 */
	public int getPes_extension_flag() {
		return pes_extension_flag;
	}

	/**
	 * @return
	 */
	public int getPes_crc_flag() {
		return pes_crc_flag;
	}

	/**
	 * @return
	 */
	public int getAdditional_copy_info_flag() {
		return additional_copy_info_flag;
	}

	/**
	 * @return
	 */
	public int getDsm_trick_mode_flag() {
		return dsm_trick_mode_flag;
	}

	/**
	 * @return
	 */
	public int getEs_rate_flag() {
		return es_rate_flag;
	}

	/**
	 * @return
	 */
	public int getEscr_flag() {
		return escr_flag;
	}

	/**
	 * @return
	 */
	public final int getPts_dts_flags() {
		return pts_dts_flags;
	}

	/**
	 * @return
	 */
	public int getOriginal_or_copy() {
		return original_or_copy;
	}

	/**
	 * @return
	 */
	public int getCopyright() {
		return copyright;
	}

	/**
	 * @return
	 */
	public int getData_alignment_indicator() {
		return data_alignment_indicator;
	}

	/**
	 * @return
	 */
	public int getPes_priority() {
		return pes_priority;
	}

	/**
	 * @return
	 */
	public int getPes_scrambling_control() {
		return pes_scrambling_control;
	}

	/**
	 * @return
	 */
	public int getMarkerBits() {
		return markerBits;
	}

	/**
	 * @param array
	 * @param offset
	 * @return the value of the PTS/DTS as described in 2.4.3.7 of iso 13813, prefix
	 *         and marker bits are ignored
	 */
	public final static long getTimeStamp(final byte[] array, final int offset) {

		long ts = getLong(array, offset, 1, 0x0E) << 29; // bits 32..30
		ts |= getLong(array, offset + 1, 2, 0xFFFE) << 14; // bits 29..15
		ts |= getLong(array, offset + 3, 2, 0xFFFE) >> 1; // bits 14..0

		return ts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Pes Header"));
		addToJtree(t, modus);
		return t;
	}

	public static String getPts_dts_flagsString(final int pts_dts_flags) {
		switch (pts_dts_flags) {
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

	public static String getStreamIDDescription(final int streamId) {

		if ((0xC0 <= streamId) && (streamId < 0xE0)) {
			return "ISO/IEC 13818-3 or ISO/IEC 11172-3 or ISO/IEC 13818-7 or ISO/IEC 14496-3 audio stream number "
					+ Integer.toHexString(streamId & 0x1F);
		}
		if ((0xE0 <= streamId) && (streamId < 0xF0)) {
			return "ITU-T Rec. H.262 | ISO/IEC 13818-2 or ISO/IEC 11172-2 or ISO/IEC 14496-2 video stream number "
					+ Integer.toHexString(streamId & 0x0F);
		}

		switch (streamId) {
		case 0xBC:
			return "program_stream_map";
		case 0xBD:
			return "private_stream_1";
		case 0xBE:
			return "padding_stream";
		case 0xBF:
			return "private_stream_2";
		case 0xF0:
			return "ECM_stream";
		case 0xF1:
			return "EMM_stream";
		case 0xF2:
			return "DSMCC_stream";
		case 0xF3:
			return "ISO/IEC_13522_stream";
		case 0xF4:
			return "ITU-T Rec. H.222.1 type A";
		case 0xF5:
			return "ITU-T Rec. H.222.1 type B";
		case 0xF6:
			return "ITU-T Rec. H.222.1 type C";
		case 0xF7:
			return "ITU-T Rec. H.222.1 type D";
		case 0xF8:
			return "ITU-T Rec. H.222.1 type E";
		case 0xF9:
			return "ancillary_stream";
		case 0xFA:
			return "ISO/IEC14496-1_SL-packetized_stream";
		case 0xFB:
			return "ISO/IEC14496-1_FlexMux_stream";
		/* ISO/IEC 13818-1:2007/FPDAM5 */
		case 0xFC:
			return "metadata stream";
		case 0xFD:
			return "extended_stream_id";
		case 0xFE:
			return "reserved data stream";

		case 0xFF:
			return "program_stream_directory";
		default:
			return "??";
		}

	}

}

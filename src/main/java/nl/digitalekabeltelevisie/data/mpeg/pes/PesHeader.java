/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PesPacketData;
import nl.digitalekabeltelevisie.gui.HTMLSource;

/**
 * @author Eric
 *
 */
public class PesHeader implements HTMLSource, TreeNode{

	private final byte[] data;
	private final int offset;

	/**
	 * @param data
	 * @param offset
	 */
	public PesHeader(final byte[] data, final int offset) {
		super();
		this.data = data;
		this.offset = offset;
	}

	public boolean isValidPesHeader(){
		return (data[offset]==0) &&
				(data[offset+1]==0) &&
				(data[offset+2]==1);
	}

	public int getStreamID(){
		return getInt(data, offset+3, 1, MASK_8BITS);
	}

	public int getPesPacketLength(){
		return getInt(data, offset+4, 2, MASK_16BITS);
	}

	public void addToJtree(final DefaultMutableTreeNode t, final int modus){
		final int stream_id = getStreamID();
		t.add(new DefaultMutableTreeNode(new KVP("stream_id",getStreamID(),PesHeader.getStreamIDDescription(getStreamID()))));
		t.add(new DefaultMutableTreeNode(new KVP("PES_packet_length",getPesPacketLength(),null)));
		if(hasExtendedHeader(stream_id)){

			t.add(new DefaultMutableTreeNode(new KVP("markerBits",getMarkerBits(),null)));
			t.add(new DefaultMutableTreeNode(new KVP("pes_scrambling_control",getPes_scrambling_control(),getPes_scrambling_control()==0?"Not scrambled":"User-defined")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_priority",getPes_priority(),getPes_priority()==1?"higher":"normal")));
			t.add(new DefaultMutableTreeNode(new KVP("data_alignment_indicator",getData_alignment_indicator(),getData_alignment_indicator()==1?"PES packet header is immediately followed by the video start code or audio syncword indicated in the data_stream_alignment_descriptor":"alignment not defined")));
			t.add(new DefaultMutableTreeNode(new KVP("copyright",getCopyright(),getCopyright()==1?"packet payload is protected by copyright":"not defined whether the material is protected by copyright")));
			t.add(new DefaultMutableTreeNode(new KVP("original_or_copy",getOriginal_or_copy(),getOriginal_or_copy()==1?"contents of the associated PES packet payload is an original":"contents of the associated PES packet payload is a copy")));

			final int pts_dts_flags = getPts_dts_flags();
			t.add(new DefaultMutableTreeNode(new KVP("pts_dts_flags",pts_dts_flags,PesHeader.getPts_dts_flagsString(pts_dts_flags))));
			t.add(new DefaultMutableTreeNode(new KVP("escr_flag",getEscr_flag(),getEscr_flag()==1?"ESCR base and extension fields are present":"no ESCR fields are present")));
			t.add(new DefaultMutableTreeNode(new KVP("es_rate_flag",getEs_rate_flag(),getEs_rate_flag()==1?"ES_rate field is present":"no ES_rate field is present")));
			t.add(new DefaultMutableTreeNode(new KVP("dsm_trick_mode_flag",getDsm_trick_mode_flag() ,getDsm_trick_mode_flag()==1?"8-bit trick mode field is present":"8-bit trick mode field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("additional_copy_info_flag",getAdditional_copy_info_flag(),getAdditional_copy_info_flag()==1?"additional_copy_info field is present":"additional_copy_info field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_crc_flag",getPes_crc_flag(),getPes_crc_flag()==1?"CRC field is present":"CRC field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_extension_flag",getPes_extension_flag() ,getPes_extension_flag()==1?"extension field is present":"extension field is not present")));
			t.add(new DefaultMutableTreeNode(new KVP("pes_header_data_length",getPes_header_data_length(),null)));
			if ((pts_dts_flags ==2) || (pts_dts_flags ==3)) {
				final long pts = getPts();
				t.add(new DefaultMutableTreeNode(new KVP("pts",pts,printTimebase90kHz(pts))));
			}
			if (pts_dts_flags ==3) {
				final long dts = getDts();
				t.add(new DefaultMutableTreeNode(new KVP("dts",dts,printTimebase90kHz(dts))));
			}
		}
	}

	/**
	 * @param stream_id
	 * @return
	 */
	public boolean hasExtendedHeader(final int stream_id) {
		return (stream_id != PesPacketData.program_stream_map)
				&& (stream_id != PesPacketData.padding_stream)
				&& (stream_id != PesPacketData.private_stream_2)
				&& (stream_id != PesPacketData.ECM_stream)
				&& (stream_id != PesPacketData.EMM_stream)
				&& (stream_id != PesPacketData.program_stream_directory)
				&& (stream_id != PesPacketData.DSMCC_stream)
				&& (stream_id != PesPacketData.ITU_T_Rec_H_222_1typeE);
	}


	public boolean hasExtendedHeader(){
		return hasExtendedHeader(getStreamID());
	}
	/**
	 * @return
	 */
	public long getDts() {
		return getTimeStamp(data,offset+14);
	}

	/**
	 * @return
	 */
	public long getPts() {
		return getTimeStamp(data,offset+9);
	}

	/**
	 * @return length of Pes header
	 */
	public final int getPes_header_data_length() {
		return getInt(data, offset+8, 1, MASK_8BITS);
	}
	/**
	 * @return
	 */
	public int getPes_extension_flag() {
		return getInt(data, offset+7, 1, MASK_1BIT);
	}
	/**
	 * @return
	 */
	public int getPes_crc_flag() {
		return getInt(data, offset+7, 1, 0x02)>>1;
	}
	/**
	 * @return
	 */
	public int getAdditional_copy_info_flag() {
		return getInt(data, offset+7, 1, 0x04)>>2;
	}
	/**
	 * @return
	 */
	public int getDsm_trick_mode_flag() {
		return getInt(data, offset+7, 1, 0x08)>>3;
	}
	/**
	 * @return
	 */
	public int getEs_rate_flag() {
		return getInt(data, offset+7, 1, 0x10)>>4;
	}
	/**
	 * @return
	 */
	public int getEscr_flag() {
		return getInt(data, offset+7, 1, 0x20)>>5;
	}
	/**
	 * @return
	 */
	public final int getPts_dts_flags() {
		return getInt(data, offset+7, 1, 0xC0)>>6;
	}
	/**
	 * @return
	 */
	public int getOriginal_or_copy() {
		return getInt(data, offset+6, 1, MASK_1BIT);
	}
	/**
	 * @return
	 */
	public int getCopyright() {
		return getInt(data, offset+6, 1, 0x02) >>1;
	}
	/**
	 * @return
	 */
	public int getData_alignment_indicator() {
		return getInt(data, offset+6, 1, 0x04) >>2;
	}
	/**
	 * @return
	 */
	public int getPes_priority() {
		return getInt(data, offset+6, 1, 0x08) >>3;
	}
	/**
	 * @return
	 */
	public int getPes_scrambling_control() {
		return getInt(data, offset+6, 1, 0x30) >>4;
	}
	/**
	 * @return
	 */
	public int getMarkerBits() {
		return getInt(data, offset+6, 1, 0xC0) >>6;
	}

	/**
	 * @param array
	 * @param offset
	 * @return the value of the PTS/DTS as described in 2.4.3.7 of iso 13813, prefix and marker bits are ignored
	 */
	public final static long getTimeStamp(final byte[] array, final int offset) {

		long ts = getLong(array, offset, 1, 0x0E) << 29; // bits 32..30
		ts |= getLong(array, offset + 1, 2, 0xFFFE) << 14; // bits 29..15
		ts |= getLong(array, offset + 3, 2, 0xFFFE) >> 1; // bits 14..0

		return ts;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Pes Header"));
		addToJtree(t, modus);
		return t;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
	 */
	@Override
	public String getHTML() {
		// TODO Auto-generated method stub
		return null;
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


}

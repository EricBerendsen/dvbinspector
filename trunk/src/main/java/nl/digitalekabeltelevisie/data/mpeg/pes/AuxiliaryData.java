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

package nl.digitalekabeltelevisie.data.mpeg.pes;

import static nl.digitalekabeltelevisie.util.Utils.MASK_14BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_4BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getBytes;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.indexOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;



/**
 * Implements Annex B (normative): Auxiliary Data in the Video Elementary Stream of ETSI TS 101 154 V1.11.1 (2012-11)
 *
 * see also ATSC Digital Television Standard: Part 4 – MPEG-2 Video System Characteristics Document A/53 Part 4:2009, 7 August 2009
 *
 * Digital Video Broadcasting (DVB);  Specification for the use of Video and Audio Coding in Broadcasting Applications based on the MPEG-2 Transport Stream
 *
 * used for both UserData in MPEG2 encoded Video, and user_data_registered_itu_t_t35 Sei_message in H.264/AVC
 *
 * @see http://www.atsc.org/cms/standards/a53/a_53-Part-4-2009.pdf
 *
 * @author Eric
 *
 */
public class AuxiliaryData implements TreeNode{

	public static class BarData implements TreeNode{

		int top_bar_flag;
		int bottom_bar_flag;
		int left_bar_flag;
		int right_bar_flag;
		int reserved;

		int marker_bits_top;
		int line_number_end_of_top_bar;
		int marker_bits_bottom;
		int line_number_start_of_bottom_bar;
		int marker_bits_left;
		int pixel_number_end_of_left_bar;
		int marker_bits_right;
		int pixel_number_start_of_right_bar;

		public BarData(final byte[] data, final int offset, final int len){
			top_bar_flag = getInt(data,offset,1,0x80)>>7;
			bottom_bar_flag = getInt(data,offset,1,0x40)>>6;
			left_bar_flag = getInt(data,offset,1,0x20)>>5;
			right_bar_flag = getInt(data,offset,1,0x10)>>4;
			reserved = getInt(data,offset,1,0x0F);
			int o = offset+1;
			if (top_bar_flag == 1) {
				marker_bits_top = getInt(data,o,1,0xC0)>>6;
				line_number_end_of_top_bar = getInt(data, o, 2,MASK_14BITS);
				o+=2;
			}
			if (bottom_bar_flag == 1) {
				marker_bits_bottom = getInt(data,o,1,0xC0)>>6;
				line_number_start_of_bottom_bar = getInt(data, o, 2,MASK_14BITS);
				o+=2;
			}
			if (left_bar_flag == 1) {
				marker_bits_left  = getInt(data,o,1,0xC0)>>6;
				pixel_number_end_of_left_bar = getInt(data, o, 2,MASK_14BITS);
				o+=2;
			}
			if (right_bar_flag == 1) {
				marker_bits_right = getInt(data,o,1,0xC0)>>6;
				pixel_number_start_of_right_bar = getInt(data, o, 2,MASK_14BITS);
				o+=2;
			}
		}

		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("bar_data()"));
			t.add(new DefaultMutableTreeNode(new KVP("top_bar_flag",top_bar_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("bottom_bar_flag",bottom_bar_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("left_bar_flag",left_bar_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("right_bar_flag",right_bar_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			if (top_bar_flag == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("marker_bits",marker_bits_top,null)));
				t.add(new DefaultMutableTreeNode(new KVP("line_number_end_of_top_bar",line_number_end_of_top_bar,null)));
			}
			if (bottom_bar_flag == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("marker_bits",marker_bits_bottom,null)));
				t.add(new DefaultMutableTreeNode(new KVP("line_number_start_of_bottom_bar",line_number_start_of_bottom_bar,null)));
			}
			if (left_bar_flag == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("marker_bits",marker_bits_left,null)));
				t.add(new DefaultMutableTreeNode(new KVP("pixel_number_end_of_left_bar",pixel_number_end_of_left_bar,null)));
			}
			if (right_bar_flag == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("marker_bits",marker_bits_right,null)));
				t.add(new DefaultMutableTreeNode(new KVP("pixel_number_start_of_right_bar",pixel_number_start_of_right_bar,null)));
			}
			return t;
		}

	}

	public static class CCData implements TreeNode{

		public static class Construct implements TreeNode{

			private int one_bit;// (set to '1")
			private int reserved;
			private int cc_valid;
			private int cc_type;
			private int cc_data_1;
			private int cc_data_2;

			/**
			 * @param data
			 * @param localOffset
			 */
			public Construct(final byte[] data, final int localOffset) {
				if(localOffset<data.length){
					one_bit = getInt(data,localOffset,1,0x80)>>7;
				reserved = getInt(data,localOffset,1,0x71)>>3;
				cc_valid = getInt(data,localOffset,1,0x04)>>2;
				cc_type = getInt(data,localOffset,1,0x03);
				}
				if((localOffset+1)<data.length){
					cc_data_1 = getInt(data,localOffset+1,1,MASK_8BITS);
				}
				if((localOffset+2)<data.length){
					cc_data_2 = getInt(data,localOffset+2,1,MASK_8BITS);
				}
			}

			/* (non-Javadoc)
			 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
			 */
			@Override
			public DefaultMutableTreeNode getJTreeNode(final int modus) {
				final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("construct"));
				t.add(new DefaultMutableTreeNode(new KVP("one_bit",one_bit,"shall be '1' to maintain backwards compatibility with previous versions of CEA-708-C")));
				t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
				t.add(new DefaultMutableTreeNode(new KVP("cc_valid",cc_valid,cc_valid==1?"the two closed caption data bytes that follow are valid":"the two data bytes are invalid")));
				t.add(new DefaultMutableTreeNode(new KVP("cc_type",cc_type,null)));
				t.add(new DefaultMutableTreeNode(new KVP("cc_data_1",cc_data_1,null)));
				t.add(new DefaultMutableTreeNode(new KVP("cc_data_2",cc_data_2,null)));

				return t;
			}

		}

		private final int reserved;
		private final int process_cc_data_flag;
		private final int zero_bit;
		private final int cc_count;
		private final int reserved2;
		private final List<Construct> constructs = new ArrayList<Construct>();
		private final int marker_bits;


		public CCData(final byte[] data, final int offset, final int len){
			reserved = getInt(data,offset,1,0x80)>>7;
				process_cc_data_flag = getInt(data,offset,1,0x40)>>6;
				zero_bit = getInt(data,offset,1,0x20)>>5;
				cc_count = getInt(data,offset,1,0x1F);
				reserved2= getInt(data,offset+1,1,MASK_8BITS);
				int localOffset = offset+2;
				for (int i = 0; i < cc_count; i++) {
					final Construct construct = new Construct(data, localOffset);
					constructs.add(construct);
					localOffset+=3;
				}
				marker_bits= getInt(data,localOffset,1,MASK_8BITS);
		}

		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
		 */
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("cc_data()"));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			t.add(new DefaultMutableTreeNode(new KVP("process_cc_data_flag",process_cc_data_flag,process_cc_data_flag==1?"cc_data shall be parsed and its meaning processed":"cc_data shall be discarded")));
			t.add(new DefaultMutableTreeNode(new KVP("zero_bit",zero_bit,null)));
			t.add(new DefaultMutableTreeNode(new KVP("cc_count",cc_count,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2,null)));
			addListJTree(t,constructs,modus,"CC Constructs");
			//t.add(new DefaultMutableTreeNode(new KVP("constructs",constructs)));
			t.add(new DefaultMutableTreeNode(new KVP("marker_bits",marker_bits,null)));
			return t;
		}
	}


	private byte[] data = null;
	int offset = 0;
	int len = 0;
	byte [] user_identifier;
	private boolean isAFD;
	private int active_format_flag;
	private int reserved = 0;
	private int reserved2 = 0;
	private int active_format;

	private boolean isDVB1data;
	private int user_data_type_code;
	private byte[] user_data_type_structure;
	private BarData barData;
	private CCData ccData;



	/**
	 *
	 */
	public AuxiliaryData(final byte[] data, final int offset, final int len) {
		this.data = data;
		this.offset = offset;
		this.len = len;
		user_identifier = Arrays.copyOfRange(data, offset, offset+4);
		if(indexOf(data, new byte[]{0x44,0x54,0x47,0x31}, offset)==offset){ // DTG1
			isAFD = true;
			active_format_flag = getInt(data,offset+4,1,0x40)>>6;
				reserved = getInt(data,offset+4,1,0x3F);
				if(active_format_flag==1){
					reserved2= getInt(data,offset+5,1,0xF0)>>4;
					active_format = getInt(data,offset+5,1,MASK_4BITS);
				}
		}
		if(indexOf(data, new byte[]{0x47,0x41,0x39,0x34}, offset)==offset){ // GA94
			isDVB1data = true;
			user_data_type_code = getInt(data,offset+4,1,MASK_8BITS);
			user_data_type_structure = getBytes(data, offset+5, len-5);
			if(user_data_type_code==0x06){
				barData = new BarData(user_data_type_structure, 0, len-5);
			}else if(user_data_type_code==0x03){
				ccData = new CCData(user_data_type_structure, 0, len-5);
			}
		}

	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Auxilary data",data,offset, len,isAFD?"Active Format Description":null));
		t.add(new DefaultMutableTreeNode(new KVP("user_identifier",user_identifier,null)));
		if(isAFD){
			t.add(new DefaultMutableTreeNode(new KVP("active_format_flag",active_format_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2,null)));
			t.add(new DefaultMutableTreeNode(new KVP("active_format",active_format,getActiveFormatString(active_format))));
		}
		if(isDVB1data){
			t.add(new DefaultMutableTreeNode(new KVP("user_data_type_code",user_data_type_code,getUserDataTypeString(user_data_type_code))));
			t.add(new DefaultMutableTreeNode(new KVP("user_data_type_structure",user_data_type_structure,null)));
			if(user_data_type_code==0x06){ // bardata
				t.add(barData.getJTreeNode(modus));
			}else if(user_data_type_code==0x03){
				t.add(ccData.getJTreeNode(modus));
			}
		}
		return t;
	}


	public static String getUserDataTypeString(final int user_data_type_code) {
		switch (user_data_type_code) {
		case 0x03:
			return "cc data";
		case 0x06:
			return "bar data";
		case 0x07:
			return "multi_region_disparity";
		default:
			if((user_data_type_code >=0)&&(user_data_type_code <=0xff)){
				return "DVB Reserved";
			}else{
				return "unknown/error";
			}
		}
	}


	public static String getActiveFormatString(final int active_format) {
		switch (active_format) {
		case 0x00:
			return "reserved";
		case 0x01:
			return "reserved";
		case 0x02:
			return "box 16:9 (top)";
		case 0x03:
			return "box 14:9 (top)";
		case 0x04:
			return "box > 16:9 (centre)";
		case 0x05:
			return "reserved";
		case 0x06:
			return "reserved";
		case 0x07:
			return "reserved";
		case 0x08:
			return "Active format is the same as the coded frame";
		case 0x09:
			return "4:3 (centre)";
		case 0x0A:
			return "16:9 (centre)";
		case 0x0B:
			return "14:9 (centre)";
		case 0x0C:
			return "reserved";
		case 0x0D:
			return "4:3 (with shoot & protect 14:9 centre)";
		case 0x0E:
			return "16:9 (with shoot & protect 14:9 centre)";
		case 0x0F:
			return "16:9 (with shoot & protect 4:3 centre)";
		default:
			return "unknown/error";
		}
	}


	public byte[] getData() {
		return data;
	}


	public int getOffset() {
		return offset;
	}


	public int getLen() {
		return len;
	}


	public byte[] getUser_identifier() {
		return user_identifier;
	}


	public boolean isAFD() {
		return isAFD;
	}


	public int getActive_format_flag() {
		return active_format_flag;
	}


	public int getReserved() {
		return reserved;
	}


	public int getReserved2() {
		return reserved2;
	}


	public int getActive_format() {
		return active_format;
	}


	public boolean isDVB1data() {
		return isDVB1data;
	}


	public int getUser_data_type_code() {
		return user_data_type_code;
	}


	public byte[] getUser_data_type_structure() {
		return user_data_type_structure;
	}


	public BarData getBarData() {
		return barData;
	}


	public CCData getCcData() {
		return ccData;
	}

}

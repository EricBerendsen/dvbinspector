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

package nl.digitalekabeltelevisie.data.mpeg.pes.video.common;

import static nl.digitalekabeltelevisie.util.Utils.MASK_14BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;

public class BarData implements TreeNode{

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
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("bar_data()");
		t.add(new KVP("top_bar_flag",top_bar_flag));
		t.add(new KVP("bottom_bar_flag",bottom_bar_flag));
		t.add(new KVP("left_bar_flag",left_bar_flag));
		t.add(new KVP("right_bar_flag",right_bar_flag));
		t.add(new KVP("reserved",reserved));
		if (top_bar_flag == 1) {
			t.add(new KVP("marker_bits",marker_bits_top));
			t.add(new KVP("line_number_end_of_top_bar",line_number_end_of_top_bar));
		}
		if (bottom_bar_flag == 1) {
			t.add(new KVP("marker_bits",marker_bits_bottom));
			t.add(new KVP("line_number_start_of_bottom_bar",line_number_start_of_bottom_bar));
		}
		if (left_bar_flag == 1) {
			t.add(new KVP("marker_bits",marker_bits_left));
			t.add(new KVP("pixel_number_end_of_left_bar",pixel_number_end_of_left_bar));
		}
		if (right_bar_flag == 1) {
			t.add(new KVP("marker_bits",marker_bits_right));
			t.add(new KVP("pixel_number_start_of_right_bar",pixel_number_start_of_right_bar));
		}
		return t;
	}

}
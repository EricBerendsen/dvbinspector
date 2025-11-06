/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video266;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 *
 * Rec. ITU-T H.266 (04/2022) 7.3.2.14 Slice layer RBSP syntax
 */
public class Slice_layer_rbsp extends RBSP {
	
	

	// 7.3.7 Slice header syntax
	private class SliceHeader implements TreeNode{

		private int sh_picture_header_in_slice_header_flag;
		private PictureHeaderStructure picture_header_structure;

		public SliceHeader(BitSource bitSource) {
			sh_picture_header_in_slice_header_flag = bitSource.u(1);
			
			if( sh_picture_header_in_slice_header_flag != 0) {
				picture_header_structure = new PictureHeaderStructure(bitSource);
			}
		}

		@Override
		public KVP getJTreeNode(int modus) {
			KVP t = new KVP("slice_header");
			KVP sh_picture_header_in_slice_header_flag_node = new KVP("sh_picture_header_in_slice_header_flag",sh_picture_header_in_slice_header_flag);
			t.add(sh_picture_header_in_slice_header_flag_node);
			if( sh_picture_header_in_slice_header_flag != 0) {
				sh_picture_header_in_slice_header_flag_node.add(picture_header_structure.getJTreeNode(modus));
			}
			return t;
		}
		
		
	}

	private SliceHeader slice_header;

	protected Slice_layer_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		slice_header = new SliceHeader(bitSource);
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("slice_layer_rbsp");
		t.add(slice_header.getJTreeNode(modus));
		return t;
	}

}

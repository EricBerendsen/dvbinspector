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
import nl.digitalekabeltelevisie.util.BitSource;

/**
 *
 * Rec. ITU-T H.266 (04/2022) 7.3.2.8 Picture header structure syntax
 */

public class PictureHeaderStructure implements TreeNode{
	

	private int ph_gdr_or_irap_pic_flag;
	private int ph_non_ref_pic_flag;
	private int ph_gdr_pic_flag;
	private int ph_inter_slice_allowed_flag;
	private int ph_intra_slice_allowed_flag;
	private int ph_pic_parameter_set_id;

	public PictureHeaderStructure(BitSource bitSource) {
		ph_gdr_or_irap_pic_flag = bitSource.u(1);
		ph_non_ref_pic_flag = bitSource.u(1);

		if (ph_gdr_or_irap_pic_flag != 0) {
			ph_gdr_pic_flag = bitSource.u(1);
		}
		ph_inter_slice_allowed_flag = bitSource.u(1);
		if (ph_inter_slice_allowed_flag != 0) {
			ph_intra_slice_allowed_flag = bitSource.u(1);
		}
		ph_pic_parameter_set_id = bitSource.ue();
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("picture_header_structure");
		t.add(new KVP("ph_gdr_or_irap_pic_flag",ph_gdr_or_irap_pic_flag,ph_gdr_or_irap_pic_flag==1?"current picture is a GDR or IRAP picture":"current picture is not a GDR"));
		t.add(new KVP("ph_non_ref_pic_flag",ph_non_ref_pic_flag,ph_non_ref_pic_flag == 1?"current picture is never used as a reference picture":"current picture might or might not be used as a reference picture"));
		 
		if (ph_gdr_or_irap_pic_flag != 0) {
			t.add(new KVP("ph_gdr_pic_flag",ph_gdr_pic_flag,ph_gdr_pic_flag == 1?"current picture is a GDR picture":"current picture is not a GDR picture"));
		}
		KVP ph_inter_slice_allowed_flag_node = new KVP("ph_inter_slice_allowed_flag",ph_inter_slice_allowed_flag,ph_inter_slice_allowed_flag == 0?"all coded slices of the picture have sh_slice_type equal to 2.":"there might or might not be one or more coded slices in the picture that have sh_slice_type equal to 0 or 1");
		t.add(ph_inter_slice_allowed_flag_node);
		if (ph_inter_slice_allowed_flag != 0) {
			ph_inter_slice_allowed_flag_node.add(new KVP("ph_intra_slice_allowed_flag",ph_intra_slice_allowed_flag,ph_intra_slice_allowed_flag == 0?"all coded slices of the picture have sh_slice_type equal to 0 or 1":"there might or might not be one or more coded slices in the picture that have sh_slice_type equal to 2"));
		}
		t.add(new KVP("ph_pic_parameter_set_id", ph_pic_parameter_set_id));

		return t;
	}
	
}

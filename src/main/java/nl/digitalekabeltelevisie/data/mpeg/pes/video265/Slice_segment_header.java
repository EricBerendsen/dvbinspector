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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.BitSource;

public class Slice_segment_header implements TreeNode {
	
	// based onN 7.3.6.1 General slice segment header syntax
	// Rec. ITU-T H.265 v5 (02/2018) 

	private int first_slice_segment_in_pic_flag;
	private int no_output_of_prior_pics_flag;
	private NALUnitType nal_unit_type;
	private int slice_pic_parameter_set_id;

	public Slice_segment_header(BitSource bitSource, NALUnitType nal_unit_type) {
		this.nal_unit_type = nal_unit_type;
		first_slice_segment_in_pic_flag = bitSource.u(1);
		if (nal_unit_type.getType() >= NALUnitType.BLA_W_LP.getType()
				&& nal_unit_type.getType() <= NALUnitType.RSV_IRAP_VCL23.getType()) {
			no_output_of_prior_pics_flag = bitSource.u(1);
		}
		slice_pic_parameter_set_id= bitSource.ue(); 
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("slice_segment_header"));
		t.add(new DefaultMutableTreeNode(new KVP("first_slice_segment_in_pic_flag",first_slice_segment_in_pic_flag,null)));
		if (nal_unit_type.getType() >= NALUnitType.BLA_W_LP.getType()
				&& nal_unit_type.getType() <= NALUnitType.RSV_IRAP_VCL23.getType()) {
			t.add(new DefaultMutableTreeNode(new KVP("no_output_of_prior_pics_flag",no_output_of_prior_pics_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("slice_pic_parameter_set_id",slice_pic_parameter_set_id,null)));

		return t;
	}

}

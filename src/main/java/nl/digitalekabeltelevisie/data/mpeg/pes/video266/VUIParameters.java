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
import nl.digitalekabeltelevisie.util.LookUpList;

/**
 * 
 * Based on Rec. ITU-T H.274 (05/2022) 7.2 VUI parameters syntax
 *
 */
public class VUIParameters implements TreeNode {
	
	private static final LookUpList colour_primaries_list = new LookUpList.Builder().
			add(0,"Reserved").
			add(1,"BT.709-6").
			add(2,"Unspecified").
			add(3,"Reserved").
			add(4,"BT.470-6 System M (historical)").
			add(5,"BT.601-7 625").
			add(6,"BT.601-7 525").
			add(7,"SMPTE ST 240").
			add(8,"Generic film").
			add(9,"BT.2020-2").
			add(10,"SMPTE ST 428-1").
			add(11,"SMPTE RP 431-2").
			add(12,"SMPTE EG 432-1").
			add(13,21,"Reserved").
			add(22,"No corresponding industry specification identified").
			add(23,255,"Reserved").
			build();

	
	private int payloadSize;
	private int vui_progressive_source_flag;
	private int vui_interlaced_source_flag;
	private int vui_non_packed_constraint_flag;
	private int vui_non_projected_constraint_flag;
	private int vui_aspect_ratio_info_present_flag;
	private int vui_aspect_ratio_constant_flag;
	private int vui_aspect_ratio_idc;
	private int vui_sar_width;
	private int vui_sar_height;
	private int vui_overscan_info_present_flag;
	private int vui_overscan_appropriate_flag;
	private int vui_colour_description_present_flag;
	private int vui_colour_primaries;
	private int vui_transfer_characteristics;
	private int vui_matrix_coeffs;
	private int vui_full_range_flag;
	private int vui_chroma_loc_info_present_flag;
	private int vui_chroma_sample_loc_type_frame;
	private int vui_chroma_sample_loc_type_top_field;
	private int vui_chroma_sample_loc_type_bottom_field;

	public VUIParameters(int payloadSize, BitSource bitSource) {
		this.payloadSize = payloadSize;
		
		vui_progressive_source_flag = bitSource.u(1);
		vui_interlaced_source_flag = bitSource.u(1);
		vui_non_packed_constraint_flag = bitSource.u(1);
		vui_non_projected_constraint_flag = bitSource.u(1);
		vui_aspect_ratio_info_present_flag = bitSource.u(1);
		if (vui_aspect_ratio_info_present_flag != 0) {
			vui_aspect_ratio_constant_flag = bitSource.u(1);
			vui_aspect_ratio_idc = bitSource.u(8);
			if (vui_aspect_ratio_idc == 255) {
				vui_sar_width = bitSource.u(16);
				vui_sar_height = bitSource.u(16);
			}
		}
		
		vui_overscan_info_present_flag = bitSource.u(1);
		if (vui_overscan_info_present_flag != 0) {
			vui_overscan_appropriate_flag = bitSource.u(1);
		}
		vui_colour_description_present_flag = bitSource.u(1);
		if (vui_colour_description_present_flag != 0) {
			vui_colour_primaries = bitSource.u(8);
			vui_transfer_characteristics = bitSource.u(8);
			vui_matrix_coeffs = bitSource.u(8);
			vui_full_range_flag = bitSource.u(1);
		}
		vui_chroma_loc_info_present_flag = bitSource.u(1);
		
		if (vui_chroma_loc_info_present_flag != 0) {
			if (vui_progressive_source_flag != 0 && vui_interlaced_source_flag == 0) {
				vui_chroma_sample_loc_type_frame = bitSource.ue();
			} else {
				vui_chroma_sample_loc_type_top_field = bitSource.ue();
				vui_chroma_sample_loc_type_bottom_field = bitSource.ue();
			}
		}

	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = new KVP("vui_parameters(payloadSize:"+payloadSize+")");
		t.add(new KVP("vui_progressive_source_flag",vui_progressive_source_flag));
		t.add(new KVP("vui_interlaced_source_flag",vui_interlaced_source_flag,getSourceFlagsDescription(vui_progressive_source_flag,vui_interlaced_source_flag)));
		t.add(new KVP("vui_non_packed_constraint_flag",vui_non_packed_constraint_flag));
		t.add(new KVP("vui_non_projected_constraint_flag",vui_non_projected_constraint_flag));

		KVP vui_aspect_ratio_info_present_flag_node = new KVP("vui_aspect_ratio_info_present_flag",vui_aspect_ratio_info_present_flag);

		t.add(vui_aspect_ratio_info_present_flag_node);
		if (vui_aspect_ratio_info_present_flag != 0) {
			vui_aspect_ratio_info_present_flag_node.add(new KVP("vui_aspect_ratio_constant_flag",vui_aspect_ratio_constant_flag));
			KVP vui_aspect_ratio_idcNode = new KVP("vui_aspect_ratio_idc",vui_aspect_ratio_idc);
			vui_aspect_ratio_info_present_flag_node.add(vui_aspect_ratio_idcNode);
			
			if (vui_aspect_ratio_idc == 255) {
				vui_aspect_ratio_idcNode.add(new KVP("vui_sar_width",vui_sar_width));
				vui_aspect_ratio_idcNode.add(new KVP("vui_sar_height",vui_sar_height));
			}
		}
		
		KVP vui_overscan_info_present_flag_node = new KVP("vui_overscan_info_present_flag",vui_overscan_info_present_flag);
		t.add(vui_overscan_info_present_flag_node);
		if (vui_overscan_info_present_flag != 0) {
			vui_overscan_info_present_flag_node.add(new KVP("vui_overscan_appropriate_flag",vui_overscan_appropriate_flag));
		}

		KVP vui_colour_description_present_flag_node = new KVP("vui_colour_description_present_flag",vui_colour_description_present_flag);
		t.add(vui_colour_description_present_flag_node);
		if (vui_colour_description_present_flag != 0) {
			vui_colour_description_present_flag_node.add(new KVP("vui_colour_primaries",vui_colour_primaries,colour_primaries_list.get(vui_colour_primaries)));
			vui_colour_description_present_flag_node.add(new KVP("vui_transfer_characteristics",vui_transfer_characteristics));
			vui_colour_description_present_flag_node.add(new KVP("vui_matrix_coeffs",vui_matrix_coeffs));
			vui_colour_description_present_flag_node.add(new KVP("vui_full_range_flag",vui_full_range_flag));
		}
		
		KVP vui_chroma_loc_info_present_flag_node = new KVP("vui_chroma_loc_info_present_flag",vui_chroma_loc_info_present_flag);
		t.add(vui_chroma_loc_info_present_flag_node);

		if (vui_chroma_loc_info_present_flag != 0) {
			if (vui_progressive_source_flag != 0 && vui_interlaced_source_flag == 0) {
				vui_chroma_loc_info_present_flag_node.add(new KVP("vui_chroma_sample_loc_type_frame",vui_chroma_sample_loc_type_frame));
			} else {
				vui_chroma_loc_info_present_flag_node.add(new KVP("vui_chroma_sample_loc_type_top_field",vui_chroma_sample_loc_type_top_field));
				vui_chroma_loc_info_present_flag_node.add(new KVP("vui_chroma_sample_loc_type_bottom_field",vui_chroma_sample_loc_type_bottom_field));
			}
		}

		return t;
	}

	/**
	 * @param vui_progressive_source_flag2
	 * @param vui_interlaced_source_flag2
	 * @return
	 */
	private static String getSourceFlagsDescription(int vui_progressive_source_flag, int vui_interlaced_source_flag) {
		if( vui_progressive_source_flag ==1 &&  vui_interlaced_source_flag ==0) {
			return "source scan type progressive only";
		} else if( vui_progressive_source_flag ==0 &&  vui_interlaced_source_flag == 1) {
			return "source scan type interlaced only";
		} else if( vui_progressive_source_flag ==0 &&  vui_interlaced_source_flag == 0) {
			return "source scan type unknown or unspecified";
		} else { // ( vui_progressive_source_flag ==1 &&  vui_interlaced_source_flag == 1)
			return "source scan type of each picture is indicated at the picture level using the syntax element ffi_source_scan_type in a frame-field information SEI message";
		}
			
	}

}

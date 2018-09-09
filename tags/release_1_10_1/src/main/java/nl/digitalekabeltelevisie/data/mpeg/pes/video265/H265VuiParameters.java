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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.AbstractVuiParamters;
import nl.digitalekabeltelevisie.util.BitSource;

public class H265VuiParameters extends AbstractVuiParamters implements TreeNode {

	private final int sps_max_sub_layers_minus1;




	private final int neutral_chroma_indication_flag;
	private final int field_seq_flag;
	private final int frame_field_info_present_flag;
	private final int default_display_window_flag;
	private int def_disp_win_left_offset;
	private int def_disp_win_right_offset;
	private int def_disp_win_top_offset;
	private int def_disp_win_bottom_offset;
	private final int vui_timing_info_present_flag;
	private long vui_num_units_in_tick;
	private long vui_time_scale;
	private int vui_poc_proportional_to_timing_flag;
	private int vui_num_ticks_poc_diff_one_minus1;
	private int vui_hrd_parameters_present_flag;

	private H265HrdParameters hrd_parameters;

	private final int bitstream_restriction_flag;

	private int tiles_fixed_structure_flag;
	private int motion_vectors_over_pic_boundaries_flag;
	private int restricted_ref_pic_lists_flag;
	private int min_spatial_segmentation_idc;
	private int max_bytes_per_pic_denom;
	private int max_bits_per_min_cu_denom;
	private int log2_max_mv_length_horizontal;
	private int log2_max_mv_length_vertical;


	public H265VuiParameters(final int sps_max_sub_layers_minus1, final BitSource bitSource) {
		super(bitSource);
		this.sps_max_sub_layers_minus1= sps_max_sub_layers_minus1;

		neutral_chroma_indication_flag=bitSource.u(1);
		field_seq_flag=bitSource.u(1);
		frame_field_info_present_flag=bitSource.u(1);
		default_display_window_flag=bitSource.u(1);
		if( default_display_window_flag==1 ) {
			def_disp_win_left_offset=bitSource.ue();
			def_disp_win_right_offset=bitSource.ue();
			def_disp_win_top_offset=bitSource.ue();
			def_disp_win_bottom_offset=bitSource.ue();
		}
		vui_timing_info_present_flag=bitSource.u(1);


		if( vui_timing_info_present_flag==1 ) {
			vui_num_units_in_tick=bitSource.readBitsLong(32);
			vui_time_scale=bitSource.readBitsLong(32);
			vui_poc_proportional_to_timing_flag=bitSource.u(1);
			if( vui_poc_proportional_to_timing_flag==1 ){
				vui_num_ticks_poc_diff_one_minus1=bitSource.ue();
			}
			vui_hrd_parameters_present_flag=bitSource.u(1);
			if( vui_hrd_parameters_present_flag==1 ){
				hrd_parameters = new H265HrdParameters( 1, sps_max_sub_layers_minus1, bitSource);
			}
		}
		bitstream_restriction_flag=bitSource.u(1);
		if( bitstream_restriction_flag!=0 ) {
			tiles_fixed_structure_flag=bitSource.u(1);
			motion_vectors_over_pic_boundaries_flag=bitSource.u(1);
			restricted_ref_pic_lists_flag=bitSource.u(1);
			min_spatial_segmentation_idc=bitSource.ue();
			max_bytes_per_pic_denom=bitSource.ue();
			max_bits_per_min_cu_denom=bitSource.ue();
			log2_max_mv_length_horizontal=bitSource.ue();
			log2_max_mv_length_vertical=bitSource.ue();
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("vui_parameters"));
		addCommonFields(t);
		t.add(new DefaultMutableTreeNode(new KVP("neutral_chroma_indication_flag",neutral_chroma_indication_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("field_seq_flag",field_seq_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_field_info_present_flag",frame_field_info_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("default_display_window_flag",default_display_window_flag,null)));
		if( default_display_window_flag==1 ) {
			t.add(new DefaultMutableTreeNode(new KVP("def_disp_win_left_offset",def_disp_win_left_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("def_disp_win_right_offset",def_disp_win_right_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("def_disp_win_top_offset",def_disp_win_top_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("def_disp_win_bottom_offset",def_disp_win_bottom_offset,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("vui_timing_info_present_flag",vui_timing_info_present_flag,null)));


		if( vui_timing_info_present_flag==1 ) {
			t.add(new DefaultMutableTreeNode(new KVP("vui_num_units_in_tick",vui_num_units_in_tick,null)));
			t.add(new DefaultMutableTreeNode(new KVP("vui_time_scale",vui_time_scale,getClockTickString(vui_num_units_in_tick, vui_time_scale) )));
			t.add(new DefaultMutableTreeNode(new KVP("vui_poc_proportional_to_timing_flag",vui_poc_proportional_to_timing_flag,null)));
			if( vui_poc_proportional_to_timing_flag==1 ){
				t.add(new DefaultMutableTreeNode(new KVP("vui_num_ticks_poc_diff_one_minus1",vui_num_ticks_poc_diff_one_minus1,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("vui_hrd_parameters_present_flag",vui_hrd_parameters_present_flag,null)));
			if(vui_hrd_parameters_present_flag==1){
				t.add(hrd_parameters.getJTreeNode(modus));
			}
		}

		t.add(new DefaultMutableTreeNode(new KVP("bitstream_restriction_flag",bitstream_restriction_flag,null)));
		if(bitstream_restriction_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("tiles_fixed_structure_flag",tiles_fixed_structure_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("motion_vectors_over_pic_boundaries_flag",motion_vectors_over_pic_boundaries_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("restricted_ref_pic_lists_flag",restricted_ref_pic_lists_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("min_spatial_segmentation_idc",min_spatial_segmentation_idc,null)));
			t.add(new DefaultMutableTreeNode(new KVP("max_bytes_per_pic_denom",max_bytes_per_pic_denom,null)));
			t.add(new DefaultMutableTreeNode(new KVP("max_bits_per_min_cu_denom",max_bits_per_min_cu_denom,null)));
			t.add(new DefaultMutableTreeNode(new KVP("log2_max_mv_length_horizontal",log2_max_mv_length_horizontal,null)));
			t.add(new DefaultMutableTreeNode(new KVP("log2_max_mv_length_vertical",log2_max_mv_length_vertical,null)));
		}

		return t;
	}

	/**
	 * @param num_units_in_tick
	 * @param time_scale
	 * @return
	 */
	public static String getClockTickString(final long num_units_in_tick, final long time_scale) {
		return "clock tick:" + (((double) num_units_in_tick) / ((double) time_scale)) + " seconds";
	}

}

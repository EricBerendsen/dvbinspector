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

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

public class Video_parameter_set_rbsp extends RBSP {


	private static final Logger	logger	= Logger.getLogger(Video_parameter_set_rbsp.class.getName());
	private final int vps_video_parameter_set_id;
	private final int vps_base_layer_internal_flag;
	private final int vps_base_layer_available_flag;
	private final int vps_max_layers_minus1;
	private final int vps_max_sub_layers_minus1;
	private final int vps_temporal_id_nesting_flag;
	private final int vps_reserved_0xffff_16bits;
	private final ProfileTierLevel profile_tier_level;
	private final int vps_sub_layer_ordering_info_present_flag;
	private final int[] vps_max_dec_pic_buffering_minus1;
	private final int[] vps_max_num_reorder_pics;
	private final int[] vps_max_latency_increase_plus1;
	private final int vps_max_layer_id;
	private final int vps_num_layer_sets_minus1;
	private final int[][] layer_id_included_flag;
	private final int vps_timing_info_present_flag;
	private long vps_num_units_in_tick;
	private long vps_time_scale;
	private int vps_poc_proportional_to_timing_flag;
	private int vps_num_ticks_poc_diff_one_minus1;
	private int vps_num_hrd_parameters;
	private int[] hrd_layer_set_idx;
	private int[] cprms_present_flag;
	private H265HrdParameters[] hrd_parameters;
	private int vps_extension_flag;

	// based on 7.3.2.1 Video parameter set RBSP syntax Rec. ITU-T H.265 v2 (10/2014)


	public Video_parameter_set_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);

		vps_video_parameter_set_id = bitSource.u(4);
		vps_base_layer_internal_flag = bitSource.u(1);
		vps_base_layer_available_flag = bitSource.u(1);
		vps_max_layers_minus1 = bitSource.u(6);
		vps_max_sub_layers_minus1 = bitSource.u(3);
		vps_temporal_id_nesting_flag = bitSource.u(1);
		vps_reserved_0xffff_16bits = bitSource.u(16);
		profile_tier_level = new ProfileTierLevel(1, vps_max_sub_layers_minus1, bitSource);

		vps_sub_layer_ordering_info_present_flag = bitSource.u(1);

		vps_max_dec_pic_buffering_minus1 = new int[vps_max_sub_layers_minus1+1];
		vps_max_num_reorder_pics = new int[vps_max_sub_layers_minus1+1];
		vps_max_latency_increase_plus1 = new int[vps_max_sub_layers_minus1+1];
		for(int i = ( (vps_sub_layer_ordering_info_present_flag!=0) ? 0 : vps_max_sub_layers_minus1 ); i <= vps_max_sub_layers_minus1; i++ ) {
			vps_max_dec_pic_buffering_minus1[ i ] = bitSource.ue();
			vps_max_num_reorder_pics[ i ] = bitSource.ue();
			vps_max_latency_increase_plus1[ i ] = bitSource.ue();
		}
		vps_max_layer_id = bitSource.u(6);
		vps_num_layer_sets_minus1 = bitSource.ue();
		layer_id_included_flag = new int [vps_num_layer_sets_minus1+1] [vps_max_layer_id+1];
		for(int i = 1; i <= vps_num_layer_sets_minus1; i++ ){
			for(int j = 0; j <= vps_max_layer_id; j++ ){
				layer_id_included_flag[ i ][ j ] = bitSource.u(1);
			}
		}
		vps_timing_info_present_flag = bitSource.u(1);
		if( vps_timing_info_present_flag!=0 ) {
			vps_num_units_in_tick = bitSource.readBitsLong(32);
			vps_time_scale = bitSource.readBitsLong(32);
			vps_poc_proportional_to_timing_flag = bitSource.u(1);
			if( vps_poc_proportional_to_timing_flag!=0 ){
				vps_num_ticks_poc_diff_one_minus1 = bitSource.ue();
			}
			vps_num_hrd_parameters = bitSource.ue();
			hrd_layer_set_idx = new int [vps_num_hrd_parameters];
			cprms_present_flag= new int [vps_num_hrd_parameters];
			hrd_parameters = new H265HrdParameters[vps_num_hrd_parameters];
			for(int i = 0; i < vps_num_hrd_parameters; i++ ) {
				hrd_layer_set_idx[ i ] = bitSource.ue();
				if( i > 0 ){
					cprms_present_flag[ i ] = bitSource.u(1);
				}
				hrd_parameters [i] = new H265HrdParameters( cprms_present_flag[ i ], vps_max_sub_layers_minus1,bitSource);
			}
			vps_extension_flag = bitSource.u(1);
			if( vps_extension_flag!=0 ){
				logger.warning("vps_extension_data_flag not implemented");
			}
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Video_parameter_set_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("vps_video_parameter_set_id",vps_video_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vps_base_layer_internal_flag",vps_base_layer_internal_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vps_base_layer_available_flag",vps_base_layer_available_flag,getBaseLayerDescription(vps_base_layer_internal_flag,vps_base_layer_available_flag))));


		t.add(new DefaultMutableTreeNode(new KVP("vps_max_layers_minus1",vps_max_layers_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vps_max_sub_layers_minus1",vps_max_sub_layers_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vps_temporal_id_nesting_flag",vps_temporal_id_nesting_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vps_reserved_0xffff_16bits",vps_reserved_0xffff_16bits,null)));
		t.add(profile_tier_level.getJTreeNode(modus));


		t.add(new DefaultMutableTreeNode(new KVP("vps_sub_layer_ordering_info_present_flag",vps_sub_layer_ordering_info_present_flag,null)));


		for(int i = ( (vps_sub_layer_ordering_info_present_flag!=0) ? 0 : vps_max_sub_layers_minus1 ); i <= vps_max_sub_layers_minus1; i++ ) {
			t.add(new DefaultMutableTreeNode(new KVP("vps_max_dec_pic_buffering_minus1["+i+"]",vps_max_dec_pic_buffering_minus1[i],null)));
			t.add(new DefaultMutableTreeNode(new KVP("vps_max_num_reorder_pics["+i+"]",vps_max_num_reorder_pics[i],null)));
			t.add(new DefaultMutableTreeNode(new KVP("vps_max_latency_increase_plus1["+i+"]",vps_max_latency_increase_plus1[i],null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("vps_max_layer_id",vps_max_layer_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vps_num_layer_sets_minus1",vps_num_layer_sets_minus1,null)));
		for(int i = 1; i <= vps_num_layer_sets_minus1; i++ ){
			for(int j = 0; j <= vps_max_layer_id; j++ ){
				t.add(new DefaultMutableTreeNode(new KVP("layer_id_included_flag["+i+"]["+j+"]",layer_id_included_flag[i][j],null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("vps_timing_info_present_flag",vps_timing_info_present_flag,null)));
		if( vps_timing_info_present_flag!=0 ) {
			t.add(new DefaultMutableTreeNode(new KVP("vps_num_units_in_tick",vps_num_units_in_tick,null)));
			t.add(new DefaultMutableTreeNode(new KVP("vps_time_scale",vps_time_scale,getClockTickString(vps_num_units_in_tick, vps_time_scale))));
			t.add(new DefaultMutableTreeNode(new KVP("vps_poc_proportional_to_timing_flag",vps_poc_proportional_to_timing_flag,null)));
			if( vps_poc_proportional_to_timing_flag!=0 ){
				t.add(new DefaultMutableTreeNode(new KVP("vps_num_ticks_poc_diff_one_minus1",vps_num_ticks_poc_diff_one_minus1,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("vps_num_hrd_parameters",vps_num_hrd_parameters,null)));
			for(int i = 0; i < vps_num_hrd_parameters; i++ ) {
				t.add(new DefaultMutableTreeNode(new KVP("hrd_layer_set_idx["+i+"]",hrd_layer_set_idx[i],null)));
				if( i > 0 ){
					cprms_present_flag[ i ] = bitSource.u(1);
					t.add(new DefaultMutableTreeNode(new KVP("cprms_present_flag["+i+"]",cprms_present_flag[i],null)));
				}
				t.add(hrd_parameters[i].getJTreeNode(modus));
			}
			t.add(new DefaultMutableTreeNode(new KVP("vps_extension_flag",vps_extension_flag,null)));
			if(vps_extension_flag!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("vps_extension_data_flag")));
				return t;
			}
		}
		return t;
	}

	/**
	 * @param vps_base_layer_internal_flag2
	 * @param vps_base_layer_available_flag2
	 * @return
	 */
	private String getBaseLayerDescription(final int vps_base_layer_internal_flag1, final int vps_base_layer_available_flag) {
		// TODO Auto-generated method stub
		if((vps_base_layer_internal_flag!=0) &&(vps_base_layer_available_flag!=0)){
			return "base layer is present in the bitstream";
		}else if((vps_base_layer_internal_flag==0) &&(vps_base_layer_available_flag!=0)){
			return "base layer is provided by an external means not specified";
		}else if((vps_base_layer_internal_flag!=0) &&(vps_base_layer_available_flag==0)){
			return "base layer is not available (neither present in the bitstream nor provided by external means) but the VPS includes information of the base layer as if it were present in the bitstream";
		}else if((vps_base_layer_internal_flag==0) &&(vps_base_layer_available_flag==0)){
			return "base layer is not available (neither present in the bitstream nor provided by external means) but the VPS includes information of the base layer as if it were provided by an external means";
		} else {
			return null; // Should not happen
		}
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
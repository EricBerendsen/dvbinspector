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

import java.util.*;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.Utils;

public class Seq_parameter_set_rbsp extends RBSP {


	private static final Logger	logger	= Logger.getLogger(Seq_parameter_set_rbsp.class.getName());

	// based on 7.3.2.2.1 General sequence parameter set RBSP syntax Rec. ITU-T H.265 v2 (10/2014)
	private final int sps_video_parameter_set_id;

	private final int sps_max_sub_layers_minus1;
	private final int sps_temporal_id_nesting_flag;

	private final ProfileTierLevel profile_tier_level;

	private final int sps_seq_parameter_set_id;
	private final int chroma_format_idc;
	private int separate_colour_plane_flag;

	private final int pic_width_in_luma_samples;
	private final int pic_height_in_luma_samples;
	private final int conformance_window_flag;

	private int conf_win_left_offset;
	private int conf_win_right_offset;
	private int conf_win_top_offset;
	private int conf_win_bottom_offset;

	private final int bit_depth_luma_minus8;
	private final int bit_depth_chroma_minus8;
	private final int log2_max_pic_order_cnt_lsb_minus4;
	private final int sps_sub_layer_ordering_info_present_flag;

	private final int[] sps_max_dec_pic_buffering_minus1;
	private final int[] sps_max_num_reorder_pics;
	private final int[] sps_max_latency_increase_plus1;

	private final int log2_min_luma_coding_block_size_minus3;
	private final int log2_diff_max_min_luma_coding_block_size;
	private final int log2_min_luma_transform_block_size_minus2;
	private final int log2_diff_max_min_luma_transform_block_size;
	private final int max_transform_hierarchy_depth_inter;
	private final int max_transform_hierarchy_depth_intra;
	private final int scaling_list_enabled_flag;

	private int sps_scaling_list_data_present_flag;
	
	private ScalingListData sps_scaling_list_data;

	private final int amp_enabled_flag;
	private final int sample_adaptive_offset_enabled_flag;
	private final int pcm_enabled_flag;

	private int pcm_sample_bit_depth_luma_minus1;

	private int pcm_sample_bit_depth_chroma_minus1;

	private int log2_min_pcm_luma_coding_block_size_minus3;

	private int log2_diff_max_min_pcm_luma_coding_block_size;

	private int pcm_loop_filter_disabled_flag;

	private final int num_short_term_ref_pic_sets;
	
	private List<StRefPicSet> stRefPicSetList = new ArrayList<>();

	private int long_term_ref_pics_present_flag;

	private int num_long_term_ref_pics_sps;

	private int sps_temporal_mvp_enabled_flag;

	private int strong_intra_smoothing_enabled_flag;

	private int vui_parameters_present_flag;

	private H265VuiParameters vui_parameters;

	private int sps_extension_present_flag;

	private int sps_range_extension_flag;
	private int sps_multilayer_extension_flag;
	private int sps_3d_extension_flag;
	private int sps_scc_extension_flag; 
	

	private int sps_extension_4bits;






	public Seq_parameter_set_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		sps_video_parameter_set_id = bitSource.u(4);
		sps_max_sub_layers_minus1 = bitSource.u(3);
		sps_temporal_id_nesting_flag = bitSource.u(1);
		profile_tier_level = new ProfileTierLevel(1, sps_max_sub_layers_minus1, bitSource);
		sps_seq_parameter_set_id = bitSource.ue();
		chroma_format_idc = bitSource.ue();

		if( chroma_format_idc == 3 ){
			separate_colour_plane_flag = bitSource.u(1);
		}

		pic_width_in_luma_samples = bitSource.ue();
		pic_height_in_luma_samples = bitSource.ue();
		conformance_window_flag = bitSource.u(1);

		if( conformance_window_flag==1 ) {
			conf_win_left_offset = bitSource.ue();
			conf_win_right_offset = bitSource.ue();
			conf_win_top_offset = bitSource.ue();
			conf_win_bottom_offset = bitSource.ue();
		}

		bit_depth_luma_minus8 = bitSource.ue();
		bit_depth_chroma_minus8 = bitSource.ue();
		log2_max_pic_order_cnt_lsb_minus4 = bitSource.ue();
		sps_sub_layer_ordering_info_present_flag = bitSource.u(1);


		sps_max_dec_pic_buffering_minus1 = new int[sps_max_sub_layers_minus1+1];
		sps_max_num_reorder_pics = new int[sps_max_sub_layers_minus1+1];
		sps_max_latency_increase_plus1 = new int[sps_max_sub_layers_minus1+1];

		for (int i = ((sps_sub_layer_ordering_info_present_flag == 1) ? 0 : sps_max_sub_layers_minus1); i <= sps_max_sub_layers_minus1; i++) {
			sps_max_dec_pic_buffering_minus1[i] = bitSource.ue();
			sps_max_num_reorder_pics[i] = bitSource.ue();
			sps_max_latency_increase_plus1[i] = bitSource.ue();
		}

		log2_min_luma_coding_block_size_minus3 = bitSource.ue();
		log2_diff_max_min_luma_coding_block_size = bitSource.ue();
		log2_min_luma_transform_block_size_minus2 = bitSource.ue();
		log2_diff_max_min_luma_transform_block_size = bitSource.ue();
		max_transform_hierarchy_depth_inter = bitSource.ue();
		max_transform_hierarchy_depth_intra = bitSource.ue();
		scaling_list_enabled_flag = bitSource.u(1);

		if(scaling_list_enabled_flag==1) {
			sps_scaling_list_data_present_flag = bitSource.u(1);
			if( sps_scaling_list_data_present_flag==1){
				
				sps_scaling_list_data= new ScalingListData(bitSource);
			}
		}

		amp_enabled_flag = bitSource.u(1);
		sample_adaptive_offset_enabled_flag = bitSource.u(1);
		pcm_enabled_flag = bitSource.u(1);

		if(pcm_enabled_flag==1) {
			pcm_sample_bit_depth_luma_minus1 = bitSource.u(4);
			pcm_sample_bit_depth_chroma_minus1 = bitSource.u(4);
			log2_min_pcm_luma_coding_block_size_minus3 = bitSource.ue();
			log2_diff_max_min_pcm_luma_coding_block_size = bitSource.ue();
			pcm_loop_filter_disabled_flag = bitSource.u(1);
		}

		num_short_term_ref_pic_sets = bitSource.ue();
		if(num_short_term_ref_pic_sets!=0){
			for(int i = 0; i < num_short_term_ref_pic_sets; i++) {
				StRefPicSet st_ref_pic_set = new StRefPicSet(i, num_short_term_ref_pic_sets, bitSource);
				if(st_ref_pic_set.notImplemented) {
					return;
				}
				stRefPicSetList.add(st_ref_pic_set);
			}
		}
		long_term_ref_pics_present_flag = bitSource.u(1);
		if( long_term_ref_pics_present_flag==1 ) {
			num_long_term_ref_pics_sps = bitSource.ue();
			logger.warning("long_term_ref_pics_present==1 not implemented");
			//			for(int i = 0; i < num_long_term_ref_pics_sps; i++ ) {
			//				lt_ref_pic_poc_lsb_sps[ i ]= = bitSource.u(v);
			//				used_by_curr_pic_lt_sps_flag[ i ] = bitSource.u(1);
			//			}
		}
		sps_temporal_mvp_enabled_flag = bitSource.u(1);
		strong_intra_smoothing_enabled_flag = bitSource.u(1);
		vui_parameters_present_flag = bitSource.u(1);
		if(vui_parameters_present_flag==1){
			vui_parameters = new H265VuiParameters(sps_max_sub_layers_minus1,bitSource);
		}

		sps_extension_present_flag = bitSource.u(1);
		if( sps_extension_present_flag !=0) {
			sps_range_extension_flag = bitSource.u(1);
			sps_multilayer_extension_flag = bitSource.u(1);
			sps_3d_extension_flag = bitSource.u(1);
			sps_scc_extension_flag = bitSource.u(1);
			
			sps_extension_4bits = bitSource.u(6);
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("seq_parameter_set_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("sps_video_parameter_set_id",sps_video_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_max_sub_layers_minus1",sps_max_sub_layers_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_temporal_id_nesting_flag",sps_temporal_id_nesting_flag,null)));

		t.add(profile_tier_level.getJTreeNode(modus));
		t.add(new DefaultMutableTreeNode(new KVP("sps_seq_parameter_set_id",sps_seq_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("chroma_format_idc",chroma_format_idc,getChroma_format_idcString(chroma_format_idc))));


		if( chroma_format_idc == 3 ){
			t.add(new DefaultMutableTreeNode(new KVP("separate_colour_plane_flag",separate_colour_plane_flag,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("pic_width_in_luma_samples",pic_width_in_luma_samples,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_height_in_luma_samples",pic_height_in_luma_samples,null)));
		t.add(new DefaultMutableTreeNode(new KVP("conformance_window_flag",conformance_window_flag,null)));


		if( conformance_window_flag==1 ) {
			t.add(new DefaultMutableTreeNode(new KVP("conf_win_left_offset",conf_win_left_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("conf_win_right_offset",conf_win_right_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("conf_win_top_offset",conf_win_top_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("conf_win_bottom_offset",conf_win_bottom_offset,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("bit_depth_luma_minus8",bit_depth_luma_minus8,null)));
		t.add(new DefaultMutableTreeNode(new KVP("bit_depth_chroma_minus8",bit_depth_chroma_minus8,null)));
		t.add(new DefaultMutableTreeNode(new KVP("log2_max_pic_order_cnt_lsb_minus4",log2_max_pic_order_cnt_lsb_minus4,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_sub_layer_ordering_info_present_flag",sps_sub_layer_ordering_info_present_flag,null)));




		for (int i = ((sps_sub_layer_ordering_info_present_flag == 1) ? 0 : sps_max_sub_layers_minus1); i <= sps_max_sub_layers_minus1; i++) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_max_dec_pic_buffering_minus1["+i+"]",sps_max_dec_pic_buffering_minus1[i],null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_max_num_reorder_pics["+i+"]",sps_max_num_reorder_pics[i],null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_max_latency_increase_plus1["+i+"]",sps_max_latency_increase_plus1[i],null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("log2_min_luma_coding_block_size_minus3",log2_min_luma_coding_block_size_minus3,null)));
		t.add(new DefaultMutableTreeNode(new KVP("log2_diff_max_min_luma_coding_block_size",log2_diff_max_min_luma_coding_block_size,null)));
		t.add(new DefaultMutableTreeNode(new KVP("log2_min_luma_transform_block_size_minus2",log2_min_luma_transform_block_size_minus2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("log2_diff_max_min_luma_transform_block_size",log2_diff_max_min_luma_transform_block_size,null)));
		t.add(new DefaultMutableTreeNode(new KVP("max_transform_hierarchy_depth_inter",max_transform_hierarchy_depth_inter,null)));
		t.add(new DefaultMutableTreeNode(new KVP("max_transform_hierarchy_depth_intra",max_transform_hierarchy_depth_intra,null)));

		t.add(new DefaultMutableTreeNode(new KVP("scaling_list_enabled_flag",scaling_list_enabled_flag,null)));


		if(scaling_list_enabled_flag==1) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_scaling_list_data_present_flag_present_flag",sps_scaling_list_data_present_flag,null)));
			if(sps_scaling_list_data_present_flag==1){
				t.add(sps_scaling_list_data.getJTreeNode(modus));
			}
		}


		t.add(new DefaultMutableTreeNode(new KVP("amp_enabled_flag",amp_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sample_adaptive_offset_enabled_flag",sample_adaptive_offset_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pcm_enabled_flag",pcm_enabled_flag,null)));


		if(pcm_enabled_flag==1) {
			t.add(new DefaultMutableTreeNode(new KVP("pcm_sample_bit_depth_luma_minus1",pcm_sample_bit_depth_luma_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pcm_sample_bit_depth_chroma_minus1",pcm_sample_bit_depth_chroma_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("log2_min_pcm_luma_coding_block_size_minus3",log2_min_pcm_luma_coding_block_size_minus3,null)));
			t.add(new DefaultMutableTreeNode(new KVP("log2_diff_max_min_pcm_luma_coding_block_size",log2_diff_max_min_pcm_luma_coding_block_size,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pcm_loop_filter_disabled_flag",pcm_loop_filter_disabled_flag,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("num_short_term_ref_pic_sets",num_short_term_ref_pic_sets,null)));
		if (num_short_term_ref_pic_sets != 0) {
			Utils.addListJTree(t, stRefPicSetList, modus, "st_ref_pic_sets");

		}
		t.add(new DefaultMutableTreeNode(new KVP("long_term_ref_pics_present_flag",long_term_ref_pics_present_flag,null)));

		if( long_term_ref_pics_present_flag==1 ) {
			t.add(new DefaultMutableTreeNode(new KVP("num_long_term_ref_pics_sps",num_long_term_ref_pics_sps,null)));
			if(num_long_term_ref_pics_sps!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("num_long_term_ref_pics_sps!=0")));
				return t;
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_temporal_mvp_enabled_flag",sps_temporal_mvp_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("strong_intra_smoothing_enabled_flag",strong_intra_smoothing_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vui_parameters_present_flag",vui_parameters_present_flag,null)));
		if(vui_parameters_present_flag==1){
			t.add(vui_parameters.getJTreeNode(modus));
		}

		t.add(new DefaultMutableTreeNode(new KVP("sps_extension_present_flag",sps_extension_present_flag,null)));

		if( sps_extension_present_flag !=0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_range_extension_flag",sps_range_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_multilayer_extension_flag",sps_multilayer_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_3d_extension_flag",sps_3d_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_scc_extension_flag",sps_scc_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_extension_4bits",sps_extension_4bits,null)));

			if(sps_range_extension_flag!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("sps_range_extension()")));
				return t;
			}
			if(sps_multilayer_extension_flag!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("sps_multilayer_extension()")));
				return t;
			}
			if(sps_3d_extension_flag!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("sps_3d_extension()")));
				return t;
			}
			if(sps_scc_extension_flag!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("sps_scc_extension()")));
				return t;
			}
		}

		return t;
	}


}

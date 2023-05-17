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

package nl.digitalekabeltelevisie.data.mpeg.pes.video266;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

public class Seq_parameter_set_rbsp extends RBSP {
	
	public class RefPicListStruct implements TreeNode{
		
		private int listIdx;
		private int rplsIdx;



		// based on 7.3.10 Reference picture list structure syntax


		/**
		 * @param i
		 * @param j
		 * @param bitSource
		 */
		public RefPicListStruct(int listIdx, int rplsIdx, BitSource bitSource) {
			this.listIdx = listIdx;
			this.rplsIdx = rplsIdx;
			
			num_ref_entries[listIdx][rplsIdx] = bitSource.ue();
			
			final int num_ref_entries_listIdx_rplsIdx = num_ref_entries[listIdx][rplsIdx];
			
			if( sps_long_term_ref_pics_flag!=0 && rplsIdx < sps_num_ref_pic_lists[ listIdx ] && num_ref_entries_listIdx_rplsIdx > 0 ) {
				ltrp_in_header_flag[listIdx][rplsIdx] = bitSource.u(1);
			}
			
			
			inter_layer_ref_pic_flag[listIdx][rplsIdx] = new int[num_ref_entries_listIdx_rplsIdx];
			st_ref_pic_flag[listIdx][rplsIdx] = new int[num_ref_entries_listIdx_rplsIdx];
			abs_delta_poc_st[listIdx][rplsIdx] = new int[num_ref_entries_listIdx_rplsIdx];
			strp_entry_sign_flag[listIdx][rplsIdx] = new int[num_ref_entries_listIdx_rplsIdx];
			rpls_poc_lsb_lt[listIdx][rplsIdx] = new int[num_ref_entries_listIdx_rplsIdx];
			ilrp_idx[listIdx][rplsIdx] = new int[num_ref_entries_listIdx_rplsIdx];
			
			for (int i = 0; i < num_ref_entries_listIdx_rplsIdx; i++) {
				int j = 0;
				if (sps_inter_layer_prediction_enabled_flag != 0) {
					inter_layer_ref_pic_flag[listIdx][rplsIdx][i] = bitSource.u(1);
				}
				if (inter_layer_ref_pic_flag[listIdx][rplsIdx][i] == 0) {
					if (sps_long_term_ref_pics_flag != 0) {
						st_ref_pic_flag[listIdx][rplsIdx][i] = bitSource.u(1);
					}else {
						// default value when not present = 1
						// GRRRRRRRRRRRR
						//
						// 7.4.11 Reference picture list structure semantics
						// When inter_layer_ref_pic_flag[ listIdx ][ rplsIdx ][ i ] is equal to 0 
						// and st_ref_pic_flag[ listIdx ][ rplsIdx ][ i ] is not present, 
						// the value of st_ref_pic_flag[ listIdx ][ rplsIdx ][ i ] 
						// is inferred to be equal to 1.
						st_ref_pic_flag[listIdx][rplsIdx][i] = 1;
					}
					if (st_ref_pic_flag[listIdx][rplsIdx][i] != 0) {
						abs_delta_poc_st[listIdx][rplsIdx][i] = bitSource.ue();
						if (AbsDeltaPocSt(listIdx, rplsIdx, i) > 0) {
							strp_entry_sign_flag[listIdx][rplsIdx][i] = bitSource.u(1);
						}
					} else if (ltrp_in_header_flag[listIdx][rplsIdx] == 0)
						rpls_poc_lsb_lt[listIdx][rplsIdx][j++] = bitSource.u(sps_log2_max_pic_order_cnt_lsb_minus4 + 4);
				} else {
					ilrp_idx[listIdx][rplsIdx][i] = bitSource.ue();
				}
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ref_pic_list_struct(listIdx="+listIdx+", rplsIdx="+rplsIdx+")"));
			t.add(new DefaultMutableTreeNode(new KVP("num_ref_entries["+listIdx+"]["+rplsIdx+"]",num_ref_entries[listIdx][rplsIdx],null)));

			final int num_ref_entries_listIdx_rplsIdx = num_ref_entries[listIdx][rplsIdx];
			
			if( sps_long_term_ref_pics_flag!=0 && rplsIdx < sps_num_ref_pic_lists[ listIdx ] && num_ref_entries_listIdx_rplsIdx > 0 ) {
				t.add(new DefaultMutableTreeNode(new KVP("ltrp_in_header_flag["+listIdx+"]["+rplsIdx+"]",ltrp_in_header_flag[listIdx][rplsIdx],null)));
			}
			

			for (int i = 0; i < num_ref_entries_listIdx_rplsIdx; i++) {
				
				DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(new KVP("entry: "+i));
				t.add(entryNode);
				if (sps_inter_layer_prediction_enabled_flag != 0) {
					entryNode.add(new DefaultMutableTreeNode(new KVP("inter_layer_ref_pic_flag["+listIdx+"]["+rplsIdx+"]["+i+"]",inter_layer_ref_pic_flag[listIdx][rplsIdx][i],null)));
				}
				if (inter_layer_ref_pic_flag[listIdx][rplsIdx][i] == 0) {
					if (sps_long_term_ref_pics_flag != 0) {
						entryNode.add(new DefaultMutableTreeNode(new KVP("st_ref_pic_flag["+listIdx+"]["+rplsIdx+"]["+i+"]",st_ref_pic_flag[listIdx][rplsIdx][i],null)));
					}
					if (st_ref_pic_flag[listIdx][rplsIdx][i] != 0) {
						entryNode.add(new DefaultMutableTreeNode(new KVP("abs_delta_poc_st["+listIdx+"]["+rplsIdx+"]["+i+"]",abs_delta_poc_st[listIdx][rplsIdx][i],null)));
						if (AbsDeltaPocSt(listIdx, rplsIdx, i) > 0) {
							entryNode.add(new DefaultMutableTreeNode(new KVP("strp_entry_sign_flag["+listIdx+"]["+rplsIdx+"]["+i+"]",strp_entry_sign_flag[listIdx][rplsIdx][i],null)));
						}
					} else if (ltrp_in_header_flag[listIdx][rplsIdx] == 0)
						entryNode.add(new DefaultMutableTreeNode(new KVP("rpls_poc_lsb_lt["+listIdx+"]["+rplsIdx+"]["+i+"]",rpls_poc_lsb_lt[listIdx][rplsIdx][i],null)));
				} else {
					entryNode.add(new DefaultMutableTreeNode(new KVP("ilrp_idx["+listIdx+"]["+rplsIdx+"]["+i+"]",ilrp_idx[listIdx][rplsIdx][i],null)));
				}
			}

			
			return t;
		}
		
		
		
		// 7.4.11 Reference picture list structure semantics (150)
		private int AbsDeltaPocSt(int listIdx, int rplsIdx, int i) {
			
			if( ( sps_weighted_pred_flag!=0 || sps_weighted_bipred_flag!=0 ) && i != 0 ) {
				return abs_delta_poc_st[ listIdx ][ rplsIdx ][ i ];
			}
			return abs_delta_poc_st[ listIdx ][ rplsIdx ][ i ] + 1;
		}
	}


	public class GeneralTimingHrdParameters implements TreeNode {

		private int num_units_in_tick;
		private int time_scale;
		private int general_nal_hrd_params_present_flag;
		private int general_vcl_hrd_params_present_flag;
		private int general_same_pic_timing_in_all_ols_flag;
		private int general_du_hrd_params_present_flag;
		private int tick_divisor_minus2;
		private int bit_rate_scale;
		private int cpb_size_scale;
		private int cpb_size_du_scale;
		private int hrd_cpb_cnt_minus1;

		/**
		 * @param bitSource
		 */
		public GeneralTimingHrdParameters(BitSource bitSource) {
			num_units_in_tick = bitSource.u(32);
			time_scale = bitSource.u(32);
			general_nal_hrd_params_present_flag = bitSource.u(1);
			general_vcl_hrd_params_present_flag = bitSource.u(1);
			if (general_nal_hrd_params_present_flag != 0 || general_vcl_hrd_params_present_flag != 0) {
				general_same_pic_timing_in_all_ols_flag = bitSource.u(1);
				general_du_hrd_params_present_flag = bitSource.u(1);
				if (general_du_hrd_params_present_flag != 0) {
					tick_divisor_minus2 = bitSource.u(8);
				}
				bit_rate_scale = bitSource.u(4);
				cpb_size_scale = bitSource.u(4);
				if (general_du_hrd_params_present_flag != 0) {
					cpb_size_du_scale = bitSource.u(4);
				}
				hrd_cpb_cnt_minus1 = bitSource.ue();
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("general_timing_hrd_parameters"));


			t.add(new DefaultMutableTreeNode(new KVP("num_units_in_tick",num_units_in_tick,null)));
			t.add(new DefaultMutableTreeNode(new KVP("time_scale",time_scale,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_nal_hrd_params_present_flag",general_nal_hrd_params_present_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("general_vcl_hrd_params_present_flag",general_vcl_hrd_params_present_flag,null)));

			if (general_nal_hrd_params_present_flag != 0 || general_vcl_hrd_params_present_flag != 0) {
				t.add(new DefaultMutableTreeNode(new KVP("general_same_pic_timing_in_all_ols_flag",general_same_pic_timing_in_all_ols_flag,null)));
				t.add(new DefaultMutableTreeNode(new KVP("general_du_hrd_params_present_flag",general_du_hrd_params_present_flag,null)));
				if (general_du_hrd_params_present_flag != 0) {
					t.add(new DefaultMutableTreeNode(new KVP("tick_divisor_minus2",tick_divisor_minus2,null)));
				}
				t.add(new DefaultMutableTreeNode(new KVP("bit_rate_scale",bit_rate_scale,null)));
				t.add(new DefaultMutableTreeNode(new KVP("cpb_size_scale",cpb_size_scale,null)));
				if (general_du_hrd_params_present_flag != 0) {
					t.add(new DefaultMutableTreeNode(new KVP("cpb_size_du_scale",cpb_size_du_scale,null)));
				}
				t.add(new DefaultMutableTreeNode(new KVP("hrd_cpb_cnt_minus1",hrd_cpb_cnt_minus1,null)));
			}
			

			return t;
		}

	}

	public class OlsTimingHrdParameters implements TreeNode{

		private int firstSubLayer;
		private int maxSubLayersVal;
		private int[] fixed_pic_rate_general_flag;
		private int[] fixed_pic_rate_within_cvs_flag;
		private int[] elemental_duration_in_tc_minus1;
		private int[] low_delay_hrd_flag;
		private SublayerHRDparameters[] nal_sublayer_hrd_parameters;
		private SublayerHRDparameters[] vcl_sublayer_hrd_parameters;

		/**
		 * @param firstSubLayer
		 * @param sps_max_sublayers_minus1
		 * @param bitSource
		 */
		public OlsTimingHrdParameters(int firstSubLayer, int maxSubLayersVal, BitSource bitSource) {
			
			this.firstSubLayer = firstSubLayer;
			this.maxSubLayersVal = maxSubLayersVal;
			
			fixed_pic_rate_general_flag = new int[maxSubLayersVal+1];
			fixed_pic_rate_within_cvs_flag = new int[maxSubLayersVal+1];
			elemental_duration_in_tc_minus1 = new int[maxSubLayersVal+1];
			low_delay_hrd_flag = new int[maxSubLayersVal+1];
			
			nal_sublayer_hrd_parameters = new SublayerHRDparameters[maxSubLayersVal+1];
			vcl_sublayer_hrd_parameters = new SublayerHRDparameters[maxSubLayersVal+1];
			
			for (int i = firstSubLayer; i <= maxSubLayersVal; i++) {
				fixed_pic_rate_general_flag[i] = bitSource.u(1);
				if (fixed_pic_rate_general_flag[i] == 0) {
					fixed_pic_rate_within_cvs_flag[i] = bitSource.u(1);
				}else {
					
					// 7.4.6.2 OLS timing and HRD parameters semantics
					// GRRRR
					// When fixed_pic_rate_general_flag[ i ] is equal to 1, 
					// the value of fixed_pic_rate_within_cvs_flag[ i ] is inferred to be equal to 1.
					fixed_pic_rate_within_cvs_flag[i] = 1;
				}
				if (fixed_pic_rate_within_cvs_flag[i] != 0) {
					elemental_duration_in_tc_minus1[i] = bitSource.ue();
				}
				else if ((general_timing_hrd_parameters.general_nal_hrd_params_present_flag != 0
						|| general_timing_hrd_parameters.general_vcl_hrd_params_present_flag != 0)
						&& general_timing_hrd_parameters.hrd_cpb_cnt_minus1 == 0) {
					low_delay_hrd_flag[i] = bitSource.u(1);
				}

				 if( general_timing_hrd_parameters.general_nal_hrd_params_present_flag !=0) {
					 nal_sublayer_hrd_parameters[i] = new SublayerHRDparameters(i,bitSource);
				 }
				 if( general_timing_hrd_parameters.general_vcl_hrd_params_present_flag !=0) {
					 vcl_sublayer_hrd_parameters[i] = new SublayerHRDparameters(i,bitSource);
				 }
			}
		}

		

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("ols_timing_hrd_parameters(firstSubLayer="+firstSubLayer+", MaxSubLayersVal="+maxSubLayersVal+")"));
//
//			fixed_pic_rate_general_flag = new int[maxSubLayersVal+1];
//			fixed_pic_rate_within_cvs_flag = new int[maxSubLayersVal+1];
//			elemental_duration_in_tc_minus1 = new int[maxSubLayersVal+1];
//			low_delay_hrd_flag = new int[maxSubLayersVal+1];
//			
//			nal_sublayer_hrd_parameters = new SublayerHRDparameters[maxSubLayersVal+1];
//			vcl_sublayer_hrd_parameters = new SublayerHRDparameters[maxSubLayersVal+1];
			
			for (int i = firstSubLayer; i <= maxSubLayersVal; i++) {
				final DefaultMutableTreeNode fixed_pic_rate_general_flagNode = new DefaultMutableTreeNode(new KVP("fixed_pic_rate_general_flag["+i+"]",fixed_pic_rate_general_flag[i] ,null));
				t.add(fixed_pic_rate_general_flagNode);

				if (fixed_pic_rate_general_flag[i] == 0) {
					fixed_pic_rate_general_flagNode.add(new DefaultMutableTreeNode(new KVP("fixed_pic_rate_within_cvs_flag["+i+"]",fixed_pic_rate_within_cvs_flag[i] ,null)));
				}
				if (fixed_pic_rate_within_cvs_flag[i] != 0) {
					t.add(new DefaultMutableTreeNode(new KVP("elemental_duration_in_tc_minus1["+i+"]",elemental_duration_in_tc_minus1[i],null)));
				}
				else if ((general_timing_hrd_parameters.general_nal_hrd_params_present_flag != 0
						|| general_timing_hrd_parameters.general_vcl_hrd_params_present_flag != 0)
						&& general_timing_hrd_parameters.hrd_cpb_cnt_minus1 == 0) {
					t.add(new DefaultMutableTreeNode(new KVP("low_delay_hrd_flag["+i+"]",low_delay_hrd_flag[i],null)));
				}

				 if( general_timing_hrd_parameters.general_nal_hrd_params_present_flag !=0) {
					final DefaultMutableTreeNode general_nal_hrd_params_present_flagNode = new DefaultMutableTreeNode(new KVP("if( general_nal_hrd_params_present_flag)"));
					t.add(general_nal_hrd_params_present_flagNode);
					general_nal_hrd_params_present_flagNode.add(nal_sublayer_hrd_parameters[i].getJTreeNode(modus));
				 }
				 if( general_timing_hrd_parameters.general_vcl_hrd_params_present_flag !=0) {
						final DefaultMutableTreeNode general_vcl_hrd_params_present_flagNode = new DefaultMutableTreeNode(new KVP("if( general_vcl_hrd_params_present_flag)"));
						t.add(general_vcl_hrd_params_present_flagNode);
						general_vcl_hrd_params_present_flagNode.add(vcl_sublayer_hrd_parameters[i].getJTreeNode(modus));
				 }
			}
			
			return t;
		}

	}
	
	public class SublayerHRDparameters implements TreeNode{

		
		// these should be global, but in two versions, for vcl and nal
		private int[][] bit_rate_value_minus1;
		private int[][] cpb_size_value_minus1;
		private int[][] cpb_size_du_value_minus1;
		private int[][] bit_rate_du_value_minus1;
		private int[][] cbr_flag;

		private int subLayerId;
		/**
		 * @param i
		 * @param bitSource
		 */
		public SublayerHRDparameters(int subLayerId, BitSource bitSource) {
			this.subLayerId = subLayerId;
			
			bit_rate_value_minus1 = new int[subLayerId+1][general_timing_hrd_parameters.hrd_cpb_cnt_minus1+1];
			cpb_size_value_minus1 = new int[subLayerId+1][general_timing_hrd_parameters.hrd_cpb_cnt_minus1+1];
			cpb_size_du_value_minus1 = new int[subLayerId+1][general_timing_hrd_parameters.hrd_cpb_cnt_minus1+1];
			bit_rate_du_value_minus1 = new int[subLayerId+1][general_timing_hrd_parameters.hrd_cpb_cnt_minus1+1];
			cbr_flag = new int[subLayerId+1][general_timing_hrd_parameters.hrd_cpb_cnt_minus1+1];

			for (int j = 0; j <= general_timing_hrd_parameters.hrd_cpb_cnt_minus1; j++) {
				bit_rate_value_minus1[subLayerId][j] = bitSource.ue();
				cpb_size_value_minus1[subLayerId][j] = bitSource.ue();
				if (general_timing_hrd_parameters.general_du_hrd_params_present_flag != 0) {
					cpb_size_du_value_minus1[subLayerId][j] = bitSource.ue();
					bit_rate_du_value_minus1[subLayerId][j] = bitSource.ue();
				}
				cbr_flag[subLayerId][j] = bitSource.u(1);
			}

		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("sublayer_hrd_parameters( subLayerId="+subLayerId+")"));
			for (int j = 0; j <= general_timing_hrd_parameters.hrd_cpb_cnt_minus1; j++) {
				t.add(new DefaultMutableTreeNode(new KVP("bit_rate_value_minus1["+subLayerId+"]["+j+"]",bit_rate_value_minus1[subLayerId][j],null)));
				t.add(new DefaultMutableTreeNode(new KVP("cpb_size_value_minus1["+subLayerId+"]["+j+"]",cpb_size_value_minus1[subLayerId][j],null)));
				if (general_timing_hrd_parameters.general_du_hrd_params_present_flag != 0) {
					t.add(new DefaultMutableTreeNode(new KVP("cpb_size_du_value_minus1["+subLayerId+"]["+j+"]",cpb_size_du_value_minus1[subLayerId][j],null)));
					t.add(new DefaultMutableTreeNode(new KVP("bit_rate_du_value_minus1["+subLayerId+"]["+j+"]",bit_rate_du_value_minus1[subLayerId][j],null)));
				}
				t.add(new DefaultMutableTreeNode(new KVP("cbr_flag["+subLayerId+"]["+j+"]",cbr_flag[subLayerId][j],null)));
			}

			return t;
		}
		
	}

	
	private static final Logger	logger	= Logger.getLogger(Seq_parameter_set_rbsp.class.getName());

	// based on 7.3.2.4 Sequence parameter set RBSP syntax Rec. ITU-T H.266 (04/2022)

	private final int sps_seq_parameter_set_id;


	private final int sps_video_parameter_set_id;

	private final int sps_max_sublayers_minus1;

	private int sps_chroma_format_idc;

	private int sps_log2_ctu_size_minus5;

	private int sps_ptl_dpb_hrd_params_present_flag;

	private int sps_gdr_enabled_flag;

	private int sps_ref_pic_resampling_enabled_flag;

	private ProfileTierLevel profile_tier_level;

	private int sps_ref_pic_resampling_en;

	private int sps_res_change_in_clvs_allowed_flag;

	private int sps_pic_width_max_in_luma_samples;

	private int sps_pic_height_max_in_luma_samples;

	private int sps_conformance_window_flag;

	private int sps_conf_win_left_offset;

	private int sps_conf_win_right_offset;

	private int sps_conf_win_top_offset;

	private int sps_conf_win_bottom_offset;

	private int sps_subpic_info_present_flag;

	private int sps_num_subpics_minus1;

	private int sps_independent_subpics_flag;

	private int sps_subpic_same_size_flag;

	private int[] sps_subpic_ctu_top_left_x;
	private int[] sps_subpic_ctu_top_left_y;
	private int[] sps_subpic_width_minus1;
	private int[] sps_subpic_height_minus1;

	private int[] sps_subpic_treated_as_pic_flag;
	private int[] sps_loop_filter_across_subpic_enabled_flag;

	private int sps_subpic_id_len_minus1;
	private int sps_subpic_id_mapping_explicitly_signalled_flag;
	private int sps_subpic_id_mapping_present_flag;

	private int[] sps_subpic_id;

	private int sps_bitdepth_minus8;

	private int sps_entropy_coding_sync_enabled_flag;

	private int sps_entry_point_offsets_present_flag;

	private int sps_log2_max_pic_order_cnt_lsb_minus4;

	private int sps_poc_msb_cycle_flag;

	private int sps_poc_msb_cycle_len_minus1;

	private int sps_num_extra_ph_bytes;
	private int[] sps_extra_ph_bit_present_flag;

	private int sps_num_extra_sh_bytes;
	private int[] sps_extra_sh_bit_present_flag;

	private int sps_sublayer_dpb_params_flag;
	private DpdParameters dpb_parameters;

	private int sps_log2_min_luma_coding_block_size_minus2;
	private int sps_partition_constraints_override_enabled_flag;
	private int sps_log2_diff_min_qt_min_cb_intra_slice_luma;
	private int sps_max_mtt_hierarchy_depth_intra_slice_luma;

	private int sps_log2_diff_max_bt_min_qt_intra_slice_luma;
	private int sps_log2_diff_max_tt_min_qt_intra_slice_luma;

	private int sps_qtbtt_dual_tree_intra_flag;

	private int sps_log2_diff_min_qt_min_cb_intra_slice_chroma;
	private int sps_max_mtt_hierarchy_depth_intra_slice_chroma;
	private int sps_log2_diff_max_bt_min_qt_intra_slice_chroma;
	private int sps_log2_diff_max_tt_min_qt_intra_slice_chroma;

	private int sps_log2_diff_min_qt_min_cb_inter_slice;
	private int sps_max_mtt_hierarchy_depth_inter_slice;
	private int sps_log2_diff_max_bt_min_qt_inter_slice;
	private int sps_log2_diff_max_tt_min_qt_inter_slice;

	private int CtbLog2SizeY;
	private int CtbSizeY;

	private int sps_max_luma_transform_size_64_flag;

	private int sps_transform_skip_enabled_flag;
	private int sps_log2_transform_skip_max_size_minus2;
	private int sps_bdpcm_enabled_flag;

	private int sps_mts_enabled_flag;
	private int sps_explicit_mts_intra_enabled_flag;
	private int sps_explicit_mts_inter_enabled_flag;

	private int sps_lfnst_enabled_flag;

	private int sps_joint_cbcr_enabled_flag;

	private int sps_same_qp_table_for_chroma_flag;

	private int numQpTables;

	private int[] sps_qp_table_start_minus26;

	private int[] sps_num_points_in_qp_table_minus1;

	private int[][] sps_delta_qp_in_val_minus1;

	private int[][] sps_delta_qp_diff_val;

	private int bitsAvailable;

	private int sps_sao_enabled_flag;

	private int sps_alf_enabled_flag;

	private int sps_ccalf_enabled_flag;

	private int sps_lmcs_enabled_flag;

	private int sps_weighted_pred_flag;

	private int sps_weighted_bipred_flag;

	private int sps_long_term_ref_pics_flag;

	private int sps_inter_layer_prediction_enabled_flag;

	private int sps_idr_rpl_present_flag;

	private int sps_rpl1_same_as_rpl0_flag;

	private int[] sps_num_ref_pic_lists;

	private RefPicListStruct[][] refPicListStructList;
	
	private int[][] num_ref_entries;
	private int[][] ltrp_in_header_flag;
	private int[][][] inter_layer_ref_pic_flag;
	private int[][][] st_ref_pic_flag;
	private int[][][] abs_delta_poc_st;
	private int[][][] strp_entry_sign_flag;
	private int[][][] rpls_poc_lsb_lt;
	private int[][][] ilrp_idx;

	private int sps_ref_wraparound_enabled_flag;

	private int sps_temporal_mvp_enabled_flag;

	private int sps_sbtmvp_enabled_flag;

	private int sps_amvr_enabled_flag;

	private int sps_bdof_enabled_flag;

	private int sps_bdof_control_present_in_ph_flag;

	private int sps_smvd_enabled_flag;

	private int sps_dmvr_enabled_flag;

	private int sps_dmvr_control_present_in_ph_flag;

	private int sps_mmvd_enabled_flag;

	private int sps_mmvd_fullpel_only_enabled_flag;

	private int sps_six_minus_max_num_merge_cand;

	private int sps_sbt_enabled_flag;

	private int sps_affine_enabled_flag;

	private int sps_five_minus_max_num_subblock_merge_cand;

	private int sps_6param_affine_enabled_flag;

	private int sps_affine_amvr_enabled_flag;

	private int sps_affine_prof_enabled_flag;

	private int sps_prof_control_present_in_ph_flag;

	private int sps_bcw_enabled_flag;

	private int sps_ciip_enabled_flag;

	private int sps_gpm_enabled_flag;

	private int sps_max_num_merge_cand_minus_max_num_gpm_cand;

	private int sps_log2_parallel_merge_level_minus2;

	private int sps_isp_enabled_flag;

	private int sps_mrl_enabled_flag;

	private int sps_mip_enabled_flag;

	private int sps_cclm_enabled_flag;

	private int sps_chroma_horizontal_collocated_flag;

	private int sps_chroma_vertical_collocated_flag;

	private int sps_palette_enabled_flag;

	private int sps_act_enabled_flag;

	private int sps_min_qp_prime_ts;

	private int sps_ibc_enabled_flag;

	private int sps_six_minus_max_num_ibc_merge_cand;

	private int sps_ladf_enabled_flag;

	private int sps_num_ladf_intervals_minus2;

	private int sps_ladf_lowest_interval_qp_offset;

	private int[] sps_ladf_qp_offset;

	private int[] sps_ladf_delta_threshold_minus1;

	private int sps_explicit_scaling_list_enabled_flag;

	private int sps_scaling_matrix_for_lfnst_disabled_flag;

	private int sps_scaling_matrix_for_alternative_colour_space_disabled_flag;

	private int sps_scaling_matrix_designated_colour_space_flag;

	private int sps_dep_quant_enabled_flag;

	private int sps_sign_data_hiding_enabled_flag;

	private int sps_virtual_boundaries_enabled_flag;

	private int sps_virtual_boundaries_present_flag;

	private int sps_num_ver_virtual_boundaries;

	private int[] sps_virtual_boundary_pos_x_minus1;

	private int sps_num_hor_virtual_boundaries;

	private int[] sps_virtual_boundary_pos_y_minus1;

	private int sps_timing_hrd_params_present_flag;
	
	private GeneralTimingHrdParameters general_timing_hrd_parameters = null;

	private int sps_sublayer_cpb_params_present_flag;
	
	private OlsTimingHrdParameters ols_timing_hrd_parameters = null;

	private int sps_field_seq_flag;

	private int sps_vui_parameters_present_flag;

	private int sps_vui_payload_size_minus1;




	public Seq_parameter_set_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		sps_seq_parameter_set_id = bitSource.u(4);
		sps_video_parameter_set_id = bitSource.u(4);
		sps_max_sublayers_minus1 = bitSource.u(3);
		
		sps_chroma_format_idc = bitSource.u(2);
		sps_log2_ctu_size_minus5 = bitSource.u(2);
		sps_ptl_dpb_hrd_params_present_flag = bitSource.u(1);
		if( sps_ptl_dpb_hrd_params_present_flag ==1) {
			profile_tier_level = new ProfileTierLevel(1, sps_max_sublayers_minus1, bitSource);
		}
		
		sps_gdr_enabled_flag = bitSource.u(1);
		sps_ref_pic_resampling_enabled_flag = bitSource.u(1);
		if (sps_ref_pic_resampling_enabled_flag == 1) {
			sps_res_change_in_clvs_allowed_flag = bitSource.u(1);
		}
		sps_pic_width_max_in_luma_samples = bitSource.ue();
		sps_pic_height_max_in_luma_samples = bitSource.ue();
		sps_conformance_window_flag = bitSource.u(1);		
		
		if (sps_conformance_window_flag == 1) {
			sps_conf_win_left_offset = bitSource.ue();
			sps_conf_win_right_offset = bitSource.ue();
			sps_conf_win_top_offset = bitSource.ue();
			sps_conf_win_bottom_offset = bitSource.ue();
		}
		sps_subpic_info_present_flag = bitSource.u(1);
		
		if (sps_subpic_info_present_flag == 1) {
			logger.info("sps_subpic_info_present_flag == 1 not implemented");
//			sps_num_subpics_minus1 = bitSource.ue();
//			if (sps_num_subpics_minus1 > 0) {
//				sps_independent_subpics_flag = bitSource.u(1);
//				sps_subpic_same_size_flag = bitSource.u(1);
//			}
//			for (int i = 0; sps_num_subpics_minus1 > 0 && i <= sps_num_subpics_minus1; i++) {
//				if (sps_subpic_same_size_flag == 0 || i == 0) {
//					int CtbSizeY = 0;
//					if (i > 0 && sps_pic_width_max_in_luma_samples > CtbSizeY) {
//						sps_subpic_ctu_top_left_x[i] = bitSource.u(v);
//					}
//					if (i > 0 && sps_pic_height_max_in_luma_samples > CtbSizeY) {
//						sps_subpic_ctu_top_left_y[i] = bitSource.u(v);
//					}
//					if (i < sps_num_subpics_minus1 && sps_pic_width_max_in_luma_samples > CtbSizeY) {
//						sps_subpic_width_minus1[i] = bitSource.u(v);
//					}
//					if (i < sps_num_subpics_minus1 && sps_pic_height_max_in_luma_samples > CtbSizeY) {
//						sps_subpic_height_minus1[i] = bitSource.u(v);
//					}
//				}
//				if (sps_independent_subpics_flag == 0) {
//					sps_subpic_treated_as_pic_flag[i] = bitSource.u(1);
//					sps_loop_filter_across_subpic_enabled_flag[i] = bitSource.u(1);
//				}
//			}
//			sps_subpic_id_len_minus1 = bitSource.ue();
//			sps_subpic_id_mapping_explicitly_signalled_flag = bitSource.u(1);
//			if (sps_subpic_id_mapping_explicitly_signalled_flag == 1) {
//				sps_subpic_id_mapping_present_flag = bitSource.u(1);
//				if (sps_subpic_id_mapping_present_flag == 1) {
//					for (int i = 0; i <= sps_num_subpics_minus1; i++) {
//						sps_subpic_id[i] = bitSource.u(1);
//					}
//				}
//			}
		}
		sps_bitdepth_minus8 = bitSource.ue();
		sps_entropy_coding_sync_enabled_flag = bitSource.u(1);
		sps_entry_point_offsets_present_flag = bitSource.u(1);
		sps_log2_max_pic_order_cnt_lsb_minus4 = bitSource.u(4);
		sps_poc_msb_cycle_flag = bitSource.u(1);
		if (sps_poc_msb_cycle_flag == 1) {
			sps_poc_msb_cycle_len_minus1 = bitSource.ue();
		}

		sps_num_extra_ph_bytes = bitSource.u(2);
		sps_extra_ph_bit_present_flag = new int[sps_num_extra_ph_bytes * 8];
		for (int i = 0; i < (sps_num_extra_ph_bytes * 8); i++) {
			sps_extra_ph_bit_present_flag[i] = bitSource.u(1);
		}
		
		sps_num_extra_sh_bytes = bitSource.u(2);
		sps_extra_sh_bit_present_flag = new int[sps_num_extra_sh_bytes * 8];
		for (int i = 0; i < (sps_num_extra_sh_bytes * 8); i++) {
			sps_extra_sh_bit_present_flag[i] = bitSource.u(1);
		}
		
		
		if (sps_ptl_dpb_hrd_params_present_flag == 1) {
			if (sps_max_sublayers_minus1 > 0) {
				sps_sublayer_dpb_params_flag = bitSource.u(1);
			}
			dpb_parameters = new DpdParameters(sps_max_sublayers_minus1, sps_sublayer_dpb_params_flag, bitSource);
		}
		sps_log2_min_luma_coding_block_size_minus2 = bitSource.ue();
		sps_partition_constraints_override_enabled_flag = bitSource.u(1);
		sps_log2_diff_min_qt_min_cb_intra_slice_luma = bitSource.ue();
		sps_max_mtt_hierarchy_depth_intra_slice_luma = bitSource.ue();
		
		if (sps_max_mtt_hierarchy_depth_intra_slice_luma != 0) {
			sps_log2_diff_max_bt_min_qt_intra_slice_luma = bitSource.ue();
			sps_log2_diff_max_tt_min_qt_intra_slice_luma = bitSource.ue();
		}
		if (sps_chroma_format_idc != 0) {
			sps_qtbtt_dual_tree_intra_flag = bitSource.u(1);
		}
		if (sps_qtbtt_dual_tree_intra_flag == 1) {
			sps_log2_diff_min_qt_min_cb_intra_slice_chroma = bitSource.ue();
			sps_max_mtt_hierarchy_depth_intra_slice_chroma = bitSource.ue();
			if (sps_max_mtt_hierarchy_depth_intra_slice_chroma != 0) {
				sps_log2_diff_max_bt_min_qt_intra_slice_chroma = bitSource.ue();
				sps_log2_diff_max_tt_min_qt_intra_slice_chroma = bitSource.ue();
			}
		}
		
		sps_log2_diff_min_qt_min_cb_inter_slice = bitSource.ue();
		sps_max_mtt_hierarchy_depth_inter_slice = bitSource.ue();
		if (sps_max_mtt_hierarchy_depth_inter_slice != 0) {
			sps_log2_diff_max_bt_min_qt_inter_slice = bitSource.ue();
			sps_log2_diff_max_tt_min_qt_inter_slice = bitSource.ue();
		}
		
		// 7.4.3.4 Sequence parameter set RBSP semantics 
		CtbLog2SizeY = sps_log2_ctu_size_minus5 + 5; //  (35)
		CtbSizeY = 1 << CtbLog2SizeY ;  //(36
		
		if (CtbSizeY > 32) {
			sps_max_luma_transform_size_64_flag = bitSource.u(1);
		}

		sps_transform_skip_enabled_flag = bitSource.u(1);
		if (sps_transform_skip_enabled_flag == 1) {
			sps_log2_transform_skip_max_size_minus2 = bitSource.ue();
			sps_bdpcm_enabled_flag = bitSource.u(1);
		}
		sps_mts_enabled_flag = bitSource.u(1);
		if (sps_mts_enabled_flag == 1) {
			sps_explicit_mts_intra_enabled_flag = bitSource.u(1);
			sps_explicit_mts_inter_enabled_flag = bitSource.u(1);
		}
		
		sps_lfnst_enabled_flag = bitSource.u(1);
		if (sps_chroma_format_idc != 0) {
			sps_joint_cbcr_enabled_flag = bitSource.u(1);
			sps_same_qp_table_for_chroma_flag = bitSource.u(1);
			numQpTables = (sps_same_qp_table_for_chroma_flag != 0) ? 1 : (sps_joint_cbcr_enabled_flag != 0 ? 3 : 2);
			sps_qp_table_start_minus26 = new int[numQpTables];
			sps_num_points_in_qp_table_minus1 = new int[numQpTables];
			
			sps_delta_qp_in_val_minus1 = new int[numQpTables][];
			sps_delta_qp_diff_val = new int[numQpTables][];
			for (int i = 0; i < numQpTables; i++) {
				sps_qp_table_start_minus26[i] = bitSource.se();
				sps_num_points_in_qp_table_minus1[i] = bitSource.ue();
				sps_delta_qp_in_val_minus1[i] = new int[sps_num_points_in_qp_table_minus1[i]+1];
				sps_delta_qp_diff_val[i] = new int[sps_num_points_in_qp_table_minus1[i]+1];
				for (int j = 0; j <= sps_num_points_in_qp_table_minus1[i]; j++) {
					sps_delta_qp_in_val_minus1[i][j] = bitSource.ue();
					sps_delta_qp_diff_val[i][j] = bitSource.ue();
				}
			}
		}
		
		sps_sao_enabled_flag = bitSource.u(1);
		sps_alf_enabled_flag = bitSource.u(1);
		if (sps_alf_enabled_flag == 1 && sps_chroma_format_idc != 0) {
			sps_ccalf_enabled_flag = bitSource.u(1);		
		}
		
		
		sps_lmcs_enabled_flag = bitSource.u(1);
		sps_weighted_pred_flag = bitSource.u(1);
		sps_weighted_bipred_flag = bitSource.u(1);
		sps_long_term_ref_pics_flag = bitSource.u(1);
		if (sps_video_parameter_set_id > 0) {
			sps_inter_layer_prediction_enabled_flag = bitSource.u(1);
		}
		sps_idr_rpl_present_flag = bitSource.u(1);
		sps_rpl1_same_as_rpl0_flag = bitSource.u(1);		
		
		
		final int num_sps_lists = (sps_rpl1_same_as_rpl0_flag!=0) ? 1 : 2;
		
		refPicListStructList = new RefPicListStruct[num_sps_lists][];
		
		sps_num_ref_pic_lists = new int[num_sps_lists];
		
		num_ref_entries = new int[num_sps_lists][];
		ltrp_in_header_flag = new int[num_sps_lists][];
		inter_layer_ref_pic_flag = new int[num_sps_lists][][];
		st_ref_pic_flag = new int[num_sps_lists][][];
		abs_delta_poc_st = new int[num_sps_lists][][];
		strp_entry_sign_flag = new int[num_sps_lists][][];
		rpls_poc_lsb_lt = new int[num_sps_lists][][];
		ilrp_idx = new int[num_sps_lists][][];

		for(int i = 0; i < num_sps_lists; i++ ) {
			sps_num_ref_pic_lists[i] = bitSource.ue();
			int sps_num_ref_pic_lists_i = sps_num_ref_pic_lists[i];  //local helper
			refPicListStructList[i] = new RefPicListStruct[sps_num_ref_pic_lists_i];

			num_ref_entries[i] = new int[sps_num_ref_pic_lists_i];
			ltrp_in_header_flag[i] = new int[sps_num_ref_pic_lists_i];
			inter_layer_ref_pic_flag[i] = new int[sps_num_ref_pic_lists_i][];
			st_ref_pic_flag[i] = new int[sps_num_ref_pic_lists_i][];
			abs_delta_poc_st[i] = new int[sps_num_ref_pic_lists_i][];
			strp_entry_sign_flag[i] = new int[sps_num_ref_pic_lists_i][];
			rpls_poc_lsb_lt[i] = new int[sps_num_ref_pic_lists_i][];
			ilrp_idx[i] = new int[sps_num_ref_pic_lists_i][];

			for(int j = 0; j < sps_num_ref_pic_lists_i; j++) {
				RefPicListStruct ref_pic_list_struct = new RefPicListStruct( i, j ,bitSource);
				refPicListStructList[i][j]=ref_pic_list_struct;
			}
		}

		sps_ref_wraparound_enabled_flag = bitSource.u(1);
		sps_temporal_mvp_enabled_flag = bitSource.u(1);
		if (sps_temporal_mvp_enabled_flag != 0) {
			sps_sbtmvp_enabled_flag = bitSource.u(1);
		}
		sps_amvr_enabled_flag = bitSource.u(1);
		sps_bdof_enabled_flag = bitSource.u(1);
		if (sps_bdof_enabled_flag != 0) {
			sps_bdof_control_present_in_ph_flag = bitSource.u(1);
		}
		sps_smvd_enabled_flag = bitSource.u(1);
		sps_dmvr_enabled_flag = bitSource.u(1);
		if (sps_dmvr_enabled_flag != 0) {
			sps_dmvr_control_present_in_ph_flag = bitSource.u(1);
		}
		sps_mmvd_enabled_flag = bitSource.u(1);
		if (sps_mmvd_enabled_flag != 0) {
			sps_mmvd_fullpel_only_enabled_flag = bitSource.u(1);
		}
		
		
		sps_six_minus_max_num_merge_cand = bitSource.ue();
		sps_sbt_enabled_flag = bitSource.u(1);
		sps_affine_enabled_flag = bitSource.u(1);
		if (sps_affine_enabled_flag != 0) {
			sps_five_minus_max_num_subblock_merge_cand = bitSource.ue();
			sps_6param_affine_enabled_flag = bitSource.u(1);
			if (sps_amvr_enabled_flag != 0) {
				sps_affine_amvr_enabled_flag = bitSource.u(1);
			}
			sps_affine_prof_enabled_flag = bitSource.u(1);
			if (sps_affine_prof_enabled_flag != 0) {
				sps_prof_control_present_in_ph_flag = bitSource.u(1);
			}
		}
		sps_bcw_enabled_flag = bitSource.u(1);
		sps_ciip_enabled_flag = bitSource.u(1);

		int MaxNumMergeCand = 6 - sps_six_minus_max_num_merge_cand;
		
		if (MaxNumMergeCand >= 2) {
			sps_gpm_enabled_flag = bitSource.u(1);
			if (sps_gpm_enabled_flag != 0 && MaxNumMergeCand >= 3) {
				sps_max_num_merge_cand_minus_max_num_gpm_cand = bitSource.ue();
			}
		}
		
		sps_log2_parallel_merge_level_minus2 = bitSource.ue();
		sps_isp_enabled_flag = bitSource.u(1);
		sps_mrl_enabled_flag = bitSource.u(1);
		sps_mip_enabled_flag = bitSource.u(1);
		if (sps_chroma_format_idc != 0) {
			sps_cclm_enabled_flag = bitSource.u(1);
		}
		if (sps_chroma_format_idc == 1) {
			sps_chroma_horizontal_collocated_flag = bitSource.u(1);
			sps_chroma_vertical_collocated_flag = bitSource.u(1);
		}
			
		sps_palette_enabled_flag = bitSource.u(1);
		if (sps_chroma_format_idc == 3 && sps_max_luma_transform_size_64_flag == 0)
			sps_act_enabled_flag = bitSource.u(1);
		if (sps_transform_skip_enabled_flag != 0 || sps_palette_enabled_flag != 0) {
			sps_min_qp_prime_ts = bitSource.ue();
		}
		sps_ibc_enabled_flag = bitSource.u(1);
		if (sps_ibc_enabled_flag != 0) {
			sps_six_minus_max_num_ibc_merge_cand = bitSource.ue();
		}
		sps_ladf_enabled_flag = bitSource.u(1);
		
		
		
		if (sps_ladf_enabled_flag != 0) {
			sps_num_ladf_intervals_minus2 = bitSource.u(2);
			sps_ladf_lowest_interval_qp_offset = bitSource.se();
			
			sps_ladf_qp_offset = new int[sps_num_ladf_intervals_minus2 + 1];
			sps_ladf_delta_threshold_minus1 = new int[sps_num_ladf_intervals_minus2 + 1];
			for (int i = 0; i < sps_num_ladf_intervals_minus2 + 1; i++) {
				sps_ladf_qp_offset[i] = bitSource.se();
				sps_ladf_delta_threshold_minus1[i] = bitSource.ue();
			}
		}
		
		
		sps_explicit_scaling_list_enabled_flag = bitSource.u(1);
		if (sps_lfnst_enabled_flag != 0 && sps_explicit_scaling_list_enabled_flag != 0) {
			sps_scaling_matrix_for_lfnst_disabled_flag = bitSource.u(1);
		}
		if (sps_act_enabled_flag != 0 && sps_explicit_scaling_list_enabled_flag != 0) {
			sps_scaling_matrix_for_alternative_colour_space_disabled_flag = bitSource.u(1);
		}
		if (sps_scaling_matrix_for_alternative_colour_space_disabled_flag != 0) {
			sps_scaling_matrix_designated_colour_space_flag = bitSource.u(1);
		}
		sps_dep_quant_enabled_flag = bitSource.u(1);
		sps_sign_data_hiding_enabled_flag = bitSource.u(1);
		sps_virtual_boundaries_enabled_flag = bitSource.u(1);
		
		if (sps_virtual_boundaries_enabled_flag != 0) {
			sps_virtual_boundaries_present_flag = bitSource.u(1);
			if (sps_virtual_boundaries_present_flag != 0) {
				sps_num_ver_virtual_boundaries = bitSource.ue();
				sps_virtual_boundary_pos_x_minus1 = new int[sps_num_ver_virtual_boundaries];
				for (int i = 0; i < sps_num_ver_virtual_boundaries; i++) {
					sps_virtual_boundary_pos_x_minus1[i] = bitSource.ue();
				}
				sps_num_hor_virtual_boundaries = bitSource.ue();
				sps_virtual_boundary_pos_y_minus1 = new int[sps_num_hor_virtual_boundaries];
				for (int i = 0; i < sps_num_hor_virtual_boundaries; i++) {
					sps_virtual_boundary_pos_y_minus1[i] = bitSource.ue();
				}
			}
		}			
			
		
		if (sps_ptl_dpb_hrd_params_present_flag != 0) {
			sps_timing_hrd_params_present_flag = bitSource.u(1);
			if (sps_timing_hrd_params_present_flag != 0) {
				general_timing_hrd_parameters = new GeneralTimingHrdParameters(bitSource);
				if (sps_max_sublayers_minus1 > 0) {
					sps_sublayer_cpb_params_present_flag = bitSource.u(1);
				}
				int firstSubLayer = (sps_sublayer_cpb_params_present_flag != 0) ? 0 : sps_max_sublayers_minus1;
				ols_timing_hrd_parameters = new OlsTimingHrdParameters(firstSubLayer, sps_max_sublayers_minus1,
						bitSource);
			}
		}			
		sps_field_seq_flag = bitSource.u(1);
		sps_vui_parameters_present_flag = bitSource.u(1);
		if (sps_vui_parameters_present_flag != 0) {
			sps_vui_payload_size_minus1 = bitSource.ue();
		}		
		
		bitsAvailable = bitSource.available();

		System.err.println("bitsAvailable: "+bitsAvailable);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("seq_parameter_set_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("sps_seq_parameter_set_id",sps_seq_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_video_parameter_set_id",sps_video_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_max_sublayers_minus1",sps_max_sublayers_minus1,null)));

		t.add(new DefaultMutableTreeNode(new KVP("sps_chroma_format_idc",sps_chroma_format_idc,getSpsChromaFormatIdcString(sps_chroma_format_idc))));
		t.add(new DefaultMutableTreeNode(new KVP("sps_log2_ctu_size_minus5",sps_log2_ctu_size_minus5,null)));
		final DefaultMutableTreeNode sps_ptl_dpb_hrd_params_present_flagNode = new DefaultMutableTreeNode(new KVP("sps_ptl_dpb_hrd_params_present_flag",sps_ptl_dpb_hrd_params_present_flag,null));
		t.add(sps_ptl_dpb_hrd_params_present_flagNode);
		if( sps_ptl_dpb_hrd_params_present_flag ==1) {
			sps_ptl_dpb_hrd_params_present_flagNode.add(profile_tier_level.getJTreeNode(modus));
		}
		
		t.add(new DefaultMutableTreeNode(new KVP("sps_gdr_enabled_flag",sps_gdr_enabled_flag,null)));

		final DefaultMutableTreeNode sps_ref_pic_resampling_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_ref_pic_resampling_enabled_flag",sps_ref_pic_resampling_enabled_flag,null));
		t.add(sps_ref_pic_resampling_enabled_flagNode);
		if (sps_ref_pic_resampling_enabled_flag == 1) {
			sps_ref_pic_resampling_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_res_change_in_clvs_allowed_flag",sps_res_change_in_clvs_allowed_flag,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("sps_pic_width_max_in_luma_samples",sps_pic_width_max_in_luma_samples,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_pic_height_max_in_luma_samples",sps_pic_height_max_in_luma_samples,null)));
		final DefaultMutableTreeNode sps_conformance_window_flagNode = new DefaultMutableTreeNode(new KVP("sps_conformance_window_flag",sps_conformance_window_flag,null));
		t.add(sps_conformance_window_flagNode);

		
		if (sps_conformance_window_flag == 1) {
			sps_conformance_window_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_conf_win_left_offset",sps_conf_win_left_offset,null)));
			sps_conformance_window_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_conf_win_right_offset",sps_conf_win_right_offset,null)));
			sps_conformance_window_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_conf_win_top_offset",sps_conf_win_top_offset,null)));
			sps_conformance_window_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_conf_win_bottom_offset",sps_conf_win_bottom_offset,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_subpic_info_present_flag",sps_subpic_info_present_flag,null)));

		if( sps_subpic_info_present_flag ==1) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP(" sps_subpic_info_present_flag ==1")));
			return t;
		}
		
		
		t.add(new DefaultMutableTreeNode(new KVP("sps_bitdepth_minus8",sps_bitdepth_minus8,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_entropy_coding_sync_enabled_flag",sps_entropy_coding_sync_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_entry_point_offsets_present_flag",sps_entry_point_offsets_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_log2_max_pic_order_cnt_lsb_minus4",sps_log2_max_pic_order_cnt_lsb_minus4,null)));
		final DefaultMutableTreeNode sps_poc_msb_cycle_flagNode = new DefaultMutableTreeNode(new KVP("sps_poc_msb_cycle_flag",sps_poc_msb_cycle_flag,null));
		t.add(sps_poc_msb_cycle_flagNode);
		
		if (sps_poc_msb_cycle_flag == 1) {
			sps_poc_msb_cycle_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_poc_msb_cycle_len_minus1",sps_poc_msb_cycle_len_minus1,null)));
		}

		final DefaultMutableTreeNode sps_num_extra_ph_bytesNode = new DefaultMutableTreeNode(new KVP("sps_num_extra_ph_bytes",sps_num_extra_ph_bytes,null));
		t.add(sps_num_extra_ph_bytesNode);
		for (int i = 0; i < (sps_num_extra_ph_bytes * 8); i++) {
			sps_num_extra_ph_bytesNode.add(new DefaultMutableTreeNode(new KVP("sps_extra_ph_bit_present_flag["+i+"]",sps_extra_ph_bit_present_flag[i],null)));
		}
		
		final DefaultMutableTreeNode sps_num_extra_sh_bytesNode = new DefaultMutableTreeNode(new KVP("sps_num_extra_sh_bytes",sps_num_extra_sh_bytes,null));
		t.add(sps_num_extra_sh_bytesNode);
		for (int i = 0; i < (sps_num_extra_sh_bytes * 8); i++) {
			sps_num_extra_sh_bytesNode.add(new DefaultMutableTreeNode(new KVP("sps_extra_sh_bit_present_flag["+i+"]",sps_extra_sh_bit_present_flag[i],null)));
		}


		if (sps_ptl_dpb_hrd_params_present_flag == 1) {
			if (sps_max_sublayers_minus1 > 0) {
				t.add(new DefaultMutableTreeNode(new KVP("sps_sublayer_dpb_params_flag",sps_sublayer_dpb_params_flag,null)));
			}
			t.add(dpb_parameters.getJTreeNode(modus));
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_log2_min_luma_coding_block_size_minus2",sps_log2_min_luma_coding_block_size_minus2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_partition_constraints_override_enabled_flag",sps_partition_constraints_override_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_min_qt_min_cb_intra_slice_luma",sps_log2_diff_min_qt_min_cb_intra_slice_luma,null)));

		final DefaultMutableTreeNode sps_max_mtt_hierarchy_depth_intra_slice_lumaNode = new DefaultMutableTreeNode(new KVP("sps_max_mtt_hierarchy_depth_intra_slice_luma",sps_max_mtt_hierarchy_depth_intra_slice_luma,null));
		t.add(sps_max_mtt_hierarchy_depth_intra_slice_lumaNode);
		
		
		if (sps_max_mtt_hierarchy_depth_intra_slice_luma != 0) {
			sps_max_mtt_hierarchy_depth_intra_slice_lumaNode.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_max_bt_min_qt_intra_slice_luma",sps_log2_diff_max_bt_min_qt_intra_slice_luma,null)));
			sps_max_mtt_hierarchy_depth_intra_slice_lumaNode.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_max_tt_min_qt_intra_slice_luma",sps_log2_diff_max_tt_min_qt_intra_slice_luma,null)));
		}
		if (sps_chroma_format_idc != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_qtbtt_dual_tree_intra_flag",sps_qtbtt_dual_tree_intra_flag,null)));
		}
		if (sps_qtbtt_dual_tree_intra_flag == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_min_qt_min_cb_intra_slice_chroma",sps_log2_diff_min_qt_min_cb_intra_slice_chroma,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_max_mtt_hierarchy_depth_intra_slice_chroma",sps_max_mtt_hierarchy_depth_intra_slice_chroma,null)));
			if (sps_max_mtt_hierarchy_depth_intra_slice_chroma != 0) {
				t.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_max_bt_min_qt_intra_slice_chroma",sps_log2_diff_max_bt_min_qt_intra_slice_chroma,null)));
				t.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_max_tt_min_qt_intra_slice_chroma",sps_log2_diff_max_tt_min_qt_intra_slice_chroma,null)));
			}
		}

		t.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_min_qt_min_cb_inter_slice",sps_log2_diff_min_qt_min_cb_inter_slice,null)));
		final DefaultMutableTreeNode sps_max_mtt_hierarchy_depth_inter_sliceNode = new DefaultMutableTreeNode(new KVP("sps_max_mtt_hierarchy_depth_inter_slice",sps_max_mtt_hierarchy_depth_inter_slice,null));
		t.add(sps_max_mtt_hierarchy_depth_inter_sliceNode);
		
		if (sps_max_mtt_hierarchy_depth_inter_slice != 0) {
			sps_max_mtt_hierarchy_depth_inter_sliceNode.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_max_bt_min_qt_inter_slice",sps_log2_diff_max_bt_min_qt_inter_slice,null)));
			sps_max_mtt_hierarchy_depth_inter_sliceNode.add(new DefaultMutableTreeNode(new KVP("sps_log2_diff_max_tt_min_qt_inter_slice",sps_log2_diff_max_tt_min_qt_inter_slice,null)));
		}
		
		if (CtbSizeY > 32) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_max_luma_transform_size_64_flag",sps_max_luma_transform_size_64_flag,null)));
		}

		final DefaultMutableTreeNode sps_transform_skip_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_transform_skip_enabled_flag",sps_transform_skip_enabled_flag,null));
		t.add(sps_transform_skip_enabled_flagNode);
		if (sps_transform_skip_enabled_flag == 1) {
			sps_transform_skip_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_log2_transform_skip_max_size_minus2",sps_log2_transform_skip_max_size_minus2,null)));
			sps_transform_skip_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_bdpcm_enabled_flag",sps_bdpcm_enabled_flag,null)));
		}
		final DefaultMutableTreeNode sps_mts_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_mts_enabled_flag",sps_mts_enabled_flag,null));
		t.add(sps_mts_enabled_flagNode);
		if (sps_mts_enabled_flag == 1) {
			sps_mts_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_explicit_mts_intra_enabled_flag",sps_explicit_mts_intra_enabled_flag,null)));
			sps_mts_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_explicit_mts_inter_enabled_flag",sps_explicit_mts_inter_enabled_flag,null)));
		}

		
		t.add(new DefaultMutableTreeNode(new KVP("sps_lfnst_enabled_flag",sps_lfnst_enabled_flag,null)));
		if (sps_chroma_format_idc != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_joint_cbcr_enabled_flag",sps_joint_cbcr_enabled_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_same_qp_table_for_chroma_flag",sps_same_qp_table_for_chroma_flag,null)));

			for (int i = 0; i < numQpTables; i++) {
				t.add(new DefaultMutableTreeNode(new KVP("sps_qp_table_start_minus26["+i+"]" ,sps_qp_table_start_minus26[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("sps_num_points_in_qp_table_minus1["+i+"]" ,sps_num_points_in_qp_table_minus1[i],null)));
				for (int j = 0; j <= sps_num_points_in_qp_table_minus1[i]; j++) {
					t.add(new DefaultMutableTreeNode(new KVP("sps_delta_qp_in_val_minus1["+i+"]["+j+"]" ,sps_delta_qp_in_val_minus1[i][j],null)));
					t.add(new DefaultMutableTreeNode(new KVP("sps_delta_qp_diff_val["+i+"]["+j+"]" ,sps_delta_qp_diff_val[i][j],null)));
				}
			}
		}
		
		
		
		t.add(new DefaultMutableTreeNode(new KVP("sps_sao_enabled_flag",sps_sao_enabled_flag,null)));
		final DefaultMutableTreeNode sps_alf_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_alf_enabled_flag",sps_alf_enabled_flag,null));
		t.add(sps_alf_enabled_flagNode);
		if (sps_alf_enabled_flag == 1 && sps_chroma_format_idc != 0) {
			sps_alf_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_ccalf_enabled_flag",sps_ccalf_enabled_flag,null)));
		}


		t.add(new DefaultMutableTreeNode(new KVP("sps_lmcs_enabled_flag",sps_lmcs_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_weighted_pred_flag",sps_weighted_pred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_weighted_bipred_flag",sps_weighted_bipred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_long_term_ref_pics_flag",sps_long_term_ref_pics_flag,null)));
		
		if (sps_video_parameter_set_id > 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_inter_layer_prediction_enabled_flag",sps_inter_layer_prediction_enabled_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_idr_rpl_present_flag",sps_idr_rpl_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_rpl1_same_as_rpl0_flag",sps_rpl1_same_as_rpl0_flag,null)));

		
		final int num_sps_lists = (sps_rpl1_same_as_rpl0_flag!=0) ? 1 : 2;
		
		
		for(int i = 0; i < num_sps_lists; i++ ) {
			
			DefaultMutableTreeNode refPicList = new DefaultMutableTreeNode(new KVP("sps_num_ref_pic_lists["+i+"]",sps_num_ref_pic_lists[i] ,null));
			t.add(refPicList);

			for(int j = 0; j < sps_num_ref_pic_lists[i]; j++) {
				refPicList.add(refPicListStructList[i][j].getJTreeNode(modus));
			}
		}

		t.add(new DefaultMutableTreeNode(new KVP("sps_ref_wraparound_enabled_flag",sps_ref_wraparound_enabled_flag,null)));
		

		final DefaultMutableTreeNode sps_temporal_mvp_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_temporal_mvp_enabled_flag",sps_temporal_mvp_enabled_flag,null));
		t.add(sps_temporal_mvp_enabled_flagNode);
		if (sps_temporal_mvp_enabled_flag != 0) {
			sps_temporal_mvp_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_sbtmvp_enabled_flag",sps_sbtmvp_enabled_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_amvr_enabled_flag",sps_amvr_enabled_flag,null)));
		final DefaultMutableTreeNode sps_bdof_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_bdof_enabled_flag",sps_bdof_enabled_flag,null));
		t.add(sps_bdof_enabled_flagNode);
		if (sps_bdof_enabled_flag != 0) {
			sps_bdof_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_bdof_control_present_in_ph_flag",sps_bdof_control_present_in_ph_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_smvd_enabled_flag",sps_smvd_enabled_flag,null)));
		final DefaultMutableTreeNode sps_dmvr_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_dmvr_enabled_flag",sps_dmvr_enabled_flag,null));
		t.add(sps_dmvr_enabled_flagNode);
		if (sps_dmvr_enabled_flag != 0) {
			sps_dmvr_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_dmvr_control_present_in_ph_flag",sps_dmvr_control_present_in_ph_flag,null)));
		}
		final DefaultMutableTreeNode sps_mmvd_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_mmvd_enabled_flag",sps_mmvd_enabled_flag,null));
		t.add(sps_mmvd_enabled_flagNode);
		if (sps_mmvd_enabled_flag != 0) {
			sps_mmvd_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_mmvd_fullpel_only_enabled_flag",sps_mmvd_fullpel_only_enabled_flag,null)));
		}
		
		
		t.add(new DefaultMutableTreeNode(new KVP("sps_six_minus_max_num_merge_cand",sps_six_minus_max_num_merge_cand,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_sbt_enabled_flag",sps_sbt_enabled_flag,null)));
		final DefaultMutableTreeNode sps_affine_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_affine_enabled_flag",sps_affine_enabled_flag,null));
		t.add(sps_affine_enabled_flagNode);
		if (sps_affine_enabled_flag != 0) {
			sps_affine_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_five_minus_max_num_subblock_merge_cand",sps_five_minus_max_num_subblock_merge_cand,null)));
			final DefaultMutableTreeNode sps_6param_affine_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_6param_affine_enabled_flag",sps_6param_affine_enabled_flag,null));
			sps_affine_enabled_flagNode.add(sps_6param_affine_enabled_flagNode);
			if (sps_amvr_enabled_flag != 0) {
				sps_6param_affine_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_affine_amvr_enabled_flag",sps_affine_amvr_enabled_flag,null)));
			}
			final DefaultMutableTreeNode sps_affine_prof_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_affine_prof_enabled_flag",sps_affine_prof_enabled_flag,null));
			sps_affine_enabled_flagNode.add(sps_affine_prof_enabled_flagNode);
			if (sps_affine_prof_enabled_flag != 0) {
				sps_affine_prof_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_prof_control_present_in_ph_flag",sps_prof_control_present_in_ph_flag,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_bcw_enabled_flag",sps_bcw_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_ciip_enabled_flag",sps_ciip_enabled_flag,null)));

		int MaxNumMergeCand = 6 - sps_six_minus_max_num_merge_cand;
		
		if (MaxNumMergeCand >= 2) {
			final DefaultMutableTreeNode sps_gpm_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_gpm_enabled_flag",sps_gpm_enabled_flag,null));
			t.add(sps_gpm_enabled_flagNode);
			if (sps_gpm_enabled_flag != 0 && MaxNumMergeCand >= 3) {
				sps_gpm_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_max_num_merge_cand_minus_max_num_gpm_cand",sps_max_num_merge_cand_minus_max_num_gpm_cand,null)));
			}
		}
		

		t.add(new DefaultMutableTreeNode(new KVP("sps_log2_parallel_merge_level_minus2",sps_log2_parallel_merge_level_minus2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_isp_enabled_flag",sps_isp_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_mrl_enabled_flag",sps_mrl_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_mip_enabled_flag",sps_mip_enabled_flag,null)));


		if (sps_chroma_format_idc != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_cclm_enabled_flag",sps_cclm_enabled_flag,null)));
		}
		if (sps_chroma_format_idc == 1) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_chroma_horizontal_collocated_flag",sps_chroma_horizontal_collocated_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("sps_chroma_vertical_collocated_flag",sps_chroma_vertical_collocated_flag,null)));
		}

		
		
		t.add(new DefaultMutableTreeNode(new KVP("sps_palette_enabled_flag",sps_palette_enabled_flag,null)));
		if (sps_chroma_format_idc == 3 && sps_max_luma_transform_size_64_flag == 0)
			t.add(new DefaultMutableTreeNode(new KVP("sps_act_enabled_flag",sps_act_enabled_flag,null)));
		if (sps_transform_skip_enabled_flag != 0 || sps_palette_enabled_flag != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_min_qp_prime_ts",sps_min_qp_prime_ts,null)));
		}
		final DefaultMutableTreeNode sps_ibc_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_ibc_enabled_flag",sps_ibc_enabled_flag,null));
		t.add(sps_ibc_enabled_flagNode);
		if (sps_ibc_enabled_flag != 0) {
			sps_ibc_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_six_minus_max_num_ibc_merge_cand",sps_six_minus_max_num_ibc_merge_cand,null)));
		}
		final DefaultMutableTreeNode sps_ladf_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_ladf_enabled_flag",sps_ladf_enabled_flag,null));
		t.add(sps_ladf_enabled_flagNode);


		
		if (sps_ladf_enabled_flag != 0) {
			sps_ladf_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_num_ladf_intervals_minus2",sps_num_ladf_intervals_minus2,null)));
			sps_ladf_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_ladf_lowest_interval_qp_offset",sps_ladf_lowest_interval_qp_offset,null)));
			
			for (int i = 0; i < sps_num_ladf_intervals_minus2 + 1; i++) {
				sps_ladf_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_ladf_qp_offset["+i+"]",sps_ladf_qp_offset[i],null)));
				sps_ladf_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_ladf_delta_threshold_minus1["+i+"]",sps_ladf_delta_threshold_minus1[i],null)));
			}
		}
		
		final DefaultMutableTreeNode sps_explicit_scaling_list_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_explicit_scaling_list_enabled_flag",sps_explicit_scaling_list_enabled_flag,null));
		t.add(sps_explicit_scaling_list_enabled_flagNode);
		if (sps_lfnst_enabled_flag != 0 && sps_explicit_scaling_list_enabled_flag != 0) {
			sps_explicit_scaling_list_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_scaling_matrix_for_lfnst_disabled_flag",sps_scaling_matrix_for_lfnst_disabled_flag,null)));
		}
		if (sps_act_enabled_flag != 0 && sps_explicit_scaling_list_enabled_flag != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_scaling_matrix_for_alternative_colour_space_disabled_flag",sps_scaling_matrix_for_alternative_colour_space_disabled_flag,null)));
		}
		if (sps_scaling_matrix_for_alternative_colour_space_disabled_flag != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("sps_scaling_matrix_designated_colour_space_flag",sps_scaling_matrix_designated_colour_space_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("sps_dep_quant_enabled_flag",sps_dep_quant_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sps_sign_data_hiding_enabled_flag",sps_sign_data_hiding_enabled_flag,null)));
		final DefaultMutableTreeNode sps_virtual_boundaries_enabled_flagNode = new DefaultMutableTreeNode(new KVP("sps_virtual_boundaries_enabled_flag",sps_virtual_boundaries_enabled_flag,null));
		t.add(sps_virtual_boundaries_enabled_flagNode);

		
		
		if (sps_virtual_boundaries_enabled_flag != 0) {
			final DefaultMutableTreeNode sps_virtual_boundaries_present_flagNode = new DefaultMutableTreeNode(new KVP("sps_virtual_boundaries_present_flag",sps_virtual_boundaries_present_flag,null));
			sps_virtual_boundaries_enabled_flagNode.add(sps_virtual_boundaries_present_flagNode);
			if (sps_virtual_boundaries_present_flag != 0) {
				sps_virtual_boundaries_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_num_ver_virtual_boundaries",sps_num_ver_virtual_boundaries,null)));
				for (int i = 0; i < sps_num_ver_virtual_boundaries; i++) {
					sps_virtual_boundaries_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_virtual_boundary_pos_x_minus1["+i+"]",sps_virtual_boundary_pos_x_minus1[i],null)));
				}
				sps_virtual_boundaries_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_num_hor_virtual_boundaries",sps_num_hor_virtual_boundaries,null)));
				for (int i = 0; i < sps_num_hor_virtual_boundaries; i++) {
					sps_virtual_boundaries_enabled_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_virtual_boundary_pos_y_minus1["+i+"]",sps_virtual_boundary_pos_y_minus1[i],null)));
				}
			}
		}			

		
		if (sps_ptl_dpb_hrd_params_present_flag != 0) {
			final DefaultMutableTreeNode sps_timing_hrd_params_present_flagNode = new DefaultMutableTreeNode(new KVP("sps_timing_hrd_params_present_flag",sps_timing_hrd_params_present_flag,null));
			t.add(sps_timing_hrd_params_present_flagNode);
			if (sps_timing_hrd_params_present_flag != 0) {
				sps_timing_hrd_params_present_flagNode.add(general_timing_hrd_parameters.getJTreeNode(modus));
				if (sps_max_sublayers_minus1 > 0) {
					sps_timing_hrd_params_present_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_sublayer_cpb_params_present_flag",sps_sublayer_cpb_params_present_flag,null)));
				}
				sps_timing_hrd_params_present_flagNode.add(ols_timing_hrd_parameters.getJTreeNode(modus));
			}
		}			

		t.add(new DefaultMutableTreeNode(new KVP("sps_field_seq_flag",sps_field_seq_flag,null)));
		final DefaultMutableTreeNode sps_vui_parameters_present_flagNode = new DefaultMutableTreeNode(new KVP("sps_vui_parameters_present_flag",sps_vui_parameters_present_flag,null));
		t.add(sps_vui_parameters_present_flagNode);
		
		if (sps_vui_parameters_present_flag != 0) {
			sps_vui_parameters_present_flagNode.add(new DefaultMutableTreeNode(new KVP("sps_vui_payload_size_minus1",sps_vui_payload_size_minus1,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("bitsAvailable",bitsAvailable,null)));


		return t;
	}


	private static String getSpsChromaFormatIdcString(int sps_chroma_format_idc) {

		return switch (sps_chroma_format_idc) {
		case 0 -> "Monochrome";
		case 1 -> "4:2:0";
		case 2 -> "4:2:2";
		case 3 -> "4:4:4";
		default -> "Unexpected value: " + sps_chroma_format_idc;

		};
	}

}

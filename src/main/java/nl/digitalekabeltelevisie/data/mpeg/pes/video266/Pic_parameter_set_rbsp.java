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

import static nl.digitalekabeltelevisie.gui.utils.GuiUtils.getNotImplementedKVP;

import java.util.logging.Logger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.exception.ParsingNotImplementedException;

/**
 * @author Eric
 * 
 * Based on 7.3.2.5 Picture parameter set RBSP syntax Rec. ITU-T H.266 (04/2022)
 *
 */
public class Pic_parameter_set_rbsp extends RBSP {


	private static final String FOR_INT_I_FROM_0_TO_I_PPS_NUM_SLICES_IN_PIC_MINUS1 = "for(int i = 0; i < pps_num_slices_in_pic_minus1; i++ )";

	private static Logger	logger	= Logger.getLogger(Pic_parameter_set_rbsp.class.getName());

	// based on 7.3.2.3.1 General picture parameter set RBSP syntax Rec. ITU-T H.265 v2 (10/2014)
	private int pps_pic_parameter_set_id;
	private int pps_seq_parameter_set_id;
	private int pps_mixed_nalu_types_in_pic_flag;
	private int pps_pic_width_in_luma_samples;
	private int pps_pic_height_in_luma_samples;
	private int pps_conformance_window_flag;
	private int pps_conf_win_left_offset;
	private int pps_conf_win_right_offset;
	private int pps_conf_win_top_offset;
	private int pps_conf_win_bottom_offset;
	private int pps_scaling_window_explicit_signalling_flag;
	private int pps_scaling_win_left_offset;
	private int pps_scaling_win_right_offset;
	private int pps_scaling_win_top_offset;
	private int pps_scaling_win_bottom_offset;
	private int pps_output_flag_present_flag;
	private int pps_no_pic_partition_flag;
	private int pps_subpic_id_mapping_present_flag;
	private int pps_num_subpics_minus1;
	private int pps_subpic_id_len_minus1;
	private int[] pps_subpic_id;

//	private int pps_log2_ctu_size_minus5;
//
//	private int pps_num_exp_tile_columns_minus1;
//	private int pps_num_exp_tile_rows_minus1;
//	private int[] pps_tile_column_width_minus1;
//	private int[] pps_tile_row_height_minus1;
//	private int NumTilesInPic;
//	private int pps_loop_filter_across_tiles_enabled_flag;
//	private int pps_rect_slice_flag;

	private int pps_cabac_init_present_flag;
	private int[] pps_num_ref_idx_default_active_minus1 = new int[2];
	private int pps_rpl1_idx_present_flag;
	private int pps_weighted_pred_flag;
	private int pps_weighted_bipred_flag;
	private int pps_ref_wraparound_enabled_flag;
	private int pps_pic_width_minus_wraparound_offset;
	private int pps_init_qp_minus26;
	private int pps_cu_qp_delta_enabled_flag;
	private int pps_chroma_tool_offsets_present_flag;
	private int pps_cb_qp_offset;
	private int pps_cr_qp_offset;

	private int pps_joint_cbcr_qp_offset_present_flag;
	private int pps_joint_cbcr_qp_offset_value;
	private int pps_slice_chroma_qp_offsets_present_flag;
	private int pps_cu_chroma_qp_offset_list_enabled_flag;
	private int pps_chroma_qp_offset_list_len_minus1;

	private int[] pps_cb_qp_offset_list;
	private int[] pps_cr_qp_offset_list;
	private int[] pps_joint_cbcr_qp_offset_list;

	private int pps_deblocking_filter_control_present_flag;
	private int pps_deblocking_filter_override_enabled_flag;
	private int pps_deblocking_filter_disabled_flag;
	private int pps_dbf_info_in_ph_flag;
	private int pps_luma_beta_offset_div2;
	private int pps_luma_tc_offset_div2;
	private int pps_cb_beta_offset_div2;
	private int pps_cb_tc_offset_div2;
	private int pps_cr_beta_offset_div2;
	private int pps_cr_tc_offset_div2;

	private int pps_rpl_info_in_ph_flag;
	private int pps_sao_info_in_ph_flag;
	private int pps_alf_info_in_ph_flag;
	private int pps_wp_info_in_ph_flag;
	private int pps_qp_delta_info_in_ph_flag;
	private int pps_picture_header_extension_present_flag;
	private int pps_slice_header_extension_present_flag;
	private int pps_extension_flag;

	private int pps_log2_ctu_size_minus5;

	private int pps_num_exp_tile_columns_minus1;

	private int pps_num_exp_tile_rows_minus1;

	private int[] pps_tile_column_width_minus1;

	private int[] pps_tile_row_height_minus1;

	private int NumTilesInPic;

	private int pps_loop_filter_across_tiles_enabled_flag;

	// Rec. ITU-T H.266 (04/2022) 7.4.3.5 Picture parameter set RBSP semantics
	// When not present, the value of pps_rect_slice_flag is inferred to be equal to 1.
	
	private int pps_rect_slice_flag = 1;

	private int pps_single_slice_per_subpic_flag;

	private int pps_num_slices_in_pic_minus1;

	private int pps_tile_idx_delta_present_flag;

	private int pps_loop_filter_across_slices_enabled_flag;
	
	String parsingExceptionMsg;

	private int bitsAvailable;

	/**
	 * @param rbsp_bytes
	 * @param numBytesInRBSP
	 */
	public Pic_parameter_set_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
			try {
			pps_pic_parameter_set_id = bitSource.u(6);
			pps_seq_parameter_set_id = bitSource.u(4);
			pps_mixed_nalu_types_in_pic_flag = bitSource.u(1);
			pps_pic_width_in_luma_samples = bitSource.ue();
			pps_pic_height_in_luma_samples = bitSource.ue();
			pps_conformance_window_flag = bitSource.u(1);
			
			if (pps_conformance_window_flag == 1) {
				pps_conf_win_left_offset = bitSource.ue();
				pps_conf_win_right_offset = bitSource.ue();
				pps_conf_win_top_offset = bitSource.ue();
	
				pps_conf_win_bottom_offset = bitSource.ue();
			}
			pps_scaling_window_explicit_signalling_flag = bitSource.u(1);
			
			if (pps_scaling_window_explicit_signalling_flag == 1) {
				pps_scaling_win_left_offset = bitSource.se();
				pps_scaling_win_right_offset = bitSource.se();
				pps_scaling_win_top_offset = bitSource.se();
				pps_scaling_win_bottom_offset = bitSource.se();
			}
			pps_output_flag_present_flag = bitSource.u(1);
			pps_no_pic_partition_flag = bitSource.u(1);
			pps_subpic_id_mapping_present_flag = bitSource.u(1);
			
			if (pps_subpic_id_mapping_present_flag == 1) {
				if (pps_no_pic_partition_flag == 0) {
					pps_num_subpics_minus1 = bitSource.ue();
				}
				pps_subpic_id_len_minus1 = bitSource.ue();
				pps_subpic_id = new int[pps_num_subpics_minus1 + 1];
				for (int i = 0; i <= pps_num_subpics_minus1; i++) {
					int v = pps_subpic_id_len_minus1 + 1;
					pps_subpic_id[i] = bitSource.u(v);
				}
			}
			
			if (pps_no_pic_partition_flag == 0) {
				pps_log2_ctu_size_minus5 = bitSource.u(2);
				pps_num_exp_tile_columns_minus1 = bitSource.ue();
				pps_num_exp_tile_rows_minus1 = bitSource.ue();
				pps_tile_column_width_minus1 = new int[pps_num_exp_tile_columns_minus1 + 1];
				for (int i = 0; i <= pps_num_exp_tile_columns_minus1; i++) {
					pps_tile_column_width_minus1[i] = bitSource.ue();
				}
				pps_tile_row_height_minus1 = new int[pps_num_exp_tile_rows_minus1 + 1];
				for (int i = 0; i <= pps_num_exp_tile_rows_minus1; i++) {
					pps_tile_row_height_minus1[i] = bitSource.ue();
				}
				NumTilesInPic = (pps_num_exp_tile_columns_minus1 + 1) * (pps_num_exp_tile_rows_minus1 + 1);
				if (NumTilesInPic > 1) {
					pps_loop_filter_across_tiles_enabled_flag = bitSource.u(1);
					pps_rect_slice_flag = bitSource.u(1);
				}
	
				if (pps_rect_slice_flag != 0) {
					pps_single_slice_per_subpic_flag = bitSource.u(1);
				}
				
				if (pps_rect_slice_flag != 0 && pps_single_slice_per_subpic_flag == 0) {
					pps_num_slices_in_pic_minus1 = bitSource.ue();
					if (pps_num_slices_in_pic_minus1 > 1) {
						pps_tile_idx_delta_present_flag = bitSource.u(1);
					}
					// check not from spec, just to prevent entering for loop
					if(pps_num_slices_in_pic_minus1 >0) {
						logger.info(FOR_INT_I_FROM_0_TO_I_PPS_NUM_SLICES_IN_PIC_MINUS1 +" not supported");
						throw new ParsingNotImplementedException(FOR_INT_I_FROM_0_TO_I_PPS_NUM_SLICES_IN_PIC_MINUS1);
						
					}
	//				for(int i = 0; i < pps_num_slices_in_pic_minus1; i++ ) {
	//					if( SliceTopLeftTileIdx[ i ] % NumTileColumns != NumTileColumns − 1 )
	//						pps_slice_width_in_tiles_minus1[ i ] = bitSource.ue();
	//					if( SliceTopLeftTileIdx[ i ] / NumTileColumns != NumTileRows − 1 && ( pps_tile_idx_delta_present_flag | | SliceTopLeftTileIdx[ i ] % NumTileColumns = = 0 ) )
	//						pps_slice_height_in_tiles_minus1[ i ] = bitSource.ue();
	//					if( pps_slice_width_in_tiles_minus1[ i ] == 0 
	//							&& pps_slice_height_in_tiles_minus1[ i ] == 0 
	//							&& RowHeightVal[ SliceTopLeftTileIdx[ i ] / NumTileColumns ] > 1 ) {
	//						pps_num_exp_slices_in_tile[ i ] = bitSource.ue();
	//						for( j = 0; j < pps_num_exp_slices_in_tile[ i ]; j++ )
	//							pps_exp_slice_height_in_ctus_minus1[ i ][ j ] = bitSource.ue();
	//						i += NumSlicesInTile[ i ] − 1
	//					}
	//					if( pps_tile_idx_delta_present_flag && i < pps_num_slices_in_pic_minus1 )
	//						pps_tile_idx_delta_val[ i ] = bitSource.se();
	//				}
				}				
							
						
				if (pps_rect_slice_flag == 0 || pps_single_slice_per_subpic_flag != 0
						|| pps_num_slices_in_pic_minus1 > 0) {
					pps_loop_filter_across_slices_enabled_flag = bitSource.u(1);
				}
			}
			
			
			pps_cabac_init_present_flag = bitSource.u(1);
			for (int i = 0; i < 2; i++) {
				pps_num_ref_idx_default_active_minus1[i] = bitSource.ue();
			}
			pps_rpl1_idx_present_flag = bitSource.u(1);
			pps_weighted_pred_flag = bitSource.u(1);
			pps_weighted_bipred_flag = bitSource.u(1);
			pps_ref_wraparound_enabled_flag = bitSource.u(1);
			
			if (pps_ref_wraparound_enabled_flag == 1) {
				pps_pic_width_minus_wraparound_offset = bitSource.ue();
			}
			pps_init_qp_minus26 = bitSource.se();
			pps_cu_qp_delta_enabled_flag = bitSource.u(1);
			pps_chroma_tool_offsets_present_flag = bitSource.u(1);
			
			if (pps_chroma_tool_offsets_present_flag == 1) {
				pps_cb_qp_offset = bitSource.se();
				pps_cr_qp_offset = bitSource.se();
				pps_joint_cbcr_qp_offset_present_flag = bitSource.u(1);
				if (pps_joint_cbcr_qp_offset_present_flag == 1) {
					pps_joint_cbcr_qp_offset_value = bitSource.se();
				}
				pps_slice_chroma_qp_offsets_present_flag = bitSource.u(1);
				pps_cu_chroma_qp_offset_list_enabled_flag = bitSource.u(1);
				if (pps_cu_chroma_qp_offset_list_enabled_flag == 1) {
					pps_chroma_qp_offset_list_len_minus1 = bitSource.ue();
					pps_cb_qp_offset_list = new int[pps_chroma_qp_offset_list_len_minus1 + 1];
					pps_cr_qp_offset_list = new int[pps_chroma_qp_offset_list_len_minus1 + 1];
					pps_joint_cbcr_qp_offset_list = new int[pps_chroma_qp_offset_list_len_minus1 + 1];
					for (int i = 0; i <= pps_chroma_qp_offset_list_len_minus1; i++) {
						pps_cb_qp_offset_list[i] = bitSource.se();
						pps_cr_qp_offset_list[i] = bitSource.se();
						if (pps_joint_cbcr_qp_offset_present_flag == 1) {
							pps_joint_cbcr_qp_offset_list[i] = bitSource.se();
						}
					}
				}
			}
	
			pps_deblocking_filter_control_present_flag = bitSource.u(1);
			
			if (pps_deblocking_filter_control_present_flag == 1) {
				pps_deblocking_filter_override_enabled_flag = bitSource.u(1);
				pps_deblocking_filter_disabled_flag = bitSource.u(1);
				if (pps_no_pic_partition_flag == 0 && pps_deblocking_filter_override_enabled_flag == 1) {
					pps_dbf_info_in_ph_flag = bitSource.u(1);
				}
				if (pps_deblocking_filter_disabled_flag == 0) {
					pps_luma_beta_offset_div2 = bitSource.se();
					pps_luma_tc_offset_div2 = bitSource.se();
					if (pps_chroma_tool_offsets_present_flag == 1) {
						pps_cb_beta_offset_div2 = bitSource.se();
						pps_cb_tc_offset_div2 = bitSource.se();
						pps_cr_beta_offset_div2 = bitSource.se();
						pps_cr_tc_offset_div2 = bitSource.se();
					}
				}
	
			}
			
			if (pps_no_pic_partition_flag == 0) {
				pps_rpl_info_in_ph_flag = bitSource.u(1);
				pps_sao_info_in_ph_flag = bitSource.u(1);
				pps_alf_info_in_ph_flag = bitSource.u(1);
				if ((pps_weighted_pred_flag == 1 || pps_weighted_bipred_flag == 1) && pps_rpl_info_in_ph_flag == 1) {
					pps_wp_info_in_ph_flag = bitSource.u(1);
				}
				pps_qp_delta_info_in_ph_flag = bitSource.u(1);
			}
			pps_picture_header_extension_present_flag = bitSource.u(1);
			pps_slice_header_extension_present_flag = bitSource.u(1);
			pps_extension_flag = bitSource.u(1);		
			
			bitsAvailable = bitSource.available();
		} catch (ParsingNotImplementedException pni) {
			parsingExceptionMsg = pni.getMessage();
			logger.warning("Not implmented: "+parsingExceptionMsg);

		}
		
	}


	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("Pic_parameter_set_rbsp");
		t.add(new KVP("pps_pic_parameter_set_id",pps_pic_parameter_set_id));
		t.add(new KVP("pps_seq_parameter_set_id",pps_seq_parameter_set_id));
		t.add(new KVP("pps_mixed_nalu_types_in_pic_flag",pps_mixed_nalu_types_in_pic_flag));
		t.add(new KVP("pps_pic_width_in_luma_samples",pps_pic_width_in_luma_samples));
		t.add(new KVP("pps_pic_height_in_luma_samples",pps_pic_height_in_luma_samples));
		t.add(new KVP("pps_conformance_window_flag",pps_conformance_window_flag));

		if (pps_conformance_window_flag == 1) {
			t.add(new KVP("pps_conf_win_left_offset",pps_conf_win_left_offset));
			t.add(new KVP("pps_conf_win_right_offset",pps_conf_win_right_offset));
			t.add(new KVP("pps_conf_win_top_offset",pps_conf_win_top_offset));
			t.add(new KVP("pps_conf_win_bottom_offset",pps_conf_win_bottom_offset));
		}

		t.add(new KVP("pps_scaling_window_explicit_signalling_flag",pps_scaling_window_explicit_signalling_flag));

		if (pps_scaling_window_explicit_signalling_flag == 1) {
			t.add(new KVP("pps_scaling_win_left_offset",pps_scaling_win_left_offset));
			t.add(new KVP("pps_scaling_win_right_offset",pps_scaling_win_right_offset));
			t.add(new KVP("pps_scaling_win_top_offset",pps_scaling_win_top_offset));
			t.add(new KVP("pps_scaling_win_bottom_offset",pps_scaling_win_bottom_offset));
		}
		t.add(new KVP("pps_output_flag_present_flag",pps_output_flag_present_flag));
		t.add(new KVP("pps_no_pic_partition_flag",pps_no_pic_partition_flag));
		t.add(new KVP("pps_subpic_id_mapping_present_flag",pps_subpic_id_mapping_present_flag));

		if (pps_subpic_id_mapping_present_flag == 1) {
			if (pps_no_pic_partition_flag == 0) {
				t.add(new KVP("pps_num_subpics_minus1",pps_num_subpics_minus1));
			}
			t.add(new KVP("pps_subpic_id_len_minus1",pps_subpic_id_len_minus1));
			for (int i = 0; i <= pps_num_subpics_minus1; i++) {
				t.add(new KVP("pps_subpic_id["+i+"]",pps_subpic_id[i]));
			}
		}
		

		if (pps_no_pic_partition_flag == 0) {
			t.add(new KVP("pps_log2_ctu_size_minus5",pps_log2_ctu_size_minus5));
			t.add(new KVP("pps_num_exp_tile_columns_minus1",pps_num_exp_tile_columns_minus1));
			t.add(new KVP("pps_num_exp_tile_rows_minus1",pps_num_exp_tile_rows_minus1));
			
			for (int i = 0; i <= pps_num_exp_tile_columns_minus1; i++) {
				t.add(new KVP("pps_tile_column_width_minus1["+i+"]",pps_tile_column_width_minus1[i]));
			}
			for (int i = 0; i <= pps_num_exp_tile_rows_minus1; i++) {
				t.add(new KVP("pps_tile_row_height_minus1["+i+"]",pps_tile_row_height_minus1[i]));
			}
			if (NumTilesInPic > 1) {
				t.add(new KVP("pps_loop_filter_across_tiles_enabled_flag",pps_loop_filter_across_tiles_enabled_flag));
				t.add(new KVP("pps_rect_slice_flag",pps_rect_slice_flag));
			}
			
			if (pps_rect_slice_flag != 0) {
				t.add(new KVP("pps_single_slice_per_subpic_flag",pps_single_slice_per_subpic_flag));
			}

			if (pps_rect_slice_flag != 0 && pps_single_slice_per_subpic_flag == 0) {
				t.add(new KVP("pps_num_slices_in_pic_minus1",pps_num_slices_in_pic_minus1));
				if (pps_num_slices_in_pic_minus1 > 1) {
					t.add(new KVP("pps_tile_idx_delta_present_flag",pps_tile_idx_delta_present_flag));
				}

			}
			
			if (FOR_INT_I_FROM_0_TO_I_PPS_NUM_SLICES_IN_PIC_MINUS1.equals(parsingExceptionMsg)) {
				t.add(getNotImplementedKVP(FOR_INT_I_FROM_0_TO_I_PPS_NUM_SLICES_IN_PIC_MINUS1));
				return t;
			}
			
			if( pps_rect_slice_flag ==0 || pps_single_slice_per_subpic_flag !=0 || pps_num_slices_in_pic_minus1 > 0 ) {
				t.add(new KVP("pps_loop_filter_across_slices_enabled_flag",pps_loop_filter_across_slices_enabled_flag));
			}
		}

		
		t.add(new KVP("pps_cabac_init_present_flag",pps_cabac_init_present_flag));

		for (int i = 0; i < 2; i++) {
			t.add(new KVP("pps_num_ref_idx_default_active_minus1["+i+"]",pps_num_ref_idx_default_active_minus1[i]));
		}
		t.add(new KVP("pps_rpl1_idx_present_flag",pps_rpl1_idx_present_flag));
		t.add(new KVP("pps_weighted_pred_flag",pps_weighted_pred_flag));
		t.add(new KVP("pps_weighted_bipred_flag",pps_weighted_bipred_flag));
		t.add(new KVP("pps_ref_wraparound_enabled_flag",pps_ref_wraparound_enabled_flag));
		if (pps_ref_wraparound_enabled_flag == 1) {
			t.add(new KVP("pps_pic_width_minus_wraparound_offset",pps_pic_width_minus_wraparound_offset));
		}

		t.add(new KVP("pps_init_qp_minus26",pps_init_qp_minus26));
		t.add(new KVP("pps_cu_qp_delta_enabled_flag",pps_cu_qp_delta_enabled_flag));

		t.add(new KVP("pps_chroma_tool_offsets_present_flag",pps_chroma_tool_offsets_present_flag));

		if (pps_chroma_tool_offsets_present_flag == 1) {
			t.add(new KVP("pps_cb_qp_offset",pps_cb_qp_offset,"shall be in the range of −12 to 12, inclusive"));
			t.add(new KVP("pps_cr_qp_offset",pps_cr_qp_offset,"shall be in the range of −12 to 12, inclusive"));
			t.add(new KVP("pps_joint_cbcr_qp_offset_present_flag",pps_joint_cbcr_qp_offset_present_flag));
			if (pps_joint_cbcr_qp_offset_present_flag == 1) {
				t.add(new KVP("pps_joint_cbcr_qp_offset_value",pps_joint_cbcr_qp_offset_value,"shall be in the range of −12 to 12, inclusive"));
			}
			t.add(new KVP("pps_slice_chroma_qp_offsets_present_flag",pps_slice_chroma_qp_offsets_present_flag));
			t.add(new KVP("pps_cu_chroma_qp_offset_list_enabled_flag",pps_cu_chroma_qp_offset_list_enabled_flag));
			if (pps_cu_chroma_qp_offset_list_enabled_flag == 1) {
				t.add(new KVP("pps_chroma_qp_offset_list_len_minus1",pps_chroma_qp_offset_list_len_minus1,"shall be in the range of 0 to 5, inclusive"));
				for (int i = 0; i <= pps_chroma_qp_offset_list_len_minus1; i++) {
					t.add(new KVP("pps_cb_qp_offset_list["+i+"]",pps_cb_qp_offset_list[i],"shall be in the range of −12 to 12, inclusive"));
					t.add(new KVP("pps_cr_qp_offset_list["+i+"]",pps_cr_qp_offset_list[i],"shall be in the range of −12 to 12, inclusive"));
					if (pps_joint_cbcr_qp_offset_present_flag == 1) {
						t.add(new KVP("pps_joint_cbcr_qp_offset_list["+i+"]",pps_joint_cbcr_qp_offset_list[i],"shall be in the range of −12 to 12, inclusive"));
					}
				}
			}
		}

		t.add(new KVP("pps_deblocking_filter_control_present_flag",pps_deblocking_filter_control_present_flag));

		if (pps_deblocking_filter_control_present_flag == 1) {
			t.add(new KVP("pps_deblocking_filter_override_enabled_flag",pps_deblocking_filter_override_enabled_flag));
			t.add(new KVP("pps_deblocking_filter_disabled_flag",pps_deblocking_filter_disabled_flag));
			if (pps_no_pic_partition_flag == 0 && pps_deblocking_filter_override_enabled_flag == 1) {
				t.add(new KVP("pps_dbf_info_in_ph_flag",pps_dbf_info_in_ph_flag));
			}
			if (pps_deblocking_filter_disabled_flag == 0) {
				t.add(new KVP("pps_luma_beta_offset_div2",pps_luma_beta_offset_div2,"shall be in the range of −12 to 12, inclusive"));
				t.add(new KVP("pps_luma_tc_offset_div2",pps_luma_tc_offset_div2,"shall be in the range of −12 to 12, inclusive"));
				if (pps_chroma_tool_offsets_present_flag == 1) {
					t.add(new KVP("pps_cb_beta_offset_div2",pps_cb_beta_offset_div2,"shall be in the range of −12 to 12, inclusive"));
					t.add(new KVP("pps_cb_tc_offset_div2",pps_cb_tc_offset_div2,"shall be in the range of −12 to 12, inclusive"));
					t.add(new KVP("pps_cr_beta_offset_div2",pps_cr_beta_offset_div2,"shall be in the range of −12 to 12, inclusive"));
					t.add(new KVP("pps_cr_tc_offset_div2",pps_cr_tc_offset_div2,"shall be in the range of −12 to 12, inclusive"));
				}
			}

		}

		if (pps_no_pic_partition_flag == 0) {
			t.add(new KVP("pps_rpl_info_in_ph_flag",pps_rpl_info_in_ph_flag));
			t.add(new KVP("pps_sao_info_in_ph_flag",pps_sao_info_in_ph_flag));
			t.add(new KVP("pps_alf_info_in_ph_flag",pps_alf_info_in_ph_flag));
			if ((pps_weighted_pred_flag == 1 || pps_weighted_bipred_flag == 1) && pps_rpl_info_in_ph_flag == 1) {
				t.add(new KVP("pps_wp_info_in_ph_flag",pps_wp_info_in_ph_flag));
			}
			t.add(new KVP("pps_qp_delta_info_in_ph_flag",pps_qp_delta_info_in_ph_flag));
		}
		t.add(new KVP("pps_picture_header_extension_present_flag",pps_picture_header_extension_present_flag));
		t.add(new KVP("pps_slice_header_extension_present_flag",pps_slice_header_extension_present_flag));
		t.add(new KVP("pps_extension_flag",pps_extension_flag));
		
		t.add(new KVP("bitsAvailable",bitsAvailable));
		
		return t;

}

}

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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/**
 * @author Eric
 * 
 * Based on 7.3.2.3.1 General picture parameter set RBSP syntax Rec. ITU-T H.265 v5 (02/2018)
 *
 */
public class Pic_parameter_set_rbsp extends RBSP {


	// based on 7.3.2.3.1 General picture parameter set RBSP syntax Rec. ITU-T H.265 v2 (10/2014)
	private final int pps_pic_parameter_set_id;
	private final int ps_seq_parameter_set_id;
	private final int dependent_slice_segments_enabled_flag;
	private final int output_flag_present_flag;

	private final int num_extra_slice_header_bits;

	private final int sign_data_hiding_enabled_flag;

	private final int cabac_init_present_flag;

	private final int num_ref_idx_l0_default_active_minus1;

	private final int num_ref_idx_l1_default_active_minus1;

	private final int init_qp_minus26;

	private final int constrained_intra_pred_flag;

	private final int transform_skip_enabled_flag;

	private final int cu_qp_delta_enabled_flag;

	private int diff_cu_qp_delta_depth;

	private final int pps_cb_qp_offset;

	private final int pps_cr_qp_offset;

	private final int pps_slice_chroma_qp_offsets_present_flag;

	private final int weighted_pred_flag;

	private final int weighted_bipred_flag;

	private final int transquant_bypass_enabled_flag;

	private final int tiles_enabled_flag;

	private final int entropy_coding_sync_enabled_flag;

	private int num_tile_columns_minus1;

	private int num_tile_rows_minus1;

	private int uniform_spacing_flag;

	private int loop_filter_across_tiles_enabled_flag;

	private int[] column_width_minus1;

	private int[] row_height_minus1;

	private final int pps_loop_filter_across_slices_enabled_flag;

	private final int deblocking_filter_control_present_flag;

	private int deblocking_filter_override_enabled_flag;

	private int pps_deblocking_filter_disabled_flag;

	private int pps_beta_offset_div2;

	private int pps_tc_offset_div2;

	private final int pps_scaling_list_data_present_flag;
	
	private ScalingListData pps_scaling_list_data;

	private final int lists_modification_present_flag;

	private final int log2_parallel_merge_level_minus2;

	private final int slice_segment_header_extension_present_flag;

	private final int pps_extension_present_flag;
	private int pps_range_extension_flag;
	private int pps_multilayer_extension_flag;
	private int pps_3d_extension_flag;
	private int pps_scc_extension_flag ;
	private int pps_extension_4bits;

	


	/**
	 * @param rbsp_bytes
	 * @param numBytesInRBSP
	 */
	public Pic_parameter_set_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		pps_pic_parameter_set_id = bitSource.ue();
		ps_seq_parameter_set_id = bitSource.ue();
		dependent_slice_segments_enabled_flag = bitSource.u(1);
		output_flag_present_flag = bitSource.u(1);
		num_extra_slice_header_bits = bitSource.u(3);
		sign_data_hiding_enabled_flag = bitSource.u(1);
		cabac_init_present_flag = bitSource.u(1);
		num_ref_idx_l0_default_active_minus1 = bitSource.ue();
		num_ref_idx_l1_default_active_minus1 = bitSource.ue();
		init_qp_minus26 = bitSource.se();
		constrained_intra_pred_flag = bitSource.u(1);
		transform_skip_enabled_flag = bitSource.u(1);
		cu_qp_delta_enabled_flag = bitSource.u(1);
		if( cu_qp_delta_enabled_flag == 1 ){
			diff_cu_qp_delta_depth= bitSource.ue();
		}
		pps_cb_qp_offset = bitSource.se();
		pps_cr_qp_offset = bitSource.se();
		pps_slice_chroma_qp_offsets_present_flag = bitSource.u(1);
		weighted_pred_flag = bitSource.u(1);
		weighted_bipred_flag = bitSource.u(1);
		transquant_bypass_enabled_flag = bitSource.u(1);
		tiles_enabled_flag = bitSource.u(1);
		entropy_coding_sync_enabled_flag = bitSource.u(1);

		if( tiles_enabled_flag ==1) {
			num_tile_columns_minus1 = bitSource.ue();
			num_tile_rows_minus1 = bitSource.ue();
			uniform_spacing_flag = bitSource.u(1);
			if( uniform_spacing_flag ==0) {
				column_width_minus1 = new int [num_tile_columns_minus1];
				for(int i = 0; i < num_tile_columns_minus1; i++ ){
					column_width_minus1[ i ] = bitSource.ue();
				}
				row_height_minus1 = new int [num_tile_rows_minus1];
				for(int i = 0; i < num_tile_rows_minus1; i++ ){
					row_height_minus1[ i ] = bitSource.ue();
				}
			}
			loop_filter_across_tiles_enabled_flag = bitSource.u(1);
		}
		pps_loop_filter_across_slices_enabled_flag = bitSource.u(1);
		deblocking_filter_control_present_flag = bitSource.u(1);

		if (deblocking_filter_control_present_flag != 0) {
			deblocking_filter_override_enabled_flag = bitSource.u(1);
			pps_deblocking_filter_disabled_flag = bitSource.u(1);
			if (pps_deblocking_filter_disabled_flag == 0) {
				pps_beta_offset_div2 = bitSource.se();
				pps_tc_offset_div2 = bitSource.se();
			}
		}
		pps_scaling_list_data_present_flag = bitSource.u(1);

		if( pps_scaling_list_data_present_flag==1){
			pps_scaling_list_data = new ScalingListData(bitSource);
		}

		lists_modification_present_flag = bitSource.u(1);
		log2_parallel_merge_level_minus2 = bitSource.ue();
		slice_segment_header_extension_present_flag = bitSource.u(1);
		pps_extension_present_flag = bitSource.u(1);
		if( pps_extension_present_flag ==1) {
			pps_range_extension_flag = bitSource.u(1);
			pps_multilayer_extension_flag = bitSource.u(1);
			pps_3d_extension_flag = bitSource.u(1);
			pps_scc_extension_flag = bitSource.u(1);
			pps_extension_4bits = bitSource.u(4);
		}
	}


	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Pic_parameter_set_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("pps_pic_parameter_set_id",pps_pic_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("ps_seq_parameter_set_id",ps_seq_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("dependent_slice_segments_enabled_flag",dependent_slice_segments_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("output_flag_present_flag",output_flag_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_extra_slice_header_bits",num_extra_slice_header_bits,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sign_data_hiding_enabled_flag",sign_data_hiding_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("cabac_init_present_flag",cabac_init_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_ref_idx_l0_default_active_minus1",num_ref_idx_l0_default_active_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_ref_idx_l1_default_active_minus1",num_ref_idx_l1_default_active_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("init_qp_minus26",init_qp_minus26,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constrained_intra_pred_flag",constrained_intra_pred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transform_skip_enabled_flag",transform_skip_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("cu_qp_delta_enabled_flag",cu_qp_delta_enabled_flag,null)));
		if( cu_qp_delta_enabled_flag == 1 ){
			t.add(new DefaultMutableTreeNode(new KVP("diff_cu_qp_delta_depth",diff_cu_qp_delta_depth,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("pps_cb_qp_offset",pps_cb_qp_offset,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pps_cr_qp_offset",pps_cr_qp_offset,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pps_slice_chroma_qp_offsets_present_flag",pps_slice_chroma_qp_offsets_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("weighted_pred_flag",weighted_pred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("weighted_bipred_flag",weighted_bipred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("transquant_bypass_enabled_flag",transquant_bypass_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("tiles_enabled_flag",tiles_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("entropy_coding_sync_enabled_flag",entropy_coding_sync_enabled_flag,null)));


		if( tiles_enabled_flag ==1) {
			t.add(new DefaultMutableTreeNode(new KVP("num_tile_columns_minus1",num_tile_columns_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("num_tile_rows_minus1",num_tile_rows_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("uniform_spacing_flag",uniform_spacing_flag,null)));
			if( uniform_spacing_flag ==0) {
				for(int i = 0; i < num_tile_columns_minus1; i++ ){
					column_width_minus1[ i ] = bitSource.ue();
					t.add(new DefaultMutableTreeNode(new KVP("column_width_minus1["+i+"]",column_width_minus1[ i ],null)));
				}
				for(int i = 0; i < num_tile_rows_minus1; i++ ){
					row_height_minus1[ i ] = bitSource.ue();
					t.add(new DefaultMutableTreeNode(new KVP("row_height_minus1["+i+"]",row_height_minus1[ i ],null)));
				}
			}
			t.add(new DefaultMutableTreeNode(new KVP("loop_filter_across_tiles_enabled_flag",loop_filter_across_tiles_enabled_flag,null)));
		}

		t.add(new DefaultMutableTreeNode(new KVP("pps_loop_filter_across_slices_enabled_flag",pps_loop_filter_across_slices_enabled_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("deblocking_filter_control_present_flag",deblocking_filter_control_present_flag,null)));

		if (deblocking_filter_control_present_flag != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("deblocking_filter_override_enabled_flag",deblocking_filter_override_enabled_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pps_deblocking_filter_disabled_flag",pps_deblocking_filter_disabled_flag,null)));
			if (pps_deblocking_filter_disabled_flag == 0) {
				t.add(new DefaultMutableTreeNode(new KVP("pps_beta_offset_div2",pps_beta_offset_div2,null)));
				t.add(new DefaultMutableTreeNode(new KVP("pps_tc_offset_div2",pps_tc_offset_div2,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("pps_scaling_list_data_present_flag",pps_scaling_list_data_present_flag,null)));
		if(pps_scaling_list_data_present_flag==1){
			t.add(pps_scaling_list_data.getJTreeNode(modus));
		}

		t.add(new DefaultMutableTreeNode(new KVP("lists_modification_present_flag",lists_modification_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("log2_parallel_merge_level_minus2",log2_parallel_merge_level_minus2,null)));
		t.add(new DefaultMutableTreeNode(new KVP("slice_segment_header_extension_present_flag",slice_segment_header_extension_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pps_extension_present_flag",pps_extension_present_flag,null)));
		if( pps_extension_present_flag ==1) {
			t.add(new DefaultMutableTreeNode(new KVP("pps_range_extension_flag",pps_range_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pps_multilayer_extension_flag",pps_multilayer_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pps_3d_extension_flag",pps_3d_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pps_scc_extension_flag",pps_scc_extension_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("pps_extension_4bits",pps_extension_4bits,null)));
			if(pps_range_extension_flag==1){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("pps_range_extension()")));
				return t;
			}
			if(pps_multilayer_extension_flag==1){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("pps_multilayer_extension()")));
				return t;
			}
			if(pps_3d_extension_flag==1){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("pps_3d_extension()")));
				return t;
			}
			if(pps_scc_extension_flag==1){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("pps_scc_extension()")));
				return t;
			}
			if(pps_extension_4bits!=0){
				t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("ppps_extension_data_flag")));
				return t;
			}
		}
		return t;
	}

}

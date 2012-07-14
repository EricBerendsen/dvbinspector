package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

public class Pic_parameter_set_rbsp extends RBSP {
	
	 // based on 7.3.2.2 Picture parameter set RBSP syntax Rec. ITU-T H.264 (03/2010) – Prepublished version  

	private static Logger	logger	= Logger.getLogger(Pic_parameter_set_rbsp.class.getName());

	private int pic_parameter_set_id;
	private int seq_parameter_set_id;
	private int entropy_coding_mode_flag;
	private int pic_order_present_flag;
	private int num_slice_groups_minus1;
	
	
	private int num_ref_idx_l0_active_minus1;
	private int num_ref_idx_l1_active_minus1;
	private int weighted_pred_flag;
	private int weighted_bipred_idc;
	private int pic_init_qp_minus26;/* relative to 26 */ 
	private int pic_init_qs_minus26; /* relative to 26 */ 
	private int chroma_qp_index_offset;
	private int deblocking_filter_control_present_flag;
	private int constrained_intra_pred_flag;
	private int redundant_pic_cnt_present_flag;

	private int transform_8x8_mode_flag;
	private int pic_scaling_matrix_present_flag;

	private int [] seq_scaling_list_present_flag=new int [8];
	private int [][] delta_scale = new int [8][];
	private int [] deltas_read = new int[8];  // helper, does not match data in PES  

	private int second_chroma_qp_index_offset;

	
	protected Pic_parameter_set_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		pic_parameter_set_id = bitSource.ue();
		seq_parameter_set_id = bitSource.ue();
		entropy_coding_mode_flag = bitSource.u(1);
		pic_order_present_flag = bitSource.u(1);
		num_slice_groups_minus1 = bitSource.ue();
		
		if(num_slice_groups_minus1 > 0) {
			// TODO
			logger.warning(" (num_slice_groups_minus1 > 0 not implemented");

//			slice_group_map_type 1 ue(v)
//			if( slice_group_map_type = = 0 )
//			for( iGroup = 0; iGroup <= num_slice_groups_minus1; iGroup++ )
//			run_length_minus1[ iGroup ] 1 ue(v)
//			else if( slice_group_map_type = = 2 )
//			for( iGroup = 0; iGroup < num_slice_groups_minus1; iGroup++ ) {
//			top_left[ iGroup ] 1 ue(v)
//			bottom_right[ iGroup ] 1 ue(v)
//			}
//			else if( slice_group_map_type = = 3 | |
//			slice_group_map_type = = 4 | |
//			slice_group_map_type = = 5 ) {
//			slice_group_change_direction_flag 1 u(1)
//			slice_group_change_rate_minus1 1 ue(v)
//			} else if( slice_group_map_type = = 6 ) {
//			pic_size_in_map_units_minus1 1 ue(v)
//			for( i = 0; i <= pic_size_in_map_units_minus1; i++ )
//			slice_group_id[ i ] 1 u(v)
//			}
		}
		num_ref_idx_l0_active_minus1 = bitSource.ue();
		num_ref_idx_l1_active_minus1 = bitSource.ue();
		weighted_pred_flag = bitSource.u(1);
		weighted_bipred_idc = bitSource.u(2);
		pic_init_qp_minus26  = bitSource.se();/* relative to 26 */ 
		pic_init_qs_minus26  = bitSource.se(); /* relative to 26 */ 
		chroma_qp_index_offset  = bitSource.se();
		deblocking_filter_control_present_flag = bitSource.u(1);
		constrained_intra_pred_flag  = bitSource.u(1);
		redundant_pic_cnt_present_flag  = bitSource.u(1);
		if(bitSource.available()>=10){ // TODO  how to handle if( more_rbsp_data( ) ) { 
			// need at least 3 bits for 3 fields, make sure entire byte is available
			transform_8x8_mode_flag = bitSource.u(1);
			pic_scaling_matrix_present_flag = bitSource.u(1);
			if( pic_scaling_matrix_present_flag!=0 ){
				for( int i = 0; i < 6 + 2* transform_8x8_mode_flag; i++ ) {
					seq_scaling_list_present_flag[ i ] =bitSource.u(1);
					if( seq_scaling_list_present_flag[ i ]!=0 ){
						if( i < 6 ){
							delta_scale[i] = new int[16];
							deltas_read[i] = scaling_list( delta_scale[i], 16,bitSource);
						}else{
							delta_scale[i] = new int[64];
							deltas_read[i] = scaling_list( delta_scale[i], 64,bitSource);
						}
					}
				}
			}

			second_chroma_qp_index_offset = bitSource.se();
		}
		
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("pic_parameter_set_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("pic_parameter_set_id",pic_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("seq_parameter_set_id",seq_parameter_set_id,null)));
		t.add(new DefaultMutableTreeNode(new KVP("entropy_coding_mode_flag",entropy_coding_mode_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_order_present_flag",pic_order_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_slice_groups_minus1",num_slice_groups_minus1,null)));

		t.add(new DefaultMutableTreeNode(new KVP("num_ref_idx_l0_active_minus1",num_ref_idx_l0_active_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_ref_idx_l1_active_minus1",num_ref_idx_l1_active_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("weighted_pred_flag",weighted_pred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("weighted_bipred_idc",weighted_bipred_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_init_qp_minus26",pic_init_qp_minus26,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_init_qs_minus26",pic_init_qs_minus26,null)));
		t.add(new DefaultMutableTreeNode(new KVP("chroma_qp_index_offset",chroma_qp_index_offset,null)));
		t.add(new DefaultMutableTreeNode(new KVP("deblocking_filter_control_present_flag",deblocking_filter_control_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constrained_intra_pred_flag",constrained_intra_pred_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("redundant_pic_cnt_present_flag",redundant_pic_cnt_present_flag,null)));

		t.add(new DefaultMutableTreeNode(new KVP("transform_8x8_mode_flag",transform_8x8_mode_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_scaling_matrix_present_flag",pic_scaling_matrix_present_flag,null)));
		if( pic_scaling_matrix_present_flag!=0 ){
			for( int i = 0; i < 6 + 2* transform_8x8_mode_flag; i++ ) {
				t.add(new DefaultMutableTreeNode(new KVP("seq_scaling_list_present_flag["+i+"]",seq_scaling_list_present_flag[ i ],null)));
				if( seq_scaling_list_present_flag[ i ]!=0 ){
					if( i < 6 ){
						t.add(getScalingListJTree( delta_scale[i], i, 16,deltas_read[i]));
					}else{
						t.add(getScalingListJTree( delta_scale[i], i, 64,deltas_read[i]));
					}
				}
			}

		}
		t.add(new DefaultMutableTreeNode(new KVP("second_chroma_qp_index_offset",second_chroma_qp_index_offset,null)));

		return t;
	}

	public int getPic_parameter_set_id() {
		return pic_parameter_set_id;
	}

	public int getSeq_parameter_set_id() {
		return seq_parameter_set_id;
	}

	public int getEntropy_coding_mode_flag() {
		return entropy_coding_mode_flag;
	}

	public int getPic_order_present_flag() {
		return pic_order_present_flag;
	}

	public int getNum_slice_groups_minus1() {
		return num_slice_groups_minus1;
	}

	public int getNum_ref_idx_l0_active_minus1() {
		return num_ref_idx_l0_active_minus1;
	}

	public int getNum_ref_idx_l1_active_minus1() {
		return num_ref_idx_l1_active_minus1;
	}

	public int getWeighted_pred_flag() {
		return weighted_pred_flag;
	}

	public int getWeighted_bipred_idc() {
		return weighted_bipred_idc;
	}

	public int getPic_init_qp_minus26() {
		return pic_init_qp_minus26;
	}

	public int getPic_init_qs_minus26() {
		return pic_init_qs_minus26;
	}

	public int getChroma_qp_index_offset() {
		return chroma_qp_index_offset;
	}

	public int getDeblocking_filter_control_present_flag() {
		return deblocking_filter_control_present_flag;
	}

	public int getConstrained_intra_pred_flag() {
		return constrained_intra_pred_flag;
	}

	public int getRedundant_pic_cnt_present_flag() {
		return redundant_pic_cnt_present_flag;
	}

	public int getTransform_8x8_mode_flag() {
		return transform_8x8_mode_flag;
	}

	public int getPic_scaling_matrix_present_flag() {
		return pic_scaling_matrix_present_flag;
	}

	public int[] getSeq_scaling_list_present_flag() {
		return seq_scaling_list_present_flag;
	}

	public int[][] getDelta_scale() {
		return delta_scale;
	}

	public int[] getDeltas_read() {
		return deltas_read;
	}

	public int getSecond_chroma_qp_index_offset() {
		return second_chroma_qp_index_offset;
	}

}

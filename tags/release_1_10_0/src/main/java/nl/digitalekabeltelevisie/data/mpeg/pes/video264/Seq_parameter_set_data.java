package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

public class Seq_parameter_set_data {

	private static final Logger	logger	= Logger.getLogger(Seq_parameter_set_data.class.getName());

	private int bit_depth_chroma_minus8 ;
	private int bit_depth_luma_minus8;
	private int chroma_format_idc;
	private final int constraint_set0_flag;
	private final int constraint_set1_flag;
	private final int constraint_set2_flag;
	private final int constraint_set3_flag;
	private final int constraint_set4_flag;
	private final int constraint_set5_flag;
	private final int [][] delta_scale = new int [8][];
	private final int [] deltas_read = new int[8];  // helper, does not match data in PES
	private final int direct_8x8_inference_flag;
	private int frame_crop_bottom_offset;
	private int frame_crop_left_offset;
	private int frame_crop_right_offset;
	private int frame_crop_top_offset;
	private final int frame_cropping_flag;
	private final int frame_mbs_only_flag;
	private final int gaps_in_frame_num_value_allowed_flag;
	private final int level_idc;
	private final int log2_max_frame_num_minus4;
	private int log2_max_pic_order_cnt_lsb_minus4;
	private final int max_num_ref_frames;
	private int mb_adaptive_frame_field_flag;
	private final int pic_height_in_map_units_minus1;
	private final int pic_order_cnt_type;
	private final int pic_width_in_mbs_minus1;
	// based on 7.3.2.1.1 Sequence parameter set data syntax Rec. ITU-T H.264 (03/2010) – Prepublished version
	private final int profile_idc;
	private int qpprime_y_zero_transform_bypass_flag;
	private final int reserved_zero_2bits;  // http://www.cardinalpeak.com/blog/?p=878
	private int separate_colour_plane_flag;
	private final int seq_parameter_set_id;
	private final int [] seq_scaling_list_present_flag=new int [8];
	private int seq_scaling_matrix_present_flag ;
	private VuiParameters vui_parameters;
	private final int vui_parameters_present_flag;


	public Seq_parameter_set_data(BitSource bitSource) {
		profile_idc = bitSource.u(8);
		constraint_set0_flag = bitSource.u(1);
		constraint_set1_flag = bitSource.u(1);
		constraint_set2_flag = bitSource.u(1);
		constraint_set3_flag = bitSource.u(1);
		constraint_set4_flag = bitSource.u(1);
		constraint_set5_flag = bitSource.u(1);

		reserved_zero_2bits = bitSource.u(2);
		level_idc = bitSource.u(8);
		seq_parameter_set_id = bitSource.ue();
		if( (profile_idc == 100) || (profile_idc == 110) ||
				(profile_idc == 122) || (profile_idc == 144) || (profile_idc == 44) ||
				(profile_idc == 83) || (profile_idc == 86) || (profile_idc == 118) ||
				(profile_idc == 128) ) {
			chroma_format_idc = bitSource.ue();
			if( chroma_format_idc == 3 ){
				separate_colour_plane_flag =bitSource.u(1);
			}
			bit_depth_luma_minus8 =bitSource.ue();
			bit_depth_chroma_minus8 =bitSource.ue();
			qpprime_y_zero_transform_bypass_flag=bitSource.u(1);

			seq_scaling_matrix_present_flag =bitSource.u(1);
			if( seq_scaling_matrix_present_flag==1 ){
				for( int i = 0; i < ( ( chroma_format_idc != 3 ) ? 8 : 12 ); i++ ) { // Rec. ITU-T H.264 (03/2010) 7.3.2.1.1 Sequence parameter set data syntax
					seq_scaling_list_present_flag[ i ] =bitSource.u(1);
					if( seq_scaling_list_present_flag[ i ]!=0 ){
						if( i < 6 ){
							delta_scale[i] = new int[16];
							deltas_read[i] = RBSP.scaling_list( delta_scale[i], 16,bitSource);
						}else{
							delta_scale[i] = new int[64];
							deltas_read[i] = RBSP.scaling_list( delta_scale[i], 64,bitSource);
						}
					}
				}
			}
		}

		log2_max_frame_num_minus4 = bitSource.ue();
		pic_order_cnt_type = bitSource.ue();

		if(pic_order_cnt_type == 0){
			log2_max_pic_order_cnt_lsb_minus4 = bitSource.ue();
		}else if( pic_order_cnt_type == 1 ) {
			logger.warning(" pic_order_cnt_type == 1 not implemented");
//			delta_pic_order_always_zero_flag 0 u(1)
//			offset_for_non_ref_pic 0 se(v)
//			offset_for_top_to_bottom_field 0 se(v)
//			num_ref_frames_in_pic_order_cnt_cycle 0 ue(v)
//			for( i = 0; i < num_ref_frames_in_pic_order_cnt_cycle; i++ )
//			offset_for_ref_frame[ i ] 0 se(v)
		}
		max_num_ref_frames = bitSource.ue();
		gaps_in_frame_num_value_allowed_flag =bitSource.u(1);
		pic_width_in_mbs_minus1 = bitSource.ue();
		pic_height_in_map_units_minus1 = bitSource.ue();
		frame_mbs_only_flag =bitSource.u(1);
		if(frame_mbs_only_flag==0 ){
			mb_adaptive_frame_field_flag=bitSource.u(1);
		}
		direct_8x8_inference_flag=bitSource.u(1);
		frame_cropping_flag=bitSource.u(1);
		if( frame_cropping_flag!=0 ) {
			frame_crop_left_offset = bitSource.ue();
			frame_crop_right_offset = bitSource.ue();
			frame_crop_top_offset = bitSource.ue();
			frame_crop_bottom_offset = bitSource.ue();
		}
		vui_parameters_present_flag=bitSource.u(1);
		if(vui_parameters_present_flag!=0){
			vui_parameters = new VuiParameters(bitSource);
		}
	}

	public void addToJTree(DefaultMutableTreeNode t, int modus) {
		t.add(new DefaultMutableTreeNode(new KVP("profile_idc",profile_idc,RBSP.getProfileIdcString(profile_idc))));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set0_flag",constraint_set0_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set1_flag",constraint_set1_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set2_flag",constraint_set2_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set3_flag",constraint_set3_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set4_flag",constraint_set4_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("constraint_set5_flag",constraint_set5_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_zero_2bits",reserved_zero_2bits,null)));
		t.add(new DefaultMutableTreeNode(new KVP("level_idc",level_idc,null)));
		t.add(new DefaultMutableTreeNode(new KVP("seq_parameter_set_id",seq_parameter_set_id,null)));
		if( (profile_idc == 100) || (profile_idc == 110) ||
				(profile_idc == 122) || (profile_idc == 244) || (profile_idc == 44) ||
				(profile_idc == 83) || (profile_idc == 86) || (profile_idc == 118) ||
				(profile_idc == 128) ) {
			t.add(new DefaultMutableTreeNode(new KVP("chroma_format_idc",chroma_format_idc,RBSP.getChroma_format_idcString(chroma_format_idc))));
			if( chroma_format_idc == 3 ){
				t.add(new DefaultMutableTreeNode(new KVP("separate_colour_plane_flag",separate_colour_plane_flag,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("bit_depth_luma_minus8",bit_depth_luma_minus8,null)));
			t.add(new DefaultMutableTreeNode(new KVP("bit_depth_chroma_minus8",bit_depth_chroma_minus8,null)));
			t.add(new DefaultMutableTreeNode(new KVP("qpprime_y_zero_transform_bypass_flag",qpprime_y_zero_transform_bypass_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("seq_scaling_matrix_present_flag",seq_scaling_matrix_present_flag,null)));

			if( seq_scaling_matrix_present_flag==1 ){
				for( int i = 0; i < ( ( chroma_format_idc != 3 ) ? 8 : 12 ); i++ ) { // Rec. ITU-T H.264 (03/2010) 7.3.2.1.1 Sequence parameter set data syntax
					t.add(new DefaultMutableTreeNode(new KVP("seq_scaling_list_present_flag["+i+"]",seq_scaling_list_present_flag[ i ],null)));
					if( seq_scaling_list_present_flag[ i ]!=0 ){
						if( i < 6 ){
							t.add(RBSP.getScalingListJTree( delta_scale[i], i, 16,deltas_read[i]));
						}else{
							t.add(RBSP.getScalingListJTree( delta_scale[i], i, 64,deltas_read[i]));
						}
					}
				}
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("log2_max_frame_num_minus4",log2_max_frame_num_minus4,"MaxFrameNum="+BitSource.powerOf2[log2_max_frame_num_minus4+4])));
		t.add(new DefaultMutableTreeNode(new KVP("pic_order_cnt_type",pic_order_cnt_type,null)));

		if(pic_order_cnt_type == 0){
			t.add(new DefaultMutableTreeNode(new KVP("log2_max_pic_order_cnt_lsb_minus4",log2_max_pic_order_cnt_lsb_minus4,null)));
		}else if( pic_order_cnt_type == 1 ) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("pic_order_cnt_type == 1")));

		}

		t.add(new DefaultMutableTreeNode(new KVP("max_num_ref_frames",max_num_ref_frames,null)));
		t.add(new DefaultMutableTreeNode(new KVP("gaps_in_frame_num_value_allowed_flag",gaps_in_frame_num_value_allowed_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("pic_width_in_mbs_minus1",pic_width_in_mbs_minus1,"PicWidthInSamples="+(16*(pic_width_in_mbs_minus1+1)))));
		t.add(new DefaultMutableTreeNode(new KVP("pic_height_in_map_units_minus1",pic_height_in_map_units_minus1,"PicHeightInSamples="+(( 2-frame_mbs_only_flag ) * (pic_height_in_map_units_minus1 + 1)*16))));
		t.add(new DefaultMutableTreeNode(new KVP("frame_mbs_only_flag",frame_mbs_only_flag,frame_mbs_only_flag==0?"coded pictures of the coded video sequence may either be coded fields or coded frames":"every coded picture of the coded video sequence is a coded frame containing only frame macroblocks")));
		if(frame_mbs_only_flag==0 ){
			t.add(new DefaultMutableTreeNode(new KVP("mb_adaptive_frame_field_flag",mb_adaptive_frame_field_flag,mb_adaptive_frame_field_flag==0?"no switching between frame and field macroblocks within a picture":"possible use of switching between frame and field macroblocks within frames" )));
		}

		t.add(new DefaultMutableTreeNode(new KVP("direct_8x8_inference_flag",direct_8x8_inference_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("frame_cropping_flag",frame_cropping_flag,null)));

		if( frame_cropping_flag!=0 ) {
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_left_offset",frame_crop_left_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_right_offset",frame_crop_right_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_top_offset",frame_crop_top_offset,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_crop_bottom_offset",frame_crop_bottom_offset,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("vui_parameters_present_flag",vui_parameters_present_flag,null)));
		if(vui_parameters_present_flag!=0){
			t.add(vui_parameters.getJTreeNode(modus));
		}

		
	}

	public int getProfile_idc() {
		return profile_idc;
	}

	public int getConstraint_set0_flag() {
		return constraint_set0_flag;
	}

	public int getConstraint_set1_flag() {
		return constraint_set1_flag;
	}

	public int getConstraint_set2_flag() {
		return constraint_set2_flag;
	}

	public int getConstraint_set3_flag() {
		return constraint_set3_flag;
	}

	public int getConstraint_set4_flag() {
		return constraint_set4_flag;
	}

	public int getConstraint_set5_flag() {
		return constraint_set5_flag;
	}

	public int getReserved_zero_2bits() {
		return reserved_zero_2bits;
	}

	public int getLevel_idc() {
		return level_idc;
	}

	public int getSeq_parameter_set_id() {
		return seq_parameter_set_id;
	}

	public int getChroma_format_idc() {
		return chroma_format_idc;
	}

	public int getSeparate_colour_plane_flag() {
		return separate_colour_plane_flag;
	}

	public int getBit_depth_luma_minus8() {
		return bit_depth_luma_minus8;
	}

	public int getBit_depth_chroma_minus8() {
		return bit_depth_chroma_minus8;
	}

	public int getQpprime_y_zero_transform_bypass_flag() {
		return qpprime_y_zero_transform_bypass_flag;
	}

	public int getSeq_scaling_matrix_present_flag() {
		return seq_scaling_matrix_present_flag;
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

	public int getLog2_max_frame_num_minus4() {
		return log2_max_frame_num_minus4;
	}

	public int getPic_order_cnt_type() {
		return pic_order_cnt_type;
	}

	public int getLog2_max_pic_order_cnt_lsb_minus4() {
		return log2_max_pic_order_cnt_lsb_minus4;
	}

	public int getMax_num_ref_frames() {
		return max_num_ref_frames;
	}

	public int getGaps_in_frame_num_value_allowed_flag() {
		return gaps_in_frame_num_value_allowed_flag;
	}

	public int getPic_width_in_mbs_minus1() {
		return pic_width_in_mbs_minus1;
	}

	public int getPic_height_in_map_units_minus1() {
		return pic_height_in_map_units_minus1;
	}

	public int getFrame_mbs_only_flag() {
		return frame_mbs_only_flag;
	}

	public int getMb_adaptive_frame_field_flag() {
		return mb_adaptive_frame_field_flag;
	}

	public int getDirect_8x8_inference_flag() {
		return direct_8x8_inference_flag;
	}

	public int getFrame_cropping_flag() {
		return frame_cropping_flag;
	}

	public int getFrame_crop_left_offset() {
		return frame_crop_left_offset;
	}

	public int getFrame_crop_right_offset() {
		return frame_crop_right_offset;
	}

	public int getFrame_crop_top_offset() {
		return frame_crop_top_offset;
	}

	public int getFrame_crop_bottom_offset() {
		return frame_crop_bottom_offset;
	}

	public int getVui_parameters_present_flag() {
		return vui_parameters_present_flag;
	}

	public VuiParameters getVui_parameters() {
		return vui_parameters;
	}

	

}

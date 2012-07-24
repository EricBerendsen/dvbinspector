package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public class VuiParameters implements TreeNode {

	// TODO add hrd_parameters( ) as found in D:\ts\cypheros\Testbild H264 1080p alternating video_full_range_flag.ts and Testbild H264 1080p alternating video_full_range_flag.ts
	// SEE E.1.2 HRD parameters syntax
	private static Logger	logger	= Logger.getLogger(VuiParameters.class.getName());
	
	private int aspect_ratio_info_present_flag;
	private int aspect_ratio_idc;
	private int sar_width;
	private int sar_height;
	private int overscan_info_present_flag;
	private int overscan_appropriate_flag;

	private int video_signal_type_present_flag;
	private int video_format;
	private int video_full_range_flag;
	private int colour_description_present_flag;
	private int colour_primaries;
	private int transfer_characteristics;
	private int matrix_coefficients;
	
	private int chroma_loc_info_present_flag;
	private int chroma_sample_loc_type_top_field;
	private int chroma_sample_loc_type_bottom_field;
	private int timing_info_present_flag;
	private int num_units_in_tick;
	private int time_scale;
	private int fixed_frame_rate_flag;


	private int nal_hrd_parameters_present_flag;
	private int vcl_hrd_parameters_present_flag;
	private int low_delay_hrd_flag;
	private int pic_struct_present_flag;
	private int bitstream_restriction_flag;
	private int motion_vectors_over_pic_boundaries_flag;
	private int max_bytes_per_pic_denom;
	private int max_bits_per_mb_denom;
	private int log2_max_mv_length_horizontal;
	private int log2_max_mv_length_vertical;
	private int num_reorder_frames;
	private int max_dec_frame_buffering;

	public VuiParameters(BitSource bitSource) {
		aspect_ratio_info_present_flag=bitSource.u(1);
		if( aspect_ratio_info_present_flag!=0 ) {
			aspect_ratio_idc =bitSource.u(8);
			if( aspect_ratio_idc == 255 ) { //Extended_SAR
				sar_width =bitSource.u(16);
				sar_height =bitSource.u(16);
			}
		}
		overscan_info_present_flag =bitSource.u(1);
		if( overscan_info_present_flag!=0 ){
			overscan_appropriate_flag =bitSource.u(1);
		}
		video_signal_type_present_flag =bitSource.u(1);
		if( video_signal_type_present_flag!=0 ){
			video_format =bitSource.u(3);
			video_full_range_flag =bitSource.u(1);
			colour_description_present_flag =bitSource.u(1);
			if( colour_description_present_flag!=0 ) {
				colour_primaries =bitSource.u(8);
				transfer_characteristics =bitSource.u(8);
				matrix_coefficients =bitSource.u(8);
			}
		}
		
		chroma_loc_info_present_flag  =bitSource.u(1);
		if( chroma_loc_info_present_flag!=0) {
			chroma_sample_loc_type_top_field  =bitSource.ue();
			chroma_sample_loc_type_bottom_field  =bitSource.ue();
		}
		timing_info_present_flag =bitSource.u(1);
		if( timing_info_present_flag !=0) {
			num_units_in_tick  =bitSource.u(32);
			time_scale =bitSource.u(32);
			fixed_frame_rate_flag  =bitSource.u(1);
		}
		
		nal_hrd_parameters_present_flag=bitSource.u(1);
		if( nal_hrd_parameters_present_flag!=0 ){
			logger.warning("  nal_hrd_parameters_present_flag!=0 not implemented");
			//hrd_parameters( )
		}
		vcl_hrd_parameters_present_flag=bitSource.u(1);
		if( vcl_hrd_parameters_present_flag!=0 ){
			logger.warning("  vcl_hrd_parameters_present_flag!=0 not implemented");
			//hrd_parameters( )
		}
		if( nal_hrd_parameters_present_flag!=0 || vcl_hrd_parameters_present_flag!=0 ){
			low_delay_hrd_flag=bitSource.u(1);
		}
		pic_struct_present_flag=bitSource.u(1);
		bitstream_restriction_flag=bitSource.u(1);
		if( bitstream_restriction_flag!=0 ) {
			motion_vectors_over_pic_boundaries_flag=bitSource.u(1);
			max_bytes_per_pic_denom=bitSource.ue();
			max_bits_per_mb_denom=bitSource.ue();
			log2_max_mv_length_horizontal=bitSource.ue();
			log2_max_mv_length_vertical=bitSource.ue();
			num_reorder_frames=bitSource.ue();
			max_dec_frame_buffering=bitSource.ue();
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("vui_parameters"));
		t.add(new DefaultMutableTreeNode(new KVP("aspect_ratio_info_present_flag",aspect_ratio_info_present_flag,null)));
		if( aspect_ratio_info_present_flag!=0 ) {
			t.add(new DefaultMutableTreeNode(new KVP("aspect_ratio_idc",aspect_ratio_idc,getAspectRationIdcString(aspect_ratio_idc))));
			if( aspect_ratio_idc == 255 ) { //Extended_SAR
				t.add(new DefaultMutableTreeNode(new KVP("sar_width",sar_width,null)));
				t.add(new DefaultMutableTreeNode(new KVP("sar_height",sar_height,null)));
			}
		}
		t.add(new DefaultMutableTreeNode(new KVP("overscan_info_present_flag",overscan_info_present_flag,null)));
		if( overscan_info_present_flag!=0 ){
			t.add(new DefaultMutableTreeNode(new KVP("overscan_appropriate_flag",overscan_appropriate_flag,overscan_appropriate_flag==1?"suitable for display using overscan":"output should not be displayed using overscan")));
		}
		t.add(new DefaultMutableTreeNode(new KVP("video_signal_type_present_flag",video_signal_type_present_flag,null)));
		if( video_signal_type_present_flag!=0 ){
			t.add(new DefaultMutableTreeNode(new KVP("video_format",video_format,getVideoFormatString(video_format))));

			t.add(new DefaultMutableTreeNode(new KVP("video_full_range_flag",video_full_range_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("colour_description_present_flag",colour_description_present_flag,null)));
			if( colour_description_present_flag!=0 ) {
				t.add(new DefaultMutableTreeNode(new KVP("colour_primaries",colour_primaries,null)));
				t.add(new DefaultMutableTreeNode(new KVP("transfer_characteristics",transfer_characteristics,null)));
				t.add(new DefaultMutableTreeNode(new KVP("matrix_coefficients",matrix_coefficients,null)));
			}
		}
		
		t.add(new DefaultMutableTreeNode(new KVP("chroma_loc_info_present_flag",chroma_loc_info_present_flag,null)));
		if( chroma_loc_info_present_flag!=0) {
			t.add(new DefaultMutableTreeNode(new KVP("chroma_sample_loc_type_top_field",chroma_sample_loc_type_top_field,null)));
			t.add(new DefaultMutableTreeNode(new KVP("chroma_sample_loc_type_bottom_field",chroma_sample_loc_type_bottom_field,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("timing_info_present_flag",timing_info_present_flag,null)));
		if( timing_info_present_flag !=0) {
			t.add(new DefaultMutableTreeNode(new KVP("num_units_in_tick",num_units_in_tick,null)));
			t.add(new DefaultMutableTreeNode(new KVP("time_scale",time_scale,null)));
			t.add(new DefaultMutableTreeNode(new KVP("fixed_frame_rate_flag",fixed_frame_rate_flag,null)));
		}


		t.add(new DefaultMutableTreeNode(new KVP("nal_hrd_parameters_present_flag",nal_hrd_parameters_present_flag,null)));
		if( nal_hrd_parameters_present_flag!=0 ){
			//logger.warning("  nal_hrd_parameters_present_flag!=0 not implemented");
			//hrd_parameters( )
		}
		t.add(new DefaultMutableTreeNode(new KVP("vcl_hrd_parameters_present_flag",vcl_hrd_parameters_present_flag,null)));
		if( vcl_hrd_parameters_present_flag!=0 ){
			//logger.warning("  vcl_hrd_parameters_present_flag!=0 not implemented");
			//hrd_parameters( )
		}
		if( nal_hrd_parameters_present_flag!=0 || vcl_hrd_parameters_present_flag!=0 ){
			t.add(new DefaultMutableTreeNode(new KVP("low_delay_hrd_flag",low_delay_hrd_flag,null)));
		}
		t.add(new DefaultMutableTreeNode(new KVP("pic_struct_present_flag",pic_struct_present_flag,null)));

		t.add(new DefaultMutableTreeNode(new KVP("bitstream_restriction_flag",bitstream_restriction_flag,null)));
		if(bitstream_restriction_flag!=0){
			t.add(new DefaultMutableTreeNode(new KVP("motion_vectors_over_pic_boundaries_flag",motion_vectors_over_pic_boundaries_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("max_bytes_per_pic_denom",max_bytes_per_pic_denom,null)));
			t.add(new DefaultMutableTreeNode(new KVP("max_bits_per_mb_denom",max_bits_per_mb_denom,null)));
			t.add(new DefaultMutableTreeNode(new KVP("log2_max_mv_length_horizontal",log2_max_mv_length_horizontal,null)));
			t.add(new DefaultMutableTreeNode(new KVP("log2_max_mv_length_vertical",log2_max_mv_length_vertical,null)));
			t.add(new DefaultMutableTreeNode(new KVP("num_reorder_frames",num_reorder_frames,null)));
			t.add(new DefaultMutableTreeNode(new KVP("max_dec_frame_buffering",max_dec_frame_buffering,null)));
		}

		return t;
	}

	public static String getVideoFormatString(final int video_format) {

		switch (video_format) {
		case 0  : return "Component";
		case 1  : return "PAL";
		case 2  : return "NTSC";
		case 3  : return "SECAM";
		case 4  : return "MAC";
		case 5  : return "Unspecified video format";
		default:
			return "reserved"; 
		}
	}

	public static String getAspectRationIdcString(final int aspect_ratio_idc) {

		switch (aspect_ratio_idc) {
		case 0 : return "Unspecified";
		case 1 : return "1:1 (square)"; 
		case 2 : return "12:11";
		case 3 : return "10:11"; 
		case 4 : return "16:11";
		case 5 : return "40:33"; 
		case 6 : return "24:11"; 
		case 7 : return "20:11"; 
		case 8 : return "32:11"; 
		case 9 : return "80:33"; 
		case 10 : return "18:11"; 
		case 11 : return "15:11"; 
		case 12 : return "64:33"; 
		case 13 : return "160:99";
		case 14 : return "4:3"; 
		case 15 : return "3:2"; 
		case 16 : return "2:1"; 
		case 255 : return "Extended_SAR";

		default:
			return "reserved";
		}
	}

	public int getAspect_ratio_info_present_flag() {
		return aspect_ratio_info_present_flag;
	}

	public int getAspect_ratio_idc() {
		return aspect_ratio_idc;
	}

	public int getSar_width() {
		return sar_width;
	}

	public int getSar_height() {
		return sar_height;
	}

	public int getOverscan_info_present_flag() {
		return overscan_info_present_flag;
	}

	public int getOverscan_appropriate_flag() {
		return overscan_appropriate_flag;
	}

	public int getVideo_signal_type_present_flag() {
		return video_signal_type_present_flag;
	}

	public int getVideo_format() {
		return video_format;
	}

	public int getVideo_full_range_flag() {
		return video_full_range_flag;
	}

	public int getColour_description_present_flag() {
		return colour_description_present_flag;
	}

	public int getColour_primaries() {
		return colour_primaries;
	}

	public int getTransfer_characteristics() {
		return transfer_characteristics;
	}

	public int getMatrix_coefficients() {
		return matrix_coefficients;
	}

	public int getChroma_loc_info_present_flag() {
		return chroma_loc_info_present_flag;
	}

	public int getChroma_sample_loc_type_top_field() {
		return chroma_sample_loc_type_top_field;
	}

	public int getChroma_sample_loc_type_bottom_field() {
		return chroma_sample_loc_type_bottom_field;
	}

	public int getTiming_info_present_flag() {
		return timing_info_present_flag;
	}

	public int getNum_units_in_tick() {
		return num_units_in_tick;
	}

	public int getTime_scale() {
		return time_scale;
	}

	public int getFixed_frame_rate_flag() {
		return fixed_frame_rate_flag;
	}

	public int getNal_hrd_parameters_present_flag() {
		return nal_hrd_parameters_present_flag;
	}

	public int getVcl_hrd_parameters_present_flag() {
		return vcl_hrd_parameters_present_flag;
	}

	public int getLow_delay_hrd_flag() {
		return low_delay_hrd_flag;
	}

	public int getPic_struct_present_flag() {
		return pic_struct_present_flag;
	}

	public int getBitstream_restriction_flag() {
		return bitstream_restriction_flag;
	}

	public int getMotion_vectors_over_pic_boundaries_flag() {
		return motion_vectors_over_pic_boundaries_flag;
	}

	public int getMax_bytes_per_pic_denom() {
		return max_bytes_per_pic_denom;
	}

	public int getMax_bits_per_mb_denom() {
		return max_bits_per_mb_denom;
	}

	public int getLog2_max_mv_length_horizontal() {
		return log2_max_mv_length_horizontal;
	}

	public int getLog2_max_mv_length_vertical() {
		return log2_max_mv_length_vertical;
	}

	public int getNum_reorder_frames() {
		return num_reorder_frames;
	}

	public int getMax_dec_frame_buffering() {
		return max_dec_frame_buffering;
	}

}

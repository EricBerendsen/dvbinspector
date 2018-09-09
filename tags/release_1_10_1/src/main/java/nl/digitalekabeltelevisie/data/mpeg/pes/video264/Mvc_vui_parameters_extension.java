package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.BitSource;

public class Mvc_vui_parameters_extension {

	private int vui_mvc_num_ops_minus1;
	private int[] vui_mvc_temporal_id;
	private int[] vui_mvc_num_target_output_views_minus1;
	private int[][] vui_mvc_view_id;
	private int[] vui_mvc_timing_info_present_flag;
	private int[] vui_mvc_num_units_in_tick;
	private int[] vui_mvc_time_scale;
	private int[] vui_mvc_fixed_frame_rate_flag;
	private int[] vui_mvc_nal_hrd_parameters_present_flag;
	private HrdParameters[] mvc_nal_hrd_parameters;
	private int[] vui_mvc_vcl_hrd_parameters_present_flag;
	private HrdParameters[] mvc_vcl_hrd_parameters;
	private int[] vui_mvc_low_delay_hrd_flag;
	private int[] vui_mvc_pic_struct_present_flag;


	public Mvc_vui_parameters_extension(BitSource bitSource) {
		
		
		vui_mvc_num_ops_minus1 = bitSource.ue();
		
		vui_mvc_temporal_id = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_num_target_output_views_minus1 = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_view_id = new int[vui_mvc_num_ops_minus1+1][];
		vui_mvc_timing_info_present_flag = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_num_units_in_tick = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_time_scale = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_fixed_frame_rate_flag = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_nal_hrd_parameters_present_flag = new int[vui_mvc_num_ops_minus1+1];
		mvc_nal_hrd_parameters = new HrdParameters[vui_mvc_num_ops_minus1+1]; 
		vui_mvc_vcl_hrd_parameters_present_flag = new int[vui_mvc_num_ops_minus1+1];
		mvc_vcl_hrd_parameters = new HrdParameters[vui_mvc_num_ops_minus1+1]; 
		vui_mvc_low_delay_hrd_flag = new int[vui_mvc_num_ops_minus1+1];
		vui_mvc_pic_struct_present_flag = new int[vui_mvc_num_ops_minus1+1];
		
		for (int i = 0; i <= vui_mvc_num_ops_minus1; i++) {
			vui_mvc_temporal_id[i] = bitSource.u(3);
			vui_mvc_num_target_output_views_minus1[i] = bitSource.ue();
			vui_mvc_view_id[i] = new int[vui_mvc_num_target_output_views_minus1[i] + 1];
			for (int j = 0; j <= vui_mvc_num_target_output_views_minus1[i]; j++) {
				vui_mvc_view_id[i][j] = bitSource.ue();
			}
			vui_mvc_timing_info_present_flag[i] = bitSource.u(1);
			if (vui_mvc_timing_info_present_flag[i] == 1) {
				vui_mvc_num_units_in_tick[i] = bitSource.u(32);
				vui_mvc_time_scale[i] = bitSource.u(32);
				vui_mvc_fixed_frame_rate_flag[i] = bitSource.u(1);
			}
			vui_mvc_nal_hrd_parameters_present_flag[i] = bitSource.u(1);
			if (vui_mvc_nal_hrd_parameters_present_flag[i] == 1) {
				mvc_nal_hrd_parameters[i] = new HrdParameters(bitSource);
			}

			vui_mvc_vcl_hrd_parameters_present_flag[i] = bitSource.u(1);

			if (vui_mvc_vcl_hrd_parameters_present_flag[i] == 1) {
				mvc_vcl_hrd_parameters[i] = new HrdParameters(bitSource);
			}

			if ((vui_mvc_nal_hrd_parameters_present_flag[i] == 1)
					|| (vui_mvc_vcl_hrd_parameters_present_flag[i] == 1)) {
				vui_mvc_low_delay_hrd_flag[i] = bitSource.u(1);
			}
			vui_mvc_pic_struct_present_flag[i] = bitSource.u(1);
		}
	}
	
	public DefaultMutableTreeNode getJTreeNode(int modus){
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("mvc_vui_parameters_extension"));
		DefaultMutableTreeNode vui_mvc_num_ops_minus1Node = new DefaultMutableTreeNode(new KVP("vui_mvc_num_ops_minus1",vui_mvc_num_ops_minus1,null));
		t.add(vui_mvc_num_ops_minus1Node);

		for (int i = 0; i <= vui_mvc_num_ops_minus1; i++) {
			vui_mvc_num_ops_minus1Node.add(new DefaultMutableTreeNode(new KVP("vui_mvc_temporal_id["+i+"]",vui_mvc_temporal_id[ i ],null)));
			DefaultMutableTreeNode vui_mvc_num_target_output_views_minus1Node = new DefaultMutableTreeNode(new KVP("vui_mvc_num_target_output_views_minus1["+i+"]",vui_mvc_num_target_output_views_minus1[ i ],null));
			vui_mvc_num_ops_minus1Node.add(vui_mvc_num_target_output_views_minus1Node);

			for (int j = 0; j <= vui_mvc_num_target_output_views_minus1[i]; j++) {
				vui_mvc_num_target_output_views_minus1Node.add(new DefaultMutableTreeNode(new KVP("vui_mvc_view_id["+i+","+j+"]",vui_mvc_view_id[i][j],null)));
			}
			DefaultMutableTreeNode vui_mvc_timing_info_present_flagNode = new DefaultMutableTreeNode(new KVP("vui_mvc_timing_info_present_flag["+i+"]",vui_mvc_timing_info_present_flag[ i ],null));
			vui_mvc_num_ops_minus1Node.add(vui_mvc_timing_info_present_flagNode);
			if(vui_mvc_timing_info_present_flag[i]==1){
				vui_mvc_timing_info_present_flagNode.add(new DefaultMutableTreeNode(new KVP("vui_mvc_num_units_in_tick["+i+"]",vui_mvc_num_units_in_tick[i],null)));
				vui_mvc_timing_info_present_flagNode.add(new DefaultMutableTreeNode(new KVP("vui_mvc_time_scale["+i+"]",vui_mvc_time_scale[i],null)));
				vui_mvc_timing_info_present_flagNode.add(new DefaultMutableTreeNode(new KVP("vui_mvc_fixed_frame_rate_flag["+i+"]",vui_mvc_fixed_frame_rate_flag[i],null)));
			}
			DefaultMutableTreeNode vui_mvc_nal_hrd_parameters_present_flagNode = new DefaultMutableTreeNode(new KVP("vui_mvc_nal_hrd_parameters_present_flag["+i+"]",vui_mvc_nal_hrd_parameters_present_flag[ i ],null));
			vui_mvc_num_ops_minus1Node.add(vui_mvc_nal_hrd_parameters_present_flagNode);
			if(vui_mvc_nal_hrd_parameters_present_flag[ i ]==1){
				vui_mvc_nal_hrd_parameters_present_flagNode.add(mvc_nal_hrd_parameters[i].getJTreeNode(modus));
			}
			DefaultMutableTreeNode vui_mvc_vcl_hrd_parameters_present_flagNode = new DefaultMutableTreeNode(new KVP("vui_mvc_vcl_hrd_parameters_present_flag["+i+"]",vui_mvc_vcl_hrd_parameters_present_flag[ i ],null));
			vui_mvc_num_ops_minus1Node.add(vui_mvc_vcl_hrd_parameters_present_flagNode);
			if(vui_mvc_vcl_hrd_parameters_present_flag[ i ]==1){
				vui_mvc_vcl_hrd_parameters_present_flagNode.add(mvc_vcl_hrd_parameters[i].getJTreeNode(modus));
			}
			
			if ((vui_mvc_nal_hrd_parameters_present_flag[i] == 1)
					|| (vui_mvc_vcl_hrd_parameters_present_flag[i] == 1)) {
				vui_mvc_num_ops_minus1Node.add(new DefaultMutableTreeNode(new KVP("vui_mvc_low_delay_hrd_flag["+i+"]",vui_mvc_low_delay_hrd_flag[i],null)));
			}
			vui_mvc_num_ops_minus1Node.add(new DefaultMutableTreeNode(new KVP("vui_mvc_pic_struct_present_flag["+i+"]",vui_mvc_pic_struct_present_flag[i],null)));
		}

		return t;
	}

}

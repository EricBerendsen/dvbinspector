/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class H265HrdParameters  implements TreeNode{

	private final int commonInfPresentFlag;
	private final int maxNumSubLayersMinus1;

	private int nal_hrd_parameters_present_flag;
	private int vcl_hrd_parameters_present_flag;
	private int sub_pic_hrd_params_present_flag;
	private int tick_divisor_minus2;
	private int du_cpb_removal_delay_increment_length_minus1;
	private int sub_pic_cpb_params_in_pic_timing_sei_flag;
	private int dpb_output_delay_du_length_minus1;
	private int bit_rate_scale;
	private int cpb_size_scale;
	private int cpb_size_du_scale;
	private int initial_cpb_removal_delay_length_minus1;
	private int au_cpb_removal_delay_length_minus1;
	private int dpb_output_delay_length_minus1;
	private final int[] fixed_pic_rate_general_flag;
	private final int[] fixed_pic_rate_within_cvs_flag;
	private final int[] elemental_duration_in_tc_minus1;
	private final int[] low_delay_hrd_flag;
	private final int[] cpb_cnt_minus1;
	private final SubLayerHRDParameters[] sub_layer_hrd_parameters_nal;
	private final SubLayerHRDParameters[] sub_layer_hrd_parameters_vcl;

	/**
	 * @param bitSource
	 * @param i
	 * @param sps_max_sub_layers_minus1
	 */
	public H265HrdParameters(final int commonInfPresentFlag, final int maxNumSubLayersMinus1, final BitSource bitSource) {

		this.commonInfPresentFlag=commonInfPresentFlag;
		this.maxNumSubLayersMinus1=maxNumSubLayersMinus1;

		if( commonInfPresentFlag==1 ) {
			nal_hrd_parameters_present_flag=bitSource.u(1);
			vcl_hrd_parameters_present_flag=bitSource.u(1);
			if( (nal_hrd_parameters_present_flag==1) || (vcl_hrd_parameters_present_flag==1)){
				sub_pic_hrd_params_present_flag=bitSource.u(1);
				if( sub_pic_hrd_params_present_flag==1 ) {
					tick_divisor_minus2=bitSource.u(8);
					du_cpb_removal_delay_increment_length_minus1=bitSource.u(5);
					sub_pic_cpb_params_in_pic_timing_sei_flag=bitSource.u(1);
					dpb_output_delay_du_length_minus1=bitSource.u(5);
				}
				bit_rate_scale=bitSource.u(4);
				cpb_size_scale=bitSource.u(4);
				if( sub_pic_hrd_params_present_flag==1 ){
					cpb_size_du_scale=bitSource.u(4);
				}
				initial_cpb_removal_delay_length_minus1=bitSource.u(5);
				au_cpb_removal_delay_length_minus1=bitSource.u(5);
				dpb_output_delay_length_minus1=bitSource.u(5);
			}
		}
		fixed_pic_rate_general_flag = new int[maxNumSubLayersMinus1+1];
		fixed_pic_rate_within_cvs_flag = new int[maxNumSubLayersMinus1+1];
		elemental_duration_in_tc_minus1 = new int[maxNumSubLayersMinus1+1];
		low_delay_hrd_flag = new int[maxNumSubLayersMinus1+1];
		cpb_cnt_minus1 = new int[maxNumSubLayersMinus1+1];
		sub_layer_hrd_parameters_nal = new SubLayerHRDParameters[maxNumSubLayersMinus1+1];
		sub_layer_hrd_parameters_vcl = new SubLayerHRDParameters[maxNumSubLayersMinus1+1];

		for(int i = 0; i <= maxNumSubLayersMinus1; i++ ) {
			fixed_pic_rate_general_flag[ i ]=bitSource.u(1);
			if(fixed_pic_rate_general_flag[ i ]==0 ){
				fixed_pic_rate_within_cvs_flag[ i ]=bitSource.u(1);
			}
			if( fixed_pic_rate_within_cvs_flag[ i ]!=0 ){
				elemental_duration_in_tc_minus1[ i ]=bitSource.ue();
			}else{
				low_delay_hrd_flag[ i ]=bitSource.u(1);
			}
			if(low_delay_hrd_flag[ i ]==0 ){
				cpb_cnt_minus1[ i ]=bitSource.ue();
			}
			if( nal_hrd_parameters_present_flag!=0 ){
				sub_layer_hrd_parameters_nal[i]= new SubLayerHRDParameters(i,cpb_cnt_minus1[ i ],sub_pic_hrd_params_present_flag, bitSource);
			}
			if( vcl_hrd_parameters_present_flag!=0 ){
				sub_layer_hrd_parameters_vcl[i]= new SubLayerHRDParameters(i,cpb_cnt_minus1[ i ],sub_pic_hrd_params_present_flag, bitSource);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("hrd_parameters(commonInfPresentFlag="+commonInfPresentFlag+", maxNumSubLayersMinus1="+maxNumSubLayersMinus1+")"));
		t.add(new DefaultMutableTreeNode(new KVP("nal_hrd_parameters_present_flag",nal_hrd_parameters_present_flag,null)));
		t.add(new DefaultMutableTreeNode(new KVP("vcl_hrd_parameters_present_flag",vcl_hrd_parameters_present_flag,null)));
		if( (nal_hrd_parameters_present_flag==1) || (vcl_hrd_parameters_present_flag==1)){
			t.add(new DefaultMutableTreeNode(new KVP("sub_pic_hrd_params_present_flag",sub_pic_hrd_params_present_flag,null)));
			if( sub_pic_hrd_params_present_flag==1 ) {
				t.add(new DefaultMutableTreeNode(new KVP("tick_divisor_minus2",tick_divisor_minus2,null)));
				t.add(new DefaultMutableTreeNode(new KVP("du_cpb_removal_delay_increment_length_minus1",du_cpb_removal_delay_increment_length_minus1,null)));
				t.add(new DefaultMutableTreeNode(new KVP("sub_pic_cpb_params_in_pic_timing_sei_flag",sub_pic_cpb_params_in_pic_timing_sei_flag,null)));
				t.add(new DefaultMutableTreeNode(new KVP("dpb_output_delay_du_length_minus1",dpb_output_delay_du_length_minus1,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("bit_rate_scale",bit_rate_scale,null)));
			t.add(new DefaultMutableTreeNode(new KVP("cpb_size_scale",cpb_size_scale,null)));
			if( sub_pic_hrd_params_present_flag==1 ){
				t.add(new DefaultMutableTreeNode(new KVP("cpb_size_du_scale",cpb_size_du_scale,null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("initial_cpb_removal_delay_length_minus1",initial_cpb_removal_delay_length_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("au_cpb_removal_delay_length_minus1",au_cpb_removal_delay_length_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("dpb_output_delay_length_minus1",dpb_output_delay_length_minus1,null)));
		}
		for(int i = 0; i <= maxNumSubLayersMinus1; i++ ) {
			t.add(new DefaultMutableTreeNode(new KVP("fixed_pic_rate_general_flag["+i+"]",fixed_pic_rate_general_flag[i],null)));
			if(fixed_pic_rate_general_flag[ i ]==0 ){
				t.add(new DefaultMutableTreeNode(new KVP("fixed_pic_rate_within_cvs_flag["+i+"]",fixed_pic_rate_within_cvs_flag[i],null)));
			}
			if( fixed_pic_rate_within_cvs_flag[ i ]!=0 ){
				t.add(new DefaultMutableTreeNode(new KVP("elemental_duration_in_tc_minus1["+i+"]",elemental_duration_in_tc_minus1[i],null)));
			}else{
				t.add(new DefaultMutableTreeNode(new KVP("low_delay_hrd_flag["+i+"]",low_delay_hrd_flag[i],null)));
			}
			if(low_delay_hrd_flag[ i ]==0 ){
				t.add(new DefaultMutableTreeNode(new KVP("cpb_cnt_minus1["+i+"]",cpb_cnt_minus1[i],null)));
			}
			if( nal_hrd_parameters_present_flag!=0 ){
				t.add(sub_layer_hrd_parameters_nal[i].getJTreeNode(modus));
			}
			if( vcl_hrd_parameters_present_flag!=0 ){
				t.add(sub_layer_hrd_parameters_vcl[i].getJTreeNode(modus));
			}
		}


		return t;
	}

}

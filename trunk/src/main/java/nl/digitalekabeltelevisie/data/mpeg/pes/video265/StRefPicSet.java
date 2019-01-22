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

import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

// based on 7.3.7 Short-term reference picture set syntax  Rec. ITU-T H.265 v5 (02/2018) 



import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.BitSource;

public class StRefPicSet implements TreeNode {

	private static final Logger	logger	= Logger.getLogger(StRefPicSet.class.getName());

	private int stRpsIdx;
	private int inter_ref_pic_set_prediction_flag;
	private int delta_idx_minus1;
	private int delta_rps_sign;
	private int abs_delta_rps_minus1;
	private int []used_by_curr_pic_flag;
	private int num_negative_pics;
	private int num_positive_pics;
	private int[] delta_poc_s0_minus1;
	private int[] used_by_curr_pic_s0_flag;
	private int[] delta_poc_s1_minus1;
	private int[] used_by_curr_pic_s1_flag;
	
	
	boolean notImplemented = false;

	public StRefPicSet(int stRpsIdx , int num_short_term_ref_pic_sets, BitSource bitSource) {
		this.stRpsIdx = stRpsIdx;
		if( stRpsIdx  !=  0 ) {
			inter_ref_pic_set_prediction_flag =bitSource.u(1);
		}
		if( inter_ref_pic_set_prediction_flag !=0) {
			
			notImplemented = true;
			logger.warning("if( inter_ref_pic_set_prediction_flag ) not implemented");
			
			
//			if( stRpsIdx  ==  num_short_term_ref_pic_sets ) {
//				delta_idx_minus1  =bitSource.ue();
//			}
//			delta_rps_sign = bitSource.u(1);
//			abs_delta_rps_minus1 = bitSource.ue();
//			// Rec. ITU-T H.265 v5 (02/2018) p. 103 (7-59)
//			int RefRpsIdx = stRpsIdx - ( delta_idx_minus1 + 1 ) ;
//			for(int j = 0; j  <=  NumDeltaPocs[ RefRpsIdx ]; j++ ) {
//				used_by_curr_pic_flag[ j ]  =bitSource.u(1);
//				if( !used_by_curr_pic_flag[ j ] ) {
//					use_delta_flag[ j ] u(1);
//				}
//			}
		} else {    
			num_negative_pics = bitSource.ue();
			num_positive_pics = bitSource.ue();
			delta_poc_s0_minus1 = new int[num_negative_pics];
			used_by_curr_pic_s0_flag = new int[num_negative_pics];
			for(int i = 0; i < num_negative_pics; i++ ) {
				delta_poc_s0_minus1[ i ] = bitSource.ue();
				used_by_curr_pic_s0_flag[ i ] = bitSource.u(1);
			}
			delta_poc_s1_minus1 = new int[num_positive_pics];
			used_by_curr_pic_s1_flag = new int[num_positive_pics];
			for(int i = 0; i < num_positive_pics; i++ ) {
				delta_poc_s1_minus1[ i ]  = bitSource.ue();
				used_by_curr_pic_s1_flag[ i ]  = bitSource.u(1);
			}
		}
	}
	

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Short-term reference picture set st_ref_pic_set("+stRpsIdx+")"));
		
		if (stRpsIdx != 0) {
			t.add(new DefaultMutableTreeNode(new KVP("inter_ref_pic_set_prediction_flag",inter_ref_pic_set_prediction_flag,null)));
		}

		if(notImplemented) {
			t.add(new DefaultMutableTreeNode(GuiUtils.getNotImplementedKVP("if( inter_ref_pic_set_prediction_flag ) {")));
			return t;
		}
		if( inter_ref_pic_set_prediction_flag !=0) {
		} else {
			t.add(new DefaultMutableTreeNode(new KVP("num_negative_pics",num_negative_pics,null)));
			t.add(new DefaultMutableTreeNode(new KVP("num_positive_pics",num_positive_pics,null)));
			delta_poc_s0_minus1 = new int[num_negative_pics];
			used_by_curr_pic_s0_flag = new int[num_negative_pics];
			for(int i = 0; i < num_negative_pics; i++ ) {
				t.add(new DefaultMutableTreeNode(new KVP("delta_poc_s0_minus1["+ i +"]",delta_poc_s0_minus1[ i ],null)));
				t.add(new DefaultMutableTreeNode(new KVP("used_by_curr_pic_s0_flag["+ i +"]",used_by_curr_pic_s0_flag[ i ],null)));
			}
			delta_poc_s1_minus1 = new int[num_positive_pics];
			used_by_curr_pic_s1_flag = new int[num_positive_pics];
			for(int i = 0; i < num_positive_pics; i++ ) {
				t.add(new DefaultMutableTreeNode(new KVP("delta_poc_s1_minus1["+ i +"]",delta_poc_s1_minus1[ i ],null)));
				t.add(new DefaultMutableTreeNode(new KVP("used_by_curr_pic_s1_flag["+ i +"]",used_by_curr_pic_s1_flag[ i ],null)));
			}
			
		}
		
		return t;
	}

}


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

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.util.BitSource;


// based on Rec. ITU-T H.265 v5 (02/2018) 
// 7.3.4 Scaling list data syntax 

public class ScalingListData implements TreeNode {
	
	int[] [] scaling_list_pred_mode_flag = new int [4][6];
	int[] [] scaling_list_pred_matrix_id_delta = new int [4][6];
	int[] [] scaling_list_dc_coef_minus8 = new int [4][6];
	int[] [] [] scalingList = new int [4][6][64];
	
	// This time it is easiest to build tree in constructor, because of many helper vars in definition
	DefaultMutableTreeNode result = new DefaultMutableTreeNode(new KVP("scaling_list_data"));
	
	public ScalingListData(BitSource bs) {

		for (int sizeId = 0; sizeId < 4; sizeId++) {
			for (int matrixId = 0; matrixId < 6; matrixId += (sizeId == 3) ? 3 : 1) {
				int u = bs.u(1);
				scaling_list_pred_mode_flag[sizeId][matrixId] = u;
				result.add(new DefaultMutableTreeNode(new KVP("scaling_list_pred_mode_flag["+sizeId+"]["+matrixId+"]",u,null)));
				if (scaling_list_pred_mode_flag[sizeId][matrixId] == 0) {
					int ue = bs.ue();
					scaling_list_pred_matrix_id_delta[sizeId][matrixId] = ue;
					result.add(new DefaultMutableTreeNode(new KVP("scaling_list_pred_matrix_id_delta["+sizeId+"]["+matrixId+"]",ue,null)));
				} else {
					int nextCoef = 8;
					int coefNum = Math.min(64, (1 << (4 + (sizeId << 1))));
					if (sizeId > 1) {
						int se = bs.se();
						scaling_list_dc_coef_minus8[sizeId - 2][matrixId] = se;
						result.add(new DefaultMutableTreeNode(new KVP("scaling_list_dc_coef_minus8["+(sizeId - 2)+"]["+matrixId+"]",se,null)));
						nextCoef = scaling_list_dc_coef_minus8[sizeId - 2][matrixId] + 8;
					}
					for (int i = 0; i < coefNum; i++) {
						int se = bs.se();
						int scaling_list_delta_coef = se;
						result.add(new DefaultMutableTreeNode(new KVP("scaling_list_delta_coef", se,
								"(sizeId:" + sizeId + ", matrixId:" + matrixId + ", i:" + i + ")")));

						nextCoef = (nextCoef + scaling_list_delta_coef + 256) % 256;
						scalingList[sizeId][matrixId][i] = nextCoef;
					}
				}
			}
		}
	}		
	
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		

		return result;
	}

}

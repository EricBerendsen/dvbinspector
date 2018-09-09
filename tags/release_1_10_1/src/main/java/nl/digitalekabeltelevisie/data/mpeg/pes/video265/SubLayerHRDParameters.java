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
public class SubLayerHRDParameters implements TreeNode {

	private final int subLayerId;

	private final int[] bit_rate_value_minus1;
	private final int[] cpb_size_value_minus1;
	private final int[] cpb_size_du_value_minus1;
	private final int[] bit_rate_du_value_minus1;
	private final int[] cbr_flag;

	private final int CpbCnt;

	private final int sub_pic_hrd_params_present_flag;

	/**
	 * @param i
	 * @param cpb_cnt_minus1
	 * @param bitSource
	 */
	public SubLayerHRDParameters(final int subLayerId, final int CpbCnt, final int sub_pic_hrd_params_present_flag, final BitSource bitSource) {
		this.subLayerId = subLayerId;
		this.CpbCnt = CpbCnt;
		this.sub_pic_hrd_params_present_flag=sub_pic_hrd_params_present_flag;
		bit_rate_value_minus1 = new int[CpbCnt+1];
		cpb_size_value_minus1 = new int[CpbCnt+1];
		cpb_size_du_value_minus1 = new int[CpbCnt+1];
		bit_rate_du_value_minus1 = new int[CpbCnt+1];
		cbr_flag = new int[CpbCnt+1];
		for(int i = 0; i <= CpbCnt; i++ ) {
			bit_rate_value_minus1[ i ]=bitSource.ue();
			cpb_size_value_minus1[ i ]=bitSource.ue();
			if( sub_pic_hrd_params_present_flag!=0 ) {
				cpb_size_du_value_minus1[ i ]=bitSource.ue();
				bit_rate_du_value_minus1[ i ]=bitSource.ue();
			}
			cbr_flag[ i ]=bitSource.u(1);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("SubLayerHRDParameters(i="+subLayerId+")"));
		for(int i = 0; i <= CpbCnt; i++ ) {
			t.add(new DefaultMutableTreeNode(new KVP("bit_rate_value_minus1["+i+"]",bit_rate_value_minus1[i],null)));
			t.add(new DefaultMutableTreeNode(new KVP("cpb_size_value_minus1["+i+"]",cpb_size_value_minus1[i],null)));
			if( sub_pic_hrd_params_present_flag!=0 ) {
				t.add(new DefaultMutableTreeNode(new KVP("cpb_size_du_value_minus1["+i+"]",cpb_size_du_value_minus1[i],null)));
				t.add(new DefaultMutableTreeNode(new KVP("bit_rate_du_value_minus1["+i+"]",bit_rate_du_value_minus1[i],null)));
			}
			t.add(new DefaultMutableTreeNode(new KVP("cbr_flag["+i+"]",cbr_flag[i],null)));
		}

		return t;
	}

}

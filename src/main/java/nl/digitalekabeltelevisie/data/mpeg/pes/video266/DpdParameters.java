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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 * 
 * based on 7.3.4 DPB parameters syntax Rec. ITU-T H.266 (04/2022)
 *
 */
public class DpdParameters implements TreeNode {
	
	int maxSubLayersMinus1;
	int subLayerInfoFlag;
	
	private int[] dpb_max_dec_pic_buffering_minus1;
	private int[] dpb_max_num_reorder_pics;
	private int[] dpb_max_latency_increase_plus1;

	/**
	 * @param sps_max_sublayers_minus1
	 * @param sps_sublayer_dpb_params_flag
	 * @param bitSource
	 */
	public DpdParameters(int maxSubLayersMinus1, int subLayerInfoFlag, BitSource bitSource) {
		
		this.maxSubLayersMinus1 = maxSubLayersMinus1;
		this.subLayerInfoFlag = subLayerInfoFlag;
		
		dpb_max_dec_pic_buffering_minus1 = new int[maxSubLayersMinus1+1];
		dpb_max_num_reorder_pics = new int[maxSubLayersMinus1+1];
		dpb_max_latency_increase_plus1 = new int[maxSubLayersMinus1+1];
		
		for (int i = (subLayerInfoFlag == 1 ? 0 : maxSubLayersMinus1); i <= maxSubLayersMinus1; i++) {
			dpb_max_dec_pic_buffering_minus1[i] = bitSource.ue();
			dpb_max_num_reorder_pics[i] = bitSource.ue();
			dpb_max_latency_increase_plus1[i] = bitSource.ue();
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {
		
		KVP t = new KVP("dpb_parameters( MaxSubLayersMinus1="+maxSubLayersMinus1 +", , subLayerInfoFlag ="+subLayerInfoFlag+")");
		for (int i = (subLayerInfoFlag == 1 ? 0 : maxSubLayersMinus1); i <= maxSubLayersMinus1; i++) {
			t.add(new KVP("dpb_max_dec_pic_buffering_minus1["+i+"]",dpb_max_dec_pic_buffering_minus1[i]));
			t.add(new KVP("dpb_max_num_reorder_pics["+i+"]",dpb_max_num_reorder_pics[i]));
			t.add(new KVP("dpb_max_latency_increase_plus1["+i+"]",dpb_max_latency_increase_plus1[i]));
		}

		return t;
	}

}

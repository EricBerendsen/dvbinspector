/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

public class HrdParameters implements TreeNode {

	public static class CPB implements TreeNode {

		private CPB(BitSource bitSource) {
			super();
			this.bit_rate_value_minus1 = bitSource.ue();
			this.cpb_size_value_minus1 = bitSource.ue();
			this.cbr_flag = bitSource.u(1);
		}

		private int bit_rate_value_minus1;
		private int cpb_size_value_minus1;
		private int cbr_flag;

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("CPB"));
			t.add(new DefaultMutableTreeNode(new KVP("bit_rate_value_minus1",bit_rate_value_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("cpb_size_value_minus1",cpb_size_value_minus1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("cbr_flag",cbr_flag,null)));

			return t;
		}

	}

	private int cpb_cnt_minus1;
	private int bit_rate_scale;
	private int cpb_size_scale;
	private List<CPB> cpbs = new ArrayList<HrdParameters.CPB>();

	private int initial_cpb_removal_delay_length_minus1;
	private int cpb_removal_delay_length_minus1;
	private int dpb_output_delay_length_minus1;
	private int time_offset_length;



	public HrdParameters(BitSource bitSource) {
		cpb_cnt_minus1=bitSource.ue();
		bit_rate_scale=bitSource.u(4);
		cpb_size_scale=bitSource.u(4);
		for(int schedSelIdx = 0; schedSelIdx <= cpb_cnt_minus1; schedSelIdx++ ) {
			CPB cpb = new CPB(bitSource);
			cpbs.add(cpb);
		}

		initial_cpb_removal_delay_length_minus1=bitSource.u(5);
		cpb_removal_delay_length_minus1=bitSource.u(5);
		dpb_output_delay_length_minus1=bitSource.u(5);
		time_offset_length=bitSource.u(5);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("hrd_parameters"));
		t.add(new DefaultMutableTreeNode(new KVP("cpb_cnt_minus1",cpb_cnt_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("bit_rate_scale",bit_rate_scale,null)));
		t.add(new DefaultMutableTreeNode(new KVP("cpb_size_scale",cpb_size_scale,null)));
		addListJTree(t,cpbs,modus,"CPBs");

		t.add(new DefaultMutableTreeNode(new KVP("initial_cpb_removal_delay_length_minus1",initial_cpb_removal_delay_length_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("cpb_removal_delay_length_minus1",cpb_removal_delay_length_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("dpb_output_delay_length_minus1",dpb_output_delay_length_minus1,null)));
		t.add(new DefaultMutableTreeNode(new KVP("time_offset_length",time_offset_length,null)));

		return t;
	}


}

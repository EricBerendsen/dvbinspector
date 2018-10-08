/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pid.t2mi;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

public class DynamicL1PostSignalling implements TreeNode {
	
	public class PLP implements TreeNode{
		
		private int plp_id;
		private int plp_start;
		private int plp_num_blocks;
		private int reserved_2;

		public PLP(BitSource bs) {
			plp_id  = bs.readBits(8 );
			plp_start  = bs.readBits(22 );
			plp_num_blocks  = bs.readBits(10 );
			reserved_2  = bs.readBits(8 );
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PLP"));
			t.add(new DefaultMutableTreeNode(new KVP("plp_id", plp_id, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_start", plp_start, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_num_blocks", plp_num_blocks, null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_2", reserved_2, null)));
			return t;
		}
		
	}

	public class AuxPrivateDyn implements TreeNode{
		
		private long aux_private_dyn;

		public AuxPrivateDyn(BitSource bs) {
			aux_private_dyn  = bs.readBitsLong(48);
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("aux_private_dyn",aux_private_dyn,null));
			return t;
		}
		
		
	}
	private int frame_idx;
	private int sub_slice_interval;
	private int type_2_start;
	private int l1_change_counter;
	private int start_rf_idx;
	private int reserved_1;
	
	private List<PLP> plpList = new ArrayList<>();
	private int reserved_3;
	private List<AuxPrivateDyn> aux_private_dynList = new ArrayList<>();

	public DynamicL1PostSignalling(BitSource bs, Configurable1PostSignalling configurable1PostSignalling) {
		frame_idx = bs.readBits(8);
		sub_slice_interval = bs.readBits(22);
		type_2_start = bs.readBits(22);
		l1_change_counter = bs.readBits(8);
		start_rf_idx = bs.readBits(3);
		reserved_1 = bs.readBits(8);
		for(int i=0; i<configurable1PostSignalling.getNum_plp();i++){
			plpList.add(new PLP(bs));
		}
		reserved_3  = bs.readBits(8 );
		for(int  i=0; i<configurable1PostSignalling.getNum_aux();i++){
			aux_private_dynList.add(new AuxPrivateDyn(bs));
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Dynamic L1-post signalling"));
		
		t.add(new DefaultMutableTreeNode(new KVP("frame_idx",frame_idx,null)));
		t.add(new DefaultMutableTreeNode(new KVP("sub_slice_interval",sub_slice_interval,null)));
		t.add(new DefaultMutableTreeNode(new KVP("type_2_start",type_2_start,null)));
		t.add(new DefaultMutableTreeNode(new KVP("l1_change_counter",l1_change_counter,null)));
		t.add(new DefaultMutableTreeNode(new KVP("start_rf_idx",start_rf_idx,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_1",reserved_1,null)));

		Utils.addListJTree(t,plpList,modus,"PLPs");
		t.add(new DefaultMutableTreeNode(new KVP("reserved_3",reserved_3,null)));
		Utils.addListJTree(t,aux_private_dynList,modus,"AUX_PRIVATE_DYNs");
		return t;
	}

}

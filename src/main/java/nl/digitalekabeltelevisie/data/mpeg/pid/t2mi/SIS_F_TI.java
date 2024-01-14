/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.Utils;

/**
 *  DVB BlueBook A175r1 (July 2023)   Table 2: Syntax of Framing & Timing Information (F&TI), DVB-T2 case
 */
public class SIS_F_TI extends Payload {
	
	public class PLPInfo implements TreeNode{
		
		
		private int plp_id;
		private int intl_frame_start;
		private int rfu;
		private int matype1;
		private int matype2;
		private int tto_e;
		private int tto_m;
		private int tto_l;
		private int first_iscr;
		private int bufs;
		private int first_dfl;
		private int first_syncd;
		private int next_first_syncd;
		private int mode;
		private long pcrftsp;
		private long frame_pkt_count;
		private long dpcrif;

		public PLPInfo(BitSource bs) {
			plp_id = bs.readBits(8);
			intl_frame_start = bs.readBits(1);
			rfu = bs.readBits(7);
			matype1 = bs.readBits(8);
			matype2 = bs.readBits(8);
			tto_e = bs.readBits(5);
			tto_m = bs.readBits(7);
			tto_l = bs.readBits(8);
			first_iscr = bs.readBits(22);
			bufs = bs.readBits(10 + 2);
			first_dfl = bs.readBits(16);
			first_syncd = bs.readBits(16);
			next_first_syncd = bs.readBits(16);
			mode = bs.readBits(8);
			pcrftsp = bs.readBitsLong(48);
			frame_pkt_count = bs.readBitsLong(32);
			dpcrif = bs.readBitsLong(32);
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PLPInfo"));
			
			t.add(new DefaultMutableTreeNode(new KVP("plp_id",plp_id,null)));

			
			
			t.add(new DefaultMutableTreeNode(new KVP("intl_frame_start",intl_frame_start,"should be 1")));
			t.add(new DefaultMutableTreeNode(new KVP("rfu",rfu,null)));
			t.add(new DefaultMutableTreeNode(new KVP("MATYPE1",matype1,null)));
			t.add(new DefaultMutableTreeNode(new KVP("MATYPE2",matype2,null)));
			t.add(new DefaultMutableTreeNode(new KVP("TTO_E",tto_e,null)));
			t.add(new DefaultMutableTreeNode(new KVP("TTO_M",tto_m,null)));
			t.add(new DefaultMutableTreeNode(new KVP("TTO_L",tto_l,null)));
			t.add(new DefaultMutableTreeNode(new KVP("FIRST_ISCR",first_iscr,null)));
			t.add(new DefaultMutableTreeNode(new KVP("BUFS",bufs,null)));
			t.add(new DefaultMutableTreeNode(new KVP("FIRST_DFL",first_dfl,null)));
			t.add(new DefaultMutableTreeNode(new KVP("FIRST_SYNCD",first_syncd,null)));
			t.add(new DefaultMutableTreeNode(new KVP("NEXT_FIRST_SYNCD",next_first_syncd,null)));
			t.add(new DefaultMutableTreeNode(new KVP("MODE",mode,null)));
			t.add(new DefaultMutableTreeNode(new KVP("PCRFTSP",pcrftsp,null)));
			t.add(new DefaultMutableTreeNode(new KVP("Frame_Pkt_Count",frame_pkt_count,null)));
			t.add(new DefaultMutableTreeNode(new KVP("DPCRIF",dpcrif,null)));
			
			return t;
		}
		
		
		
	}

	private int frame_idx;
	private List<PLPInfo> plpInfoList = new  ArrayList<>();


	protected SIS_F_TI(byte[] data) {
		super(data);
		
		frame_idx = getFrame_idx();
		
		BitSource bs = new BitSource(data, 7, data.length - 4); // crc (4)
		while(bs.available() >= 254) {
			PLPInfo plpInfo = new PLPInfo(bs);
			plpInfoList.add(plpInfo);
		}
	
	}

	
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode payloadNode = new DefaultMutableTreeNode(new KVP("payload"));
		payloadNode.add(new DefaultMutableTreeNode(new KVP("frame_idx",frame_idx,null)));
		Utils.addListJTree(payloadNode,plpInfoList,modus,"PLPInfos");
		return payloadNode;
	}

	
	public int getFrame_idx() {
		return getInt(data, 6, 1, MASK_8BITS);
	}

}

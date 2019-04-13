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

public class Configurable1PostSignalling implements TreeNode {
	
	public class Frequency implements TreeNode {

		private int rf_idx;
		private int frequency;

		public Frequency(BitSource bs) {
			rf_idx = bs.readBits(3);
			frequency = bs.readBits(32);
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("rf_idx:"+rf_idx+", frequency:"+frequency ));
			t.add(new DefaultMutableTreeNode(new KVP("rf_idx",rf_idx,null)));
			t.add(new DefaultMutableTreeNode(new KVP("frequency",frequency,null)));
			return t;
		}

		public int getRf_idx() {
			return rf_idx;
		}

		public int getFrequency() {
			return frequency;
		}
		
	}

	
	public class PLP implements TreeNode {

		private int plp_id;
		private int plp_type;
		private int plp_payload_type;
		private int ff_flag;
		private int first_rf_idx;
		private int first_frame_idx;
		private int plp_group_id;
		private int plp_cod;
		private int plp_mod;
		private int plp_rotation;
		private int plp_fec_type;
		private int plp_num_blocks_max;
		private int frame_interval;
		private int time_il_length;
		private int time_il_type;
		private int in_band_a_flag;
		private int in_band_b_flag;
		private int reserved_1;
		private int plp_mode;
		private int static_flag;
		private int static_padding_flag;

		public PLP(BitSource bs) {
			plp_id  = bs.readBits(8 );
			plp_type  = bs.readBits(3 );
			plp_payload_type  = bs.readBits(5 );
			ff_flag  = bs.readBits(1 );
			first_rf_idx  = bs.readBits(3 );
			first_frame_idx  = bs.readBits(8 );
			plp_group_id  = bs.readBits(8 );
			plp_cod  = bs.readBits(3 );
			plp_mod  = bs.readBits(3 );
			plp_rotation  = bs.readBits(1 );
			plp_fec_type  = bs.readBits(2 );
			plp_num_blocks_max  = bs.readBits(10 );
			frame_interval  = bs.readBits(8 );
			time_il_length  = bs.readBits(8 );
			time_il_type  = bs.readBits(1 );
			in_band_a_flag  = bs.readBits(1 );
			in_band_b_flag  = bs.readBits(1 );
			reserved_1  = bs.readBits(11 );
			plp_mode  = bs.readBits(2 );
			static_flag  = bs.readBits(1 );
			static_padding_flag  = bs.readBits(1 );
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PLP"));
			t.add(new DefaultMutableTreeNode(new KVP("plp_id", plp_id, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_type", plp_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_payload_type", plp_payload_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("ff_flag", ff_flag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("first_rf_idx", first_rf_idx, null)));
			t.add(new DefaultMutableTreeNode(new KVP("first_frame_idx", first_frame_idx, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_group_id", plp_group_id, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_cod", plp_cod, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_mod", plp_mod, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_rotation", plp_rotation, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_fec_type", plp_fec_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_num_blocks_max", plp_num_blocks_max, null)));
			t.add(new DefaultMutableTreeNode(new KVP("frame_interval", frame_interval, null)));
			t.add(new DefaultMutableTreeNode(new KVP("time_il_length", time_il_length, null)));
			t.add(new DefaultMutableTreeNode(new KVP("time_il_type", time_il_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("in_band_a_flag", in_band_a_flag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("in_band_b_flag", in_band_b_flag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_1", reserved_1, null)));
			t.add(new DefaultMutableTreeNode(new KVP("plp_mode", plp_mode, null)));
			t.add(new DefaultMutableTreeNode(new KVP("static_flag", static_flag, null)));
			t.add(new DefaultMutableTreeNode(new KVP("static_padding_flag", static_padding_flag, null)));
			return t;
		}

		public int getPlp_id() {
			return plp_id;
		}

		public int getPlp_type() {
			return plp_type;
		}

		public int getPlp_payload_type() {
			return plp_payload_type;
		}

		public int getFf_flag() {
			return ff_flag;
		}

		public int getFirst_rf_idx() {
			return first_rf_idx;
		}

		public int getFirst_frame_idx() {
			return first_frame_idx;
		}

		public int getPlp_group_id() {
			return plp_group_id;
		}

		public int getPlp_cod() {
			return plp_cod;
		}

		public int getPlp_mod() {
			return plp_mod;
		}

		public int getPlp_rotation() {
			return plp_rotation;
		}

		public int getPlp_fec_type() {
			return plp_fec_type;
		}

		public int getPlp_num_blocks_max() {
			return plp_num_blocks_max;
		}

		public int getFrame_interval() {
			return frame_interval;
		}

		public int getTime_il_length() {
			return time_il_length;
		}

		public int getTime_il_type() {
			return time_il_type;
		}

		public int getIn_band_a_flag() {
			return in_band_a_flag;
		}

		public int getIn_band_b_flag() {
			return in_band_b_flag;
		}

		public int getReserved_1() {
			return reserved_1;
		}

		public int getPlp_mode() {
			return plp_mode;
		}

		public int getStatic_flag() {
			return static_flag;
		}

		public int getStatic_padding_flag() {
			return static_padding_flag;
		}

	}
	
	
	public class AUX implements TreeNode {

		private int aux_stream_type;
		private int aux_private_conf;

		public AUX(BitSource bs) {
			aux_stream_type = bs.readBits(4);
			aux_private_conf = bs.readBits(28);
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AUX"));
			t.add(new DefaultMutableTreeNode(new KVP("aux_stream_type", aux_stream_type, null)));
			t.add(new DefaultMutableTreeNode(new KVP("aux_private_conf", aux_private_conf, null)));
			return t;
		}

		public int getAux_stream_type() {
			return aux_stream_type;
		}

		public int getAux_private_conf() {
			return aux_private_conf;
		}

	}

	private int sub_slices_per_frame;
	private int num_plp;
	private int num_aux;
	private int aux_config_rfu;

	private List<Frequency> frequencyList = new ArrayList<>();
	private L1PreSignallingData l1PreSignallingData;
	private int fef_type;
	private int fef_length;
	private int fef_interval;
	
	private List<PLP> plpList = new ArrayList<>();
	private int fef_length_msb;
	private int reserved_2;
	private List<AUX> auxList = new ArrayList<>();

	public Configurable1PostSignalling(BitSource bs, L1PreSignallingData l1PreSignallingData) {
		this.l1PreSignallingData = l1PreSignallingData;
		sub_slices_per_frame  = bs.readBits(15 );
		num_plp  = bs.readBits(8 );
		num_aux  = bs.readBits(4 );
		aux_config_rfu  = bs.readBits(8 );
		int num_rf = l1PreSignallingData.getNum_rf();
		for(int i=0;i< num_rf;i++){
			frequencyList.add(new Frequency(bs));
		}

		if((l1PreSignallingData.getS2() & 0b1) ==1) {
			fef_type  = bs.readBits(4 );
			fef_length  = bs.readBits(22 );
			fef_interval  = bs.readBits(8 );
		}

		for (int i = 0; i < num_plp; i++) {
			PLP plp = new PLP(bs);
			plpList.add(plp);
		}

		fef_length_msb  = bs.readBits(2 );
		reserved_2  = bs.readBits(30 );
		
		for (int i = 0; i < num_aux; i++) {
			AUX aux = new AUX(bs);
			auxList.add(aux);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Configurable L1-post signalling"));
		
		t.add(new DefaultMutableTreeNode(new KVP("sub_slices_per_frame",sub_slices_per_frame,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_plp",num_plp,null)));
		t.add(new DefaultMutableTreeNode(new KVP("num_aux",num_aux,null)));
		t.add(new DefaultMutableTreeNode(new KVP("aux_config_rfu",aux_config_rfu,null)));
		
		
		Utils.addListJTree(t,frequencyList,modus,"Frequencies");
		
		if((l1PreSignallingData.getS2() & 0b1) ==1) {
			t.add(new DefaultMutableTreeNode(new KVP("fef_type",fef_type,null)));
			t.add(new DefaultMutableTreeNode(new KVP("fef_length",fef_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("fef_interval",fef_interval,null)));
		}

		Utils.addListJTree(t,plpList,modus,"PLPs");

		t.add(new DefaultMutableTreeNode(new KVP("fef_length_msb",fef_length_msb,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved_2",reserved_2,null)));
		
		Utils.addListJTree(t,auxList,modus,"AUXs");

		return t;

	}

	public int getSub_slices_per_frame() {
		return sub_slices_per_frame;
	}

	public int getNum_plp() {
		return num_plp;
	}

	public int getNum_aux() {
		return num_aux;
	}

	public int getAux_config_rfu() {
		return aux_config_rfu;
	}

	public List<Frequency> getFrequencyList() {
		return frequencyList;
	}

	public L1PreSignallingData getL1PreSignallingData() {
		return l1PreSignallingData;
	}

	public int getFef_type() {
		return fef_type;
	}

	public int getFef_length() {
		return fef_length;
	}

	public int getFef_interval() {
		return fef_interval;
	}

	public List<PLP> getPlpList() {
		return plpList;
	}

	public int getFef_length_msb() {
		return fef_length_msb;
	}

	public int getReserved_2() {
		return reserved_2;
	}

	public List<AUX> getAuxList() {
		return auxList;
	}
	


}

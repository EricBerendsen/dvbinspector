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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;
import nl.digitalekabeltelevisie.util.LookUpList;


//based on ETSI EN 302 755 V1.4.1 (2015-07) 7.2.2
public class L1PreSignallingData implements TreeNode {
	
	private static LookUpList type_list = new LookUpList.Builder().
			add(0x00 ,"Transport Stream (TS) only").
			add(0x01 ,"Generic Stream (GSE and/or GFPS and/or GCS) but not TS").
			add(0x02 ,"Both TS and Generic Stream").
			add(0x03,0xff ,"Reserved for future use ").
			build();
	
	private static LookUpList guard_interval_list = new LookUpList.Builder().
			add(0, "1/32").
			add(1, "1/16").
			add(2, "1/8").
			add(3, "1/4").
			add(4, "1/128").
			add(5, "19/128").
			add(6, "19/256").
			add(7, "Reserved for future use").
			build();


			
			
	private int type;
	private int bwt_ext;
	private int s1;
	private int s2;
	private int l1_repetition_flag;
	private int guard_interval;
	private int papr;
	private int l1_mod;
	private int l1_cod;
	private int l1_fec_type;
	private int l1_post_size;
	
	private int l1_post_info_size;
	private int pilot_pattern;
	private int tx_id_availability ;
	private int cell_id ;
	private int network_id ;
	private int t2_system_id ;
	private int num_t2_frames ;
	private int num_data_symbols;
	private int regen_flag ;
	private int l1_post_extension ;
	private int num_rf ;
	private int current_rf_idx ;
	private int t2_version ;
	private int l1_post_scrambled;
	private int t2_base_lite;
	private int reserved ;


	public L1PreSignallingData(BitSource bs) {
		
		type = bs.readBits(8 );
		bwt_ext = bs.readBits(1 );
		s1 = bs.readBits(3 );
		s2 = bs.readBits(4 );
		l1_repetition_flag = bs.readBits(1 );
		guard_interval = bs.readBits(3 );
		papr = bs.readBits(4 );
		l1_mod = bs.readBits(4 );
		l1_cod = bs.readBits(2 );
		l1_fec_type = bs.readBits(2 );
		l1_post_size = bs.readBits(18 );
		l1_post_info_size = bs.readBits(18 );
		pilot_pattern = bs.readBits(4 );
		tx_id_availability = bs.readBits(8 );
		cell_id = bs.readBits(16 );
		network_id = bs.readBits(16 );
		t2_system_id = bs.readBits(16 );
		num_t2_frames = bs.readBits(8 );
		num_data_symbols = bs.readBits(12 );
		regen_flag = bs.readBits(3 );
		l1_post_extension = bs.readBits(1 );
		num_rf = bs.readBits(3 );
		current_rf_idx = bs.readBits(3 );
		t2_version = bs.readBits(4 );
		l1_post_scrambled = bs.readBits(1 );
		t2_base_lite = bs.readBits(1 );
		reserved = bs.readBits(4 );
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode l1cpre = new DefaultMutableTreeNode(new KVP("L1-Pre Signalling data "));
		
		l1cpre.add(new DefaultMutableTreeNode(new KVP("type",type,type_list.get(type))));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("bwt_ext",bwt_ext,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("s1",s1,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("s2",s2,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_repetition_flag",l1_repetition_flag,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("guard_interval",guard_interval,guard_interval_list.get(guard_interval))));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("papr",papr,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_mod",l1_mod,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_cod",l1_cod,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_fec_type",l1_fec_type,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_post_size",l1_post_size,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_post_info_size",l1_post_info_size,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("pilot_pattern",pilot_pattern,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("tx_id_availability",tx_id_availability,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("cell_id",cell_id,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("network_id",network_id,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("t2_system_id",t2_system_id,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("num_t2_frames",num_t2_frames,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("num_data_symbols",num_data_symbols,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("regen_flag",regen_flag,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_post_extension",l1_post_extension,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("num_rf",num_rf,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("current_rf_idx",current_rf_idx,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("t2_version",t2_version,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("l1_post_scrambled",l1_post_scrambled,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("t2_base_lite",t2_base_lite,null)));
		l1cpre.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		return l1cpre;
	}

	public int getType() {
		return type;
	}

	public int getBwt_ext() {
		return bwt_ext;
	}

	public int getS1() {
		return s1;
	}

	public int getS2() {
		return s2;
	}

	public int getL1_repetition_flag() {
		return l1_repetition_flag;
	}

	public int getGuard_interval() {
		return guard_interval;
	}

	public int getPapr() {
		return papr;
	}

	public int getL1_mod() {
		return l1_mod;
	}

	public int getL1_cod() {
		return l1_cod;
	}

	public int getL1_fec_type() {
		return l1_fec_type;
	}

	public int getL1_post_size() {
		return l1_post_size;
	}

	public int getL1_post_info_size() {
		return l1_post_info_size;
	}

	public int getPilot_pattern() {
		return pilot_pattern;
	}

	public int getTx_id_availability() {
		return tx_id_availability;
	}

	public int getCell_id() {
		return cell_id;
	}

	public int getNetwork_id() {
		return network_id;
	}

	public int getT2_system_id() {
		return t2_system_id;
	}

	public int getNum_t2_frames() {
		return num_t2_frames;
	}

	public int getNum_data_symbols() {
		return num_data_symbols;
	}

	public int getRegen_flag() {
		return regen_flag;
	}

	public int getL1_post_extension() {
		return l1_post_extension;
	}

	public int getNum_rf() {
		return num_rf;
	}

	public int getCurrent_rf_idx() {
		return current_rf_idx;
	}

	public int getT2_version() {
		return t2_version;
	}

	public int getL1_post_scrambled() {
		return l1_post_scrambled;
	}

	public int getT2_base_lite() {
		return t2_base_lite;
	}

	public int getReserved() {
		return reserved;
	}


}

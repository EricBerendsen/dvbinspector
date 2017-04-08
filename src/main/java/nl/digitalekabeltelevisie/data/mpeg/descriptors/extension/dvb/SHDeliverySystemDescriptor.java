/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.*;

public class SHDeliverySystemDescriptor extends DVBExtensionDescriptor {
	
	public class Modulation implements TreeNode{
		
		private int modulation_type;
		private int interleaver_presence;
		private int interleaver_type;
		private int reserved;

		
		
		private int polarization;
		private int roll_off;
		private int modulation_mode;
		private int code_rate;
		private int symbol_rate;
		private int reserved2;

		
		private int bandwidth;
		private int priority;
		private int constellation_and_hierarchy;
		private int guard_interval;
		private int transmission_mode;
		private int common_frequency;
		
		private int common_multiplier;
		private int nof_late_taps;
		private int nof_slices;
		private int slice_distance;
		private int non_late_increments ;

		private int reserved3;



		public Modulation(BitSource bs) {
			
			modulation_type = bs.readBits(1);
			interleaver_presence = bs.readBits(1);
			interleaver_type = bs.readBits(1);
			reserved= bs.readBits(5);
			
			if (modulation_type == 0) {
				polarization = bs.readBits(2);
				roll_off = bs.readBits(2);
				modulation_mode = bs.readBits(2);
				code_rate = bs.readBits(4);
				symbol_rate = bs.readBits(5);
				reserved2 = bs.readBits(1);
			} else {
				bandwidth = bs.readBits(3);
				priority = bs.readBits(1);
				constellation_and_hierarchy = bs.readBits(3);
				code_rate = bs.readBits(4);
				guard_interval = bs.readBits(2);
				transmission_mode = bs.readBits(2);
				common_frequency = bs.readBits(1);
			}
			if (interleaver_presence == 1) {
				if (interleaver_type == 0) {
					common_multiplier = bs.readBits(6);
					nof_late_taps = bs.readBits(6);
					nof_slices = bs.readBits(6);
					slice_distance = bs.readBits(8);
					non_late_increments = bs.readBits(6);
				} else {
					common_multiplier = bs.readBits(6);
					reserved3 = bs.readBits(2);
				}
			}
			
		}
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			final DefaultMutableTreeNode t =  new DefaultMutableTreeNode(new KVP("Modulation"));
			t.add(new DefaultMutableTreeNode(new KVP("modulation_type",modulation_type,modulation_type==0?"TDM":"OFDM")));
			t.add(new DefaultMutableTreeNode(new KVP("interleaver_presence",interleaver_presence,interleaver_presence==0?"no interleaver info follows":"an interleaver info follows")));
			t.add(new DefaultMutableTreeNode(new KVP("interleaver_type",interleaver_type,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			
			if (modulation_type == 0) {
				t.add(new DefaultMutableTreeNode(new KVP("polarization",polarization,null)));
				t.add(new DefaultMutableTreeNode(new KVP("roll_off",roll_off,null)));
				t.add(new DefaultMutableTreeNode(new KVP("modulation_mode",modulation_mode,null)));
				t.add(new DefaultMutableTreeNode(new KVP("code_rate",code_rate,null)));
				t.add(new DefaultMutableTreeNode(new KVP("symbol_rate",symbol_rate,null)));
				t.add(new DefaultMutableTreeNode(new KVP("reserved2",reserved2,null)));
			}else{
				t.add(new DefaultMutableTreeNode(new KVP("bandwidth",bandwidth,null)));
				t.add(new DefaultMutableTreeNode(new KVP("priority",priority,null)));
				t.add(new DefaultMutableTreeNode(new KVP("constellation_and_hierarchy",constellation_and_hierarchy,null)));
				t.add(new DefaultMutableTreeNode(new KVP("code_rate",code_rate,null)));
				t.add(new DefaultMutableTreeNode(new KVP("guard_interval",guard_interval,null)));
				t.add(new DefaultMutableTreeNode(new KVP("transmission_mode",transmission_mode,null)));
				t.add(new DefaultMutableTreeNode(new KVP("common_frequency",common_frequency,null)));
			}
			
			if (interleaver_presence == 1) {
				if (interleaver_type == 0) {
					t.add(new DefaultMutableTreeNode(new KVP("common_multiplier",common_multiplier,null)));
					t.add(new DefaultMutableTreeNode(new KVP("nof_late_taps",nof_late_taps,null)));
					t.add(new DefaultMutableTreeNode(new KVP("nof_slices",nof_slices,null)));
					t.add(new DefaultMutableTreeNode(new KVP("slice_distance",slice_distance,null)));
					t.add(new DefaultMutableTreeNode(new KVP("non_late_increments",non_late_increments,null)));
				} else {
					t.add(new DefaultMutableTreeNode(new KVP("common_multiplier",common_multiplier,null)));
					t.add(new DefaultMutableTreeNode(new KVP("reserved3",reserved3,null)));
				}
			}

			return t;
		}
		
		
		
	}


	// T2 delivery descriptor 0x04

	private final int diversity_mode;
	private final int reserved;
	
	private List<Modulation> modulations = new ArrayList<>(); 

	public SHDeliverySystemDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		diversity_mode = getInt(b, offset+3, 1, 0xF0)>>4;
		reserved = getInt(b, offset+3, 1, MASK_4BITS);
		
		final BitSource bs =new BitSource(selector_byte, 1);
		
		while(bs.available()>0){
			Modulation modulation = new Modulation(bs);
			modulations.add(modulation);
		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("diversity_mode",diversity_mode,getDiversityModeDescription(diversity_mode))));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		
		Utils.addListJTree(t,modulations,modus,"modulations");

		return t;
	}


	private static String getDiversityModeDescription(int diversity_mode) {

		switch (diversity_mode) {

		case 0b0000:
			return "paTS:no, FEC diversity:no, FEC at phy:no, FEC at link:no";
		case 0b1000:
			return "paTS:yes, FEC diversity:no, FEC at phy:no, FEC at link:no";
		case 0b1101:
			return "paTS:yes, FEC diversity:yes, FEC at phy:no, FEC at link:yes";
		case 0b1110:
			return "paTS:yes, FEC diversity:yes, FEC at phy:yes, FEC at link:no";
		case 0b1111:
			return "paTS:yes, FEC diversity:yes, FEC at phy:yes, FEC at link:yes";
		default:
			return "reserved for future use";
		}
	}



}

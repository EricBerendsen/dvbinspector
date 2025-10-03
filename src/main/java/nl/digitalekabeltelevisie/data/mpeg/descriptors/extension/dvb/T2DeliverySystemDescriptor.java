/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.*;

public class T2DeliverySystemDescriptor extends DVBExtensionDescriptor{
	
	private class CellInfo implements TreeNode{

        public record CentreFrequency(int centre_frequency) implements TreeNode {

            @Override
            public KVP getJTreeNode(int modus) {
                return new KVP("centre_frequency", centre_frequency, formatTerrestrialFrequency(centre_frequency));
            }

        }
		
		public static class SubCellInfo implements TreeNode{
			
			int cell_id_extension ;
			int transposer_frequency ; 

			
			public SubCellInfo(int cell_id_extension,	int transposer_frequency) {
				this.cell_id_extension = cell_id_extension;
				this.transposer_frequency = transposer_frequency;
			}
			
			@Override
			public KVP getJTreeNode(int modus){
                KVP s = new KVP("SubCellInfo");
				s.add(new KVP("cell_id_extension",cell_id_extension));
				s.add(new KVP("transposer_frequency",transposer_frequency, formatTerrestrialFrequency(transposer_frequency)));
				return s;
			}
			
		}

		
		private int cell_id;
		private int frequency_loop_length;
		private List<CentreFrequency> centreFrequencyList = new ArrayList<>();
		private int centre_frequency;
		private int subcell_info_loop_length;
		private List<SubCellInfo> subCellInfoList = new ArrayList<>();

		public CellInfo(BitSource bs) {
			
			cell_id = bs.readBits(16); 
			if (tfs_flag == 1){ 
				frequency_loop_length = bs.readBits(8);
				for (int j=0;j<(frequency_loop_length/4);j++){ 
					int centre_frequency = bs.readBits(32); 
					centreFrequencyList.add(new CentreFrequency(centre_frequency));
				}
			}else{ 
				centre_frequency = bs.readBits(32); 
			}
			subcell_info_loop_length = bs.readBits(8);
			for (int k=0;k<(subcell_info_loop_length/5);k++)
			{ 
				int cell_id_extension = bs.readBits(8);
				int transposer_frequency = bs.readBits(32); 
				subCellInfoList.add(new SubCellInfo(cell_id_extension, transposer_frequency));
			}
		}

        @Override
        public KVP getJTreeNode(int modus) {
            KVP s = new KVP("CellInfo");
            s.add(new KVP("cell_id", cell_id));
            if (tfs_flag == 1) {
                s.add(new KVP("frequency_loop_length", frequency_loop_length));
                addListJTree(s, centreFrequencyList, modus, "centre frequencies");
            } else {
                s.add(new KVP("centre_frequency", centre_frequency, formatTerrestrialFrequency(centre_frequency)));
            }
            s.add(new KVP("subcell_info_loop_length", subcell_info_loop_length));
            addListJTree(s, subCellInfoList, modus, "subcell_info_loop");

            return s;
        }
		
		
	}


	private static final LookUpList  siso_miso_mode_list = new LookUpList.Builder()
			.add(0x00, "SISO")
			.add(0x01, "MISO")
			.add(0x02, "reserved for future use")
			.add(0x03, "reserved for future use")
			.build();

	private static final LookUpList bandwidth_list = new LookUpList.Builder()
			.add(0b0000, "8 MHz")
			.add(0b0001, "7 MHz")
			.add(0b0010, "6 MHz")
			.add(0b0011, "5 MHz")
			.add(0b0100, "10 MHz")
			.add(0b0101, "1,712 MHz")
			.add(0b0111, 0b1111, "reserved for future use")
			.build();

	
	private static final LookUpList guard_interval_list = new LookUpList.Builder()
			.add(0b000, "1/32")
			.add(0b001, "1/16")
			.add(0b010, "1/8")
			.add(0b011, "1/4")
			.add(0b100, "1/128")
			.add(0b101, "19/128")
			.add(0b110, "19/256")
			.add(0b111, "reserved for future use")
			.build();

	
	private static final LookUpList transmission_mode_list = new LookUpList.Builder()
			.add(0b000 , "2k mode")
			.add(0b001 , "8k mode")
			.add(0b010 , "4k mode")
			.add(0b011 , "1k mode")
			.add(0b100 , "16k mode")
			.add(0b101 , "32k mode")
			.add(0b110,0b111 , "reserved for future use")
			.build();

	// T2 delivery descriptor 0x04

	private final int plp_id;
	private final int t2_system_id;
	private int siso_miso;
	private int bandwidth;
	private int reserved_future_use;
	private int guard_interval;
	private int transmission_mode;
	private int other_frequency_flag;
	private int tfs_flag;
	private List<CellInfo> cellInfoList = new ArrayList<>();

	public T2DeliverySystemDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		plp_id = getInt(b, 3, 1, MASK_8BITS);
		t2_system_id = getInt(b, 4, 2, MASK_16BITS);
		if (descriptorLength > 4) {
			siso_miso = getInt(b, 6, 1, 0b1100_0000) >> 6; // 2 bslbf
			bandwidth = getInt(b,  6, 1, 0b0011_1100) >> 2; // 4 bslbf
			reserved_future_use = getInt(b, 6, 1, 0b0000_0011); // 2 bslbf
			guard_interval = getInt(b, 7, 1, 0b1110_0000) >> 5; // 3 bslbf
			transmission_mode = getInt(b, 7, 1, 0b0001_1100) >> 2; // 3 bslbf
			other_frequency_flag = getInt(b, 7, 1, 0b0000_0010) >> 1; // 1 bslbf
			tfs_flag = getInt(b, 7, 1, 0b0000_0001); // 1 bslbf
			BitSource bs = new BitSource(b, 8, descriptorLength + 2);
			while (bs.available() > 0) {
				CellInfo ci = new CellInfo(bs);
				cellInfoList.add(ci);
			}
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("plp_id", plp_id, null));
		t.add(new KVP("T2_system_id", t2_system_id));
		if (descriptorLength > 4) {
			t.add(new KVP("siso_miso", siso_miso, siso_miso_mode_list.get(siso_miso)));
			t.add(new KVP("bandwidth", bandwidth, bandwidth_list.get(bandwidth)));
			t.add(new KVP("reserved_future_use", reserved_future_use));
			t.add(new KVP("guard_interval", guard_interval, guard_interval_list.get(guard_interval)));
			t.add(new KVP("transmission_mode", transmission_mode, transmission_mode_list.get(transmission_mode)));
			t.add(new KVP("other_frequency_flag", other_frequency_flag));
			t.add(new KVP("tfs_flag", tfs_flag, tfs_flag==0?"No TFS arrangement in place":"TFS arrangement in place "));
			addListJTree(t,cellInfoList,modus,"cell_info_loop");
		}
		return t;
	}

	public static LookUpList getSisoMisoModeList() {
		return siso_miso_mode_list;
	}

	public static LookUpList getBandwidthList() {
		return bandwidth_list;
	}

	public static LookUpList getGuardIntervalList() {
		return guard_interval_list;
	}

	public static LookUpList getTransmissionModeList() {
		return transmission_mode_list;
	}

	public int getPlp_id() {
		return plp_id;
	}

	public int getT2_system_id() {
		return t2_system_id;
	}

	public int getSiso_miso() {
		return siso_miso;
	}

	public int getBandwidth() {
		return bandwidth;
	}

	public int getReserved_future_use() {
		return reserved_future_use;
	}

	public int getGuard_interval() {
		return guard_interval;
	}

	public int getTransmission_mode() {
		return transmission_mode;
	}

	public int getOther_frequency_flag() {
		return other_frequency_flag;
	}

	public int getTfs_flag() {
		return tfs_flag;
	}

	public List<CellInfo> getCellInfoList() {
		return cellInfoList;
	}

}

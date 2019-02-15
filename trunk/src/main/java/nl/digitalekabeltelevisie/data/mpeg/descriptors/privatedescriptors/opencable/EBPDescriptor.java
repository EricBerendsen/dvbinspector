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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.opencable;

import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.*;

public class EBPDescriptor extends Descriptor {
	
	// based on OpenCable™ Specifications 
	// Encoder Boundary Point Specification 
	// OC-SP-EBP-I01-130118
	// 7.1.3 EBP_descriptor 
	
	
	private class Partition implements TreeNode{
		private int EBP_data_explicit_flag;
		private int representation_id_flag;
		private int partition_id;
		private int reserved;
		private int EBP_PID;
		private int reserved2;
		private int boundary_flag;
		private byte[] EBP_distance;
		private int SAP_type_max;
		private int reserved3;
		private int reserved4;
		private int acquisition_time_flag;
		private long representation_id;

		public Partition(BitSource bitSource, int ebp_distance_width) {
			EBP_data_explicit_flag = bitSource.readBits(1);
			representation_id_flag = bitSource.readBits(1);
			partition_id = bitSource.readBits(5);

			if (EBP_data_explicit_flag == 0) {
				reserved = bitSource.readBits(1);
				EBP_PID = bitSource.readBits(13);
				reserved2 = bitSource.readBits(3);
			} else {
				boundary_flag = bitSource.readBits(1);
				EBP_distance = bitSource.readBytes(ebp_distance_width);
				if (boundary_flag == 1) {
					SAP_type_max = bitSource.readBits(3);
					reserved3 = bitSource.readBits(4);
				} else {
					reserved4 = bitSource.readBits(7);
				}

				acquisition_time_flag = bitSource.readBits(1);
				if (representation_id_flag == 1) {
					representation_id = bitSource.readBitsLong(64);
				}
			}
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("Partition"));
			
			t.add(new DefaultMutableTreeNode(new KVP("EBP_data_explicit_flag",EBP_data_explicit_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("representation_id_flag",representation_id_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("partition_id",partition_id,null)));

			if (EBP_data_explicit_flag == 0) {
				t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_PID",EBP_PID,null)));
				t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved2,null)));
			} else {
				t.add(new DefaultMutableTreeNode(new KVP("boundary_flag",boundary_flag,null)));
				t.add(new DefaultMutableTreeNode(new KVP("EBP_distance",EBP_distance,null)));
				if (boundary_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("SAP_type_max",SAP_type_max,null)));
					t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved3,null)));
				} else {
					t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved4,null)));
				}

				t.add(new DefaultMutableTreeNode(new KVP("acquisition_time_flag",acquisition_time_flag,null)));
				if (representation_id_flag == 1) {
					t.add(new DefaultMutableTreeNode(new KVP("representation_id",representation_id,null)));
				}
			}

			
			
			return t;
		}
		
	}

	private int num_partitions;
	private int timescale_flag;
	private int reserved;
	private int ticks_per_second;
	private int EBP_distance_width_minus_1;
	private List<Partition> partitionList = new ArrayList<>();

	public EBPDescriptor(byte[] bytes, int offset, TableSection parent) {
		super(bytes, offset, parent);
		if (descriptorLength > 0) {
			num_partitions = getInt(bytes, offset + 2, 1, 0b1111_1000) >> 3;
			timescale_flag = getInt(bytes, offset + 2, 1, 0b0000_0100) >> 2;
			reserved = getInt(bytes, offset + 2, 1, 0b0000_0011);

			int localOffset = offset + 3;

			if (timescale_flag == 1) {
				ticks_per_second = getInt(bytes, localOffset, 3, 0b1111_1111_1111_1111_1111_1000) >> 3;
				EBP_distance_width_minus_1 = getInt(bytes, localOffset + 2, 1, 0b0000_0111);
				localOffset += 3;
			}
			BitSource bitSource = new BitSource(bytes, localOffset);
			for (int i = 0; i < num_partitions; i++) {
				Partition partition = new Partition(bitSource, EBP_distance_width_minus_1 + 1);
				partitionList.add(partition);
			}
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		 if (descriptorLength > 0)     {
			 t.add(new DefaultMutableTreeNode(new KVP("num_partitions",num_partitions,null)));
			 t.add(new DefaultMutableTreeNode(new KVP("timescale_flag",timescale_flag,null)));
			 t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			 
			if (timescale_flag == 1) {
				 t.add(new DefaultMutableTreeNode(new KVP("ticks_per_second",ticks_per_second,"precision, in ticks per second, of the EBP_distance field")));
				 t.add(new DefaultMutableTreeNode(new KVP("EBP_distance_width_minus_1",EBP_distance_width_minus_1," length, in bytes (minus one), of the EBP_distance field")));
			}
			if(!partitionList.isEmpty()) {
				Utils.addListJTree(t, partitionList, modus, "Partitions");
			}
		 }

		return t;
	}

	
	@Override
	public String getDescriptorname(){
		return "EBP_descriptor";
	}

}

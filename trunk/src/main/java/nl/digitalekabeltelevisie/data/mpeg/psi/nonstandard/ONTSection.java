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

package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;

public class ONTSection extends TableSectionExtendedSyntax {
	
	public class OperatorBrand implements TreeNode{

		
		private int operator_network_id;
		private int operator_sublist_id;
		private int reserved_future_use;
		private int operator_descriptors_length;
		
		public List<Descriptor> descriptorList;
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(int modus) {
			
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("operator_brand",operator_network_id + "-"+ operator_sublist_id,null));

			t.add(new DefaultMutableTreeNode(new KVP("operator_network_id",operator_network_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("operator_sublist_id",operator_sublist_id,null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_future_use",reserved_future_use,null)));
			t.add(new DefaultMutableTreeNode(new KVP("operator_descriptors_length",operator_descriptors_length,null)));

			Utils.addListJTree(t,descriptorList,modus,"operator_descriptors");

			return t;
		}

		public int getOperator_network_id() {
			return operator_network_id;
		}

		public void setOperator_network_id(int operator_network_id) {
			this.operator_network_id = operator_network_id;
		}

		public int getOperator_sublist_id() {
			return operator_sublist_id;
		}

		public void setOperator_sublist_id(int operator_sublist_id) {
			this.operator_sublist_id = operator_sublist_id;
		}

		public int getReserved_future_use() {
			return reserved_future_use;
		}

		public void setReserved_future_use(int reserved_future_use) {
			this.reserved_future_use = reserved_future_use;
		}

		public int getOperator_descriptors_length() {
			return operator_descriptors_length;
		}

		public void setOperator_descriptors_length(int operator_descriptors_length) {
			this.operator_descriptors_length = operator_descriptors_length;
		}

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}
		
	}

	private int reserved;
	private int bouquet_descriptors_loop_length;
	
	private List<Descriptor> bouquetDescriptorList;
	private int reserved2;
	private int operator_brands_loop_length;
	
	private List<OperatorBrand> operatorBrandList;


	public ONTSection(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);
		reserved = Utils.getInt(raw_data.getData(), 8, 2, 0xF000)>>>12;
		bouquet_descriptors_loop_length = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_12BITS);
		bouquetDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),10,bouquet_descriptors_loop_length,this);
		reserved2 = Utils.getInt(raw_data.getData(), 10 +bouquet_descriptors_loop_length , 2, 0xF000)>>>12;
		operator_brands_loop_length = Utils.getInt(raw_data.getData(), 10 +bouquet_descriptors_loop_length , 2, Utils.MASK_12BITS);
		operatorBrandList = buildOperatorBrandList(raw_data.getData(), 12+bouquet_descriptors_loop_length, operator_brands_loop_length);
	}
	
	private List<OperatorBrand> buildOperatorBrandList(byte[] data, int i, int operator_brands_loop_length2) {
		final ArrayList<OperatorBrand> r = new ArrayList<>();
		int t =0;
		while(t<operator_brands_loop_length2){
			final OperatorBrand c = new OperatorBrand();
			c.setOperator_network_id(Utils.getInt(data, i+t, 2, Utils.MASK_16BITS));
			c.setOperator_sublist_id(Utils.getInt(data, i+t+2, 1, Utils.MASK_8BITS));
			c.setReserved_future_use(Utils.getInt(data, i+t+3, 1, 0xF0)>>>4);
			final int operator_descriptors_length = Utils.getInt(data, i+t+3, 2, Utils.MASK_12BITS);
			c.setOperator_descriptors_length(operator_descriptors_length);
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,i+t+5,operator_descriptors_length,this));
			t+=5+operator_descriptors_length;
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("bouquet_descriptors_loop_length",bouquet_descriptors_loop_length,null)));
		Utils.addListJTree(t,bouquetDescriptorList,modus,"bouquet_descriptors");
		t.add(new DefaultMutableTreeNode(new KVP("operator_brands_loop_length",operator_brands_loop_length,null)));
		Utils.addListJTree(t,operatorBrandList,modus,"operator_brands_loop");


		return t;
	}


}

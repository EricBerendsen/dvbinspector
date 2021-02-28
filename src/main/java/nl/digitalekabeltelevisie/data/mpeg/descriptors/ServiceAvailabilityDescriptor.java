/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.MASK_7BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ServiceAvailabilityDescriptor extends Descriptor {

	private final List<Cell> cellList = new ArrayList<>();
	private int availability_flag;
	private int reserved;


	private static class Cell implements TreeNode{
		private int cell_id;

		public Cell(final int cell_id){
			this.cell_id = cell_id;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("cell_id",cell_id,null));
			return s;

		}

	}

	public ServiceAvailabilityDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		availability_flag = getInt(b, offset + 2, 1, 0b1000_0000) >> 7;
		reserved = getInt(b, offset + 2, 1, MASK_7BITS);
		int t = 0;
		while (t < descriptorLength - 1) {
			int cell_id = getInt(b, offset + 3 + t, 1, MASK_8BITS);

			final Cell s = new Cell(cell_id);
			cellList.add(s);
			t += 1;
		}
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("availability_flag",availability_flag,getAvailabilityString())));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
		addListJTree(t,cellList,modus,"cell_ids");
		return t;
	}


	private String getAvailabilityString() {
		return availability_flag==0?
				"service is unavailable on the cells identified by the cell_ids":
				"service is available on the cells identified by the cell_ids";
	}

	public List<Cell> getServiceList() {
		return cellList;
	}


	public List<Cell> getCellList() {
		return cellList;
	}


	public int getAvailability_flag() {
		return availability_flag;
	}


	public int getReserved() {
		return reserved;
	}

}

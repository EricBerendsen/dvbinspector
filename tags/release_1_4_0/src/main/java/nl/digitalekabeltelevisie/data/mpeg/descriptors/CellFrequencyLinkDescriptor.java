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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class CellFrequencyLinkDescriptor extends Descriptor {

	private final List<Cell> cellList = new ArrayList<Cell>();


	public static class Cell implements TreeNode{
		/**
		 *
		 */
		private final int cellId ;
		private final long frequency; // 32-bit uimsbf field giving the binary coded frequency value in multiples of 10 Hz.
		private final int subcellInfoLoopLength;

		private final List<SubCell> subCellList = new ArrayList<SubCell>();

		public Cell(final int id,final long f,final int loop){
			cellId = id;
			frequency = f;
			subcellInfoLoopLength = loop;
		}

		public void addSubCell(final SubCell s){
			subCellList.add(s);
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("cell"));
			s.add(new DefaultMutableTreeNode(new KVP("cell_id",cellId,null)));
			s.add(new DefaultMutableTreeNode(new KVP("frequency",frequency,Descriptor.formatTerrestrialFrequency(frequency))));
			s.add(new DefaultMutableTreeNode(new KVP("subcell_info_loop_length",subcellInfoLoopLength,null)));
			addListJTree(s,subCellList,modus,"sub_cell_list");
			return s;
		}
	}

	public static class SubCell implements TreeNode{
		/**
		 *
		 */
		private final int cellIdExtension ;
		private final long transposerFrequency; // 32-bit uimsbf field giving the binary coded frequency value in multiples of 10 Hz.


		public SubCell(final int id,final long f){
			cellIdExtension = id;
			transposerFrequency = f;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("subcell"));
			s.add(new DefaultMutableTreeNode(new KVP("cell_id_extension",cellIdExtension,null)));
			s.add(new DefaultMutableTreeNode(new KVP("transposer_frequency",transposerFrequency,Descriptor.formatTerrestrialFrequency(transposerFrequency))));
			return s;
		}
	}

	public CellFrequencyLinkDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while ((t+6)<descriptorLength) {
			final int cell_id = getInt(b,offset+ t+2, 2, Utils.MASK_16BITS);
			final long  freq = getLong(b, offset+t+4, 4, MASK_32BITS);
			final int subcell_info_loop_length = getInt(b,offset+ t+8, 1, Utils.MASK_8BITS);
			final Cell cell = new Cell(cell_id,freq, subcell_info_loop_length);
			cellList.add(cell);
			t+=7;
			int r =0;
			while (r<subcell_info_loop_length ) {
				final int cell_id_extension = getInt(b,offset+ t+2+r, 1, Utils.MASK_8BITS);
				final long  trans_freq = getLong(b, offset+t+3, 4, MASK_32BITS);
				final SubCell s = new SubCell(cell_id_extension,trans_freq);
				cell.addSubCell(s);
				r=r+5;

			}
			t=t+r;

		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Cell cell : cellList) {
			buf.append(cell.toString());
		}


		return buf.toString();
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,cellList,modus,"cell_list");
		return t;
	}
}

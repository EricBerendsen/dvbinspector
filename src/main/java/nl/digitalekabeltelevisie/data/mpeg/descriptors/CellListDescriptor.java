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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class CellListDescriptor extends Descriptor {

	private final List<Cell> cellList = new ArrayList<>();


	public static class Cell implements TreeNode{
		/**
		 *
		 */
		private final int cellId ;

		private final int cellLatitude;
		private final int cellLongitude;
		private final int cellExtentOfLatitude;
		private final int cellExtentOfLongitude;

		// private long frequency; // 32-bit uimsbf field giving the binary coded frequency value in multiples of 10 Hz.
		private final int subcellInfoLoopLength;

		private List<SubCell> subCellList = new ArrayList<>();


		public Cell(final int cellId, final int cellLatitude, final int cellLongitude, final int cellExtentOfLatitude, final int cellExtentOfLongitude, final int subcellInfoLoopLength) {
			super();
			this.cellId = cellId;
			this.cellLatitude = cellLatitude;
			this.cellLongitude = cellLongitude;
			this.cellExtentOfLatitude = cellExtentOfLatitude;
			this.cellExtentOfLongitude = cellExtentOfLongitude;
			this.subcellInfoLoopLength = subcellInfoLoopLength;
		}


		public void addSubCell(final SubCell s){
			subCellList.add(s);
		}


		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new KVP("cell");
			s.add(new KVP("cell_id",cellId));

			s.add(new KVP("cell_latitude",cellLatitude,getCellLatitudeString(cellLatitude)));
			s.add(new KVP("cell_longitude",cellLongitude,getCellLongitudeString(cellLongitude)));
			s.add(new KVP("cell_extent_of_latitude",cellExtentOfLatitude,getCellLatitudeString(cellExtentOfLatitude)));
			s.add(new KVP("cell_extent_of_longitude",cellExtentOfLongitude,getCellLongitudeString(cellExtentOfLongitude)));
			s.add(new KVP("subcell_info_loop_length",subcellInfoLoopLength));
			addListJTree(s,subCellList,modus,"sub_cell_list");
			return s;
		}
	}

	public static record SubCell(int cellIdExtension, int subcellLatitude, int subcellLongitude, int subcellExtentOfLatitude,
			int subcellExtentOfLongitude) implements TreeNode {

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("subcell");
			s.add(new KVP("cell_id_extension", cellIdExtension));
			s.add(new KVP("subcell_latitude", subcellLatitude, getCellLatitudeString(subcellLatitude)));
			s.add(new KVP("subcell_longitude", subcellLongitude, getCellLongitudeString(subcellLongitude)));
			s.add(new KVP("subcell_extent_of_latitude", subcellExtentOfLatitude, getCellLatitudeString(subcellExtentOfLatitude)));
			s.add(new KVP("subcell_extent_of_longitude", subcellExtentOfLongitude, getCellLongitudeString(subcellExtentOfLongitude)));
			return s;
		}
	}

	public CellListDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while ((t + 9) < descriptorLength) {
			int cell_id = getInt(b, t + 2, 2, Utils.MASK_16BITS);
			int cell_latitude = getInt(b, t + 4, 2, Utils.MASK_16BITS);
			int cell_longitude = getInt(b, t + 6, 2, Utils.MASK_16BITS);
			int cell_extent_of_latitude = getInt(b, t + 8, 2, 0xFFF0) >> 4;
			int cell_extent_of_longitude = getInt(b, t + 9, 2, Utils.MASK_12BITS);
			int subcell_info_loop_length = getInt(b, t + 11, 1, Utils.MASK_8BITS);

			Cell cell = new Cell(cell_id, cell_latitude, cell_longitude, cell_extent_of_latitude, cell_extent_of_longitude,
					subcell_info_loop_length);
			cellList.add(cell);
			t += 10;
			int r = 0;
			while (r < subcell_info_loop_length) {
				int cell_id_extension = getInt(b, t + 2 + r, 1, Utils.MASK_8BITS);
				int subcell_latitude = getInt(b, t + 3 + r, 2, Utils.MASK_16BITS);
				int subcell_longitude = getInt(b, t + 5 + r, 2, Utils.MASK_16BITS);
				int subcell_extent_of_latitude = getInt(b, t + 7 + r, 2, 0xFFF0) >> 4;
				int subcell_extent_of_longitude = getInt(b, t + 8 + r, 2, Utils.MASK_12BITS);

				SubCell s = new SubCell(cell_id_extension, subcell_latitude, subcell_longitude, subcell_extent_of_latitude,
						subcell_extent_of_longitude);
				cell.addSubCell(s);
				r += 8;

			}
			t += r;
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		for (Cell cell : cellList) {
			buf.append(cell.toString());
		}


		return buf.toString();
	}

	public static String getCellLatitudeString(int latitude){
		int c=latitude;
		if(c>0x7FFF){
			c=c-0x10000;
		}
		float f=(((float)c)*((float)90))/0x8000;
		return Float.toString(f) ;
	}

	public static String getCellLongitudeString(int longitude){
		int c= longitude;
		if(c>0x7FFF){
			c=c-0x10000;
		}
		float f=(((float)c)*((float)180))/0x8000;
		return Float.toString(f) ;
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		addListJTree(t,cellList,modus,"cell_list");
		return t;
	}
}

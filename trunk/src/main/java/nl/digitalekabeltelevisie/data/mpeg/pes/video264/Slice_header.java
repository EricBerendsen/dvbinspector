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

package nl.digitalekabeltelevisie.data.mpeg.pes.video264;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * @author Eric
 *
 */
public class Slice_header implements TreeNode {
	
	private int first_mb_in_slice;
	private int slice_type;
	private int pic_parameter_set_id;

	public Slice_header(BitSource bitSource) {
		first_mb_in_slice = bitSource.ue();
		slice_type = bitSource.ue();
		pic_parameter_set_id = bitSource.ue();	
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("slice_header"));
		s.add(new DefaultMutableTreeNode(new KVP("first_mb_in_slice",first_mb_in_slice,null)));
		s.add(new DefaultMutableTreeNode(new KVP("slice_type",slice_type,getSlice_typeString(slice_type))));
		s.add(new DefaultMutableTreeNode(new KVP("pic_parameter_set_id",pic_parameter_set_id,null)));
		return s;
	}
	
	public static String getSlice_typeString(int slice_type){
		switch (slice_type) {
		case  0: return "P";
		case  1 : return "B";
		case  2 : return "I";
		case  3 : return "SP";
		case  4 : return "SI";
		case  5 : return "P";
		case  6 : return "B";
		case  7 : return "I";
		case  8 : return "SP";
		case  9 : return "SI";

		default:
			return "illegal value";
		}
		
	}

}

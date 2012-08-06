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

public class Access_unit_delimiter_rbsp extends RBSP {
	
	private final int primary_pic_type;

	protected Access_unit_delimiter_rbsp(byte[] rbsp_bytes, int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		primary_pic_type = bitSource.u(3);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("access_unit_delimiter_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("primary_pic_type",primary_pic_type,getPrimaryPicTypeString(primary_pic_type))));
		return t;
	}

	
	public static String getPrimaryPicTypeString(final int primary_pic_type) {

		StringBuilder r = new StringBuilder("slice_type values that may be present in the primary coded picture: ");
		switch (primary_pic_type) {
		case 0 : r.append("I");
			break;
		case 1 : r.append( "I, P");
		break;
		case 2 : r.append( "I, P, B");
		break;
		case 3 : r.append( "SI");
		break;
		case 4 : r.append( "SI, SP");
		break;
		case 5 : r.append( "I, SI");
		break;
		case 6 : r.append( "I, SI, P, SP");
		break;
		case 7 : r.append( "I, SI, P, SP, B");
		break;
		default:
			return "unknown";
		}
		return r.toString();
	}

	public int getPrimary_pic_type() {
		return primary_pic_type;
	}

}

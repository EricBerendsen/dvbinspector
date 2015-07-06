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

package nl.digitalekabeltelevisie.data.mpeg.pes.video265;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.pes.video26x.RBSP;

public class Access_unit_delimiter_rbsp extends RBSP {

	private final int pic_type;
	private final int available;

	protected Access_unit_delimiter_rbsp(final byte[] rbsp_bytes, final int numBytesInRBSP) {
		super(rbsp_bytes, numBytesInRBSP);
		pic_type = bitSource.u(3);

		available = bitSource.available();

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("access_unit_delimiter_rbsp"));
		t.add(new DefaultMutableTreeNode(new KVP("pic_type",pic_type,getPrimaryPicTypeString(pic_type))));
		t.add(new DefaultMutableTreeNode(new KVP("available bits left in bitSource (should be 0)",available,null)));

		return t;
	}


	public static String getPrimaryPicTypeString(final int pic_type) {

		final StringBuilder r = new StringBuilder("slice_type values that may be present in the coded picture: ");
		switch (pic_type) {
		case 0 : r.append("I");
		break;
		case 1 : r.append( "P, I");
		break;
		case 2 : r.append( "B, P, I");
		break;
		default:
			return "unknown";
		}
		return r.toString();
	}

	public int getPic_type() {
		return pic_type;
	}


}

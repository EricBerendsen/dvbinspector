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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.untable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */
public class SSUSubgroupAssociationDescriptor extends UNTDescriptor {


	private final byte[] subgroup_tag;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public SSUSubgroupAssociationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		subgroup_tag = copyOfRange(b, offset+2, offset+descriptorLength+2);



	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("subgroup_tag",subgroup_tag ,subGroupString(subgroup_tag))));
		return t;
	}

	public static String subGroupString(final byte[]b){
		final StringBuilder s = new StringBuilder();
		final int oui = getInt(b, 0, 3,MASK_24BITS);
		final int subgroup_association = getInt(b, 3, 2,MASK_16BITS);
		s.append("OUI:").append(oui).append(", (").append(getOUIString(oui)).append("), subgroup_association=").append(subgroup_association);
		return s.toString();
	}


}

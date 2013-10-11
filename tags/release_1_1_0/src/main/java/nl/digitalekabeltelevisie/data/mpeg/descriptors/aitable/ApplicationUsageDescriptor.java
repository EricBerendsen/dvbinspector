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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;


/**
 *
 */
public class ApplicationUsageDescriptor extends AITDescriptor {

	private final int usage_type;


	/**
	 * 
	 * ETSI TS 102 809 V1.1.1 (2010-01)
	 * 
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public ApplicationUsageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		usage_type = Utils.getInt(b, offset+2, 1,Utils.MASK_8BITS);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("usage_type", usage_type, getUsageTypeString(usage_type))));
		return t;
	}

	public static String getUsageTypeString(final int usage){
		if(usage==0){
			return "reserved";
		}
		if(usage==1){
			return "Digital Text application";
		}
		if((usage>=0x02)&&(usage<=0x7F)){
			return "reserved for future use";
		}
		if((usage>=0x80)&&(usage<=0xff)){
			return "usable by platform specifications";
		}
		return "unknown";

	}
}

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

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;


/**
 *
 */
public class DVBJApplicationLocationDescriptor extends AITDescriptor {
	private final int base_directory_length;
	private final byte[] base_directory_byte;

	private final int classpath_extension_length;
	private final byte[] classpath_extension_byte;

	private final byte[] initial_class_byte;


	public DVBJApplicationLocationDescriptor(final byte[] b, final int offset, final TableSection parent) {

		super(b, offset, parent);

		base_directory_length = getInt(b, offset+2, 1, MASK_8BITS);
		base_directory_byte = Utils.copyOfRange(b, offset+3, offset+base_directory_length+3);

		classpath_extension_length = getInt(b, offset+3+base_directory_length, 1, MASK_8BITS);
		classpath_extension_byte = Utils.copyOfRange(b, offset+4+base_directory_length, offset+4+base_directory_length+classpath_extension_length);

		initial_class_byte = Utils.copyOfRange(b, offset+4+base_directory_length+classpath_extension_length,offset+descriptorLength+2);


	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("base_directory_length", base_directory_length, null)));
		t.add(new DefaultMutableTreeNode(new KVP("base_directory_byte", base_directory_byte, null)));
		t.add(new DefaultMutableTreeNode(new KVP("classpath_extension_length", classpath_extension_length, null)));
		t.add(new DefaultMutableTreeNode(new KVP("classpath_extension_byte", classpath_extension_byte, null)));
		t.add(new DefaultMutableTreeNode(new KVP("initial_class_byte", initial_class_byte, null)));
		return t;
	}
}

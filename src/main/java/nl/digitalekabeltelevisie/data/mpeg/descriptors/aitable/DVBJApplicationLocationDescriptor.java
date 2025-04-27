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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class DVBJApplicationLocationDescriptor extends AITDescriptor {
	private final int base_directory_length;
	private final byte[] base_directory_byte;

	private final int classpath_extension_length;
	private final byte[] classpath_extension_byte;

	private final byte[] initial_class_byte;


	public DVBJApplicationLocationDescriptor(byte[] b, TableSection parent) {

		super(b, parent);

		base_directory_length = getInt(b, 2, 1, MASK_8BITS);
		int from = 3;
		int to = base_directory_length + 3;
		base_directory_byte = copyOfRange(b, from, to);

		classpath_extension_length = getInt(b, 3 + base_directory_length, 1, MASK_8BITS);
		classpath_extension_byte = copyOfRange(b, 4 + base_directory_length, 4 + base_directory_length + classpath_extension_length);

		initial_class_byte = copyOfRange(b, 4 + base_directory_length + classpath_extension_length, descriptorLength + 2);

	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("base_directory_length", base_directory_length));
		t.add(new KVP("base_directory_byte", base_directory_byte));
		t.add(new KVP("classpath_extension_length", classpath_extension_length));
		t.add(new KVP("classpath_extension_byte", classpath_extension_byte));
		t.add(new KVP("initial_class_byte", initial_class_byte));
		return t;
	}
}

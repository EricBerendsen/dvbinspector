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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class SimpleApplicationBoundaryDescriptor extends AITDescriptor {

	private final int boundary_extension_count;
	private final List<BoundaryExtension> boundaryExtensions= new ArrayList<>();

	public static record BoundaryExtension(int boundary_extension_length, byte[] boundary_extension_byte) implements TreeNode {

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP s = new KVP("boundary extension");
			s.add(new KVP("boundary_extension_length", boundary_extension_length));
			s.add(new KVP("boundary_extension_byte", boundary_extension_byte));
			return s;
		}

	}


	/**
	 * @param b
	 * @param parent
	 */
	public SimpleApplicationBoundaryDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		boundary_extension_count = getInt(b, 2, 1, MASK_8BITS);
		int t = 0;
		int extension = 0;

		while (extension < boundary_extension_count) {

			int boundary_extension_length = getInt(b, t + 3, 1, MASK_8BITS);
			byte[] boundary_extension_bytes = Arrays.copyOfRange(b, t + 4, t + 4 + boundary_extension_length);
			boundaryExtensions.add(new BoundaryExtension(boundary_extension_length, boundary_extension_bytes));
			t += boundary_extension_length + 1;
			extension++;
		}

	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("boundary_extension_count", boundary_extension_count));
		addListJTree(t,boundaryExtensions,modus,"boundary_extensions");
		return t;
	}
}

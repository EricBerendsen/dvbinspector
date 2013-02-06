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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class SimpleApplicationBoundaryDescriptor extends AITDescriptor {

	private final int boundary_extension_count;
	private final List<BoundaryExtension> boundaryExtensions= new ArrayList<BoundaryExtension>();


	public static class BoundaryExtension implements TreeNode{

		private final int boundary_extension_length;
		private final byte[] boundary_extension_byte;


		public BoundaryExtension(final int boundary_extension_length2,
				final byte[] boundary_extension_bytes) {
			this.boundary_extension_length = boundary_extension_length2;
			this.boundary_extension_byte = boundary_extension_bytes;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("boundary extension"));
			s.add(new DefaultMutableTreeNode(new KVP("boundary_extension_length",boundary_extension_length,null)));
			s.add(new DefaultMutableTreeNode(new KVP("boundary_extension_byte",boundary_extension_byte,null)));
			return s;
		}

	}



	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public SimpleApplicationBoundaryDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		boundary_extension_count = getInt(b, offset+2, 1, MASK_8BITS);
		int t = 0;

		while (t<boundary_extension_count) {

			final int boundary_extension_length = getInt(b, offset+t+3, 1, MASK_8BITS);
			final byte[] boundary_extension_bytes =Arrays.copyOfRange(b, offset+t+4, offset+t+4+boundary_extension_length);
			boundaryExtensions.add(new BoundaryExtension(boundary_extension_length,boundary_extension_bytes));
			t += boundary_extension_length +1;
		}

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("boundary_extension_count", boundary_extension_count, null)));
		addListJTree(t,boundaryExtensions,modus,"boundary_extensions");
		return t;
	}
}

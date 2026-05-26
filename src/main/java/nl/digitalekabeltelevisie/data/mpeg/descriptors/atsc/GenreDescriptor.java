/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class GenreDescriptor extends AtscDescriptor {

	private final int attributeCount;
	private final List<GenreAttribute> attributes = new ArrayList<>();

	public GenreDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		attributeCount = getInt(b, PRIVATE_DATA_OFFSET, 1, 0x1F);
		for (int i = 0; i < attributeCount; i++) {
			attributes.add(new GenreAttribute(getInt(b, PRIVATE_DATA_OFFSET + 1 + i, 1, MASK_8BITS)));
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("attribute_count", attributeCount));
		addListJTree(t, attributes, modus, "attributes");
		return t;
	}

	public int getAttributeCount() {
		return attributeCount;
	}

	public List<GenreAttribute> getAttributes() {
		return attributes;
	}

	public record GenreAttribute(int attribute) implements TreeNode {

		@Override
		public KVP getJTreeNode(final int modus) {
			return new KVP("attribute", attribute);
		}
	}
}

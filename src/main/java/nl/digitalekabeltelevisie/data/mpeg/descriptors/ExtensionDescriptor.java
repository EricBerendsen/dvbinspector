/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public abstract class ExtensionDescriptor extends Descriptor {

	protected final int descriptor_tag_extension;
	protected final byte[] selector_byte;

	public ExtensionDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		descriptor_tag_extension = getInt(b, PRIVATE_DATA_OFFSET, 1, MASK_8BITS);
		selector_byte=getBytes(b, PRIVATE_DATA_OFFSET + 1, descriptorLength-1);
	}

	/**
	 * This will always return a KVP, but to not break interface it still is declared as DefaultMutableTreeNode
	 */
	@Override
	public KVP getJTreeNode(int modus){

		KVP t = (KVP) super.getJTreeNode(modus);
		t.add(new KVP("descriptor_tag_extension",descriptor_tag_extension).setDescription(getDescriptorTagString()));
		t.add(new KVP("selector_byte",selector_byte));

		return t;
	}

	@Override
	public String getDescriptorname() {
		return getDescriptorname(descriptorTag, parentTableSection)+" ("+getDescriptorTagString()+")";
	}

	public abstract String getDescriptorTagString();

}

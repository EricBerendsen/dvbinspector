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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;

/**
 *
 * Based on B.2.2.4.1 Caching priority descriptor, ETSI TS 102 727 V1.1.1 (2010-01) or
 * ETSI TS 102 812 V1.3.1 Multimedia Home Platform (MHP) Specification 1.1.3 B.2.2.4.1 Label descriptor
 *
 * @author Eric
 *
 */
public class LabelDescriptor extends DSMCCDescriptor {

	private byte[] label_byte;

	public LabelDescriptor(final byte[] b, final int offset) {
		super(b, offset);
		label_byte = getBytes(b, offset + 2, descriptorLength);
	}


	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("label_byte", label_byte, null)));
		return t;
	}


}

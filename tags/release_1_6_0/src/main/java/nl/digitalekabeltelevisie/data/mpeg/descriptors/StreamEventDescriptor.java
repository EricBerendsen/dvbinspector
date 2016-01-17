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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * Based on 8.3 Stream Event Descriptor of ISO/IEC 13818-6:1998(E)
 *
 * @author Eric
 *
 */
public class StreamEventDescriptor extends Descriptor {


	private int eventId;
	private int reserved;
	private long eventNPT;
	private final byte[]  privateDataByte;

	public StreamEventDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		eventId = getInt(b, offset+2, 2, MASK_16BITS);
		reserved = getInt(b, offset+4, 4, MASK_31BITS)>>1;
		eventNPT = getLong(b, offset+7, 5, MASK_33BITS);
		privateDataByte = getBytes(b,offset+12,descriptorLength-10);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("eventId",eventId ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("reserved",reserved ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("eventNPT",eventNPT ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("privateDataByte",privateDataByte ,null)));
		return t;
	}

}

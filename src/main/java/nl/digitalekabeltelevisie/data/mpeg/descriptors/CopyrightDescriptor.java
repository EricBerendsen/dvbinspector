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

import static nl.digitalekabeltelevisie.util.Utils.getBytes;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * Rec. ITU-T H.222.0 (06/2021)  2.6.24 Copyright descriptor
 */
public class CopyrightDescriptor extends Descriptor {

	
	private final byte[]  copyright_identifier;
	private final byte[]  additional_copyright_Info;

	public CopyrightDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);

		copyright_identifier = getBytes(b,offset+2,4);
		additional_copyright_Info = getBytes(b,offset+6,descriptorLength-4);

	}
	


	public byte[] getCopyrightIdentifier() {
		return copyright_identifier;
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("copyright_identifier",copyright_identifier ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("additional_copyright_Info",additional_copyright_Info ,null)));
		return t;
	}


}

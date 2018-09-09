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

public class AncillaryDataDescriptor extends Descriptor {

	private final int ancillaryDataIdentifier;
	public AncillaryDataDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		ancillaryDataIdentifier = getInt(b,offset+2,1,MASK_8BITS);
	}

	@Override
	public String toString() {
		return super.toString() + "ancillaryDataIdentifier="+ancillaryDataIdentifier;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("ancillary_data_identifier",ancillaryDataIdentifier ,ancillaryDataIdentifierString(ancillaryDataIdentifier))));
		return t;
	}

	public static String ancillaryDataIdentifierString(final int id) {
		final StringBuilder r = new StringBuilder();
		if ((id & 0x01)!=0) {
			r.append("DVD-Video Ancillary Data ");
		}
		if ((id & 0x02)!=0) {
			r.append("Extended Ancillary Data ");
		}
		if ((id & 0x04)!=0) {
			r.append("Announcement Switching Data ");
		}
		if ((id & 0x08)!=0) {
			r.append("DAB Ancillary Data ");
		}
		if ((id & 0x10)!=0) {
			r.append("Scale Factor Error Check (ScF-CRC) ");
		}
		if ((id & 0x20)!=0) {
			r.append("MPEG-4 ancillary data ");
		}
		if ((id & 0x40)!=0) {
			r.append("RDS via UECP ");
		}
		if ((id & 0x80)!=0) {
			r.append("reserved for future use ");
		}
		return r.toString();
	}

	public int getAncillaryDataIdentifier() {
		return ancillaryDataIdentifier;
	}


}

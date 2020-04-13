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

public class RegistrationDescriptor extends Descriptor {

	public static final byte[] SCTE_35 = {'C','U','E','I'};
	public static final byte[] AC_3 = {'A','C','-','3'};

	private final byte[]  formatIdentifier;
	private final byte[]  additionalIdentificationInfo;

	public RegistrationDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);

		formatIdentifier = getBytes(b,offset+2,4);
		// see http://www.smpte-ra.org/mpegreg/mpegreg.html for list
		// TODO implement resource file for lookup?
		additionalIdentificationInfo = getBytes(b,offset+6,descriptorLength-4);

	}


	@Override
	public String toString() {
		return super.toString() + "formatIdentifier="+toHexString(formatIdentifier);
	}


	public byte[] getFormatIdentifier() {
		return formatIdentifier;
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("format_identifier",formatIdentifier ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("additional_identification_info",additionalIdentificationInfo ,null)));
		return t;
	}

}

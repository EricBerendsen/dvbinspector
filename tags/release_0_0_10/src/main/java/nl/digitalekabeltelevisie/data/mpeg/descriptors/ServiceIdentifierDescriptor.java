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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ServiceIdentifierDescriptor extends Descriptor {
	// based on 10.12.1 Service identifier descriptor, ETSI TS 102 812 V1.2.1
	
	// states "textual_service_identifier_bytes: These bytes contain the unique identifier for a service encoded using the normal
	// encoding for text strings in DVB SI."
	// so assume it is a DVBString as defined in Annex A of ETSI EN 300 468 V1.11.1(First byte may indicate encoding type)
	// however,  TS 102 812 V1.2.1 does not refer to Annex A of ETSI EN 300 468 V1.11.1, so it may just be plain bytes


	private final DVBString textual_service_identifier_bytes;

	public ServiceIdentifierDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		textual_service_identifier_bytes = new DVBString(b,offset+1);
	}

	public String getNetworkNameAsString()
	{
		return textual_service_identifier_bytes.toString();
	}

	@Override
	public String toString() {
		return super.toString() + "textual_service_identifier_bytes="+textual_service_identifier_bytes.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("textual_service_identifier_bytes_encoding",textual_service_identifier_bytes.getEncodingString() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("textual_service_identifier_bytes_length",textual_service_identifier_bytes.getLength() ,null)));
		t.add(new DefaultMutableTreeNode(new KVP("textual_service_identifier_bytes",textual_service_identifier_bytes ,null)));
		return t;
	}

}

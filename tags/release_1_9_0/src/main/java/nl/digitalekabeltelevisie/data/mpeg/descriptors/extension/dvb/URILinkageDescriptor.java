/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.LookUpList;

public class URILinkageDescriptor extends DVBExtensionDescriptor {

	private static LookUpList uri_linkage_type_list = new LookUpList.Builder().
			add(0x00,"Online SDT (OSDT) for CI Plus").
			add(0x01,"DVB-IPTV SD&S").
			add(0x02,"Material Resolution Server (MRS) for companion screen applications").
			add(0x03,0x7F,"Reserved for future use").
			add(0x80,0xFF,"User defined").
			build();

	private final int uri_linkage_type;
	private final int uri_length;
	private final byte[] uri_char;
	private int min_polling_interval;
	private byte[] private_data_byte = null; 


	public URILinkageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		uri_linkage_type = getInt(b, privateDataOffset++, 1, MASK_8BITS);
		uri_length = getInt(b, privateDataOffset++, 1, MASK_8BITS);
		uri_char = copyOfRange(b, privateDataOffset,privateDataOffset + uri_length);
		privateDataOffset += uri_length; 
		
		if ((uri_linkage_type == 0x00) || (uri_linkage_type == 0x01)) {
				min_polling_interval = getInt(b, privateDataOffset, 2, MASK_16BITS);
				privateDataOffset += 2;
		}
		if ((offset + 2 + descriptorLength) < privateDataOffset) {
			private_data_byte = copyOfRange(b, privateDataOffset, privateDataOffset + descriptorLength + 2);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(
				new KVP("uri_linkage_type", uri_linkage_type, getURILinkageTypeString(uri_linkage_type))));
		t.add(new DefaultMutableTreeNode(new KVP("uri_length", uri_length, null)));
		t.add(new DefaultMutableTreeNode(new KVP("uri_char", uri_char, null)));
		if ((uri_linkage_type == 0x00) || (uri_linkage_type == 0x01)) {
			t.add(new DefaultMutableTreeNode(new KVP("min_polling_interval", min_polling_interval, null)));
		}
		if (private_data_byte != null) {
			t.add(new DefaultMutableTreeNode(new KVP("private_data_byte", private_data_byte, null)));
		}
		return t;
	}

	private static String getURILinkageTypeString(int uri_linkage_type) {
		return uri_linkage_type_list.get(uri_linkage_type);
	}

}

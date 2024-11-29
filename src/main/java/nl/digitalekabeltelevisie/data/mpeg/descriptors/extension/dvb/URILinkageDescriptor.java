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

import static java.util.Arrays.copyOfRange;
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
			add(0x03,"DVB-I").
			add(0x04,0x7F,"Reserved for future use").
			add(0x80,0xFF,"User defined").
			build();

	private static LookUpList dvb_i_endpoint_type_list = new LookUpList.Builder().
			add(0x00,"Not used").
			add(0x01,"DVB-I Service List").
			add(0x02,"Service List Registry query").
			add(0x03,"Named DVB-I Service List").
			add(0x04,0xFF,"Reserved for future use").
			build();

	private final int uri_linkage_type;
	private final int uri_length;
	private String uri_char = "";
	private int min_polling_interval;
	private byte[] private_data_byte = null; 

	private int dvb_i_endpoint_type = 0;
	private int dvb_i_service_list_name_length = 0;
	private String dvb_i_service_list_name = "";
	private int dvb_i_service_list_provider_name_length = 0;
	private String dvb_i_service_list_provider_name = "";

	public URILinkageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, parent);
		byte[] payload=getBytes(b, PRIVATE_DATA_OFFSET+1, descriptorLength-1);
		int localOffset = 0;
		uri_linkage_type = getInt(payload, localOffset++, 1, MASK_8BITS);
		uri_length = getInt(payload, localOffset++, 1, MASK_8BITS);
		uri_char = getString(payload, localOffset, uri_length);
		localOffset += uri_length;

		if ((uri_linkage_type == 0x00) || (uri_linkage_type == 0x01)) {
			min_polling_interval = getInt(payload, localOffset, 2, MASK_16BITS);
			localOffset += 2;
		}
		else if (uri_linkage_type == 0x03) {  // for DVB-I. Refer to clause 5.1.3.3 of DVB A177
			dvb_i_endpoint_type = getInt(payload, localOffset++, 1, MASK_8BITS);
			if (dvb_i_endpoint_type == 0x03) {
				dvb_i_service_list_name_length = getInt(payload, localOffset++, 1, MASK_8BITS);
				dvb_i_service_list_name = getString(payload, localOffset, dvb_i_service_list_name_length);
				localOffset += dvb_i_service_list_name_length;
				dvb_i_service_list_provider_name_length = getInt(payload, localOffset++, 1, MASK_8BITS);
				dvb_i_service_list_provider_name = getString(payload, localOffset, dvb_i_service_list_provider_name_length);
				localOffset += dvb_i_service_list_provider_name_length;
			}
		}
		if (localOffset < payload.length)
			private_data_byte = copyOfRange(payload, localOffset, payload.length);
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new KVP("uri_linkage_type", uri_linkage_type).setDescription(getURILinkageTypeString(uri_linkage_type)));
		t.add(new KVP("uri_length", uri_length));
		t.add(new KVP("uri_char", uri_char));
		if ((uri_linkage_type == 0x00) || (uri_linkage_type == 0x01)) {
			t.add(new KVP("min_polling_interval", min_polling_interval));
		}
		if (uri_linkage_type == 0x03) {
			t.add(new KVP("dvb-i end_point_type", dvb_i_endpoint_type).setDescription(getDVBIEndpointType(dvb_i_endpoint_type)));
			if (dvb_i_endpoint_type == 0x03) {
				t.add(new KVP("dvb-i service_list_name", dvb_i_service_list_name));
				t.add(new KVP("dvb-i service_list_provider_name", dvb_i_service_list_provider_name));
			}
		}
		if (private_data_byte != null) {
			t.add(new KVP("private_data_byte", private_data_byte));
		}
		return t;
	}

	private static String getURILinkageTypeString(int uri_linkage_type) {
		return uri_linkage_type_list.get(uri_linkage_type);
	}

	private static String getDVBIEndpointType(int dvb_i_endpoint_type) {
		return dvb_i_endpoint_type_list.get(dvb_i_endpoint_type);
	}

}

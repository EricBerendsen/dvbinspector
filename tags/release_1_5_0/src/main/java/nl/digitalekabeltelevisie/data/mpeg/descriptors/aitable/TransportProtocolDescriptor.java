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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.aitable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;


/**
 *
 */
public class TransportProtocolDescriptor extends AITDescriptor {

	private final int protocol_id;
	private final int transport_protocol_label;
	private final byte[] selector_bytes;

	// 10.8.1.1 Transport via OC ETSI ES 201 812 V1.1.1
	private int remote_connection;
	private int reserved_future_use;
	private int service_id;
	private int original_network_id;
	private int transport_stream_id;
	private int component_tag;


	// HTTP over back channel only
	private int url_base_length;
	private byte[] url_base_byte;
	private int url_extension_count;
	private List<byte[]>url_extension_byte;



	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public TransportProtocolDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
		protocol_id = getInt(b, offset + 2, 2, MASK_16BITS);
		transport_protocol_label = getInt(b, offset + 4, 1, MASK_8BITS);

		selector_bytes = copyOfRange(b, offset+5, offset+descriptorLength+2);

		// 10.8.1.1 Transport via OC ETSI ES 201 812 V1.1.1
		if(protocol_id==0x01){
			remote_connection = getInt(b, offset + 5, 1, 0x80)>>7;
			reserved_future_use = getInt(b, offset + 5, 1, MASK_7BITS);
			int t=offset + 6;
			if(remote_connection == 1) {
				original_network_id = getInt(b, t, 1, MASK_16BITS);
				t+=2;
				transport_stream_id = getInt(b, t, 1, MASK_16BITS);
				t+=2;
				service_id = getInt(b, t, 1, MASK_16BITS);
				t+=2;
			}
			component_tag = getInt(b, t, 1, MASK_8BITS);
		}

		// HTTP over back channel only
		if(protocol_id==0x03){
			url_base_length= getInt(b, offset + 5, 1, MASK_8BITS);
			url_base_byte = copyOfRange(b, offset + 6,offset + 6 + url_base_length);
			url_extension_count = getInt(b, offset + 6 + url_base_length, 1, MASK_8BITS);
			url_extension_byte = new ArrayList<byte[]>();
			int t=offset + 7 + url_base_length;
			for (int i = 0; i < url_extension_count; i++) {
				final int url_extension_length = getInt(b, t, 1, MASK_8BITS);
				url_extension_byte.add(copyOfRange(b, t,t + url_extension_length));
				t += url_extension_length+1;
			}
		}




	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("protocol_id", protocol_id, getTransportProtocolIDString(protocol_id))));
		t.add(new DefaultMutableTreeNode(new KVP("transport_protocol_label", transport_protocol_label, null)));
		t.add(new DefaultMutableTreeNode(new KVP("selector_bytes", selector_bytes, null)));
		if(protocol_id==0x01){
			t.add(new DefaultMutableTreeNode(new KVP("remote_connection",remote_connection,(remote_connection == 1)?"the transport connection is provided by a service that is different to the one carrying the AIT":null)));
			t.add(new DefaultMutableTreeNode(new KVP("reserved_future_use",reserved_future_use,null)));
			if(remote_connection == 1) {
				t.add(new DefaultMutableTreeNode(new KVP("original_network_id",original_network_id,null)));
				t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transport_stream_id,null)));
				t.add(new DefaultMutableTreeNode(new KVP("service_id",service_id,null)));

			}
			t.add(new DefaultMutableTreeNode(new KVP("component_tag",component_tag,null)));

		}
		if(protocol_id==0x03){
			t.add(new DefaultMutableTreeNode(new KVP("url_base_length",url_base_length,null)));
			t.add(new DefaultMutableTreeNode(new KVP("url_base_byte",url_base_byte,null)));
			t.add(new DefaultMutableTreeNode(new KVP("url_extension_count",url_extension_count,null)));
			for (final Iterator<byte[]> iter = url_extension_byte.iterator(); iter.hasNext();) {
				final byte[] url_extension = iter.next();
				t.add(new DefaultMutableTreeNode(new KVP("url_extension_byte",url_extension,null)));
			}
		}
		return t;
	}

	private static String getTransportProtocolIDString(final int protocol_id){
		if((protocol_id>=0x0004)&&(protocol_id<=0x00ff)){
			return "Reserved for use by DVB";
		}
		switch (protocol_id) {
		case 0x00:
			return "reserved_future_use";
		case 0x01:
			return "MHP Object Carousel";
		case 0x02:
			return "IP via DVB multiprotocol encapsulation as defined in ETSI EN 301 192 [5], ETSI TR 101 202";
		case 0x03:
			return "HTTP over back channel (i.e. broadband connection).";
		case 0x0100:
			return "OpenTV";

		default:
			return "[subject to registration by DVB]";
		}

	}
}

/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

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


	// TS 102 809 V1.3.1 (2017-06) 5.3.6.2 Syntax of selector bytes for interaction channel transport
	
	// HTTP over back channel only
	private int url_base_length;
	private byte[] url_base_byte;
	private int url_extension_count;
	private List<byte[]>url_extension_byte;



	/**
	 * @param b
	 * @param parent
	 */
	public TransportProtocolDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		protocol_id = getInt(b, 2, 2, MASK_16BITS);
		transport_protocol_label = getInt(b, 4, 1, MASK_8BITS);

		selector_bytes = copyOfRange(b, 5, descriptorLength+2);

		// 10.8.1.1 Transport via OC ETSI ES 201 812 V1.1.1
		if(protocol_id==0x01){
			remote_connection = getInt(b, 5, 1, 0x80)>>7;
			reserved_future_use = getInt(b, 5, 1, MASK_7BITS);
			int t = 6;
			if(remote_connection == 1) {
				original_network_id = getInt(b, t, 2, MASK_16BITS);
				t+=2;
				transport_stream_id = getInt(b, t, 2, MASK_16BITS);
				t+=2;
				service_id = getInt(b, t, 2, MASK_16BITS);
				t+=2;
			}
			component_tag = getInt(b, t, 1, MASK_8BITS);
		}

		// HTTP over back channel only
		if(protocol_id==0x03){
			url_base_length= getInt(b, 5, 1, MASK_8BITS);
			url_base_byte = copyOfRange(b, 6,6 + url_base_length);
			url_extension_count = getInt(b, 6 + url_base_length, 1, MASK_8BITS);
			url_extension_byte = new ArrayList<>();
			int t = 7 + url_base_length;
			for (int i = 0; i < url_extension_count; i++) {
				int url_extension_length = getInt(b, t, 1, MASK_8BITS);
				t++;
				url_extension_byte.add(copyOfRange(b, t,t + url_extension_length));
				t += url_extension_length;
			}
		}




	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("protocol_id", protocol_id, getTransportProtocolIDString(protocol_id)));
		t.add(new KVP("transport_protocol_label", transport_protocol_label));
		t.add(new KVP("selector_bytes", selector_bytes));
		if(protocol_id==0x01){
			t.add(new KVP("remote_connection",remote_connection,(remote_connection == 1)?"the transport connection is provided by a service that is different to the one carrying the AIT":null));
			t.add(new KVP("reserved_future_use",reserved_future_use));
			if(remote_connection == 1) {
				t.add(new KVP("original_network_id",original_network_id));
				t.add(new KVP("transport_stream_id",transport_stream_id));
				t.add(new KVP("service_id",service_id));

			}
			t.add(new KVP("component_tag",component_tag));

		}
		if(protocol_id==0x03){
			t.add(new KVP("url_base_length",url_base_length));
			t.add(new KVP("url_base_byte",url_base_byte));
			t.add(new KVP("url_extension_count",url_extension_count));
			for (byte[] url_extension: url_extension_byte) {
				t.add(new KVP("url_extension_byte",url_extension));
			}
		}
		return t;
	}

	private static String getTransportProtocolIDString(int protocol_id){
		if((protocol_id>=0x0004)&&(protocol_id<=0x00ff)){
			return "Reserved for use by DVB";
		}
        return switch (protocol_id) {
            case 0x00 -> "reserved_future_use";
            case 0x01 -> "MHP Object Carousel";
            case 0x02 -> "IP via DVB multiprotocol encapsulation as defined in ETSI EN 301 192 [5], ETSI TR 101 202";
            case 0x03 ->  // ETSI TS 102 809 V1.3.1 (2017-06) Table 29: Protocol_id
                    "HTTP over back channel (i.e. broadband connection).";
            case 0x0100 -> "OpenTV";
            default -> "[subject to registration by DVB]";
        };

	}
}

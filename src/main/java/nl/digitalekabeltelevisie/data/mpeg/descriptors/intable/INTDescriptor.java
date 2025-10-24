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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.intable;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */

//TODO combine with UNTDescriptor, see EN 301 192 V1.6.1  8.4.5.1 Descriptor identification and location
//Note that descriptor tags from 0x00 to 0x3F share a common descriptor name space with UNT descriptors
//(see ETSI TS 102 006 [18]).

public class INTDescriptor extends Descriptor {


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public INTDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
	}

	@Override
	public String getDescriptorname(){
		return INTDescriptor.getDescriptorname(descriptorTag, parentTableSection);
	}

	@Override
	public KVP getJTreeNode(int modus){
		return super.getJTreeNode(modus);
	}
	
	
	public static String getDescriptorname(int tag, TableSection tableSection){


        return switch (tag) {
            case 0x00 -> "reserved";
            case 0x06 -> "target_smartcard_descriptor";
            case 0x07 -> "target_MAC_address_descriptor";
            case 0x08 -> "target_serial_number_descriptor";
            case 0x09 -> "target_IP_address_descriptor";
            case 0x0A -> "target_IPv6_address_descriptor";
            case 0x0C -> "IP/MAC_platform_name_descriptor";
            case 0x0D -> "IP/MAC_platform_provider_name_descriptor";
            case 0x0E -> "target_MAC_address_range_descriptor";
            case 0x0F -> "target_IP_slash_descriptor";
            case 0x10 -> "target_IP_source_slash_descriptor";
            case 0x11 -> "target_IPv6_slash_descriptor";
            case 0x12 -> "target_IPv6_source_slash_descriptor";
            case 0x13 -> "IP/MAC_stream_location_descriptor";
            case 0x14 -> "ISP_access_mode_descriptor";
            case 0x15 -> "IP/MAC_generic_stream_location_descriptor";
            default -> "illegal descriptor tag value in INT";
        };
	}


}

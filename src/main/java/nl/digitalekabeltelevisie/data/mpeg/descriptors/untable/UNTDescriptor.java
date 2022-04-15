/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
 * Based on TS 102 006
 */
package nl.digitalekabeltelevisie.data.mpeg.descriptors.untable;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */

// TODO combine with INTDescriptor, see EN 301 192 V1.6.1  8.4.5.1 Descriptor identification and location
// Note that descriptor tags from 0x00 to 0x3F share a common descriptor name space with UNT descriptors
// (see ETSI TS 102 006 [18]). Problem, 0x0C and 0x0D are not identical, so leave for now 

public class UNTDescriptor extends Descriptor {


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public UNTDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
	}

	@Override
	public String getDescriptorname(){
		return UNTDescriptor.getDescriptorname(descriptorTag, parentTableSection);
	}

	public static String getDescriptorname(final int tag, final TableSection tableSection){


		if((tag>=0x0C)&&(tag<=0x3f)){
			return "reserved for future SSU use";
		}

		switch (tag) {
		case 0x00: return "reserved";
		case 0x01: return "scheduling_descriptor";
		case 0x02: return "update_descriptor";
		case 0x03: return "ssu_location_descriptor";
		case 0x04: return "message_descriptor";
		case 0x05: return "ssu_event_name_descriptor";
		case 0x06: return "target_smartcard_descriptor";
		case 0x07: return "target_MAC_address_descriptor";
		case 0x08: return "target_serial_number_descriptor";
		case 0x09: return "target_IP_address_descriptor";
		case 0x0A: return "target_IPv6_address_descriptor";
		case 0x0B: return "ssu_subgroup_association_descriptor";
		case 0x0C: return "enhanced_message_descriptor";
		case 0x0D: return "ssu_uri_descriptor";
		default:

			return "illegal descriptor tag value in UNT";

		}
	}


}

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

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * @author Eric Berendsen
 *
 */
public class INTDescriptor extends Descriptor {


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public INTDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
	}

	@Override
	public String getDescriptorname(){
		return INTDescriptor.getDescriptorname(descriptorTag, parentTableSection);
	}

	public static String getDescriptorname(final int tag, final TableSection tableSection){



		switch (tag) {
		case 0x00: return "reserved";
		case 0x06: return "target_smartcard_descriptor";
		case 0x07: return "target_MAC_address_descriptor";
		case 0x08: return "target_serial_number_descriptor";
		case 0x09: return "target_IP_address_descriptor";
		case 0x0A: return "target_IPv6_address_descriptor";
		case 0x0C: return "IP/MAC_platform_name_descriptor";
		case 0x0D: return "IP/MAC_platform_provider_name_descriptor";
		case 0x0E: return "target_MAC_address_range_descriptor";
		case 0x0F: return "target_IP_slash_descriptor";
		case 0x10: return "target_IP_source_slash_descriptor";
		case 0x11: return "target_IPv6_slash_descriptor";
		case 0x12: return "target_IPv6_source_slash_descriptor";
		case 0x13: return "IP/MAC_stream_location_descriptor";
		case 0x14: return "ISP_access_mode_descriptor";
		case 0x15: return "IP/MAC_generic_stream_location_descriptor";

		default:

			return "illegal descriptor tag value in INT";

		}
	}


}

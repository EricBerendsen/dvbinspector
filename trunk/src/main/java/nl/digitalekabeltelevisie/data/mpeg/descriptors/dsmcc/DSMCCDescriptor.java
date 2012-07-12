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
package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

/**
 * @author Eric Berendsen
 *
 */
public class DSMCCDescriptor extends Descriptor {


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public DSMCCDescriptor(final byte[] b, final int offset) {
		super(b, offset, null);
	}

	@Override
	public String getDescriptorname(){
		return DSMCCDescriptor.getDescriptorname(descriptorTag);
	}

	public static String getDescriptorname(final int tag){


		if((tag>=0x72)&&(tag<=0x7f)){
			return "reserved MHP";
		}
		if((tag>=0x80)&&(tag<=0xff)){
			return "private_descriptor";
		}

		switch (tag) {
		case 0x00: return "reserved";
		case 0x01: return "type_descriptor";
		case 0x02: return "name_descriptor";
		case 0x03: return "info_descriptor";
		case 0x04: return "module_link_descriptor";
		case 0x05: return "CRC32_descriptor";
		case 0x06: return "location_descriptor";
		case 0x07: return "estimated_download_time_descriptor";
		case 0x08: return "group_link_descriptor";
		case 0x09: return "compressed_module_descriptor";
		case 0x0A: return "SSU_module_type"; // was "subgroup_association_descriptor". looks like DVBSnoop made a mistake in dsmcc_str.c;
		case 0x0B: return "subgroup_association_descriptor"; // was "reserved for future use by DVB";
		case 0x70: return "label_descriptor";		// MHP
		case 0x71: return "caching_priority_descriptor";	// MHP
		default:

			return "illegal descriptor tag value in DSM-CC";

		}
	}


}

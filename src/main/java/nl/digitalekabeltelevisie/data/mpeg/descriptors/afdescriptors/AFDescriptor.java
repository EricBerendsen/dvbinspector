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
package nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

/**
 * Based on ISO/IEC 13818-1:2015/Amd.1/Cor.2:2016 (E) / Rec. ITU-T H.222.0 (2015)/Amd.1/Cor.2 (07/2016) 
 * "Amendment 1: Delivery of timeline for external data
 * Technical Corrigendum 2: Clarifications and
 * corrections on pause flag, URL construction
 * and adaptation field syntax"
 * 
 * @author Eric
 *
 */

public class AFDescriptor extends Descriptor {


	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public AFDescriptor(final byte[] b, final int offset) {
		super(b, offset, null);
	}

	@Override
	public String getDescriptorname(){
		return getDescriptorname(descriptorTag);
	}

	public static String getDescriptorname(final int tag){


		if((tag>=0x00)&&(tag<=0x03)){
			return "Rec. ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}
		if((tag>=0x07)&&(tag<=0x7f)){
			return "Rec. ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}

		switch (tag) {
		case 0x04: return "Timeline Descriptor";
		case 0x05: return "Location Descriptor";
		case 0x06: return "BaseURL Descriptor";
		case 0x07: return "Cets_byte_range_descriptor"; //ISO/IEC 13818-1:2015/Amd.1/Cor.2:2016 (E)
		default:

			return "User Private";

		}
	}


}

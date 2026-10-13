/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class AtscDescriptor extends Descriptor {

	public AtscDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
	}

	@Override
	public String getDescriptorname() {
		return getDescriptorname(descriptorTag);
	}

	public static String getDescriptorname(final int tag) {
		return switch (tag) {
			case 0x80 -> "ATSC stuffing_descriptor";
			case 0x81 -> "ATSC AC-3_audio_stream_descriptor";
			case 0x86 -> "ATSC caption_service_descriptor";
			case 0x87 -> "ATSC content_advisory_descriptor";
			case 0xA0 -> "ATSC extended_channel_name_descriptor";
			case 0xA1 -> "ATSC service_location_descriptor";
			case 0xA2 -> "ATSC time_shifted_service_descriptor";
			case 0xA3 -> "ATSC component_name_descriptor";
			case 0xA8 -> "ATSC dcc_departing_request_descriptor";
			case 0xA9 -> "ATSC dcc_arriving_request_descriptor";
			case 0xAA -> "ATSC redistribution_control_descriptor";
			case 0xAB -> "ATSC genre_descriptor";
			case 0xAD -> "ATSC private_information_descriptor";
			case 0xCC -> "ATSC E-AC-3_audio_stream_descriptor";
			default -> "ATSC user defined descriptor";
		};
	}
}

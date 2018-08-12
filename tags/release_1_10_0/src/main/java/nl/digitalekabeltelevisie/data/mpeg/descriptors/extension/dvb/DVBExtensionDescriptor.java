/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class DVBExtensionDescriptor extends ExtensionDescriptor {

	public DVBExtensionDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);
	}

	public static String getDescriptorTagString(final int descriptor_tag_extension) {
		switch (descriptor_tag_extension) {

		case 0x0:
			return "image_icon_descriptor";
		case 0x1:
			return "cpcm_delivery_signalling_descriptor";
		case 0x2:
			return "CP_descriptor";
		case 0x3:
			return "CP_identifier_descriptor";
		case 0x4:
			return "T2_delivery_system_descriptor";
		case 0x5:
			return "SH_delivery_system_descriptor";
		case 0x6:
			return "supplementary_audio_descriptor";
		case 0x7:
			return "network_change_notify_descriptor";
		case 0x8:
			return "message_descriptor";
		case 0x9:
			return "target_region_descriptor";
		case 0xa:
			return "target_region_name_descriptor";
		case 0xb:
			return "service_relocated_descriptor";
		case 0xc:
			return "XAIT_PID_descriptor";
		case 0xd:
			return "C2_delivery_system_descriptor";
		case 0xe:
			return "DTS-HD_audio_stream_descriptor";
		case 0xf:
			return "DTS_Neural_descriptor";
		case 0x10:
			return "video_depth_range_descriptor";
		case 0x11:
			return "T2MI_descriptor";
		case 0x13:
			return "URI_linkage_descriptor";
		case 0x14:
			return "CI_ancillary_data_descriptor";
		case 0x15:
			return "AC-4_descriptor";
		case 0x16:
			return "C2_bundle_delivery_system_descriptor";
		case 0x17:
			return "S2X_satellite_delivery_system_descriptor";
		case 0x18:
			return "protection_message_descriptor";
		case 0x19:
			return "audio_preselection_descriptor";
		case 0x20: // based on DVB BlueBook A038 jan 2017, shpuldn't this be 0x1a ?
			return "TTML_subtitling_descriptor";

		default:
			return "reserved for future use";
		}
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}

}

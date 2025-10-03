/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

	public DVBExtensionDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
	}

	public static String getDescriptorTagString(int descriptor_tag_extension) {
        return switch (descriptor_tag_extension) {
            case 0x0 -> "image_icon_descriptor";
            case 0x1 -> "cpcm_delivery_signalling_descriptor";
            case 0x2 -> "CP_descriptor";
            case 0x3 -> "CP_identifier_descriptor";
            case 0x4 -> "T2_delivery_system_descriptor";
            case 0x5 -> "SH_delivery_system_descriptor";
            case 0x6 -> "supplementary_audio_descriptor";
            case 0x7 -> "network_change_notify_descriptor";
            case 0x8 -> "message_descriptor";
            case 0x9 -> "target_region_descriptor";
            case 0xa -> "target_region_name_descriptor";
            case 0xb -> "service_relocated_descriptor";
            case 0xc -> "XAIT_PID_descriptor";
            case 0xd -> "C2_delivery_system_descriptor";
            case 0xe -> "DTS-HD_audio_stream_descriptor";
            case 0xf -> "DTS_Neural_descriptor";
            case 0x10 -> "video_depth_range_descriptor";
            case 0x11 -> "T2MI_descriptor";
            case 0x13 -> "URI_linkage_descriptor";
            case 0x14 -> "CI_ancillary_data_descriptor";
            case 0x15 -> "AC-4_descriptor";
            case 0x16 -> "C2_bundle_delivery_system_descriptor";
            case 0x17 -> "S2X_satellite_delivery_system_descriptor";
            case 0x18 -> "protection_message_descriptor";
            case 0x19 -> "audio_preselection_descriptor";
            case 0x20 -> // based on DVB BlueBook A038 jan 2017, shpuldn't this be 0x1a ?
                    "TTML_subtitling_descriptor";
            case 0x21 ->// based on DVB BlueBook A038 jun 2019
                    "DTS-UHD_descriptor";
            case 0x22 ->// based on DVB BlueBook A038r15 nov 2022
                    "service_prominence_descriptor";
            case 0x23 ->// based on DVB BlueBook A038r15 nov 2022
                    "vvc_subpictures_descriptor";
            case 0x24 ->// based on DVB BlueBook A038r15 nov 2022
                    "S2Xv2_satellite_delivery_system_descriptor";
            default -> "reserved for future use";
        };
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}


}

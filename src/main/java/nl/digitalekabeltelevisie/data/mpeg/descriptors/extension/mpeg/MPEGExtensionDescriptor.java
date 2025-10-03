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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;


import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class MPEGExtensionDescriptor extends ExtensionDescriptor {

	public MPEGExtensionDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
	}

	public static String getDescriptorTagString(int descriptor_tag_extension) {
        return switch (descriptor_tag_extension) {
            case 0x0 -> "Reserved";
            case 0x1 -> "Forbidden";
            case 0x2 -> "ODUpdate_descriptor";
            case 0x3 -> "HEVC_timing_and_HRD_descriptor";
            case 0x4 -> "af_extensions_descriptor";
            case 0x5 -> "HEVC_operation_point_descriptor";
            case 0x6 -> "HEVC_hierarchy_extension_descriptor";
            case 0x7 -> "Green_extension_descriptor";
            case 0x8 -> "MPEG-H_3dAudio_descriptor";
            case 0x9 -> "MPEG-H_3dAudio_config_descriptor";
            case 0xa -> "MPEG-H_3dAudio_scene_descriptor";
            case 0xb -> "MPEG-H_3dAudio_text_label_descriptor";
            case 0xc -> "MPEG-H_3dAudio_multi-stream_descriptor";
            case 0xd -> "MPEG--H_3dAudio_drc_loudness_descriptor";
            case 0xe -> "MPEG--H_3dAudio_command_descriptor";
            case 0xf -> "Quality_extension_descriptor";
            case 0x10 -> "Virtual_segmentation_descriptor";
            case 0x11 -> "timed_metadata_extension_descriptor";
            case 0x12 -> "HEVC_tile_substream_descriptor";
            case 0x13 -> "HEVC_subregion_descriptor";
            case 0x14 -> "JPEG_XS_video_descriptor";
            case 0x15 -> "VVC_timing_and_HRD_descripto";
            case 0x16 -> "EVC_timing_and_HRD_descriptor";
            case 0x17 -> "LCEVC_video_descriptor"; //Rec. ITU-T H.222.0 (2021)/Amd.1 (12/2022)
            case 0x18 -> "LCEVC_linkage_descriptor"; //Rec. ITU-T H.222.0 (2021)/Amd.1 (12/2022)
            case 0x19 -> "Media_service_kind_descriptor"; //Rec. ITU-T H.222.0 (2021)/Amd.1 (12/2022)
            default -> "ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
        };
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}

}

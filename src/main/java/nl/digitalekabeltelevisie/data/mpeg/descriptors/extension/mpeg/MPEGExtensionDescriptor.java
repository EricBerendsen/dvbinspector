package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtensionDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class MPEGExtensionDescriptor extends ExtensionDescriptor {

	public MPEGExtensionDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
	}

	public static String getDescriptorTagString(final int descriptor_tag_extension) {
		switch (descriptor_tag_extension) {
		
		case 0x0:
			return "Reserved";
		case 0x1:
			return "Forbidden";
		case 0x2:
			return "ODUpdate_descriptor";
		case 0x3:
			return "HEVC_timing_and_HRD_descriptor";
		case 0x4:
			return "af_extensions_descriptor";
		case 0x5:
			return "HEVC_operation_point_descriptor";
		case 0x6:
			return "HEVC_hierarchy_extension_descriptor";
		case 0x7:
			return "Green_extension_descriptor";
		case 0x8:
			return "MPEG-H_3dAudio_descriptor";
		case 0x9:
			return "MPEG-H_3dAudio_config_descriptor";
		case 0xa:
			return "MPEG-H_3dAudio_scene_descriptor";
		case 0xb:
			return "MPEG-H_3dAudio_text_label_descriptor";
		case 0xc:
			return "MPEG-H_3dAudio_multi-stream_descriptor";
		case 0xd:
			return "MPEG--H_3dAudio_drc_loudness_descriptor";
		case 0xe:
			return "MPEG--H_3dAudio_command_descriptor";
		case 0xf:
			return "Quality_extension_descriptor";
		case 0x10:
			return "Virtual_segmentation_descriptor";
		case 0x11:
			return "timed_metadata_extension_descriptor";
		case 0x12:
			return "HEVC_tile_substream_descriptor";
		case 0x13:
			return "HEVC_subregion_descriptor";
		case 0x14:
			return "JXS_video_descriptor";
		case 0x15:
			return "VVC_timing_and_HRD_descripto";
		case 0x16:
			return "EVC_timing_and_HRD_descriptor";
		default:
			return "ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}

}

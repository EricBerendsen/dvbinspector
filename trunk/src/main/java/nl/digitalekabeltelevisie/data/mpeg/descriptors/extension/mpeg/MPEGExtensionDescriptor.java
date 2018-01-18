package nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.mpeg;

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
			return "MPEG-Quality_extension_descriptor";
		case 0x10:
			return "MPEG-Virtual_segmentation_descriptor";
		default:
			return "ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}

}

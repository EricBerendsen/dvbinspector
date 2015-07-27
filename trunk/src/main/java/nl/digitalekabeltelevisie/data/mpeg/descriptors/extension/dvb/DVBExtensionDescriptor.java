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
			return "BCI_ancillary_data_descriptor";
		case 0x15:
			return "AC-4_descriptor";
		case 0x16:
			return "C2_bundle_delivery_system_descriptor";

		default:
			return "reserved for future use";
		}
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}

}

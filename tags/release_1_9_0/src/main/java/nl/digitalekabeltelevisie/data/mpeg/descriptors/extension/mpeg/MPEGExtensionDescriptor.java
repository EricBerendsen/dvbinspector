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
		default:
			return "ITU-T H.222.0 | ISO/IEC 13818-1 Reserved";
		}
	}

	@Override
	public String getDescriptorTagString() {
		return getDescriptorTagString(descriptor_tag_extension);
	}

}

package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.util.Utils;

public final class DSMCCDescriptorFactory {


	/**
	 * 
	 */
	private DSMCCDescriptorFactory() {
		// static only
	}

	private static Logger	logger	= Logger.getLogger(DSMCCDescriptorFactory.class.getName());


	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len) {
		final List<Descriptor> r = new ArrayList<Descriptor>();
		int t = 0;

		while (t < len) {

			Descriptor d;
			try {
				d = getDSMCCDescriptor(data, offset,t);

			} catch (final RuntimeException iae) {
				// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
				// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
				d = new DSMCCDescriptor(data, t + offset);
				logger.info("Fall back for descriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
						+ DSMCCDescriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]))
						+ ")in section DSMCC data=" + d.getRawDataString()+", RuntimeException:"+iae);
			}

			t += d.getDescriptorLength() + 2;
			r.add(d);
		}

		return r;
	}

	private static Descriptor getDSMCCDescriptor(final byte[] data, final int offset, final int t) {
		Descriptor d;
		switch (Utils.getUnsignedByte(data[t + offset])) {
		case 0x05:
			d = new CRC32Descriptor(data, t + offset);
			break;

		case 0x09:
			d = new CompressedModuleDescriptor(data, t + offset);
			break;


		case 0x0A:
			d = new SSUModuleTypeDescriptor(data, t + offset);
			break;

		case 0x71:
			d = new CachingPrioriyDescriptor(data, t + offset);
			break;

		default:
			d = new DSMCCDescriptor(data, t + offset);
			// if(Utils.getUnsignedByte(data[t+offset])<128){
			logger.info("Not implemented DSMCCDescriptor:" + Utils.getUnsignedByte(data[t + offset]) + " ("
					+ DSMCCDescriptor.getDescriptorname(Utils.getUnsignedByte(data[t + offset]))
					+ ",) data=" + d.getRawDataString());
			// }
			break;
		}
		return d;
	}

}

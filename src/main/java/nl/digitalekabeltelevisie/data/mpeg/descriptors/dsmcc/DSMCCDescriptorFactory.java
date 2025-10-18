/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.dsmcc;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

public final class DSMCCDescriptorFactory {


	/**
	 *
	 */
	private DSMCCDescriptorFactory() {
		// static only
	}

	private static final Logger	logger	= Logger.getLogger(DSMCCDescriptorFactory.class.getName());


	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len) {
		final List<Descriptor> r = new ArrayList<>();
		int t = 0;

		while (t < len) {

			int descriptorLen = toUnsignedInt(data[offset + t+ 1]);
			byte[] descriptorData = Arrays.copyOfRange(data, offset + t, offset + t + descriptorLen + 2);

			Descriptor d;
			try {
				d = getDSMCCDescriptor(descriptorData);

			} catch (final RuntimeException iae) {
				// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
				// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
				d = new DSMCCDescriptor(descriptorData);
				logger.info("Fall back for descriptor:" + toUnsignedInt(descriptorData[0]) + " ("
						+ DSMCCDescriptor.getDescriptorname(toUnsignedInt(data[0]))
						+ ")in section DSMCC data=" + d.getRawDataString()+", RuntimeException:"+iae);
			}

			t += d.getDescriptorLength() + 2;
			r.add(d);
		}

		return r;
	}

	private static Descriptor getDSMCCDescriptor(byte[] data) {
		Descriptor d;
		switch (toUnsignedInt(data[0])) {
		case 0x02:
			d = new NameDescriptor(data);
			break;
		case 0x04:
			d = new ModuleLinkDescriptor(data);
			break;
		case 0x05:
			d = new CRC32Descriptor(data);
			break;

		case 0x09:
			d = new CompressedModuleDescriptor(data);
			break;

		case 0x0A:
			d = new SSUModuleTypeDescriptor(data);
			break;

		case 0x70:
			d = new LabelDescriptor(data);
			break;

		case 0x71:
			d = new CachingPriorityDescriptor(data);
			break;

		default:
			d = new DSMCCDescriptor(data);
			logger.info("Not implemented DSMCCDescriptor:" + toUnsignedInt(data[0]) + " ("
					+ DSMCCDescriptor.getDescriptorname(toUnsignedInt(data[0]))
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

}

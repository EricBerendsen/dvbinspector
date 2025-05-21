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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.afdescriptors;

import static java.lang.Byte.toUnsignedInt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

public final class AFDescriptorFactory {


	/**
	 *
	 */
	private AFDescriptorFactory() {
		// static only
	}

	private static final Logger	logger	= Logger.getLogger(AFDescriptorFactory.class.getName());


	public static List<Descriptor> buildDescriptorList(final byte[] data, final int offset, final int len) {
		final List<Descriptor> r = new ArrayList<>();
		int t = 0;

		while (t < len) {

			// make a copy of the just the bytes for the descriptor. 
			// If the descriptor constructor reads further then descriptorLen it will cause a ArrayIndexOutOfBoundsException, 
			// which will result in fall back to a standard Descriptor.
			// Reasoning: better not to interpret the data, than to show it wrong without warning. 
			//
			// see https://github.com/EricBerendsen/dvbinspector/issues/22
			
			int descriptorLen = toUnsignedInt(data[offset + t+ 1]);
			byte[] descriptorData = Arrays.copyOfRange(data, offset + t, offset + t + descriptorLen + 2);

			Descriptor d;
			try {
				d = getAFDescriptor(descriptorData);

			} catch (final RuntimeException iae) {
				// this can happen because there is an error in our code (constructor of a descriptor), OR the stream is invalid.
				// fall back to a standard Descriptor (this is highly unlikely to fail), so processing can continue
				d = new AFDescriptor(data);
				logger.info("Fall back for descriptor:" + toUnsignedInt(data[t + offset]) + " ("
						+ AFDescriptor.getDescriptorname(toUnsignedInt(data[t + offset]))
						+ ")in AFDescriptorList adaptationField. data=" + d.getRawDataString()+", RuntimeException:"+iae);
			}

			t += d.getDescriptorLength() + 2;
			r.add(d);
		}

		return r;
	}

	private static Descriptor getAFDescriptor(final byte[] data) {
		Descriptor d;
		switch (toUnsignedInt(data[0])) {
		case 0x04:
			d = new TimelineDescriptor(data);
			break;

		default:
			d = new AFDescriptor(data);
			logger.info("Not implemented AFDescriptor:" + toUnsignedInt(data[0]) + " ("
					+ AFDescriptor.getDescriptorname(toUnsignedInt(data[0]))
					+ ",) data=" + d.getRawDataString());
			break;
		}
		return d;
	}

}

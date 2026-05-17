/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_5BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_6BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class TimeShiftedServiceDescriptor extends AtscDescriptor {

	private final int reserved;
	private final int numberOfServices;
	private final List<Service> services = new ArrayList<>();

	public TimeShiftedServiceDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		reserved = getInt(b, PRIVATE_DATA_OFFSET, 1, 0xE0) >> 5;
		numberOfServices = getInt(b, PRIVATE_DATA_OFFSET, 1, MASK_5BITS);

		int offset = PRIVATE_DATA_OFFSET + 1;
		final int end = PRIVATE_DATA_OFFSET + descriptorLength;
		for (int i = 0; (i < numberOfServices) && (offset + 5 <= end); i++) {
			Service service = new Service(b, offset);
			services.add(service);
			offset += 5;
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("reserved", reserved));
		t.add(new KVP("number_of_services", numberOfServices));
		addListJTree(t, services, modus, "services");
		return t;
	}

	public int getReserved() {
		return reserved;
	}

	public int getNumberOfServices() {
		return numberOfServices;
	}

	public List<Service> getServices() {
		return services;
	}

	public static class Service implements TreeNode {

		private final int reserved1;
		private final int timeShift;
		private final int reserved2;
		private final int majorChannelNumber;
		private final int minorChannelNumber;

		Service(final byte[] data, final int offset) {
			int timeShiftBits = getInt(data, offset, 2, 0xFFFF);
			reserved1 = (timeShiftBits >> 10) & MASK_6BITS;
			timeShift = timeShiftBits & MASK_10BITS;

			int channelNumbers = getInt(data, offset + 2, 3, 0xFFFFFF);
			reserved2 = (channelNumbers >> 20) & 0x0F;
			majorChannelNumber = (channelNumbers >> 10) & MASK_10BITS;
			minorChannelNumber = channelNumbers & MASK_10BITS;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("service", majorChannelNumber + "." + minorChannelNumber);
			t.add(new KVP("reserved", reserved1));
			t.add(new KVP("time_shift", timeShift, "minutes"));
			t.add(new KVP("reserved", reserved2));
			t.add(new KVP("major_channel_number", majorChannelNumber));
			t.add(new KVP("minor_channel_number", minorChannelNumber));
			return t;
		}

		public int getReserved1() {
			return reserved1;
		}

		public int getTimeShift() {
			return timeShift;
		}

		public int getReserved2() {
			return reserved2;
		}

		public int getMajorChannelNumber() {
			return majorChannelNumber;
		}

		public int getMinorChannelNumber() {
			return minorChannelNumber;
		}
	}
}

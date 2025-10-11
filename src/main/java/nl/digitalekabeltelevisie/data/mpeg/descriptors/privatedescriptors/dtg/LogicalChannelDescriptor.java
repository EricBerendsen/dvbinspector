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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorContext;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class LogicalChannelDescriptor extends AbstractLogicalChannelDescriptor {


	public class LogicalChannel extends AbstractLogicalChannel{
		
		// ignore visible_service

		public LogicalChannel(int service_id, int reserved, int logical_channel_number){
			super(service_id, 1, reserved, logical_channel_number);
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP s = new KVP(createNodeLabel(service_id, logical_channel_number));
			s.add(new KVP("service_id", service_id));
			s.add(new KVP("reserved", reserved));
			s.add(new KVP("logical_channel_number", logical_channel_number));
			return s;
		}

	}

	public LogicalChannelDescriptor(byte[] b, TableSection parent, DescriptorContext descriptorContext) {
		super(b, parent, descriptorContext);
		int t = 0;
		while (t < descriptorLength) {
			int serviceId = getInt(b, 2 + t, 2, MASK_16BITS);
			int reserved = getInt(b, t + 4, 1, 0xFC) >> 2;
			int chNumber = getInt(b, t + 4, 2, MASK_10BITS);
			LogicalChannel s = new LogicalChannel(serviceId, reserved, chNumber);
			channelList.add(s);
			t += 4;
		}
	}

	@Override
	public String getDescriptorname(){
		return "DTG Logical Channel Descriptor";
	}

}

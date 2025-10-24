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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig;

import static nl.digitalekabeltelevisie.util.Utils.MASK_14BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorContext;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

//based on NorDig Unified ver 2.3 12.2.9.2 NorDig private; Logical Channel Descriptor (version 1)
@SuppressWarnings("ALL")
public class NordigLogicalChannelDescriptorV1 extends AbstractLogicalChannelDescriptor {


	public class LogicalChannel extends AbstractLogicalChannel{

		public LogicalChannel(int service_id, int visible_service, int reserved, int logical_channel_number){
			super(service_id, visible_service, reserved, logical_channel_number);

		}

	}

	public NordigLogicalChannelDescriptorV1(byte[] b, TableSection parent, DescriptorContext descriptorContext) {
		super(b, parent, descriptorContext);
		int t = 0;
		while (t < descriptorLength) {
			int serviceId = getInt(b, 2 + t, 2, MASK_16BITS);
			int visible = getInt(b, t + 4, 1, 0x80) >> 7; // 1 bit
			int reserved = getInt(b, t + 4, 1, 0x40) >> 6; // 1 bit
			// chNumber is 14 bits in Nordig specs V1
			int chNumber = getInt(b, t + 4, 2, MASK_14BITS);
			LogicalChannel s = new LogicalChannel(serviceId, visible, reserved, chNumber);
			channelList.add(s);
			t += 4;
		}
	}

	@Override
	public String getDescriptorname(){
		return "Nordig Logical Channel Descriptor V1";
	}


}

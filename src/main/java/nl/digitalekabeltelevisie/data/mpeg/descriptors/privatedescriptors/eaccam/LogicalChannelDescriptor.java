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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorContext;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class LogicalChannelDescriptor extends AbstractLogicalChannelDescriptor {

	public class LogicalChannel extends AbstractLogicalChannel{

		public LogicalChannel(int service_id, int visible_service_flag, int reserved, int logical_channel_number) {
			super( service_id, visible_service_flag, reserved, logical_channel_number) ;
		}
	}

	public LogicalChannelDescriptor(final byte[] b, final int offset, final TableSection parent, DescriptorContext descriptorContext) {
		super(b, offset,parent, descriptorContext);
		
		int t=0;
		while (t<descriptorLength) {
			final int serviceId=getInt(b, offset+2+t,2,MASK_16BITS);
			final int visible = getInt(b,offset+t+4,1,0x80) >>7;
			final int reserved = getInt(b,offset+t+4,1,0x7C) >>2;
			final int chNumber=getInt(b, offset+t+4,2,MASK_10BITS);
			final AbstractLogicalChannel s = new LogicalChannel(serviceId, visible, reserved, chNumber);
			channelList.add(s);
			t+=4;
		}
	}


	@Override
	public String getDescriptorname(){
		return "EACEM Logical Channel Descriptor";
	}


}

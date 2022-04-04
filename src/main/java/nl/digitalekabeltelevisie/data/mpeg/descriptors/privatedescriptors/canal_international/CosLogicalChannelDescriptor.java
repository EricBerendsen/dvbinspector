/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international;

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorContext;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CosLogicalChannelDescriptor extends AbstractLogicalChannelDescriptor {


	public class CosLogicalChannel extends AbstractLogicalChannel {
		
		int free_access_flag;

		CosLogicalChannel(int service_id, int visible_service_flag, int free_access_flag, int reserved, int logical_channel_number) {
			super(service_id, visible_service_flag, reserved, logical_channel_number);
			this.free_access_flag = free_access_flag;
		}
		
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP(createNodeLabel(service_id, logical_channel_number)));
			s.add(new DefaultMutableTreeNode(new KVP("service_id",service_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("visible_service_flag",visible_service_flag,null)));
			s.add(new DefaultMutableTreeNode(new KVP("free_access_flag",free_access_flag,null)));
			s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			s.add(new DefaultMutableTreeNode(new KVP("logical_channel_number",logical_channel_number,null)));
			return s;
		}
	}

	public CosLogicalChannelDescriptor(final byte[] b, final int offset, final TableSection parent, DescriptorContext descriptorContext) {
		super(b, offset,parent, descriptorContext);
		int t=0;
		while (t<descriptorLength) {
			final int serviceId=getInt(b, offset+2+t,2,MASK_16BITS);
			final int visible = getInt(b,offset+t+4,1,0x80) >>7;
			final int free_access = getInt(b,offset+t+4,1,0x40) >>6;
			final int reserved = getInt(b,offset+t+4,1,0x3C) >>2;
			final int chNumber=getInt(b, offset+t+4,2,MASK_10BITS);
			channelList.add(new CosLogicalChannel(serviceId, visible, free_access, reserved, chNumber));
			t+=4;
		}
	}


	@Override
	public String getDescriptorname(){
		return "cos_logical_channel_descriptor";
	}

}

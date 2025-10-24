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

import static nl.digitalekabeltelevisie.util.Utils.MASK_10BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorContext;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.logicalchannel.AbstractLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

//based on NorDig Unified ver 2.3  12.2.9.3 NorDig private; Logical Channel Descriptor (version 2)
@SuppressWarnings("ALL")
public class NordigLogicalChannelDescriptorV2 extends AbstractLogicalChannelDescriptor {

	private List<ChannelList> channelLists = new ArrayList<>();

	public static record ChannelList(int channel_list_id, DVBString channel_list_name, String country_code, int service_loop_length,
			List<LogicalChannel> logicalChannelList) implements TreeNode {

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("Channel List (" + channel_list_name + ")");
			s.add(new KVP("channel_list_id", channel_list_id));
			s.add(new KVP("channel_list_name", channel_list_name));
			s.add(new KVP("country_code", country_code));
			s.add(new KVP("service_loop_length", service_loop_length));

			addListJTree(s, logicalChannelList, modus, "Logical Channels");
			return s;
		}

	}

	public class LogicalChannel extends AbstractLogicalChannel{

		public LogicalChannel(int service_id, int visible_service, int reserved, int logical_channel_number){
			super(service_id, visible_service, reserved, logical_channel_number);
		}

	}

	public NordigLogicalChannelDescriptorV2(byte[] b, TableSection parent, DescriptorContext descriptorContext) {
		super(b, parent, descriptorContext);
		int t = 0;
		while (t < descriptorLength) {
			int channel_list_id = getInt(b, 2 + t, 1, MASK_8BITS);
			DVBString channel_list_name = new DVBString(b, t + 3);
			t += 2 + channel_list_name.getLength();
			String country_code = getISO8859_1String(b, t + 2, 3);
			t += 3;
			int service_loop_length = getInt(b, 2 + t, 1, MASK_8BITS);
			t += 1;
			List<LogicalChannel> channelList = new ArrayList<>();
			int s = 0;
			while (s < service_loop_length) {
				int serviceId = getInt(b, 2 + t + s, 2, MASK_16BITS);
				int visible = getInt(b, t + 4 + s, 1, 0x80) >> 7;
				int reserved = getInt(b, t + 4 + s, 1, 0x7C) >> 2; // 5 bits
				// chNumber is 10 bits in Nordig specs V2
				int chNumber = getInt(b, t + 4 + s, 2, MASK_10BITS);
				LogicalChannel lc = new LogicalChannel(serviceId, visible, reserved, chNumber);
				channelList.add(lc);
				s += 4;
			}
			t += s;
			ChannelList chList = new ChannelList(channel_list_id, channel_list_name, country_code, service_loop_length, channelList);
			channelLists.add(chList);
		}
	}


	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		addListJTree(t,channelLists,modus,"Channel Lists");
		return t;
	}



	@Override
	public String getDescriptorname(){
		return "Nordig Logical Channel Descriptor V2";
	}


}

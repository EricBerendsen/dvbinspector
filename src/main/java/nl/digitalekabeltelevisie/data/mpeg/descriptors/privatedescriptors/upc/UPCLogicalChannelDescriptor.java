/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.upc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

@SuppressWarnings("ALL")
public class UPCLogicalChannelDescriptor extends Descriptor {


	private List<LogicalChannel> channelList = new ArrayList<>();

	public record LogicalChannel(int city_code, int region_code, int logicalChannelNumber) implements TreeNode {

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("logical_channel " + logicalChannelNumber);
			s.add(new KVP("region_code", region_code));
			s.add(new KVP("city_code", city_code));
			s.add(new KVP("logical_channel_number", logicalChannelNumber));
			return s;
		}

	}

	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		addListJTree(t,channelList,modus,"logical_channels");
		return t;
	}

	public UPCLogicalChannelDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			int region = getInt(b, 3 + t, 1, MASK_8BITS);
			int city = getInt(b, t + 4, 2, MASK_16BITS);
			int chNumber = getInt(b, t + 6, 2, MASK_16BITS);
			LogicalChannel s = new LogicalChannel(region, city, chNumber);
			channelList.add(s);
			t += 6;
		}
	}

	@Override
	public String getDescriptorname(){
		return "UPC_logic_channel_descriptor";
	}

	/**
	 * @return
	 */
	public int getLogicalChannelNumber() {
		if ((channelList!=null)&&(channelList.size()>0)) {
			return channelList.getFirst().logicalChannelNumber;
		}
		return -1;
	}


}

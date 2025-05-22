/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan;

import static nl.digitalekabeltelevisie.util.Utils.MASK_14BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

//based on M7 FastScan Spec v7.1 Page 28 
public class M7LogicalChannelDescriptor extends M7Descriptor {

	private List<LogicalChannel> channelList = new ArrayList<>();


	public class LogicalChannel implements TreeNode{
		private int serviceID;
		private int reserved;
		private final int hidden;

		private int logicalChannelNumber;

		public LogicalChannel(final int id, final int reserved, final int hidden, final int type){
			this.serviceID = id;
			this.reserved = reserved;
			this.hidden = hidden;
			logicalChannelNumber = type;
		}

		public int getServiceID() {
			return serviceID;
		}

		public void setServiceID(final int serviceID) {
			this.serviceID = serviceID;
		}

		public int getLogicalChannelNumber() {
			return logicalChannelNumber;
		}

		public void setLogicalChannelNumber(final int serviceType) {
			this.logicalChannelNumber = serviceType;
		}


		@Override
		public KVP getJTreeNode(final int modus) {
			final KVP s = new KVP("logical_channel: " + logicalChannelNumber);
			s.add(new KVP("service_id", serviceID));
			s.add(new KVP("reserved", reserved));
			s.add(new KVP("hidden", hidden));
			s.add(new KVP("logical_channel_number", logicalChannelNumber));
			return s;
		}

		public int getVisibleServiceFlag() {
			return reserved;
		}

		public void setVisibleServiceFlag(final int visibleServiceFlag) {
			this.reserved = visibleServiceFlag;
		}


	}

	public M7LogicalChannelDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			final int serviceId = getInt(b, 2 + t, 2, MASK_16BITS);
			final int reserved = getInt(b, t + 4, 1, 0x80) >> 7; // 1 bit
			final int hidden = getInt(b, t + 4, 1, 0x40) >> 6; // 1 bit
			// chNumber is 14 bits in Nordig specs V1
			final int chNumber = getInt(b, t + 4, 2, MASK_14BITS);
			final LogicalChannel s = new LogicalChannel(serviceId, reserved, hidden, chNumber);
			channelList.add(s);
			t += 4;
		}
	}

	public int getNoServices(){
		return channelList.size();
	}


	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (int i = 0; i < getNoServices(); i++) {
			final LogicalChannel s = channelList.get(i);
			buf.append("(").append(i).append(";").append(s.getServiceID()).append(":").append(s.getLogicalChannelNumber()).append(":").append(s.getVisibleServiceFlag()).append("),");
		}


		return buf.toString();
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		final KVP t = super.getJTreeNode(modus);
		t.addList(channelList, modus, "logical_channels");
		return t;
	}

	public List<LogicalChannel> getChannelList() {
		return channelList;
	}

}

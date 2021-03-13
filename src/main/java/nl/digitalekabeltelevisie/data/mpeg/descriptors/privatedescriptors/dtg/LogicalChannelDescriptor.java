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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.dtg;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class LogicalChannelDescriptor extends Descriptor {

	private List<LogicalChannel> channelList = new ArrayList<>();


	public class LogicalChannel implements TreeNode{
		private int serviceID;
		private final int reserved;

		private int logicalChannelNumber;

		public LogicalChannel(final int id, final int res, final int type){
			serviceID = id;
			reserved = res;
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


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			//TODO SDT.getServiceName needs original_network_id and transport_stream_id from enclosing NIT section TS loop.
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("logical_channel : "+logicalChannelNumber));
			s.add(new DefaultMutableTreeNode(new KVP("service_id",serviceID,null)));
			s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			s.add(new DefaultMutableTreeNode(new KVP("logical_channel_number",logicalChannelNumber,null)));
			return s;
		}



	}

	public LogicalChannelDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int serviceId=getInt(b, offset+2+t,2,MASK_16BITS);
			final int reserved = getInt(b,offset+t+4,1,0xFC) >>2;
			final int chNumber=getInt(b, offset+t+4,2,MASK_10BITS);
			final LogicalChannel s = new LogicalChannel(serviceId, reserved, chNumber);
			channelList.add(s);
			t+=4;
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
			buf.append("(").append(i).append(";").append(s.getServiceID()).append(":").append(s.getLogicalChannelNumber()).append("),");
		}


		return buf.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,channelList,modus,"logical_channels");
		return t;
	}

	public List<LogicalChannel> getChannelList() {
		return channelList;
	}

	public void setChannelList(final List<LogicalChannel> channelList) {
		this.channelList = channelList;
	}

	@Override
	public String getDescriptorname(){
		return "DTG Logical Channel Descriptor";
	}

}

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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.nordig;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

//based on NorDig Unified ver 2.3  12.2.9.3 NorDig private; Logical Channel Descriptor (version 2)
public class NordigLogicalChannelDescriptorV2 extends Descriptor {



	private List<ChannelList> channelLists = new ArrayList<>();
	public static class ChannelList implements TreeNode{
		/**
		 * @param channel_list_id
		 * @param channel_list_name
		 * @param service_loop_length
		 * @param logicalChannelList
		 */
		private ChannelList(int channel_list_id, DVBString channel_list_name,String country_code,
				int service_loop_length, List<LogicalChannel> logicalChannelList) {
			super();
			this.channel_list_id = channel_list_id;
			this.channel_list_name = channel_list_name;
			this.country_code = country_code;
			this.service_loop_length = service_loop_length;
			this.logicalChannelList = logicalChannelList;
		}


		private int channel_list_id;
		private DVBString channel_list_name;
		private final String country_code;
		private int service_loop_length;
		private List<LogicalChannel> logicalChannelList = new ArrayList<>();


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("Channel List ("+channel_list_name+")"));
			s.add(new DefaultMutableTreeNode(new KVP("channel_list_id",channel_list_id,null)));
			s.add(new DefaultMutableTreeNode(new KVP("channel_list_name_length",channel_list_name.getLength(),null)));
			s.add(new DefaultMutableTreeNode(new KVP("channel_list_name",channel_list_name,null)));
			s.add(new DefaultMutableTreeNode(new KVP("country_code",country_code,null)));
			s.add(new DefaultMutableTreeNode(new KVP("service_loop_length",service_loop_length,null)));

			addListJTree(s, logicalChannelList, modus, "Logical Channels");
			return s;
		}

	}

	public class LogicalChannel implements TreeNode{
		private int serviceID;
		private int visibleServiceFlag;
		private final int reserved;

		private int logicalChannelNumber;

		public LogicalChannel(final int id, final int visibleService, final int res, final int type){
			serviceID = id;
			visibleServiceFlag = visibleService;
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
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("logical_channel "+logicalChannelNumber));
			s.add(new DefaultMutableTreeNode(new KVP("service_id",serviceID,null)));
			s.add(new DefaultMutableTreeNode(new KVP("visible_service",visibleServiceFlag,null)));
			s.add(new DefaultMutableTreeNode(new KVP("reserved",reserved,null)));
			s.add(new DefaultMutableTreeNode(new KVP("logical_channel_number",logicalChannelNumber,null)));
			return s;
		}

		public int getVisibleServiceFlag() {
			return visibleServiceFlag;
		}

		public void setVisibleServiceFlag(final int visibleServiceFlag) {
			this.visibleServiceFlag = visibleServiceFlag;
		}


	}

	public NordigLogicalChannelDescriptorV2(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int channel_list_id=getInt(b, offset+2+t,1,MASK_8BITS);
			DVBString channel_list_name = new DVBString(b,offset+t+3);
			t+=2+channel_list_name.getLength();
			String country_code = getISO8859_1String(b, offset+t+2, 3);
			t+=3;
			int service_loop_length=getInt(b, offset+2+t,1,MASK_8BITS);
			t+=1;
			List<LogicalChannel> channelList = new ArrayList<>();
			int s=0;
			while (s<service_loop_length) {
				final int serviceId=getInt(b, offset+2+t+s,2,MASK_16BITS);
				final int visible = getInt(b,offset+t+4+s,1,0x80) >>7;
				final int reserved = getInt(b,offset+t+4+s,1,0x7C) >>2; // 5 bits
				// chNumber is 10 bits in Nordig specs V2
				final int chNumber=getInt(b, offset+t+4+s,2,MASK_10BITS);
				final LogicalChannel lc = new LogicalChannel(serviceId, visible, reserved, chNumber);
				channelList.add(lc);
				s+=4;
			}
			t+=s;
			ChannelList chList = new ChannelList(channel_list_id, channel_list_name,country_code,service_loop_length,channelList);
			channelLists.add(chList);
		}
	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,channelLists,modus,"Channel Lists");
		return t;
	}



	@Override
	public String getDescriptorname(){
		return "Nordig Logical Channel Descriptor V2";
	}


}

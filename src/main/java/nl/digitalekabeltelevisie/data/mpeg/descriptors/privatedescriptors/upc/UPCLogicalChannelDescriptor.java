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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class UPCLogicalChannelDescriptor extends Descriptor {


	private List<LogicalChannel> channelList = new ArrayList<>();

	public static class LogicalChannel implements TreeNode{
		private final int city_code;
		private final int region_code;

		private int logicalChannelNumber;

		public LogicalChannel(final int regionCode, final int cityCode, final int number){
			city_code = cityCode;
			region_code = regionCode;
			logicalChannelNumber = number;
		}

		public int getLogicalChannelNumber() {
			return logicalChannelNumber;
		}

		public void setLogicalChannelNumber(final int serviceType) {
			this.logicalChannelNumber = serviceType;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("logical_channel " +logicalChannelNumber));
			s.add(new DefaultMutableTreeNode(new KVP("region_code",region_code,null)));
			s.add(new DefaultMutableTreeNode(new KVP("city_code",city_code,null)));
			s.add(new DefaultMutableTreeNode(new KVP("logical_channel_number",logicalChannelNumber,null)));
			return s;
		}


	}



	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,channelList,modus,"logical_channels");
		return t;
	}


	public UPCLogicalChannelDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final int region=getInt(b, offset+3+t,1,MASK_8BITS);
			final int city = getInt(b,offset+t+4,2,MASK_16BITS);
			final int chNumber=getInt(b, offset+t+6,2,MASK_16BITS);
			final LogicalChannel s = new LogicalChannel(region, city, chNumber);
			channelList.add(s);
			t+=6;
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
			return channelList.get(0).getLogicalChannelNumber();
		}
		return -1;
	}


}

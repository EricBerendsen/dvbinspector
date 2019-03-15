package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;


public class FNTsection extends TableSectionExtendedSyntax{

	List<Descriptor> networkDescriptorList;
	public List<TransportStream> transportStreamList;
	private int network_descriptors_loop_length;
	private int transport_stream_loop_length;

	public static class TransportStream implements TreeNode{
		private int transportStreamID;
		private int originalNetworkID;
		private int transportDescriptorsLength;

		public List<Descriptor> descriptorList;

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(final List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}

		public int getOriginalNetworkID() {
			return originalNetworkID;
		}

		public void setOriginalNetworkID(final int originalNetworkID) {
			this.originalNetworkID = originalNetworkID;
		}

		public int getTransportDescriptorsLength() {
			return transportDescriptorsLength;
		}

		public void setTransportDescriptorsLength(final int transportDescriptorsLength) {
			this.transportDescriptorsLength = transportDescriptorsLength;
		}

		public int getTransportStreamID() {
			return transportStreamID;
		}

		public void setTransportStreamID(final int transportStreamID) {
			this.transportStreamID = transportStreamID;
		}

		@Override
		public String toString(){
			final StringBuilder b = new StringBuilder("Service, transportStreamID=");
			b.append(getTransportStreamID()).append(", originalNetworkID=").append(getOriginalNetworkID()).append(", ");
			final Iterator<Descriptor> j=descriptorList.iterator();
			while (j.hasNext()) {
				final Descriptor d = j.next();
				b.append(d).append(", ");

			}
			return b.toString();

		}
		public DefaultMutableTreeNode getJTreeNode(final int modus){

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("transport_stream:",transportStreamID,null));

			t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("original_network_id",originalNetworkID,Utils.getOriginalNetworkIDString(originalNetworkID) )));
			t.add(new DefaultMutableTreeNode(new KVP("transport_descriptors_length",getTransportDescriptorsLength(),null)));

			Utils.addListJTree(t,descriptorList,modus,"transport_descriptors");

			return t;
		}



	}



	public FNTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);

		network_descriptors_loop_length = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_12BITS);
		transport_stream_loop_length= Utils.getInt(raw_data.getData(), 10+network_descriptors_loop_length, 2, Utils.MASK_12BITS);

		networkDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),10,network_descriptors_loop_length,this);
		transportStreamList = buildTransportStreamList(raw_data.getData(), 12+network_descriptors_loop_length, transport_stream_loop_length);
	}


	public int getOperatorNetworkID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("FNTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=").append(getTableType(tableId)). append(", NetworkID=").append(getOperatorNetworkID()).append(", ");

		return b.toString();
	}


	public List<Descriptor> getNetworkDescriptorList() {
		return networkDescriptorList;
	}



	public List<TransportStream> getTransportStreamList() {
		return transportStreamList;
	}



	public int getTransportStreamLoopLength() {
		return transport_stream_loop_length;
	}

	public int noTransportStreams() {
		return transportStreamList.size();
	}

	private final List<TransportStream> buildTransportStreamList(final byte[] data, final int i, final int programInfoLength) {
		final ArrayList<TransportStream> r = new ArrayList<TransportStream>();
		int t =0;
		while(t<programInfoLength){
			final TransportStream c = new TransportStream();
			c.setTransportStreamID(Utils.getInt(data, i+t, 2, Utils.MASK_16BITS));
			c.setOriginalNetworkID(Utils.getInt(data, i+t+2, 2, Utils.MASK_16BITS));
			c.setTransportDescriptorsLength(Utils.getInt(data, i+t+4, 2, Utils.MASK_12BITS));
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,i+t+6,c.getTransportDescriptorsLength(),this));
			t+=6+c.getTransportDescriptorsLength();
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("network_descriptors_loop_length",network_descriptors_loop_length,null)));
		Utils.addListJTree(t,networkDescriptorList,modus,"network_descriptors");
		t.add(new DefaultMutableTreeNode(new KVP("transport_stream_loop_length",getTransportStreamLoopLength(),null)));

		Utils.addListJTree(t,transportStreamList,modus,"transport_stream_loop");


		return t;
	}


}

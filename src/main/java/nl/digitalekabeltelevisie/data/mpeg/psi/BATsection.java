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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class BATsection extends TableSectionExtendedSyntax{

	private List<Descriptor>		networkDescriptorList;
	private List<TransportStream>	transportStreamList;
	private int						networkDescriptorsLength;
	private int						transportStreamLoopLength;

	public class TransportStream implements TreeNode {

		private int			transportStreamID;
		private int			originalNetworkID;
		private int			transportDescriptorsLength;

		private List<Descriptor>	descriptorList;

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
		public String toString() {
			final StringBuilder b = new StringBuilder("Service, transportStreamID=");
			b.append(getTransportStreamID()).append(", originalNetworkID=").append(getOriginalNetworkID()).append(", ");
			for (Descriptor d : descriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}

		public DefaultMutableTreeNode getJTreeNode(final int modus) {

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("transport_stream:",transportStreamID,null));

			t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id", transportStreamID, null)));
			t.add(new DefaultMutableTreeNode(new KVP("original_network_id", originalNetworkID, Utils
					.getOriginalNetworkIDString(originalNetworkID))));
			t.add(new DefaultMutableTreeNode(new KVP("transport_descriptors_length", getTransportDescriptorsLength(),
					null)));

			Utils.addListJTree(t, descriptorList, modus, "transport_descriptors");

			return t;
		}

		public Map<String, Object> getTableRowData() {
			HashMap<String, Object> streamData = new HashMap<>();

			streamData.put("bouquet_id",getTableIdExtension());
			streamData.put("bouquet_id_name",Utils.getBouquetIDString(getTableIdExtension()));

			streamData.put("transport_stream_id", getTransportStreamID());
			streamData.put("original_network_id", getOriginalNetworkID());
			streamData.put("original_network_name", Utils.getOriginalNetworkIDString(originalNetworkID));
			

			return streamData;

		}

	}

	public BATsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);

		networkDescriptorsLength = Utils.getInt(raw_data.getData(), 8, 2, 0x0FFF);
		transportStreamLoopLength = Utils.getInt(raw_data.getData(), 10 + networkDescriptorsLength, 2, 0x0FFF);

		networkDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(), 10, networkDescriptorsLength,
				this);
		transportStreamList = buildTransportStreamList(raw_data.getData(), 12 + networkDescriptorsLength,
				transportStreamLoopLength);
	}

	public int getBouqetID() {
		return getTableIdExtension();
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("BATsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=")
		.append(getTableType(tableId)).append(", batID=").append(getBouqetID()).append(", ");

		return b.toString();
	}

	public List<Descriptor> getNetworkDescriptorList() {
		return networkDescriptorList;
	}

	public void setNetworkDescriptorList(final List<Descriptor> networkDescriptorList) {
		this.networkDescriptorList = networkDescriptorList;
	}

	public int getNetworkDescriptorsLength() {
		return networkDescriptorsLength;
	}

	public void setNetworkDescriptorsLength(final int networkDescriptorsLength) {
		this.networkDescriptorsLength = networkDescriptorsLength;
	}

	public List<TransportStream> getTransportStreamList() {
		return transportStreamList;
	}

	public void setTransportStreamList(final List<TransportStream> transportStreamList) {
		this.transportStreamList = transportStreamList;
	}

	public int getTransportStreamLoopLength() {
		return transportStreamLoopLength;
	}

	public int noTransportStreams() {
		return transportStreamList.size();
	}

	public void setTransportStreamLoopLength(final int transportStreamLoopLength) {
		this.transportStreamLoopLength = transportStreamLoopLength;
	}

	private List<TransportStream> buildTransportStreamList(final byte[] data, final int i, final int programInfoLength) {
		final List<TransportStream> r = new ArrayList<>();
		int t = 0;
		while (t < programInfoLength) {
			final TransportStream c = new TransportStream();
			c.setTransportStreamID(Utils.getInt(data, i + t, 2, 0x0FFF));
			c.setOriginalNetworkID(Utils.getInt(data, i + t + 2, 2, 0x0FFF));
			c.setTransportDescriptorsLength(Utils.getInt(data, i + t + 4, 2, 0x0FFF));
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data, i + t + 6, c
					.getTransportDescriptorsLength(), this));
			t += 6 + c.getTransportDescriptorsLength();
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		KVP kvp = (KVP) t.getUserObject();
		if(!transportStreamList.isEmpty()) {
			kvp.setTableSource(this::getTableModel);
		}
		t.add(new DefaultMutableTreeNode(new KVP("network_descriptors_lengt", getNetworkDescriptorsLength(), null)));
		Utils.addListJTree(t, networkDescriptorList, modus, "network_descriptors");
		t.add(new DefaultMutableTreeNode(new KVP("transport_stream_loop_length", getTransportStreamLoopLength(), null)));
		Utils.addListJTree(t, transportStreamList, modus, "transport_stream_loop");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "bouquet_id";
	}


	public TableModel getTableModel() {
		FlexTableModel<BATsection,TransportStream> tableModel =  new FlexTableModel<>(BAT.buildBatTableHeader());

		tableModel.addData(this, getTransportStreamList());

		tableModel.process();
		return tableModel;
	}
//    
//	public List<Map<String, Object>> getRowData() {
//		List<Map<String, Object>> rowData = new ArrayList<Map<String,Object>>(); 
//
//		HashMap<String, Object> networkTableData = TableUtils.getDescriptorTableData(getNetworkDescriptorList());
//		for(TransportStream stream:transportStreamList) {
//			Map<String, Object> transportStreamTableRow = stream.getTableRowData();
//			transportStreamTableRow.putAll(networkTableData);
//			rowData.add(transportStreamTableRow);
//		}
//		return rowData;
//	}


	
//	static TableHeader<BATsection,List<BATsection.TransportStream>> buildBatTableHeader() {
//		Function<BATsection, Object> fun = BATsection::getBouqetID;
//		Function<BATsection, Object> fun2 = (b) -> Utils.getBouquetIDString(b.getBouqetID());
//		return new TableHeaderBuilder<BATsection,List<BATsection.TransportStream>>().
//				addBaseColumn("bouquet id",fun, Integer.class).
//				addBaseColumn("bouquet id",fun2, Integer.class).
////				addBaseColumn("bouquet id",BATsection::getBouqetID, Integer.class).
////				addOptionalColumn("bouquet id name", "bouquet_id_name", String.class).
////				
////				addOptionalColumn("bouquet name descriptor", "bouquet.name", String.class).
////				
////				addOptionalColumn("transport stream id", "transport_stream_id", Integer.class).
////				addOptionalColumn("original network id", "original_network_id", Integer.class).
////				addOptionalColumn("original network name", "original_network_name", String.class).
//				
//				
//				
//				build();
//	}


}

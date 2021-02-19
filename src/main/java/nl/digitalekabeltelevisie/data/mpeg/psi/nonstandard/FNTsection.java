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

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyFunc;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyListFunc;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatOrbitualPosition;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatSatelliteFrequency;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatSymbolRate;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.getFEC_innerString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor.getModulationString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor.getPolarizationString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor.getRollOffString;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.NetworkNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceListDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7LogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;


public class FNTsection extends TableSectionExtendedSyntax{

	final List<Descriptor> networkDescriptorList;
	public final List<TransportStream> transportStreamList;
	private final int network_descriptors_loop_length;
	private final int transport_stream_loop_length;

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
			for (Descriptor d : descriptorList) {
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
		networkDescriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),10,network_descriptors_loop_length,this);
		transport_stream_loop_length= Utils.getInt(raw_data.getData(), 10+network_descriptors_loop_length, 2, Utils.MASK_12BITS);
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

	private List<TransportStream> buildTransportStreamList(final byte[] data, final int i, final int programInfoLength) {
		final ArrayList<TransportStream> r = new ArrayList<>();
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
		KVP kvp = (KVP) t.getUserObject();
		kvp.setTableSource(this::getTableModel);

		t.add(new DefaultMutableTreeNode(new KVP("network_descriptors_loop_length",network_descriptors_loop_length,null)));
		Utils.addListJTree(t,networkDescriptorList,modus,"network_descriptors");
		t.add(new DefaultMutableTreeNode(new KVP("transport_stream_loop_length",getTransportStreamLoopLength(),null)));

		Utils.addListJTree(t,transportStreamList,modus,"transport_stream_loop");


		return t;
	}

	
	public TableModel getTableModel() {
		FlexTableModel<FNTsection,TransportStream> tableModel =  new FlexTableModel<>(buildFntTableHeader());

		tableModel.addData(this, getTransportStreamList());

		tableModel.process();
		return tableModel;
	}

	static TableHeader<FNTsection,TransportStream>  buildFntTableHeader() {
		return new TableHeaderBuilder<FNTsection,TransportStream>().
				addOptionalBaseColumn("network name",
						network -> findDescriptorApplyFunc(network.getNetworkDescriptorList(),
								NetworkNameDescriptor.class,
								nnd -> ("".equals(nnd.getNetworkNameAsString())?null:nnd.getNetworkNameAsString())),
						String.class).
				addRequiredRowColumn("onid", TransportStream::getOriginalNetworkID, Integer.class).
				addRequiredRowColumn("tsid", TransportStream::getTransportStreamID, Integer.class).


				addOptionalRowColumn("sat frequency",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatSatelliteFrequency(sat.getFrequency())),
						Number.class).
				addOptionalRowColumn("sat position",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatOrbitualPosition(sat.getOrbitalPosition())),
						Number.class).
				addOptionalRowColumn("sat west_east",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								SatelliteDeliverySystemDescriptor::getWestEastFlagString),
						String.class).
				addOptionalRowColumn("sat polarization",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getPolarizationString(sat.getPolarization())),
						String.class).
				addOptionalRowColumn("mod system",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								SatelliteDeliverySystemDescriptor::getModulationSystemString),
						String.class).
				addOptionalRowColumn("roll_off",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> (sat.getModulationSystem()==1? getRollOffString(sat.getRollOff()):null)),
						Number.class).
				addOptionalRowColumn("mod type",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getModulationString(sat.getModulationType())),
						String.class).
				addOptionalRowColumn("sat symbol_rate",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatSymbolRate(sat.getSymbol_rate())),
						Number.class).
				addOptionalRowColumn("sat fec_inner",
						transportStream -> findDescriptorApplyFunc(transportStream.getDescriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getFEC_innerString(sat.getFEC_inner())),
						String.class).

				addOptionalRepeatingGroupedColumn("sid",
						transportStream -> findDescriptorApplyListFunc(transportStream.getDescriptorList(),
								ServiceListDescriptor.class,
								sld -> sld.getServiceList().
								stream().
								map(ServiceListDescriptor.Service::getServiceID).
								collect(Collectors.toList())),

						Integer.class,
						"service_list").
				addOptionalRepeatingGroupedColumn("type",
						transportStream -> findDescriptorApplyListFunc(transportStream.getDescriptorList(),
								ServiceListDescriptor.class,
								sld -> sld.getServiceList().
								stream().
								map(p ->Descriptor.getServiceTypeString(p.getServiceType())).
								collect(Collectors.toList())),

						String.class,
						"service_list").

				addOptionalRepeatingGroupedColumn("lcn sid",
						transportStream -> findDescriptorApplyListFunc(transportStream.getDescriptorList(),
								M7LogicalChannelDescriptor.class,
								lcd -> lcd.getChannelList().
								stream().
								map(M7LogicalChannelDescriptor.LogicalChannel::getServiceID).
								collect(Collectors.toList())),

						Integer.class,
						"lcn_list").
				addOptionalRepeatingGroupedColumn("lcn no",
						transportStream -> findDescriptorApplyListFunc(transportStream.getDescriptorList(),
								M7LogicalChannelDescriptor.class,
								lcd -> lcd.getChannelList().
								stream().
								map(M7LogicalChannelDescriptor.LogicalChannel::getLogicalChannelNumber).
								collect(Collectors.toList())),

						Integer.class,
						"lcn_list").

				build();
	}

}

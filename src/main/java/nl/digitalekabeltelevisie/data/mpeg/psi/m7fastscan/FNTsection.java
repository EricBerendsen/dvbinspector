package nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

	private final List<Descriptor> networkDescriptorList;
	private final List<TransportStream> transportStreamList;
	private final int network_descriptors_loop_length;
	private final int transport_stream_loop_length;

	public record TransportStream(int transportStreamID, int originalNetworkID, int transportDescriptorsLength,
								  List<Descriptor> descriptorList) implements TreeNode {

		@Override
        public KVP getJTreeNode(int modus) {
			KVP kvp = new KVP("transport_stream:", transportStreamID);
			kvp.add(new KVP("transport_stream_id", transportStreamID));
			kvp.add(new KVP("original_network_id", originalNetworkID, Utils.getOriginalNetworkIDString(originalNetworkID)));
			kvp.add(new KVP("transport_descriptors_length", transportDescriptorsLength));
			kvp.addList(descriptorList, modus, "transport_descriptors");

			return kvp;
		}
	}


	public FNTsection(PsiSectionData raw_data, PID parent){
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

        return "FNTsection section=" + getSectionNumber() + ", lastSection=" + getSectionLastNumber() + ", tableType=" + getTableType(tableId) + ", NetworkID=" + getOperatorNetworkID() + ", ";
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

	private List<TransportStream> buildTransportStreamList(byte[] data, int i, int programInfoLength) {
		List<TransportStream> transportStreams = new ArrayList<>();
		int t = 0;
		while (t < programInfoLength) {
			int transportStreamID = Utils.getInt(data, i + t, 2, Utils.MASK_16BITS);
			int originalNetworkID = Utils.getInt(data, i + t + 2, 2, Utils.MASK_16BITS);
			int transportDescriptorsLength = Utils.getInt(data, i + t + 4, 2, Utils.MASK_12BITS);
			List<Descriptor> descriptorList = DescriptorFactory.buildDescriptorList(data, i + t + 6, transportDescriptorsLength, this);
			TransportStream transportStream = new TransportStream(transportStreamID, originalNetworkID, transportDescriptorsLength, descriptorList);
			transportStreams.add(transportStream);
			t += 6 + transportDescriptorsLength;

		}

		return transportStreams;
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP kvp = (KVP) super.getJTreeNode(modus);
		kvp.addTableSource(this::getTableModel, "FNT");

		kvp.add( new KVP("network_descriptors_loop_length",network_descriptors_loop_length ));
		kvp.addList(networkDescriptorList,modus,"network_descriptors");
		kvp.add( new KVP("transport_stream_loop_length", transport_stream_loop_length ));

		kvp.addList( transportStreamList,modus,"transport_stream_loop");
		return kvp;
	}

	
	public TableModel getTableModel() {
		FlexTableModel<FNTsection,TransportStream> tableModel =  new FlexTableModel<>(buildFntTableHeader());

		tableModel.addData(this, transportStreamList);

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
				addRequiredRowColumn("onid", TransportStream::originalNetworkID, Integer.class).
				addRequiredRowColumn("tsid", TransportStream::transportStreamID, Integer.class).


				addOptionalRowColumn("sat frequency",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatSatelliteFrequency(sat.getFrequency())),
						Number.class).
				addOptionalRowColumn("sat position",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatOrbitualPosition(sat.getOrbitalPosition())),
						Number.class).
				addOptionalRowColumn("sat west_east",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								SatelliteDeliverySystemDescriptor::getWestEastFlagString),
						String.class).
				addOptionalRowColumn("sat polarization",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getPolarizationString(sat.getPolarization())),
						String.class).
				addOptionalRowColumn("mod system",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								SatelliteDeliverySystemDescriptor::getModulationSystemString),
						String.class).
				addOptionalRowColumn("roll_off",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> (sat.getModulationSystem()==1? getRollOffString(sat.getRollOff()):null)),
						Number.class).
				addOptionalRowColumn("mod type",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getModulationString(sat.getModulationType())),
						String.class).
				addOptionalRowColumn("sat symbol_rate",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatSymbolRate(sat.getSymbol_rate())),
						Number.class).
				addOptionalRowColumn("sat fec_inner",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getFEC_innerString(sat.getFEC_inner())),
						String.class).

				addOptionalRepeatingGroupedColumn("sid",
						transportStream -> findDescriptorApplyListFunc(transportStream.descriptorList(),
								ServiceListDescriptor.class,
								sld -> sld.getServiceList().
								stream().
								map(ServiceListDescriptor.Service::getServiceID).
								collect(Collectors.toList())),

						Integer.class,
						"service_list").
				addOptionalRepeatingGroupedColumn("type",
						transportStream -> findDescriptorApplyListFunc(transportStream.descriptorList(),
								ServiceListDescriptor.class,
								sld -> sld.getServiceList().
								stream().
								map(p ->Descriptor.getServiceTypeString(p.getServiceType())).
								collect(Collectors.toList())),

						String.class,
						"service_list").

				addOptionalRepeatingGroupedColumn("lcn sid",
						transportStream -> findDescriptorApplyListFunc(transportStream.descriptorList(),
								M7LogicalChannelDescriptor.class,
								lcd -> lcd.getChannelList().
								stream().
								map(M7LogicalChannelDescriptor.LogicalChannel::getServiceID).
								collect(Collectors.toList())),

						Integer.class,
						"lcn_list").
				addOptionalRepeatingGroupedColumn("lcn no",
						transportStream -> findDescriptorApplyListFunc(transportStream.descriptorList(),
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

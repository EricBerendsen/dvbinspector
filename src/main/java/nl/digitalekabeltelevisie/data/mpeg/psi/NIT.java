package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatCableFrequency;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatOrbitualPosition;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatSatelliteFrequency;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatSymbolRate;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatTerrestrialFrequency;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.getFEC_innerString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor.getModulationString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor.getPolarizationString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor.getRollOffString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.TerrestrialDeliverySystemDescriptor.getBandwidtString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.TerrestrialDeliverySystemDescriptor.getPriorityString;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2DeliverySystemDescriptor.getBandwidthList;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2DeliverySystemDescriptor.getGuardIntervalList;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2DeliverySystemDescriptor.getSisoMisoModeList;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2DeliverySystemDescriptor.getTransmissionModeList;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.CableDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.NetworkNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceListDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceListDescriptor.Service;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.TerrestrialDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.extension.dvb.T2DeliverySystemDescriptor;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class NIT extends AbstractPSITabel{

	private Map<Integer, NITsection []> networks = new HashMap<>();


	public NIT(final PSI parent){
		super(parent);
	}

	public void update(final NITsection section){
		final int key = section.getNetworkID();
		NITsection[] sections = networks.computeIfAbsent(key, k -> new NITsection[section.getSectionLastNumber() + 1]);

		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP kvpNit = new KVP("NIT");
		if(!networks.isEmpty()) {
			kvpNit.addTableSource(this::getTableModel,"Transport Streams");
		}
		final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode( kvpNit);
		final TreeSet<Integer> s = new TreeSet<>(networks.keySet());

		for (Integer networkNo : s) {
			KVP kvp = new KVP("network_id", networkNo, getNetworkName(networkNo));
			kvp.addTableSource(() -> getTableForNetworkID(networkNo),"Transport Streams");
			kvp.addTableSource(() -> getServiceTableForNetworkID(networkNo),"Services");
			
			
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(kvp);

			final NITsection[] sections = networks.get(networkNo);
			for (final NITsection tsection : sections) {
				if (tsection != null) {
					if (!Utils.simpleModus(modus)) {
						addSectionVersionsToJTree(n, tsection, modus);
					} else {
						addListJTree(n, tsection.getNetworkDescriptorList(), modus, "descriptors");
						addListJTree(n, tsection.getTransportStreamList(), modus, "transport streams");
					}
				}
			}
			treeNode.add(n);
		}
		return treeNode;
	}


	public String getNetworkName(int networkNo){
		final NITsection [] sections = networks.get(networkNo);
		if(sections!=null){
			for (final NITsection section : sections) {
				if(section!= null){
					for (Descriptor d : section.getNetworkDescriptorList()) {
						if (d instanceof NetworkNameDescriptor networkNameDescriptor) {
							return networkNameDescriptor.getNetworkNameAsString();
						}
					}

				}
			}
		}
		return null;
	}
	
	public NITsection [] getNetwork(int networkNo) {
		return networks.get(networkNo);
	}


	public Map<Integer, NITsection[]> getNetworks() {
		return networks;
	}

	public int getActualNetworkID(){

		final TreeSet<Integer> s = new TreeSet<>(networks.keySet());

		for (Integer networkNo : s) {
			final NITsection[] sections = networks.get(networkNo);
			for (final NITsection tsection : sections) {
				if ((tsection != null) && (tsection.getTableId() == 0x40)) {
					return networkNo;
				}
			}
		}
		return -1;
	}

	public List<Descriptor> getNetworkDescriptors(final int networkNo){
		final ArrayList<Descriptor> res = new ArrayList<>();
		final NITsection [] sections = networks.get(networkNo);
		if(sections!=null){
			for (final NITsection tsection : sections) {
				if(tsection!=null){
					res.addAll(tsection.getNetworkDescriptorList());
				}
			}
		}
		return res;

	}


	private TableModel getTableForNetworkID(int networkNo) {
		FlexTableModel<NITsection,TransportStream> tableModel =  new FlexTableModel<>(buildNitTableHeader());

		final NITsection [] sections = networks.get(networkNo);
		
		for (final NITsection tsection : sections) {
			if(tsection!= null){
				tableModel.addData(tsection, tsection.getTransportStreamList());
			}
		}

		tableModel.process();
		return tableModel;
	}


	
	public TableModel getTableModel() {
		FlexTableModel<NITsection,TransportStream> tableModel =  new FlexTableModel<>(buildNitTableHeader());

		for(NITsection[] nitSections:networks.values()) {
			for (final NITsection tsection : nitSections) {
				if(tsection!= null){
					tableModel.addData(tsection, tsection.getTransportStreamList());
				}
			}
		}

		tableModel.process();
		return tableModel;
	}

	
	private TableModel getServiceTableForNetworkID(int networkNo) {
		FlexTableModel<TransportStream,Service> tableModel =  new FlexTableModel<>(buildServiceTableHeader());

		final NITsection [] sections = networks.get(networkNo);

		for (final NITsection tsection : sections) {
			if(tsection!= null){
				List<TransportStream> tsList = tsection.getTransportStreamList();
				for(TransportStream ts:tsList){
					List<ServiceListDescriptor> sldList = Descriptor.findGenericDescriptorsInList(ts.descriptorList(), ServiceListDescriptor.class);
					for(ServiceListDescriptor sld:sldList) {
							tableModel.addData(ts, sld.getServiceList());
					}
				}
				
			}
		}

		tableModel.process();
		return tableModel;
	}

	static TableHeader<TransportStream,Service> buildServiceTableHeader() {
		return new TableHeaderBuilder<TransportStream,Service>().
				addRequiredBaseColumn("tsid", TransportStream::transport_stream_id, Integer.class).
				addRequiredBaseColumn("onid", TransportStream::original_network_id, Integer.class).
				addRequiredRowColumn("sid",Service::getServiceID,Number.class).
				addRequiredRowColumn("type",Service::getServiceType,Integer.class).
				addRequiredRowColumn("type description",Service::getServiceTypeString,String.class).
				addOptionalRowColumn("name",Service::getServiceName,String.class).
				addOptionalRowColumn("lcn",Service::getLCN,Integer.class).
				build();
				
	}
	
	static TableHeader<NITsection,TransportStream>  buildNitTableHeader() {

		return new TableHeaderBuilder<NITsection,TransportStream>().
				addRequiredBaseColumn("network_id", NITsection::getNetworkID,Integer.class).
				addRequiredRowColumn("tsid", TransportStream::transport_stream_id, Integer.class).
				addRequiredRowColumn("onid", TransportStream::original_network_id, Integer.class).

				// terrestrial

				addOptionalRowColumn("terrestrial frequency",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								ter -> formatTerrestrialFrequency(ter.getFrequency())),
						Number.class).

				addOptionalRowColumn("terrestrial bandwidth",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								ter -> getBandwidtString(ter.getBandwidth())),
						Number.class).

				addOptionalRowColumn("terrestrial priority",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								ter -> getPriorityString(ter.getPriority())),
						String.class).
				addOptionalRowColumn("terrestrial time_slicing_indicator",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								TerrestrialDeliverySystemDescriptor::getTimeSlicingString),
						String.class).
				addOptionalRowColumn("terrestrial MPE-FEC_indicator",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								TerrestrialDeliverySystemDescriptor::getMpeFecString),
						String.class).

				addOptionalRowColumn("terrestrial constellation",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								t -> TerrestrialDeliverySystemDescriptor.getConstellationString(t.getConstellation())),
						String.class).

				
				addOptionalRowColumn("terrestrial hierarchy_information",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								t -> TerrestrialDeliverySystemDescriptor.getHierarchyInformationString(t.getHierarchy_information())),
						String.class).
				
				
				addOptionalRowColumn("terrestrial code_rate-HP_stream",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								t -> TerrestrialDeliverySystemDescriptor.getCodeRateString(t.getCode_rate_HP_stream())),
						String.class).
				
				addOptionalRowColumn("terrestrial code_rate-LP_stream",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								t -> TerrestrialDeliverySystemDescriptor.getCodeRateString(t.getCode_rate_LP_stream())),
						String.class).

				addOptionalRowColumn("terrestrial guard_interval",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								t -> TerrestrialDeliverySystemDescriptor.getGuardIntervalString(t.getGuard_interval())),
						String.class).

				addOptionalRowColumn("terrestrial transmission_mode",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								TerrestrialDeliverySystemDescriptor.class,
								t -> TerrestrialDeliverySystemDescriptor.getTransmissionModeString(t.getTransmission_mode())),
						String.class).
				
				// DVB-T2

				addOptionalRowColumn("T2 plp_id",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								T2DeliverySystemDescriptor.class,
								T2DeliverySystemDescriptor::getPlp_id),
						Integer.class).
				addOptionalRowColumn("T2_system_id",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								T2DeliverySystemDescriptor.class,
								T2DeliverySystemDescriptor::getT2_system_id),
						Integer.class).


				addOptionalRowColumn("T2 siso_miso",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								T2DeliverySystemDescriptor.class,
								t2 -> (t2.getDescriptorLength()>4? getSisoMisoModeList().get(t2.getSiso_miso()):null)),
						String.class).
				addOptionalRowColumn("T2 bandwidth",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								T2DeliverySystemDescriptor.class,
								t2 -> (t2.getDescriptorLength()>4? getBandwidthList().get(t2.getBandwidth()):null)),
						Number.class).

				addOptionalRowColumn("T2 guard_interval",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								T2DeliverySystemDescriptor.class,
								t2 -> (t2.getDescriptorLength()>4? getGuardIntervalList().get(t2.getGuard_interval()):null)),
						Number.class).
				addOptionalRowColumn("T2 transmission_mode",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								T2DeliverySystemDescriptor.class,
								t2 -> (t2.getDescriptorLength()>4? getTransmissionModeList().get(t2.getTransmission_mode()):null)),
						Number.class).



				// Cable

				addOptionalRowColumn("cable frequency",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								CableDeliverySystemDescriptor.class,
								cable -> formatCableFrequency(cable.getFrequency())),
						Number.class).
				addOptionalRowColumn("cable fec_outter",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								CableDeliverySystemDescriptor.class,
								CableDeliverySystemDescriptor::getFEC_outerString),
						String.class).

				addOptionalRowColumn("cable modulation",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								CableDeliverySystemDescriptor.class,
								cable -> CableDeliverySystemDescriptor.getModulationString(cable.getModulation())),
						String.class).

				addOptionalRowColumn("cable symbol_rate",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								CableDeliverySystemDescriptor.class,
								cable -> formatSymbolRate(cable.getSymbol_rate())),
						Number.class).
				addOptionalRowColumn("cable fec_inner",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								CableDeliverySystemDescriptor.class,
								cable -> Descriptor.getFEC_innerString(cable.getFEC_inner())),
						String.class).

				// Satellite

				addOptionalRowColumn("satellite frequency",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatSatelliteFrequency(sat.getFrequency())),
						Number.class).
				addOptionalRowColumn("satellite orbital_position",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatOrbitualPosition(sat.getOrbitalPosition())),
						Number.class).
				addOptionalRowColumn("satellite west_east_flag",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								SatelliteDeliverySystemDescriptor::getWestEastFlagString),
						String.class).
				addOptionalRowColumn("satellite polarization",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getPolarizationString(sat.getPolarization())),
						String.class).
				addOptionalRowColumn("satellite modulation_system",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								SatelliteDeliverySystemDescriptor::getModulationSystemString),
						String.class).
				addOptionalRowColumn("satellite roll_off",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> (sat.getModulationSystem()==1? getRollOffString(sat.getRollOff()):null)),
						Number.class).
				addOptionalRowColumn("satellite modulation_type",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getModulationString(sat.getModulationType())),
						String.class).
				addOptionalRowColumn("satellite symbol_rate",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> formatSymbolRate(sat.getSymbol_rate())),
						Number.class).
				addOptionalRowColumn("satellite fec_inner",
						transportStream -> findDescriptorApplyFunc(transportStream.descriptorList(),
								SatelliteDeliverySystemDescriptor.class,
								sat -> getFEC_innerString(sat.getFEC_inner())),
						String.class).
				build();
	}

	/**
	 * @param networkID
	 * @param streamID
	 * @return OriginalNetworkID for streamID, or -1 if stream not found in network with ID networkID
	 */
	public int getOriginalNetworkID(int networkID, int streamID) {
		final NITsection[] sections = networks.get(networkID);
		if (sections != null) {
			for (final NITsection tsection : sections) {
				if (tsection != null) {
					TransportStream ts = tsection.getTransportStream(streamID);
					if (ts != null) {
						return ts.original_network_id();
					}
				}
			}
		}
		return -1;
	}

}

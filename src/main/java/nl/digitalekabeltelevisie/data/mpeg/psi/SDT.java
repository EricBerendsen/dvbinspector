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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyFunc;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.getServiceTypeString;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDTsection.Service;
import nl.digitalekabeltelevisie.util.ServiceIdentification;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class SDT extends AbstractPSITabel{

	public SDT(final PSI parentPSI) {
		super(parentPSI);

	}

	// map           <orgNetworkId,   TransportStreamId>
	private final Map<Integer,HashMap<Integer, SDTsection []>> networks = new HashMap<>();

	// used for easy lookup of service names for current TS
	private SDTsection [] actualTransportStreamSDT;

	public void update(final SDTsection section) {

		final int original_network_id = section.getOriginalNetworkID();
		final int streamId = section.getTransportStreamID();

		Map<Integer, SDTsection[]> networkSections = networks.computeIfAbsent(original_network_id, HashMap::new);
		SDTsection[] tsSections = networkSections.computeIfAbsent(streamId,
				k -> new SDTsection[section.getSectionLastNumber() + 1]);

		addSectionToArray(section, tsSections);

		if (section.getTableId() == 0x42) {
			actualTransportStreamSDT = tsSections;
		}
	}

	private static void addSectionToArray(final SDTsection section, SDTsection[] tsSections) {
		if(tsSections[section.getSectionNumber()]==null){
			tsSections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = tsSections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP sdtKvp = new KVP("SDT");
		if(!networks.isEmpty()) {
			sdtKvp.addTableSource(this::getTableForSdt, "SDT");
		}
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(sdtKvp);

		final TreeSet<Integer> networksTreeSet = new TreeSet<>(networks.keySet());
		for (Integer orgNetworkId : networksTreeSet) {
			final Map<Integer, SDTsection[]> networkSections = networks.get(orgNetworkId);

			KVP kvpOrgNetwork = new KVP("original_network_id", orgNetworkId, Utils.getOriginalNetworkIDString(orgNetworkId));
			kvpOrgNetwork.addTableSource(() -> getTableForOriginalNetwork(orgNetworkId), "SDT original_network_id: " + orgNetworkId);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(kvpOrgNetwork);
			t.add(n);

			final TreeSet<Integer> streamsTreeSet = new TreeSet<>(networkSections.keySet());
			for (Integer transport_stream_id : streamsTreeSet) {
				SDTsection[] sections = networkSections.get(transport_stream_id);

				KVP kvpTsId = new KVP("transport_stream_id", transport_stream_id, null);
				kvpTsId.addTableSource(() -> getTableForTransportStreamID(orgNetworkId, transport_stream_id),"SDT transport_stream_id: "+transport_stream_id);

				final DefaultMutableTreeNode m = new DefaultMutableTreeNode(kvpTsId);
				n.add(m);

				for (SDTsection section : sections) {
					if (section != null) {
						if (!Utils.simpleModus(modus)) {
							addSectionVersionsToJTree(m, section, modus);
						} else {
							addListJTree(m, section.getServiceList(), modus, "services");
						}
					}
				}
			}
		}
		return t;
	}



	public Optional<String> getServiceNameForActualTransportStreamOptional(final int serviceID){
		return  getServiceNameForActualTransportStreamDVBString(serviceID).map(DVBString::toString);

	}

	public String getServiceNameForActualTransportStream(final int serviceID){
		return  getServiceNameForActualTransportStreamOptional(serviceID).orElse(null);

	}

	public Optional<DVBString> getServiceNameForActualTransportStreamDVBString(int serviceID){
		return getServiceForActualTransportStream(serviceID)
				.map(SDTsection.Service::getDescriptorList)
				.orElseGet(ArrayList::new)
				.stream()
				.filter(ServiceDescriptor.class::isInstance)
				.map(ServiceDescriptor.class::cast)
				.findFirst()
				.map(ServiceDescriptor::getServiceName);
	}

	public String getServiceName(final int original_network_id, final int transport_stream_id, final int serviceID){
		return getServiceNameDVBString(original_network_id,transport_stream_id,serviceID).map(DVBString::toString).orElse(null);
	}

	public Optional<DVBString> getServiceNameDVBString(final ServiceIdentification serviceIdentification){
		return getServiceNameDVBString(serviceIdentification.originalNetworkId(),serviceIdentification.transportStreamId(),serviceIdentification.serviceId());
	}

	public Optional<DVBString> getServiceNameDVBString(final int original_network_id, final int transport_stream_id, final int serviceID){

		return getService(original_network_id,transport_stream_id,serviceID)
				.map(SDTsection.Service::getDescriptorList)
				.orElseGet(ArrayList::new)
				.stream()
				.filter(ServiceDescriptor.class::isInstance)
				.map(ServiceDescriptor.class::cast)
				.findFirst()
				.map(ServiceDescriptor::getServiceName);

	}

	public Optional<SDTsection.Service> getService(final int orgNetworkId, final int transportStreamID, final int serviceID){

		HashMap<Integer, SDTsection[]> transportStreams = networks.get(orgNetworkId);

		if(transportStreams !=null) {

			final SDTsection [] sections = transportStreams.get(transportStreamID);
			if(sections!=null){
				for (SDTsection section : sections) {
					if(section!= null)  {
						for(Service service: section.getServiceList()) {
							if(service.getServiceID() == serviceID) {
								return Optional.of(service);
							}
						}
					}
				}
			}
		}
		// no service found, give up
		return Optional.empty();
	}


	public int getOrgNetworkForActualTransportStream() {
		if(actualTransportStreamSDT !=null) {

			final SDTsection [] sections = actualTransportStreamSDT;
			for (SDTsection section : sections) {
				if(section!= null)  {
					return section.getOriginalNetworkID();
				}
			}
		}
		return -1;
		
	}
	public Optional<SDTsection.Service> getServiceForActualTransportStream(final int serviceID){


		if(actualTransportStreamSDT !=null) {

			final SDTsection [] sections = actualTransportStreamSDT;
			for (SDTsection section : sections) {
				if(section!= null)  {
					for(Service service: section.getServiceList()) {
						if(service.getServiceID() == serviceID) {
							return Optional.of(service);
						}
					}
				}
			}
		}
		// no service found, give up
		return Optional.empty();
	}

	public int getTransportStreamID(final int serviceID){


		final TreeSet<Integer> t = new TreeSet<>(networks.keySet());

		for (int orgNetworkId : t) {

			HashMap<Integer, SDTsection[]> transportStreams = networks.get(orgNetworkId);
			final TreeSet<Integer> s = new TreeSet<>(transportStreams.keySet());

			for (Integer transportStreamID : s) {
				final SDTsection[] sections = transportStreams.get(transportStreamID);
				if (sections != null) {
					for (SDTsection section : sections) {
						if (section != null) {
							for (Service service : section.getServiceList()) {
								if (serviceID == service.getServiceID()) {
									return transportStreamID;
								}
							}
						}
					}
				}
			}
		}
		// no service found, give up
		return -1;
	}

	static TableHeader<SDTsection,Service>  buildSdtTableHeader() {
		return new TableHeaderBuilder<SDTsection,Service>().
				addRequiredBaseColumn("onid", SDTsection::getOriginalNetworkID, Integer.class).
				addRequiredBaseColumn("tsid", SDTsection::getTransportStreamID, Integer.class).

				addOptionalRowColumn("sid",
						Service::getServiceID,
						Integer.class).
				addOptionalRowColumn("service_name",
						service -> findDescriptorApplyFunc(service.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceName().toString()),
						String.class).
				addOptionalRowColumn("service_type",
						service -> findDescriptorApplyFunc(service.getDescriptorList(),
								ServiceDescriptor.class,
								ServiceDescriptor::getServiceType),
						Integer.class).
				addOptionalRowColumn("service_type description",
						service -> findDescriptorApplyFunc(service.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> getServiceTypeString(sd.getServiceType())),
						String.class).
				addOptionalRowColumn("service_provider_name",
						service -> findDescriptorApplyFunc(service.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceProviderName().toString()),
						String.class).
				addOptionalRowColumn("EIT_schedule_flag",
						Service::getEitScheduleFlag,
						Integer.class).
				addOptionalRowColumn("EIT_present_following_flag",
						Service::getEitPresentFollowingFlag,
						Integer.class).
				addOptionalRowColumn("running_status",
						Service::getRunningStatus,
						Integer.class).
				addOptionalRowColumn("free_CA_mode",
						Service::getFreeCAmode,
						Integer.class).
				build();
	}

	private TableModel getTableForTransportStreamID(int orgNetworkId, int tsId) {
		FlexTableModel<SDTsection,Service> tableModel =  new FlexTableModel<>(SDT.buildSdtTableHeader());
		final SDTsection [] sections = networks.get(orgNetworkId).get(tsId);

		fillTableForTsSDT(tableModel, sections);

		tableModel.process();
		return tableModel;
	}

	private TableModel getTableForOriginalNetwork(int orgNetworkId) {
		FlexTableModel<SDTsection,Service> tableModel =  new FlexTableModel<>(SDT.buildSdtTableHeader());
		HashMap<Integer, SDTsection[]> networkSDT = networks.get(orgNetworkId);

		fillTableForNetworkSDT(tableModel, networkSDT);
		
		tableModel.process();
		return tableModel;
	}

	private TableModel getTableForSdt() {
		FlexTableModel<SDTsection, Service> tableModel = new FlexTableModel<>(SDT.buildSdtTableHeader());

		networks.values().forEach(s -> fillTableForNetworkSDT(tableModel,s));

		tableModel.process();
		return tableModel;
	}

	private static void fillTableForNetworkSDT(FlexTableModel<SDTsection, Service> tableModel,final HashMap<Integer, SDTsection[]> networkSDT) {
		networkSDT.values().forEach(s-> fillTableForTsSDT(tableModel,s));
	}

	private static void fillTableForTsSDT(FlexTableModel<SDTsection, Service> tableModel, final SDTsection[] tsSDT) {
		for (final SDTsection tsection : tsSDT) {
			if (tsection != null) {
				tableModel.addData(tsection, tsection.getServiceList());
			}
		}
	}

}

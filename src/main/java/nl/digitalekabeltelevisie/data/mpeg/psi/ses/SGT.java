/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.ses;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyFunc;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyListFunc;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses.BouquetListDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses.ServiceListNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.ses.VirtualServiceIDDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.data.mpeg.psi.ses.SGTsection.Service;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

/**
 * Astra Service Guide Table, based on ASTRA_LCN_v2_6.pdf
 */
public class SGT extends AbstractPSITabel {
	
	private final Map<Integer,HashMap<Integer, SGTsection []>> service_guides = new HashMap<>();

	public void update(SGTsection section) {
		
		int pid = section.getParentPID().getPid();
		
		HashMap<Integer, SGTsection []> l =  service_guides.computeIfAbsent(pid,HashMap::new);

		int key = section.getServiceListId();
		SGTsection[] sections = l.computeIfAbsent(key, k -> new SGTsection[section.getSectionLastNumber() + 1]);
		if (sections[section.getSectionNumber()] == null) {
			sections[section.getSectionNumber()] = section;
		} else {
			TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public SGT(PSI parentPSI) {
		super(parentPSI);
	}
	
	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = new KVP("SGT (Astra Service Guide Table)");
		for(Entry<Integer, HashMap<Integer, SGTsection[]>> guide:service_guides.entrySet()) {
			
			int pid = guide.getKey();
			KVP pidKvp = new KVP("pid",pid).addTableSource(() -> getTableForPid(pid), "SGT pid: "+pid);
			t.add(pidKvp);
		
			for(int service_list_id:new TreeSet<>(guide.getValue().keySet())) {
				KVP serviceListIdKvp = new KVP("service_list_id",service_list_id);
				serviceListIdKvp.addTableSource(() -> getTableForServiceListID(pid, service_list_id),"SGT service_list_id: "+service_list_id);

				for (SGTsection tsection : guide.getValue().get(service_list_id)) {
					if(tsection!= null){
						serviceListIdKvp.add(tsection.getJTreeNode(modus));
					}
				}
				pidKvp.add(serviceListIdKvp);
			}
		}
		return t;
	}
	
	static TableHeader<SGTsection,Service>  buildSgtTableHeader() {
		
		Function<SGTsection, List<Object>> findServiceListNames = component -> findGenericDescriptorsInList(
				component.getServiceListDescriptorsList(), 
				ServiceListNameDescriptor.class)
			.stream()
			.map(serviceListNameDescriptor -> serviceListNameDescriptor.getService_list_name().toString())
			.collect(Collectors.toList());
		
		Function<Service, Object> findServiceName = service -> findDescriptorApplyFunc(service.getDescriptorList(),
				ServiceDescriptor.class,
				sd -> sd.getServiceName().toString());
		
		Function<Service, Object> findServiceProviderName = service -> findDescriptorApplyFunc(service.getDescriptorList(),
				ServiceDescriptor.class,
				sd -> sd.getServiceProviderName().toString());
		
		Function<Service, List<Object>> findBouquetNames = component -> findDescriptorApplyListFunc(
				component.getDescriptorList(), 
				BouquetListDescriptor.class,
				bouquetListDescriptor -> new ArrayList<>(bouquetListDescriptor.getBouquet_names()
						.stream()
						.map(a->a.name().toString())
						.collect(Collectors.toList())
						)
				);
		
		Function<Service, Object> findServiceId = service -> findDescriptorApplyFunc(service.getDescriptorList(),
				VirtualServiceIDDescriptor.class,
                VirtualServiceIDDescriptor::getVirtual_service_id);
		return new TableHeaderBuilder<SGTsection,Service>()
				.addRequiredBaseColumn("service_list_id", SGTsection::getServiceListId, Integer.class)

				.addOptionalRepeatingBaseColumn("bouquet_name",
						findServiceListNames,
						String.class)
				.addOptionalRowColumn("service_id",
						Service::getService_id,
						Integer.class)
				.addOptionalRowColumn("transport_stream_id",
						Service::getTransport_stream_id,
						Integer.class)
				.addOptionalRowColumn("original_network_id",
						Service::getOriginal_network_id,
						Integer.class)
				.addOptionalRowColumn("logical_channel_number",
						Service::getLogical_channel_number,
						Integer.class)
				.addOptionalRowColumn("visible_service_flag",
						Service::getVisible_service_flag,
						Integer.class)
				.addOptionalRowColumn("new_service_flag",
						Service::getNew_service_flag,
						Integer.class)
				.addOptionalRowColumn("genre_code",
						Service::getGenre_code,
						Integer.class)
				.addOptionalRowColumn("service_name",
						findServiceName,
						String.class)
				.addOptionalRowColumn("provider_name",
						findServiceProviderName,
						String.class)

				.addOptionalRepeatingRowColumn("bouquet_name ",
						findBouquetNames,
						String.class)
				.addOptionalRowColumn("virtual_service_id",
						findServiceId,
						Integer.class)
				.build();
	}

	private TableModel getTableForServiceListID(int pid, int slid) {
		FlexTableModel<SGTsection,Service> tableModel =  new FlexTableModel<>(buildSgtTableHeader());
		SGTsection [] sections = service_guides.get(pid).get(slid);
		fillTableForServiceListId(tableModel, sections);

		tableModel.process();
		return tableModel;
	}


	private TableModel getTableForPid(int pid) {
		FlexTableModel<SGTsection,Service> tableModel =  new FlexTableModel<>(buildSgtTableHeader());
		HashMap<Integer, SGTsection[]> pidSGT = service_guides.get(pid);

		fillTableForPid(tableModel, pidSGT);
		
		tableModel.process();
		return tableModel;
	}
	

	private static void fillTableForPid(FlexTableModel<SGTsection, Service> tableModel, Map<Integer, SGTsection[]> networkSDT) {
		networkSDT.values().forEach(s-> fillTableForServiceListId(tableModel,s));
	}

	private static void fillTableForServiceListId(FlexTableModel<SGTsection, Service> tableModel, SGTsection[] tsSDT) {
		for (SGTsection tsection : tsSDT) {
			if (tsection != null) {
				tableModel.addData(tsection, tsection.getServiceList());
			}
		}
	}

}

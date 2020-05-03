package nl.digitalekabeltelevisie.data.mpeg.psi;
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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.*;

public class NIT extends AbstractPSITabel{

	private Map<Integer, NITsection []> networks = new HashMap<Integer, NITsection []>();


	public NIT(final PSI parent){
		super(parent);
	}

	public void update(final NITsection section){
		final int key = section.getNetworkID();
		NITsection [] sections= networks.get(key);

		if(sections==null){
			sections = new NITsection[section.getSectionLastNumber()+1];
			networks.put(key, sections);
		}
		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP kvpNit = new KVP("NIT");
		kvpNit.setTableSource(()->getTableModel());
		final DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode( kvpNit);
		final TreeSet<Integer> s = new TreeSet<Integer>(networks.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer networkNo=i.next();

			KVP kvp = new KVP("network_id",networkNo, getNetworkName(networkNo));
			kvp.setTableSource(()->getTableForNetworkID(networkNo));
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(kvp);

			final NITsection [] sections = networks.get(networkNo);
			for (final NITsection tsection : sections) {
				if(tsection!= null){
					if(!Utils.simpleModus(modus)){
						addSectionVersionsToJTree(n, tsection, modus);
					}else{
						addListJTree(n,tsection.getNetworkDescriptorList(),modus,"descriptors");
						addListJTree(n,tsection.getTransportStreamList(),modus,"transport streams");
					}
				}
			}
			treeNode.add(n);
		}
		return treeNode;
	}


	public String getNetworkName(final int networkNo){
		String r = null;
		final NITsection [] sections = networks.get(networkNo);
		if(sections!=null){
			for (final NITsection section : sections) {
				if(section!= null){
					final Iterator<Descriptor> descs=section.getNetworkDescriptorList().iterator();
					while(descs.hasNext()){
						final Descriptor d=descs.next();
						if(d instanceof NetworkNameDescriptor) {
							r = ((NetworkNameDescriptor)d).getNetworkNameAsString();
							return r;
						}
					}

				}
			}
		}
		return r;
	}


	public int getLCN(final int networkNo, final int streamID, final int serviceID){
		int r = -1;
		final NITsection [] sections = networks.get(networkNo);
		if(sections!=null){
			for (final NITsection section : sections) {
				if(section!= null){
					for(final NITsection.TransportStream stream :section.getTransportStreamList() ){
						if(stream.getTransportStreamID()==streamID) {
							final List<LogicalChannelDescriptor> logicalChannelDescriptorList = Descriptor.findGenericDescriptorsInList(stream.getDescriptorList(),LogicalChannelDescriptor.class);
							if((logicalChannelDescriptorList!=null)&&(logicalChannelDescriptorList.size()>0)) {
								final LogicalChannelDescriptor d = logicalChannelDescriptorList.get(0); // there should be only one..
								for ( final LogicalChannelDescriptor.LogicalChannel ch : d.getChannelList()){
									if(ch.getServiceID()==serviceID){
										r = ch.getLogicalChannelNumber();
										return r;
									}
								}
							}
						}
					}
				}
			}
		}
		return r;
	}

	public int getHDSimulcastLCN(final int networkNo, final int streamID,
			final int serviceID) {
		int r = -1;
		final NITsection[] sections = networks.get(networkNo);
		if (sections != null) {
			for (final NITsection section : sections) {
				if (section != null) {
					for (final NITsection.TransportStream stream : section
							.getTransportStreamList()) {
						if (stream.getTransportStreamID() == streamID) {
							final List<HDSimulcastLogicalChannelDescriptor> hdSimulcastDescriptorList = Descriptor
									.findGenericDescriptorsInList(
											stream.getDescriptorList(),
											HDSimulcastLogicalChannelDescriptor.class);
							if ((hdSimulcastDescriptorList != null)
									&& (hdSimulcastDescriptorList.size() > 0)) {
								final HDSimulcastLogicalChannelDescriptor d = hdSimulcastDescriptorList.get(0); // there should be only one..
								for (final HDSimulcastLogicalChannelDescriptor.LogicalChannel ch : d.getChannelList()) {
									if (ch.getServiceID() == serviceID) {
										r = ch.getLogicalChannelNumber();
										return r;
									}
								}
							}
						}
					}
				}
			}
		}
		return r;
	}

	public boolean exists(final int netWorkID, final int section){
		return ((networks.get(netWorkID)!=null) &&
				(networks.get(netWorkID).length >section) &&
				(networks.get(netWorkID)[section]!=null));
	}

	public Map<Integer, NITsection[]> getNetworks() {
		return networks;
	}

	public void setNetworks(final Map<Integer, NITsection[]> networks) {
		this.networks = networks;
	}

	public int getActualNetworkID(){

		final TreeSet<Integer> s = new TreeSet<Integer>(networks.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer networkNo=i.next();
			final NITsection [] sections = networks.get(networkNo);
			for (final NITsection tsection : sections) {
				if((tsection!=null)&&(tsection.getTableId()==0x40)){
					return networkNo;
				}
			}
		}
		return -1;
	}

	public List<Descriptor> getNetworkDescriptors(final int networkNo){
		final ArrayList<Descriptor> res = new ArrayList<Descriptor>();
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

	/**
	 * @param networkNo
	 * @param streamID
	 * @return
	 */
	public TransportStream getTransportStream(final int networkNo,final int streamID) {
		final NITsection [] sections = networks.get(networkNo);
		if(sections!=null){
			for (final NITsection tsection : sections) {
				final TransportStream ts= tsection.getTransportStream(streamID);
				if(ts!=null){
					return ts;
				}
			}
		}
		return null;
	}

	private TableModel getTableForNetworkID(int networkNo) {
		return TableUtils.getTableModel(NIT::buildNitTableHeader,()->getRowDataForNetworkId(networkNo));
	}

	List<Map<String, Object>> getRowDataForNetworkId(int networkNo) {
		List<Map<String, Object>> rowData = new ArrayList<Map<String,Object>>(); 

		final NITsection [] sections = networks.get(networkNo);
		
		for (final NITsection tsection : sections) {
			if(tsection!= null){
				rowData.addAll(tsection.getRowData());
			}
		}
		return rowData;
	}

	
	public TableModel getTableModel() {
		return TableUtils.getTableModel(NIT::buildNitTableHeader,()->getRowDataForNit());
	}

	List<Map<String, Object>> getRowDataForNit() {
		List<Map<String, Object>> rowData = new ArrayList<Map<String,Object>>(); 
		
		for(NITsection[] nitSections:networks.values()) {
			for (final NITsection tsection : nitSections) {
				if(tsection!= null){
					rowData.addAll(tsection.getRowData());
				}
			}
		}
		return rowData;
	}

	static TableHeader buildNitTableHeader() {
		TableHeader tableHeader =  new TableHeader.Builder().
				addOptionalColumn("network_id", "network_id", Integer.class).
				addOptionalColumn("transport_stream_id", "transport_stream_id", Integer.class).
				addOptionalColumn("original_network_id", "original_network_id", Integer.class).
				
				// Number.class is abused to force right align of String.  
				addOptionalColumn("terrestrial frequency", "terrestrial.frequency", Number.class).
				addOptionalColumn("terrestrial bandwidth", "terrestrial.bandwidth", Number.class).
				addOptionalColumn("terrestrial priority", "terrestrial.priority", String.class).
				addOptionalColumn("terrestrial time_slicing_indicator", "terrestrial.time_slicing_indicator", String.class).
				addOptionalColumn("terrestrial fec_inner", "terrestrial.fec_inner", String.class).
				
				addOptionalColumn("T2 plp_id", "t2.plp_id", Integer.class).
				addOptionalColumn("T2_system_id", "t2.t2_system_id", Integer.class).
	
				addOptionalColumn("T2 siso_miso", "t2.siso_miso", Number.class).
				addOptionalColumn("T2 bandwidth", "t2.bandwidth", Number.class).
				addOptionalColumn("T2 guard_interval", "t2.guard_interval", Number.class).
				addOptionalColumn("T2 transmission_mode", "t2.transmission_mode", Number.class).
				
				addOptionalColumn("cable frequency", "cable.frequency", Number.class).
				addOptionalColumn("cable fec_outter", "cable.fec_outter", String.class).
				addOptionalColumn("cable modulation", "cable.modulation", String.class).
				addOptionalColumn("cable symbol_rate", "cable.symbol_rate", Number.class).
				addOptionalColumn("cable fec_inner", "cable.fec_inner", Number.class).
	
				addOptionalColumn("satellite frequency", "satellite.frequency", Number.class).
				addOptionalColumn("satellite orbital_position", "satellite.orbital_position", Number.class).
				addOptionalColumn("satellite west_east_flag", "satellite.west_east_flag", String.class).
				addOptionalColumn("satellite polarization", "satellite.polarization", String.class).
				addOptionalColumn("satellite west_east_flag", "satellite.west_east_flag", String.class).
				addOptionalColumn("satellite modulation_system", "satellite.modulation_system", String.class).
				addOptionalColumn("satellite roll_off", "satellite.roll_off", Number.class).
				addOptionalColumn("satellite modulation_type", "satellite.modulation_type", String.class).
				addOptionalColumn("satellite symbol_rate", "satellite.symbol_rate", Number.class).
				addOptionalColumn("satellite fec_inner", "satellite.fec_inner", String.class).
				
				build();
		return tableHeader;
	}


}

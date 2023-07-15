/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2023 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getBouquetIDString;
import static nl.digitalekabeltelevisie.util.Utils.getOriginalNetworkIDString;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.BouquetNameDescriptor;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class BAT extends AbstractPSITabel{

	public static final String CANAL_INTERNATIONAL_CHANNEL_LIST_TABLE_LABEL = "Canal+ International Channel List";
	public static final String TRANSPORT_STREAMS_TABLE_LABEL = "Transport Streams";
	private final Map<Integer, BATsection []> networks = new HashMap<>();

	public BAT(final PSI parent){
		super(parent);
	}

	public void update(final BATsection section){

		final int key = section.getBouqetID();
		BATsection [] sections= networks.computeIfAbsent(key, k ->  new BATsection[section.getSectionLastNumber()+1]);
		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP kvpBat = new KVP("BAT");
		if(!networks.isEmpty()) {
			kvpBat.addTableSource(this::getTableModel,TRANSPORT_STREAMS_TABLE_LABEL);
			kvpBat.addTableSource(this::getCanalTableModel,CANAL_INTERNATIONAL_CHANNEL_LIST_TABLE_LABEL);
			
			
		}
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvpBat);
		for(int bouquetNo:new TreeSet<>(networks.keySet())) {

			KVP kvp = new KVP("bouquet_id",bouquetNo, Utils.getBouquetIDString(bouquetNo));
			if(hasTransportStreams(bouquetNo)) {
				kvp.addTableSource(()->getTableForBouqetID(bouquetNo),TRANSPORT_STREAMS_TABLE_LABEL);
				kvp.addTableSource(()->getCanalTableForBouqetID(bouquetNo),CANAL_INTERNATIONAL_CHANNEL_LIST_TABLE_LABEL);
			}
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(kvp);
			final BATsection [] sections = networks.get(bouquetNo);
			for (final BATsection tsection : sections) {
				if(tsection!= null){
					if(!Utils.simpleModus(modus)){ // show all details
						n.add(tsection.getJTreeNode(modus));
					}else{ // keep it simple
						final BATsection batSection = tsection;
						addListJTree(n,batSection.getNetworkDescriptorList(),modus,"network_descriptors");
						addListJTree(n,batSection.getTransportStreamList(),modus,"transport_stream_loop");
					}
				}
			}
			t.add(n);

		}
		return t;
	}
	
	
	private TableModel getCanalTableForBouqetID(int bouquetNo) {
		FlexTableModel<BATsection, BAT.CanalChannel> tableModel = new FlexTableModel<>(
				BAT.buildCanalIntBatTableHeader());
		final BATsection [] sections = networks.get(bouquetNo);


		for (final BATsection tsection : sections) {
			if(tsection!= null){
				tableModel.addData(tsection, tsection.getCanalChannelList());
			}
		}


		tableModel.process();
		return tableModel;
	}


	private TableModel getCanalTableModel() {
		FlexTableModel<BATsection, BAT.CanalChannel> tableModel = new FlexTableModel<>(
				BAT.buildCanalIntBatTableHeader());
		for(BATsection[] batSections:networks.values()) {
			for (final BATsection tsection : batSections) {
				if(tsection!= null){
					tableModel.addData(tsection, tsection.getCanalChannelList());
				}
			}
		}


		tableModel.process();
		return tableModel;
	}

	static TableHeader<BATsection,TransportStream> buildBatTableHeader() {

		return new TableHeaderBuilder<BATsection,TransportStream>().
				addRequiredBaseColumn("bouquet id",b->b.getBouqetID(), Integer.class).
				addRequiredBaseColumn("bouquet id name", b -> getBouquetIDString(b.getBouqetID()), String.class).
				
				addOptionalBaseColumn("bouquet name descriptor",
						bat -> findDescriptorApplyFunc(bat.getNetworkDescriptorList(), 
								BouquetNameDescriptor.class,  
								bnd -> bnd.getBouquetName().toString()), 
						String.class).

				addOptionalRowColumn("tsid",
						ts -> ts.transport_stream_id(),
						Integer.class).
				addOptionalRowColumn("onid",
						ts -> ts.original_network_id(),
						Integer.class).
				
				addOptionalRowColumn("original network name",
						ts ->  getOriginalNetworkIDString(ts.original_network_id()),
						String.class).
			
			build();
	}
	
	public record CanalChannel(int ts_id, int onid, int service_id, int logical_channel_number, String service_name) {}

	static TableHeader<BATsection,CanalChannel> buildCanalIntBatTableHeader() {

		return new TableHeaderBuilder<BATsection,CanalChannel>().
				addRequiredBaseColumn("bouquet id",b->b.getBouqetID(), Integer.class).
				addRequiredBaseColumn("bouquet id name", b -> getBouquetIDString(b.getBouqetID()), String.class).
				
				addOptionalBaseColumn("bouquet name descriptor",
						bat -> findDescriptorApplyFunc(bat.getNetworkDescriptorList(), 
								BouquetNameDescriptor.class,  
								bnd -> bnd.getBouquetName().toString()), 
						String.class).

				
				addOptionalRowColumn("tsid", c -> c.ts_id() , Integer.class).
				addOptionalRowColumn("onid", c -> c.onid() , Integer.class).
				addOptionalRowColumn("service_id", c -> c.service_id() , Integer.class).
				addOptionalRowColumn("logical_channel_number", c -> c.logical_channel_number(), Integer.class).
				addOptionalRowColumn("service name", c -> c.service_name(), String.class).
				build();
	}

	private boolean hasTransportStreams(int bouqetNo) {
		final BATsection [] sections = networks.get(bouqetNo);
		
		for (final BATsection tsection : sections) {
			if(tsection!= null && !tsection.getTransportStreamList().isEmpty()){
				return true;
			}
		}
		return false;
	}
	
	
	private TableModel getTableForBouqetID(int bouqetNo) {
		FlexTableModel<BATsection,TransportStream> tableModel =  new FlexTableModel<>(buildBatTableHeader());
		final BATsection [] sections = networks.get(bouqetNo);
		
		for (final BATsection tsection : sections) {
			if(tsection!= null){
				tableModel.addData(tsection, tsection.getTransportStreamList());
			}
		}

		tableModel.process();
		return tableModel;
	}

	public TableModel getTableModel() {
		FlexTableModel<BATsection,TransportStream> tableModel =  new FlexTableModel<>(buildBatTableHeader());
		
		for(BATsection[] batSections:networks.values()) {
			for (final BATsection tsection : batSections) {
				if(tsection!= null){
					tableModel.addData(tsection, tsection.getTransportStreamList());
				}
			}
		}

		tableModel.process();
		return tableModel;
	}
	
}



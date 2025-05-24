package nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findDescriptorApplyFunc;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatOrbitualPosition;
import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.formatSatelliteFrequency;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7LogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7LogicalChannelDescriptor.LogicalChannel;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan.FNTsection.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.psi.m7fastscan.FSTsection.Service;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class OperatorFastscan implements TreeNode{
	
			
	// TODO encoding for service name and provider  is always ISO-8859-9, ignoring character selection information as defined in Annex A of ETSI EN 300 468
	// Should be taken from ONT, depending on operator
	public static final Charset m7FastScanCharset = Charset.forName("ISO-8859-9");
	
	private static final Logger	logger	= Logger.getLogger(OperatorFastscan.class.getName());

	
	private final int pid;
	private int operatorNetworkId = -1;
	private final M7Fastscan m7Fastscan;
	
	private FNTsection[] fntSections;
	private FSTsection[] fstSections;

	public OperatorFastscan(int pid, M7Fastscan m7Fastscan) {
		this.pid = pid;
		this.m7Fastscan = m7Fastscan;
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP networkKVP = new KVP("Pid",pid,getOperatorSubListName());
		if(fntSections==null){
			networkKVP.addHTMLSource(() -> "FNT Missing", "FNT Missing");
		}else if(fstSections==null){
			networkKVP.addHTMLSource(() -> "FST Missing","FST Missing");
		}else {
			networkKVP.addTableSource(this::getTableModel, "Operator Fastscan");
		}
		
		if(fstSections!=null) {
			KVP fstKvp = new KVP("FST");
			fstKvp.addTableSource(this::getFstTableModel, "FST");
			for (FSTsection fstSection : fstSections) {
				if(fstSection!= null){
					AbstractPSITabel.addSectionVersionsToJTree(fstKvp, fstSection, modus);
				}
			}

			networkKVP.add(fstKvp);
		}

		if (fntSections != null) {
			KVP fntKvp = new KVP("FNT");
			fntKvp.addTableSource(this::getFntTableModel, "FNT");
			for (FNTsection fntSection : fntSections) {
				if (fntSection != null) {
					AbstractPSITabel.addSectionVersionsToJTree(fntKvp, fntSection, modus);
				}
			}
			networkKVP.add(fntKvp);
		}
		return networkKVP;
	}

	public String getOperatorSubListName() {
		return m7Fastscan.getOperatorSubListName(operatorNetworkId,pid);
	}

	public void update(FSTsection section) {
		if(operatorNetworkId==-1) {
			operatorNetworkId = section.getOperatorNetworkID();
		}else {
			if(operatorNetworkId != section.getOperatorNetworkID()) {
				logger.log(Level.WARNING, "update::FSTsection, ocurrent peratorNetworkId:"+ operatorNetworkId +" not equal to section.getOperatorNetworkID():"+section.getOperatorNetworkID());
			}
		}

		if(fstSections == null) {
			fstSections = new FSTsection[section.getSectionLastNumber()+1];
		}
		if(fstSections[section.getSectionNumber()]==null){
			fstSections[section.getSectionNumber()] = section;
		}else{
			TableSection last = fstSections[section.getSectionNumber()];
			AbstractPSITabel.updateSectionVersion(section, last);
		}

	}

	public void update(FNTsection section) {
		if(operatorNetworkId==-1) {
			operatorNetworkId = section.getOperatorNetworkID();
		}else {
			if(operatorNetworkId != section.getOperatorNetworkID()) {
				logger.log(Level.WARNING, "update::FNTsection, ocurrent peratorNetworkId:"+ operatorNetworkId +" not equal to section.getOperatorNetworkID():"+section.getOperatorNetworkID());
			}
		}
		if(fntSections == null) {
			fntSections = new FNTsection[section.getSectionLastNumber()+1];
		}
		if(fntSections[section.getSectionNumber()]==null){
			fntSections[section.getSectionNumber()] = section;
		}else{
			TableSection last = fntSections[section.getSectionNumber()];
			AbstractPSITabel.updateSectionVersion(section, last);
		}
		
	}


	public TableModel getFntTableModel() {
		FlexTableModel<FNTsection,TransportStream> tableModel =  new FlexTableModel<>(FNTsection.buildFntTableHeader());

		for (FNTsection fntSection : fntSections) {
			if(fntSection!= null){
				tableModel.addData(fntSection, fntSection.getTransportStreamList());
			}
		}

		tableModel.process();
		return tableModel;
	}


	public TableModel getFstTableModel() {
		FlexTableModel<FSTsection,Service> tableModel =  new FlexTableModel<>(FSTsection.buildFstTableHeader());

		for (FSTsection fstSection : fstSections) {
			if(fstSection!= null){
				tableModel.addData(fstSection, fstSection.getServiceList());
			}
		}

		tableModel.process();
		return tableModel;
	}

	private TableHeader<TransportStream,Service>  buildFastscanTableHeader() {
		return new TableHeaderBuilder<TransportStream,Service>().
				addOptionalRowColumn("lcn",
						this::getLcn,
				Integer.class).
				addRequiredRowColumn("onid", Service::original_network_id, Integer.class).
				addRequiredRowColumn("tsid", Service::transport_stream_id, Integer.class).
				addRequiredRowColumn("sid", Service::service_id, Integer.class).
				addOptionalRowColumn("service name",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceName().toString(m7FastScanCharset)),
						String.class).
				addOptionalRowColumn("service type",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								ServiceDescriptor.class,
								sd -> Descriptor.getServiceTypeString(sd.getServiceType())),
						String.class).
				addOptionalRowColumn("service provider",
						operator -> findDescriptorApplyFunc(operator.descriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceProviderName().toString(m7FastScanCharset)),
						String.class).

				addOptionalRowColumn("position",
						this::getOrbitualPosition,
						Number.class).

				addOptionalRowColumn("w/e",
						this::getWestEastFlag,
						String.class).

				addOptionalRowColumn("frequency",
						this::getFrequency,
						Number.class).

				build();
	}


	private String getOrbitualPosition(Service service) {
		SatelliteDeliverySystemDescriptor sdd = getSatelliteDeliverySystemDescriptor(service);
		if(sdd!=null) {
			return formatOrbitualPosition(sdd.getOrbitalPosition());
		}
		return null;
	}
	
	private String getWestEastFlag(Service service) {
		SatelliteDeliverySystemDescriptor sdd = getSatelliteDeliverySystemDescriptor(service);
		if(sdd!=null) {
			return sdd.getWestEastFlagString();
		}
		return null;
	}

	private String getFrequency(Service service) {
		SatelliteDeliverySystemDescriptor sdd = getSatelliteDeliverySystemDescriptor(service);
		if(sdd!=null) {
			return formatSatelliteFrequency(sdd.getFrequency());
		}
		return null;
	}

	private SatelliteDeliverySystemDescriptor getSatelliteDeliverySystemDescriptor(Service service) {
		TransportStream ts = getTransportStreamFromFnt(service);
		if(ts!=null) {
			List<SatelliteDeliverySystemDescriptor>lcnDescs = Descriptor.findGenericDescriptorsInList(ts.descriptorList(),SatelliteDeliverySystemDescriptor.class);
			if(!lcnDescs.isEmpty()) {
				return lcnDescs.getFirst();
			}
		}
		return null;
	}

	private Integer getLcn(Service service) {

		TransportStream ts = getTransportStreamFromFnt(service);
		if(ts!=null) {
			List<M7LogicalChannelDescriptor>lcnDescs = Descriptor.findGenericDescriptorsInList(ts.descriptorList(),M7LogicalChannelDescriptor.class);
			if(!lcnDescs.isEmpty()) {
				M7LogicalChannelDescriptor desc = lcnDescs.getFirst();
				for(LogicalChannel channel: desc.getChannelList()) {
					if(channel.getServiceID() == service.service_id()) {
						return channel.getLogicalChannelNumber();
					}
				}
			}
		}
		return null;
	}
	
	private TransportStream getTransportStreamFromFnt(Service service) {
		for( FNTsection fntSection: fntSections){
			if(fntSection!=null) {
				for(TransportStream ts : fntSection.getTransportStreamList()) {
					if(ts.originalNetworkID() == service.original_network_id() &&
						ts.transportStreamID() == service.transport_stream_id()) {
						return ts;
					}
				}
			}
		}

		return null;
	}

	public TableModel getTableModel() {

		FlexTableModel<TransportStream,Service> tableModel =  new FlexTableModel<>(buildFastscanTableHeader());

		for (FSTsection fstSection : fstSections) {
			if(fstSection!= null){
				tableModel.addData(null, fstSection.getServiceList());
			}
		}

		tableModel.process();
		return tableModel;
	}

	public static Charset getM7FastScanCharset() {
		return m7FastScanCharset;
	}

	public int getPid() {
		return pid;
	}

	public int getOperatorNetworkId() {
		return operatorNetworkId;
	}

	public FNTsection[] getFntSections() {
		return fntSections;
	}

	public FSTsection[] getFstSections() {
		return fstSections;
	}
		
}

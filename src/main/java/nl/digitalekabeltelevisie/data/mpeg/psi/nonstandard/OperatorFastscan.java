package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;

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
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.SatelliteDeliverySystemDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7LogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.m7fastscan.M7LogicalChannelDescriptor.LogicalChannel;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.FNTsection.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard.FSTsection.Service;
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
	
	FNTsection[] fntSections;
	FSTsection[] fstSections;

	public OperatorFastscan(int pid, M7Fastscan m7Fastscan) {
		this.pid = pid;
		this.m7Fastscan = m7Fastscan;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final KVP networkKVP = new KVP("Pid",pid,getOperatorSubListName());
		if(fntSections==null){
			networkKVP.setHtmlSource(() -> "FNT Missing");
		}else if(fstSections==null){
			networkKVP.setHtmlSource(() -> "FST Missing");
		}else {
			networkKVP.setTableSource(this::getTableModel);
		}
		
		final DefaultMutableTreeNode n = new DefaultMutableTreeNode(networkKVP);
		
		if(fstSections!=null) {
			KVP fstKvp = new KVP("FST");
			DefaultMutableTreeNode fst = new DefaultMutableTreeNode(fstKvp);
			fstKvp.setTableSource(this::getFstTableModel);
			for (final FSTsection fstSection : fstSections) {
				if(fstSection!= null){
					AbstractPSITabel.addSectionVersionsToJTree(fst, fstSection, modus);
				}
			}
			
			n.add(fst);
		}

		if (fntSections != null) {
			KVP fntKvp = new KVP("FNT");
			DefaultMutableTreeNode fnt = new DefaultMutableTreeNode(fntKvp);
			fntKvp.setTableSource(this::getFntTableModel);
			for (final FNTsection fntSection : fntSections) {
				if (fntSection != null) {
					AbstractPSITabel.addSectionVersionsToJTree(fnt, fntSection, modus);
				}
			}

			n.add(fnt);
		}
		return n;
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
			final TableSection last = fstSections[section.getSectionNumber()];
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
			final TableSection last = fntSections[section.getSectionNumber()];
			AbstractPSITabel.updateSectionVersion(section, last);
		}
		
	}


	public TableModel getFntTableModel() {
		FlexTableModel<FNTsection,TransportStream> tableModel =  new FlexTableModel<>(FNTsection.buildFntTableHeader());

		for (final FNTsection fntSection : fntSections) {
			if(fntSection!= null){
				tableModel.addData(fntSection, fntSection.getTransportStreamList());
			}
		}

		tableModel.process();
		return tableModel;
	}


	public TableModel getFstTableModel() {
		FlexTableModel<FSTsection,Service> tableModel =  new FlexTableModel<>(FSTsection.buildFstTableHeader());

		for (final FSTsection fstSection : fstSections) {
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
				addRequiredRowColumn("onid", Service::getOriginalNetworkID, Integer.class).
				addRequiredRowColumn("tsid", Service::getTransportStreamID, Integer.class).
				addRequiredRowColumn("sid", Service::getService_id, Integer.class).
				addOptionalRowColumn("service name",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> sd.getServiceName().toString(m7FastScanCharset)),
						String.class).
				addOptionalRowColumn("service type",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
								ServiceDescriptor.class,
								sd -> Descriptor.getServiceTypeString(sd.getServiceType())),
						String.class).
				addOptionalRowColumn("service provider",
						operator -> findDescriptorApplyFunc(operator.getDescriptorList(),
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


	private String getOrbitualPosition(Service s) {
		SatelliteDeliverySystemDescriptor sdd = getSatelliteDeliverySystemDescriptor(s);
		if(sdd!=null) {
			return formatOrbitualPosition(sdd.getOrbitalPosition());
		}
		return null;
	}
	
	private String getWestEastFlag(Service s) {
		SatelliteDeliverySystemDescriptor sdd = getSatelliteDeliverySystemDescriptor(s);
		if(sdd!=null) {
			return sdd.getWestEastFlagString();
		}
		return null;
	}

	private String getFrequency(Service s) {
		SatelliteDeliverySystemDescriptor sdd = getSatelliteDeliverySystemDescriptor(s);
		if(sdd!=null) {
			return formatSatelliteFrequency(sdd.getFrequency());
		}
		return null;
	}

	private SatelliteDeliverySystemDescriptor getSatelliteDeliverySystemDescriptor(Service s) {
		TransportStream ts = getTransportStreamFromFnt(s);
		if(ts!=null) {
			List<SatelliteDeliverySystemDescriptor>lcnDescs = Descriptor.findGenericDescriptorsInList(ts.getDescriptorList(),SatelliteDeliverySystemDescriptor.class);
			if(!lcnDescs.isEmpty()) {
				return lcnDescs.get(0);
			}
		}
		return null;
	}

	private Integer getLcn(Service s) {

		TransportStream ts = getTransportStreamFromFnt(s);
		if(ts!=null) {
			List<M7LogicalChannelDescriptor>lcnDescs = Descriptor.findGenericDescriptorsInList(ts.getDescriptorList(),M7LogicalChannelDescriptor.class);
			if(!lcnDescs.isEmpty()) {
				M7LogicalChannelDescriptor desc = lcnDescs.get(0);
				for(LogicalChannel channel: desc.getChannelList()) {
					if(channel.getServiceID() == s.getService_id()) {
						return channel.getLogicalChannelNumber();
					}
				}
			}
		}
		return null;
	}
	
	private TransportStream getTransportStreamFromFnt(Service s) {
		for( FNTsection f: fntSections){
			if(f!=null) {
				for(TransportStream ts : f.getTransportStreamList()) {
					if(ts.getOriginalNetworkID() == s.getOriginalNetworkID() &&
						ts.getTransportStreamID() == s.getTransport_stream_id()) {
						return ts;
					}
				}
			}
		}

		return null;
	}

	public TableModel getTableModel() {

		FlexTableModel<TransportStream,Service> tableModel =  new FlexTableModel<>(buildFastscanTableHeader());

		for (final FSTsection fstSection : fstSections) {
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

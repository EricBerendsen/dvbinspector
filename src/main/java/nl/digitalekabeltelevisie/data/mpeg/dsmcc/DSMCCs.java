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
 * based on ETSI ES 201 812 V1.1.1, ETSI TS 102 809 V1.1.1, ETSI TS 102 796 V1.1.1
 */

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import java.util.*;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.AssociationTagDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.CarouselIdentifierDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DataBroadcastIDDescriptor;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.StreamIdentifierDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTs;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.PreferencesManager;

public class DSMCCs extends AbstractPSITabel{
	
	private enum DataType {UNKNOW, SSU, OBJECT_CAROUSEL, OTHER}

	private static final Logger logger = Logger.getLogger(DSMCCs.class.getName());

	private final PMTs pmts;
	public DSMCCs(final PSI parentPSI) {
		super(parentPSI);
		pmts = parentPSI.getPmts();
	}

	/**
	 * map (pid, carousel) just the data per pid
	 */
	private final Map<Integer, DSMCC> dsmccs = new TreeMap<>();

	/**
	 * map ( serviceID  all the data for a carousel for a service
	 *
	 */

	private final Map<Integer, ServiceDSMCC> objectCarousels = new TreeMap<>();

	/**
	 *
	 * put this section in every DSMCC (carousel) that uses it.
	 * iterate over all PMTs to find them
	 * @param section
	 */
	public void update(final TableSectionExtendedSyntax section){

		final int pid = section.getParentPID().getPid();

		DataType dataType = determineDataTypeFromPSI(pid);

		if(dataType.equals(DataType.UNKNOW)){
			logger.warning("Not supported type; pid:"+pid);
			return;
		}

		// just store on PID, referenced or not, both SSU and objectCarousel
		DSMCC dsmcc= dsmccs.computeIfAbsent(pid, k ->  new DSMCC(parentPSI,dataType.equals(DataType.OBJECT_CAROUSEL)));
		// TODO does not work for SSU
		dsmcc.update(section);

		if(dataType.equals(DataType.OBJECT_CAROUSEL)){
			// object Carousels only, now find the carousels for which it is used,
			for(final PMTsection[] pmtTable:pmts){
				final PMTsection pmt=pmtTable[0];
				// get carouselId from main loop
				// should be there according to ETSI TR 101 202 V1.2.1 P.29
				// in real life never found...
				final List<Component> componentsList = pmt.getComponentenList();

				for(final Component component:componentsList){
					if(component.getElementaryPID()==pid){
						int programNumber = pmt.getProgramNumber();
						ServiceDSMCC carousel = objectCarousels.computeIfAbsent(programNumber, k ->  new ServiceDSMCC(programNumber));
						int associationTag = -1;
						// now find assoctiation_tag
						List<Descriptor> componentDescriptorList = component.getComponentDescriptorList();
						final List<AssociationTagDescriptor> associationDescriptorsList = Descriptor.findGenericDescriptorsInList(componentDescriptorList, AssociationTagDescriptor.class); //AssociationTagDescriptor
						if(!associationDescriptorsList.isEmpty()){
							associationTag = associationDescriptorsList.get(0).getAssociationTag();
						}else{ // fall back to Descriptor: stream_identifier_descriptor: 0x52 (82)
							final List<StreamIdentifierDescriptor> streamidentifierDescriptorsList = Descriptor.findGenericDescriptorsInList(componentDescriptorList, StreamIdentifierDescriptor.class); //AssociationTagDescriptor
							if(!streamidentifierDescriptorsList.isEmpty()){
								associationTag = streamidentifierDescriptorsList.get(0).getComponentTag();
							}
						}
						if(associationTag>=0){
							carousel.addDSMCC(associationTag, dsmcc);
							//find if this is a boot-PID
							final List<DataBroadcastIDDescriptor> dataBroadcastIdDescriptorsList = Descriptor.findGenericDescriptorsInList(componentDescriptorList,DataBroadcastIDDescriptor.class); //AssociationTagDescriptor
							if(!dataBroadcastIdDescriptorsList.isEmpty()){
								// get the type
								final int dataBroadCastId = dataBroadcastIdDescriptorsList.get(0).getDataBroadcastId();
								carousel.addBootPID(associationTag,dataBroadCastId);
								// if there is a carousel_identifier_descriptor for this PID, store it. We may need it for bootstrapping if there is no DSI
								final List<CarouselIdentifierDescriptor> carouselIdentifierDescriptorsList = Descriptor.findGenericDescriptorsInList(componentDescriptorList,CarouselIdentifierDescriptor.class); //CarouselIdentifierDescriptor
								if(!carouselIdentifierDescriptorsList.isEmpty()){
									final CarouselIdentifierDescriptor carouselIdentifierDescriptor =  carouselIdentifierDescriptorsList.get(0); // A single PID shall only contain messages from a single object carousel and so only one carousel_identifier_descriptor shall be present in any second descriptor loop.  B.2.8 Mounting an object carousel DVB Document A137
									carousel.setCarouselIdentifierDescriptor(associationTag,carouselIdentifierDescriptor);
								}
							}
						}// no association tag, then it can not be used. do not include

					}
				}
			}
		}
	}

	private DataType determineDataTypeFromPSI(final int pid) {
		//check if this PID is referenced in a PMT which contains a broadcastID descriptor for one of its componenets that has a caroussel.
		// does not mean this PID has to be the component with the broadcastIDdescriptor

	
		for(final PMTsection[] pmtTable:pmts){
			final PMTsection pmt=pmtTable[0];
			// get carouselId from main loop
			// should be there according to ETSI TR 101 202 V1.2.1 P.29
			if(pmt.hasComponentWithPid(pid)){
				// this pid belongs to this service, now iterate over all components again to see if any of them has a valid DataBroadcastIDDescriptor
				final List<Component> comps = pmt.getComponentenList();
				for(final Component c1:comps){
					final List<DataBroadcastIDDescriptor> dataBroadcastIdDescriptorsList = Descriptor.findGenericDescriptorsInList(c1.getComponentDescriptorList(), DataBroadcastIDDescriptor.class); //AssociationTagDescriptor
					if(!dataBroadcastIdDescriptorsList.isEmpty()){
						// get the type, like HbbTV, MHP, etc..
						final DataBroadcastIDDescriptor dataBroadcastIDDescriptor= dataBroadcastIdDescriptorsList.get(0);
						if(dataBroadcastIDDescriptor.describesObjectCarousel()){
							return DataType.OBJECT_CAROUSEL;
						}
						if(dataBroadcastIDDescriptor.describesSSU()){
							return DataType.SSU;
						}
					}
				}
			}
		}
		return DataType.UNKNOW;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		
		if(!PreferencesManager.isEnableDSMCC()) {
			return new DefaultMutableTreeNode(new KVP("DSM-CCs (not enabled, select 'Settings -> Enable DSM-CC' to enable )"));
		}
		
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DSM-CCs"));
		
		dsmccs
			.values()
			.parallelStream()
			.map(k -> k.getJTreeNode(modus))
			.forEachOrdered(t::add);

		objectCarousels
			.values()
			.parallelStream()
			.map(k -> k.getJTreeNode(modus))
			.forEachOrdered(t::add);

		return t;
	}




}

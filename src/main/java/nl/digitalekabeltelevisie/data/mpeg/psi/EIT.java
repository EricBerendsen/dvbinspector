package nl.digitalekabeltelevisie.data.mpeg.psi;
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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.*;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.ArrayUtils;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.gui.EITableImage;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.*;


/**
 *
 * Based on 5.2.4 Event Information Table (EIT) of ETSI EN 300 468 V1.11.1 (2010-04)
 *
 * @author Eric
 *
 */
public class EIT extends AbstractPSITabel{

	//     original_network_id,transport_stream_id,  serviceId, table_id 
	private final Map<Integer, TreeMap<Integer, TreeMap<Integer, TreeMap<Integer,EITsection []>>>> newEit = 
			new TreeMap<>();
	
	private static final Logger	logger	= Logger.getLogger(EIT.class.getName());


	public EIT(final PSI parent){
		super(parent);
	}

	public void update(final EITsection section){

		final int original_network_id = section.getOriginalNetworkID();
		final int streamId = section.getTransportStreamID();
		final int serviceId = section.getServiceID();
		final int tableId = section.getTableId();

		TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> networkSections = newEit.computeIfAbsent(original_network_id,k -> new TreeMap<>());
		TreeMap<Integer, TreeMap<Integer, EITsection[]>> programStreamSections = networkSections.computeIfAbsent(streamId,k -> new TreeMap<>());
		TreeMap<Integer, EITsection[]> serviceSections = programStreamSections.computeIfAbsent(serviceId,k -> new TreeMap<>());
		EITsection[] tableSectionArray = serviceSections.computeIfAbsent(tableId,k -> new EITsection[section.getSectionLastNumber()+1]);
		
		if(tableSectionArray.length<=section.getSectionNumber()){ //resize if needed
			tableSectionArray = Arrays.copyOf(tableSectionArray, section.getSectionNumber()+1);
			serviceSections.put(tableId,tableSectionArray);
		}
		
		if(tableSectionArray[section.getSectionNumber()]==null){
			tableSectionArray[section.getSectionNumber()] = section;
		}else{
			final TableSection last = tableSectionArray[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		// need this KVP at end of loop to set ImageSource
		final KVP eitKVP = new KVP("EIT");
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(eitKVP);
		
		Map<ServiceIdentification, EITsection[]> allEitImageMap = new TreeMap<>();
		
		for(Entry<Integer, TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>>> network:newEit.entrySet()) {
			final Integer orgNetworkId= network.getKey();
			TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> networkSections = network.getValue();
			
			// need this KVP at end of loop to set ImageSource
			final KVP networkNodeKVP = new KVP("original_network_id", orgNetworkId,Utils.getOriginalNetworkIDString(orgNetworkId));
			final DefaultMutableTreeNode networkNode = new DefaultMutableTreeNode(networkNodeKVP);
			t.add(networkNode);
			
			Map<ServiceIdentification, EITsection[]> networkImageMap = new TreeMap<>();

			for(Entry<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> netWorkSection:networkSections.entrySet()) {
				final Integer transport_stream_id = netWorkSection.getKey();
				TreeMap<Integer, TreeMap<Integer, EITsection[]>> streams = netWorkSection.getValue();

				// need this KVP at end of loop to set ImageSource
				final KVP streamNodeKVP = new KVP("transport_stream_id", transport_stream_id,null);
				final DefaultMutableTreeNode streamNode = new DefaultMutableTreeNode(streamNodeKVP);
				networkNode.add(streamNode);

				Map<ServiceIdentification, EITsection[]> streamImageMap = new TreeMap<>();

				for(Entry<Integer, TreeMap<Integer, EITsection[]>> streamEntry: streams.entrySet()) {
					final Integer serviceId = streamEntry.getKey();
					TreeMap<Integer, EITsection[]> service = streamEntry.getValue();
					
					// for EITImage, sections of this service with tableID >80
					EITsection[] serviceSections = new EITsection[0];
				
					final KVP serviceNodeKVP = new KVP("service_id", serviceId,getParentPSI().getSdt().getServiceName(orgNetworkId, transport_stream_id, serviceId));
					final DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(serviceNodeKVP);
										
					serviceNodeKVP.setHtmlSource(() -> service.entrySet().
							stream().
							filter(s -> s.getKey()>=80).
							map(Entry::getValue).
							flatMap(Arrays::stream).
							filter(Objects::nonNull).
							map(HTMLSource::getHTML).
							collect(Collectors.joining("","<b>Schedule</b><br><br>","")));
					
					streamNode.add(serviceNode);
				
					for(Entry<Integer, EITsection[]> serviceEntry : service.entrySet()) {
						Integer tableId = serviceEntry.getKey();
						EITsection[] sections = serviceEntry.getValue();

						final KVP tableNodeKVP = new KVP("tableid", tableId,TableSection.getTableType(tableId));
						tableNodeKVP.setHtmlSource(() -> Arrays.stream(sections).
								filter(Objects::nonNull).
								map(HTMLSource::getHTML).
								collect(Collectors.joining())
								);

						final DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableNodeKVP);
						serviceNode.add(tableNode);

						if(tableId>=80) {
							serviceSections = appendSections(serviceSections, sections);
						}
						

						for (EITsection section : sections) {
							if(section!= null){
								if(!simpleModus(modus)){
									addSectionVersionsToJTree(tableNode, section, modus);
								}else{
									addListJTree(tableNode,section.getEventList(),modus,"events");
								}
							}
						}
					}
					// now all sections for service are in serviceSections
					streamImageMap.put(new ServiceIdentification(orgNetworkId, transport_stream_id, serviceId), serviceSections);
				}
				streamNodeKVP.setImageSource(new EITableImage(this, streamImageMap));
				networkImageMap.putAll(streamImageMap);
			}
			networkNodeKVP.setImageSource(new EITableImage(this, networkImageMap));
			allEitImageMap.putAll(networkImageMap);
		}
		eitKVP.setImageSource(new EITableImage(this, allEitImageMap));
		return t;

	}

	/**
	 * @param serviceSections
	 * @param sections
	 * @return
	 */
	private static EITsection[] appendSections(EITsection[] serviceSections, EITsection[] sections) {
		EITsection[] tmpArray = new EITsection[serviceSections.length + sections.length];
		System.arraycopy(serviceSections, 0, tmpArray, 0, serviceSections.length);
		System.arraycopy(sections, 0, tmpArray, serviceSections.length, sections.length);
		return tmpArray;
	}
	

	/**
	 * Returns the start time of the first, and the end time of the last event of the services in this EIT table.
	 * Only the services contained in serviceSet are used in the calculation
	 *
	 * @param serviceSet service ID of services to be included in calculation
	 * @param eitTable map of service IDs to EITSection[] Can contain sections from different Table IDs, like 0x50 and 0x51, etc... (for very long EPGs)
	 * @return Interval that covers all events in eitTable
	 */
	public static Interval getSpanningInterval(final Set<ServiceIdentification> serviceSet, Map<ServiceIdentification, EITsection[]> eitTable) {
		Date startDate = null;
		Date endDate = null;
		// services to be displayed

		for(final ServiceIdentification serviceNo : serviceSet){
			for(final EITsection section :eitTable.get(serviceNo)){
				if(section!= null){
					List<Event> eventList = section.getEventList();
					for(Event event:eventList){
						final byte[] startTime = event.getStartTime();
						if(isUndefined(startTime)){ // undefined start time
							continue;
						}
						Date eventStart = getUTCDate( startTime);
						if((startDate==null)||(startDate.after(eventStart))){
							startDate = eventStart;
						}
						if(eventStart!=null){
							try{
								Date eventEnd = new Date(eventStart.getTime()+ getDurationMillis(event.getDuration()));
								if((endDate==null)||(endDate.before(eventEnd))){
									endDate = eventEnd;
								}
							}catch(NumberFormatException nfe){
								logger.log(Level.WARNING, "getSpanningInterval: Event.duration is not a valid BCD number;", nfe);
							}
						}
					}
				}
			}
		}
		if((startDate!=null)&&(endDate!=null)){
			return new Interval(startDate,endDate);
		}
		return null;
	}

	public Map<ServiceIdentification, EITsection[]> getCombinedSchedule() {
		return getFlatEit(tableId -> tableId >= 80);
	}

	public Map<ServiceIdentification, EITsection[]> getCombinedPresentFollowing() {
		return getFlatEit(tableId -> tableId < 80);
	}

	public Map<ServiceIdentification, EITsection[]> getFlatEit(Predicate<Integer> scheduleOrPF) {
		Map<ServiceIdentification, EITsection[]> result = new TreeMap<>();

		for (Entry<Integer, TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>>> networkEntry : newEit.entrySet()) {
			int orgNetworkId = networkEntry.getKey();
			TreeMap<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> network = networkEntry.getValue();

			for (Entry<Integer, TreeMap<Integer, TreeMap<Integer, EITsection[]>>> streamEntry : network.entrySet()) {
				int streamId = streamEntry.getKey();
				TreeMap<Integer, TreeMap<Integer, EITsection[]>> stream = streamEntry.getValue();

				for (Entry<Integer, TreeMap<Integer, EITsection[]>> serviceEntry : stream.entrySet()) {
					int serviceId = serviceEntry.getKey();
					TreeMap<Integer, EITsection[]> service = serviceEntry.getValue();

					for (Entry<Integer, EITsection[]> tableEntry : service.entrySet()) {
						int tableId = tableEntry.getKey();
						if (scheduleOrPF.test(tableId)) {
							result.merge(new ServiceIdentification(orgNetworkId, streamId, serviceId),
									tableEntry.getValue(),
									ArrayUtils::addAll);
						}
					}
				}
			}
		}
		return result;
	}

}

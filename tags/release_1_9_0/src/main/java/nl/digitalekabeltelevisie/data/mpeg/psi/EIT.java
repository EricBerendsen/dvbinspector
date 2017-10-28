package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.gui.EITableImage;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.Interval;


/**
 *
 * Based on 5.2.4 Event Information Table (EIT) of ETSI EN 300 468 V1.11.1 (2010-04)
 *
 * @author Eric
 *
 */
public class EIT extends AbstractPSITabel{

	private final Map<Integer, HashMap<Integer,EITsection []>> eit = new HashMap<Integer, HashMap<Integer, EITsection []>>();
	private static final Logger	logger	= Logger.getLogger(EIT.class.getName());


	/**
	 * Helper to implement a HTMLSource for the program information for a single service (channel)
	 * in a single tableID (now/next actual stream, now/next other stream, schedule current stream,
	 * schedule other stream).
	 *
	 * @author Eric
	 *
	 */

	 // TODO as this is pure presentation logic, move to gui package ??

	public class ServiceListing implements HTMLSource {
		private final int tableID;
		private final int serviceNo;

		public ServiceListing(final int tableID, final int serviceNo){
			this.tableID = tableID;
			this.serviceNo=serviceNo;
		}

		public String getHTML() {
			final StringBuilder b = new StringBuilder();
			b.append("<code>");

			for(final EITsection section :eit.get(tableID).get(serviceNo)){
				if(section!=null){
					b.append(section.getHTMLLines());
				}
			}

			b.append("</code>");
			return b.toString();
		}
	}


	public EIT(final PSI parent){
		super(parent);
	}

	public void update(final EITsection section){

		final int tableId = section.getTableId();
		HashMap<Integer, EITsection []>  table= eit.get(tableId);

		if(table==null){
			table = new HashMap<Integer, EITsection []>();
			eit.put(tableId, table);
		}

		EITsection [] serviceSection = table.get(section.getServiceID());
		if(serviceSection==null){
			serviceSection = new EITsection [section.getSectionLastNumber()+1];
			table.put(section.getServiceID(),serviceSection);
		}
		
		if(serviceSection.length<=section.getSectionNumber()){ //resize if needed
			serviceSection = Arrays.copyOf(serviceSection, section.getSectionNumber()+1);
			table.put(section.getServiceID(),serviceSection);
		}
		if(serviceSection[section.getSectionNumber()]==null){
			serviceSection[section.getSectionNumber()] = section;
		}else{
			final TableSection last = serviceSection[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final TreeSet<Integer> tableSet = new TreeSet<Integer>(eit.keySet());

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("EIT"));

		for(final Integer tableID : tableSet ){
			final KVP tableKVP = new KVP("table_id",tableID, TableSection.getTableType(tableID));
			tableKVP.setImageSource(new EITableImage(this, eit.get(tableID)));
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(tableKVP);
			final HashMap<Integer, EITsection []> table= eit.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				final KVP kvp = new KVP("service_id",serviceNo, getParentPSI().getSdt().getServiceName(serviceNo));
				kvp.setHtmlSource(new ServiceListing(tableID, serviceNo)); // set listing for whole service

				final DefaultMutableTreeNode o = new DefaultMutableTreeNode(kvp);
				for(final EITsection section :table.get(serviceNo)){
					if(section!= null){
						if(!simpleModus(modus)){
							addSectionVersionsToJTree(o, section, modus);
						}else{
							addListJTree(o,section.getEventList(),modus,"events");
						}
					}
				}
				n.add(o);
			}
			t.add(n);
		}
		return t;
	}

	public boolean exists(final int tableId, final int tableIdExtension, final int section){
		return ((eit.get(tableId)!=null) &&
				(eit.get(tableId).get(tableIdExtension)!=null) &&
				(eit.get(tableId).get(tableIdExtension).length >section) &&
				(eit.get(tableId).get(tableIdExtension)[section]!=null));
	}

	public Map<Integer, HashMap<Integer, EITsection[]>> getEITsectionsMap() {
		return eit;
	}

	/**
	 * Returns the start time of the first, and the end time of the last event of the services in this EIT table.
	 * Only the services contained in serviceSet are used in the calculation
	 *
	 * @param serviceSet service ID of services to be included in calculation
	 * @param eitTable map of service IDs to EITSection[] Can contain sections from different Table IDs, like 0x50 and 0x51, etc... (for very long EPGs)
	 * @return Interval that covers all events in eitTable
	 */
	public static Interval getSpanningInterval(final Set<Integer> serviceSet, Map<Integer, EITsection[]> eitTable) {
		Date startDate = null;
		Date endDate = null;
		// services to be displayed

		for(final Integer serviceNo : serviceSet){
			for(final EITsection section :eitTable.get(serviceNo)){
				if(section!= null){
					List<Event> eventList = section.getEventList();
					for(Event event:eventList){
						final byte[] startTime = event.getStartTime();
						if(isFFFFFFFF(startTime)){ // undefined start time
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
		}else{
			return null;
		}
	}

	public Map<Integer, EITsection[]> getCombinedSchedule(){

		HashMap<Integer, EITsection[]> res = new HashMap<Integer, EITsection[]>();
		// actual TS
		for (int tableID = 0x50; tableID < 0x60; tableID++) {
			addSections(res, tableID);
		}
		// other TS
		for (int tableID = 0x60; tableID < 0x70; tableID++) {
			addSections(res, tableID);
		}
		return res;
	}

	public Map<Integer, EITsection[]> getCombinedPresentFollowing(){

		HashMap<Integer, EITsection[]> res = new HashMap<Integer, EITsection[]>();
		// actual TS
		addSections(res, 0x4E);
		// other TS
		addSections(res, 0x4F);
		return res;
	}

	/**
	 * @param res
	 * @param tableID
	 */
	private void addSections(Map<Integer, EITsection[]> res, int tableID) {
		Map<Integer, EITsection []> table= eit.get(tableID);
		if(table!=null){
			HashSet<Integer> serviceSet = new HashSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				EITsection[] eitArray = table.get(serviceNo); // array to be added to already found EITSections
				if((eitArray!=null)&&(eitArray.length>0)){
					EITsection[] resArray = res.get(serviceNo); //already found EITSections
					if(resArray==null){ // nothing yet, so put in new found
						res.put(serviceNo, eitArray);
					}else{
						EITsection[] combinedArray = new EITsection[resArray.length + eitArray.length];
						System.arraycopy(resArray, 0, combinedArray, 0, resArray.length);
						System.arraycopy(eitArray, 0, combinedArray, resArray.length, eitArray.length);
						res.put(serviceNo, combinedArray);
					}
				}
			}
		}
	}

}

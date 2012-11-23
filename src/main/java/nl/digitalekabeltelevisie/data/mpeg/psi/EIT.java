package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.Utils;

/**
 *
 * Based on 5.2.4 Event Information Table (EIT) of ETSI EN 300 468 V1.11.1 (2010-04)
 *
 * @author Eric
 *
 */
public class EIT extends AbstractPSITabel{

	private final Map<Integer, HashMap<Integer,EITsection []>> eit = new HashMap<Integer, HashMap<Integer, EITsection []>>();

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


		if(section.isCrc_error()){
			return;
		}
		count++;

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
		if(serviceSection[section.getSectionNumber()]==null){
			serviceSection[section.getSectionNumber()] = section;
		}else{
			final TableSection last = serviceSection[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("EIT"));
		final TreeSet<Integer> tableSet = new TreeSet<Integer>(eit.keySet());


		for(final Integer tableID : tableSet ){
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("table_id",tableID, TableSection.getTableType(tableID)));
			final HashMap<Integer, EITsection []> table= eit.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				final KVP kvp = new KVP("service_id",serviceNo, getParentPSI().getSdt().getServiceName(serviceNo));
				kvp.setHtmlSource(new ServiceListing(tableID, serviceNo)); // set listing for whole service

				final DefaultMutableTreeNode o = new DefaultMutableTreeNode(kvp);
				for(final EITsection section :table.get(serviceNo)){
					if(section!= null){
						if(!Utils.simpleModus(modus)){
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


}

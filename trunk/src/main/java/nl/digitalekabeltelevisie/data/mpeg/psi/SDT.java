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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ServiceDescriptor;
import nl.digitalekabeltelevisie.util.Utils;

public class SDT extends AbstractPSITabel{

	public SDT(final PSI parentPSI) {
		super(parentPSI);

	}

	private Map<Integer, SDTsection []> transportStreams = new HashMap<Integer, SDTsection []>();

	public void update(final SDTsection section){

		final int key = section.getTransportStreamID();
		SDTsection [] sections= transportStreams.get(key);

		if(sections==null){
			sections = new SDTsection[section.getSectionLastNumber()+1];
			transportStreams.put(key, sections);
		}
		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("SDT"));
		final TreeSet<Integer> s = new TreeSet<Integer>(transportStreams.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer transportStreamID=i.next();
			final SDTsection [] sections = transportStreams.get(transportStreamID);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("transport stream", transportStreamID.toString(),null));
			for (SDTsection section : sections) {
				if(section!= null){
					if(!Utils.simpleModus(modus)){
						n.add(section.getJTreeNode(modus));
					}else{
						addListJTree(n,section.getServiceList(),modus,"services");
					}
				}
			}
			t.add(n);

		}
		return t;
	}

	public String getServiceName(final int serviceID){
		DVBString dvbString = getServiceNameDVBString(serviceID);
		if(dvbString!=null){
			return dvbString.toString();
		}else{
			return null;
		}
	}


	public DVBString getServiceNameDVBString(final int serviceID){
		DVBString r = null;

		final SDTsection.Service service=getService(serviceID);
		if(service!=null) {
			final Iterator<Descriptor> descs=service.getDescriptorList().iterator();
			while(descs.hasNext()){
				final Descriptor d=descs.next();
				if(d instanceof ServiceDescriptor) {
					r = ((ServiceDescriptor)d).getServiceName();
					return r;
				}
			}
			// service found, but no name, give up
			return r;
		}else{ // no service found
			return r;
		}
	}

	public int getServiceType(final int serviceID){
		int r = 0;

		final SDTsection.Service service=getService(serviceID);
		if(service!=null) {
			final Iterator<Descriptor> descs=service.getDescriptorList().iterator();
			while(descs.hasNext()){
				final Descriptor d=descs.next();
				if(d instanceof ServiceDescriptor) {
					r = ((ServiceDescriptor)d).getServiceType();
					return r;
				}
			}
			// service found, but no name, give up
			return r;
		}else{ // no service found
			return r;

		}
	}


	public boolean exists(final int tableIdExtension, final int section){
		return ((transportStreams.get(tableIdExtension)!=null) &&
				(transportStreams.get(tableIdExtension).length >section) &&
				(transportStreams.get(tableIdExtension)[section]!=null));
	}

	public Map<Integer, SDTsection[]> getTransportStreams() {
		return transportStreams;
	}

	public void setTransportStreams(final Map<Integer, SDTsection[]> transportStreams) {
		this.transportStreams = transportStreams;
	}

	public SDTsection.Service getService(final int serviceID){


		final TreeSet<Integer> s = new TreeSet<Integer>(transportStreams.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer transportStreamID=i.next();
			final SDTsection [] sections = transportStreams.get(transportStreamID);
			if(sections!=null){
				for (SDTsection section : sections) {
					if(section!= null){
						final Iterator<SDTsection.Service> serviceIter=section.getServiceList().iterator();
						while(serviceIter.hasNext()){
							final SDTsection.Service service=serviceIter.next();
							if(serviceID== service.getServiceID()) {
								return service;
							}
						}
					}
				}
			}
		}
		// no service found, give up
		return null;
	}

	public int getTransportStreamID(final int serviceID){


		final TreeSet<Integer> s = new TreeSet<Integer>(transportStreams.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer transportStreamID=i.next();
			final SDTsection [] sections = transportStreams.get(transportStreamID);
			if(sections!=null){
				for (SDTsection section : sections) {
					if(section!= null){
						final Iterator<SDTsection.Service> serviceIter=section.getServiceList().iterator();
						while(serviceIter.hasNext()){
							final SDTsection.Service service=serviceIter.next();
							if(serviceID== service.getServiceID()) {
								return transportStreamID;
							}
						}
					}
				}
			}
		}
		// no service found, give up
		return -1;
	}
}

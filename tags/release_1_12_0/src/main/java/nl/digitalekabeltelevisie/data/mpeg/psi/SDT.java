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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.SDTsection.Service;
import nl.digitalekabeltelevisie.util.*;

public class SDT extends AbstractPSITabel{

	public SDT(final PSI parentPSI) {
		super(parentPSI);

	}
	
	// map<orgNetworkId,TransportStreamId>
	private Map<Integer,HashMap<Integer, SDTsection []>> networks = new HashMap<Integer, HashMap<Integer, SDTsection []>>();
	private SDTsection [] actualTransportStreamSDT;

	public void update(final SDTsection section){

		final int original_network_id = section.getOriginalNetworkID();
		final int streamId = section.getTransportStreamID();
		
		Map<Integer, SDTsection []> networkSections = networks.computeIfAbsent(original_network_id, HashMap::new);
		SDTsection [] tsSections = networkSections.computeIfAbsent(streamId, k -> new SDTsection[section.getSectionLastNumber()+1]);

		addSectionToArray(section, tsSections);
		
		if(section.getTableId()==0x42) { 
			if(actualTransportStreamSDT == null) {
				actualTransportStreamSDT = new SDTsection[section.getSectionLastNumber()+1];
			}
			addSectionToArray(section, actualTransportStreamSDT);
		}
	}

	/**
	 * @param section
	 * @param tsSections
	 */
	private static void addSectionToArray(final SDTsection section, SDTsection[] tsSections) {
		if(tsSections[section.getSectionNumber()]==null){
			tsSections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = tsSections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("SDT"));

		final TreeSet<Integer> networksTreeSet = new TreeSet<Integer>(networks.keySet());
		final Iterator<Integer> i = networksTreeSet.iterator();
		while(i.hasNext()){
			final Integer orgNetworkId=i.next();
			final Map<Integer, SDTsection []> networkSections = networks.get(orgNetworkId);
			
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("original_network_id", orgNetworkId,Utils.getOriginalNetworkIDString(orgNetworkId)));
			t.add(n);			
			
			final TreeSet<Integer> streamsTreeSet = new TreeSet<Integer>(networkSections.keySet());
			final Iterator<Integer> j = streamsTreeSet.iterator();
			while(j.hasNext()){
				final Integer transport_stream_id = j.next();
				SDTsection[] sections = networkSections.get(transport_stream_id);

				final DefaultMutableTreeNode m = new DefaultMutableTreeNode(new KVP("transport_stream_id", transport_stream_id,null));
				n.add(m);

				for (SDTsection section : sections) {
					if(section!= null){
						if(!Utils.simpleModus(modus)){
							addSectionVersionsToJTree(m, section, modus);
						}else{
							addListJTree(m,section.getServiceList(),modus,"services");
						}
					}
				}
			}
		}
		return t;
	}
	
	@Deprecated
	public String getServiceName(final int serviceID){
		DVBString dvbString = getServiceNameDVBString(serviceID);
		if(dvbString!=null){
			return dvbString.toString();
		}else{
			return null;
		}
	}


	public Optional<String> getServiceNameForActualTransportStreamOptional(final int serviceID){
		return  getServiceNameForActualTransportStreamDVBString(serviceID).map(DVBString::toString);

	}

	public String getServiceNameForActualTransportStream(final int serviceID){
		return  getServiceNameForActualTransportStreamDVBString(serviceID).map(DVBString::toString).orElse(null);

	}
	
	public Optional<DVBString> getServiceNameForActualTransportStreamDVBString(int serviceID){
		return getServiceForActualTransportStream(serviceID)
				.map(SDTsection.Service::getDescriptorList)
				.orElseGet(ArrayList<Descriptor>::new)
				.stream()
				.filter(d -> d instanceof ServiceDescriptor)
				.map(d -> (ServiceDescriptor)d)
				.findFirst()
				.map(ServiceDescriptor::getServiceName);
	}
	
	public String getServiceName(final int original_network_id, final int transport_stream_id, final int serviceID){
		return getServiceNameDVBString(original_network_id,transport_stream_id,serviceID).map(DVBString::toString).orElse(null);
	}
	
	public String getServiceName(final ServiceIdentification serviceIdentification){
		return getServiceNameDVBString(serviceIdentification.getOriginalNetworkId(),serviceIdentification.getTransportStreamId(),serviceIdentification.getServiceId()).
				map(DVBString::toString).
				orElse(null);
	}
	
	public Optional<DVBString> getServiceNameDVBString(final ServiceIdentification serviceIdentification){
		return getServiceNameDVBString(serviceIdentification.getOriginalNetworkId(),serviceIdentification.getTransportStreamId(),serviceIdentification.getServiceId());
	}
	
	public Optional<DVBString> getServiceNameDVBString(final int original_network_id, final int transport_stream_id, final int serviceID){

//		Optional<List<Descriptor>> descriptorList = getService(original_network_id,transport_stream_id,serviceID)
//				.map(SDTsection.Service::getDescriptorList);
//					
//		if (descriptorList.isPresent()) {
//			return descriptorList
//				.get()
//				.stream()
//				.filter(d -> d instanceof ServiceDescriptor)
//				.map(d -> (ServiceDescriptor) d)
//				.findFirst()
//				.map(ServiceDescriptor::getServiceName);
//		}
//		return Optional.empty();	
//



		return getService(original_network_id,transport_stream_id,serviceID)
				.map(SDTsection.Service::getDescriptorList)
				.orElseGet(ArrayList<Descriptor>::new)
				.stream()
				.filter(d -> d instanceof ServiceDescriptor)
				.map(d -> (ServiceDescriptor)d)
				.findFirst()
				.map(ServiceDescriptor::getServiceName);



//					Optional<List<Descriptor>> r = getService(original_network_id,transport_stream_id,serviceID)
//							.map(SDTsection.Service::getDescriptorList);
//							List<Descriptor> t = r.get();
//			
//							return t.stream()
//							.filter(d -> d instanceof ServiceDescriptor)
//							.map(d -> (ServiceDescriptor)d)
//							.findFirst()
//							.map(ServiceDescriptor::getServiceName);
//

	}

	@Deprecated
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

	@Deprecated
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



	public Map<Integer, SDTsection[]> getTransportStreams() {
		return null;
	}


	public Optional<SDTsection.Service> getService(final int orgNetworkId, final int transportStreamID, final int serviceID){

		HashMap<Integer, SDTsection[]> transportStreams = networks.get(orgNetworkId);
		
		if(transportStreams !=null) {
		
			final SDTsection [] sections = transportStreams.get(transportStreamID);
			if(sections!=null){
				for (SDTsection section : sections) {
					if(section!= null)  {
						for(Service service: section.getServiceList()) {
							if(service.getServiceID() == serviceID) {
								return Optional.of(service);
							}
						}
					}
				}
			}
		}
		// no service found, give up
		return Optional.empty();
	}
	
	
	public Optional<SDTsection.Service> getServiceForActualTransportStream(final int serviceID){

			
		if(actualTransportStreamSDT !=null) {
		
			final SDTsection [] sections = actualTransportStreamSDT;
			if(sections!=null){
				for (SDTsection section : sections) {
					if(section!= null)  {
						for(Service service: section.getServiceList()) {
							if(service.getServiceID() == serviceID) {
								return Optional.of(service);
							}
						}
					}
				}
			}
		}
		// no service found, give up
		return Optional.empty();
	}
	
	

	@Deprecated
	public SDTsection.Service getService(final int serviceID){

		
		final TreeSet<Integer> t = new TreeSet<Integer>(networks.keySet());
		final Iterator<Integer> j = t.iterator();
		
		while(j.hasNext()) {
			
			int orgNetworkId = j.next();
			HashMap<Integer, SDTsection[]> transportStreams = networks.get(orgNetworkId);
	
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
		}
		// no service found, give up
		return null;
	}

	public int getTransportStreamID(final int serviceID){


		final TreeSet<Integer> t = new TreeSet<Integer>(networks.keySet());
		final Iterator<Integer> j = t.iterator();
		
		while(j.hasNext()) {
			
			int orgNetworkId = j.next();
		
			HashMap<Integer, SDTsection[]> transportStreams = networks.get(orgNetworkId);
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
		}
		// no service found, give up
		return -1;
	}
}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.NetworkNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.HDSimulcastLogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.eaccam.LogicalChannelDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.NITsection.TransportStream;
import nl.digitalekabeltelevisie.util.Utils;

public class NIT extends AbstractPSITabel{

	private Map<Integer, NITsection []> networks = new HashMap<Integer, NITsection []>();


	public NIT(final PSI parent){
		super(parent);
	}

	public void update(final NITsection section){
		count++;

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

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("NIT"));
		final TreeSet<Integer> s = new TreeSet<Integer>(networks.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer networkNo=i.next();
			final NITsection [] sections = networks.get(networkNo);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("network_id",networkNo, getNetworkName(networkNo)));
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
			t.add(n);
		}
		return t;
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


}

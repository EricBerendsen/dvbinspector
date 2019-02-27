package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.util.*;

public class FST extends AbstractPSITabel{

	private static LookUpList operatorName = new LookUpList.Builder().
			add(106, "Canal Digitaal").
			add(108, "TV VLAANDEREN").
			add(109, "TELESAT").
			add(100, "HD Austria").
			add(200, "Skylink").
			add(400, "Diveo").
			build();
	
	private static LookUpList pidAllocation = new LookUpList.Builder().
			add(901,"Canal Digitaal").
			add(911,"TV VLAANDEREN").
			add(920,"TELESAT").
			add(950,"HD Austria").
			add(960,"Diveo").
			add(30,"Skylink Czech Republic list").
			add(31,"Skylink Slovak Republic list").
			build();
			
	private Map<Integer, Map<Integer, FSTsection []>> networks = new HashMap<>();


	public FST(final PSI parent){
		super(parent);
	}

	public void update(final FSTsection section){
		final int pid = section.getParentPID().getPid();
		final int key = section.getOperatorNetworkID();
		
		Map<Integer, FSTsection[]> pidNetwork = networks.computeIfAbsent(pid, k -> new HashMap<>());
		FSTsection[] sections = pidNetwork.computeIfAbsent(key, k -> new FSTsection[section.getSectionLastNumber()+1]);

		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode( new KVP("FST (M7 Fastscan Services Table)"));
		for (Integer pid : new TreeSet<Integer>(networks.keySet())) {
			Map<Integer, FSTsection[]> operatorsInPid = networks.get(pid);
			final DefaultMutableTreeNode pidNode = new DefaultMutableTreeNode(new KVP("pid",pid, pidAllocation.get(pid, "unknown"))); 
			for (Integer operator_network_id : new TreeSet<Integer>(operatorsInPid.keySet())) {
				final int networkNo = operator_network_id;
				final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("operator_network_id",operator_network_id, operatorName.get(networkNo,"unknown")));
				final FSTsection [] sections = operatorsInPid.get(operator_network_id);
				for (final FSTsection tsection : sections) {
					if(tsection!= null){
						if(!Utils.simpleModus(modus)){
							addSectionVersionsToJTree(n, tsection, modus);
						}else{
//							addListJTree(n,tsection.getNetworkDescriptorList(),modus,"descriptors");
//							addListJTree(n,tsection.getTransportStreamList(),modus,"transport streams");
						}
					}
				}
				pidNode.add(n);
			}
			t.add(pidNode);
		}
		return t;
	}

	public String getOperatorNetworkName(final int networkNo){
		return operatorName.get(networkNo,"unknown");
	}


}

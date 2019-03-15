package nl.digitalekabeltelevisie.data.mpeg.psi.nonstandard;

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

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.util.LookUpList;

public class M7Fastscan implements TreeNode {
	
	Map<Integer, Map<Integer,OperatorFastscan>> operators = new HashMap<>();

	@SuppressWarnings("unused")
	private PSI parentPSI;

	private static LookUpList operatorName = new LookUpList.Builder().
			add(106, "Canal Digitaal").
			add(108, "TV VLAANDEREN").
			add(109, "TELESAT").
			add(100, "HD Austria").
			add(200, "Skylink").
			add(400, "Diveo").
			build();
	
	public M7Fastscan(PSI psi) {
		this.parentPSI = psi;
	}


	public void update(final FSTsection section){
		final int pid = section.getParentPID().getPid();
		int operator = section.getOperatorNetworkID();
		
		OperatorFastscan countryFastscan= findOperatorFastscan(operator,pid);  
		countryFastscan.update(section);
	}

	
	private OperatorFastscan findOperatorFastscan(int operator, int pid) {
		Map<Integer, OperatorFastscan> p = operators.computeIfAbsent(operator, k -> new HashMap<>());
		OperatorFastscan o = p.computeIfAbsent(pid, k -> new OperatorFastscan(pid));
		return o;
	}


	public void update(final FNTsection section){
		final int pid = section.getParentPID().getPid();
		int operator = section.getOperatorNetworkID();
		
		OperatorFastscan countryFastscan= findOperatorFastscan(operator,pid);  
		countryFastscan.update(section);
	}
	

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode( new KVP("M7 Fastscan"));
		for (Integer operatorId : new TreeSet<Integer>(operators.keySet())) {
			Map<Integer, OperatorFastscan> operatorsInPid = operators.get(operatorId);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("operator_network_id",operatorId, operatorName.get(operatorId,"unknown")));
			t.add(n);
			for (Integer pid : new TreeSet<Integer>(operatorsInPid.keySet())) {
				OperatorFastscan operatorFastscan = operatorsInPid.get(pid);
				n.add(operatorFastscan.getJTreeNode(modus));
			}
			
		}
			
		return t;
	}

}

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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

/**
 * @author Eric
 *
 * based on ETSI ES 201 812 V1.1.1 (Multimedia Home Platform (MHP) Specification 1.0.3
 * ETSI TS 102 809 V1.1.1, (Signalling and carriage of interactive applications and services in Hybrid broadcast/broadband environments)
 * ETSI TS 102 796 V1.1.1 (Hybrid Broadcast Broadband TV)
 */
public class AITs extends AbstractPSITabel{


	public AITs(final PSI parentPSI) {
		super(parentPSI);

	}

	private final Map<Integer, AIT> aits = new HashMap<>();

	public void update(final AITsection section){

		final int pid = section.getParentPID().getPid();
		AIT ait = aits.get(pid);

		if(ait==null){
			ait = new AIT(parentPSI);
			aits.put(pid, ait);
		}
		ait.update(section);
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AITs"));

		for (Integer pid : new TreeSet<>(aits.keySet())) {
			final AIT ait = aits.get(pid);
			t.add(ait.getJTreeNode(modus));

		}
		return t;
	}




}

package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

/**
 * @author Eric
 *
 * based on ETSI TS 102 323 V1.5.1 (2012-01) Digital Video Broadcasting (DVB);
 * Carriage and signalling of TV-Anytime information
 * in DVB transport streams
 * 10.4 Related content table
 */
public class RCTs extends AbstractPSITabel{


	public RCTs(final PSI parentPSI) {
		super(parentPSI);

	}

	private final Map<Integer, RCT> rcts = new HashMap<>();

	public void update(final RCTsection section){

		final int pid = section.getParentPID().getPid();
		RCT  ait= rcts.get(pid);

		if(ait==null){
			ait = new RCT(parentPSI);
			rcts.put(pid, ait);
		}
		ait.update(section);
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("RCTs"));
		final SortedSet<Integer> s = new TreeSet<>(rcts.keySet());

		for (Integer pid : s) {
			final RCT rct = rcts.get(pid);
			t.add(rct.getJTreeNode(modus));

		}
		return t;
	}




}

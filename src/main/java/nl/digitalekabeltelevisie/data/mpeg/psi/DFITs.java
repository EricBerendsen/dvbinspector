package nl.digitalekabeltelevisie.data.mpeg.psi;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

//based on EN 303 560 V1.1.1 (2018-05) 

public class DFITs extends AbstractPSITabel {
	
	private final Map<Integer, DFIT> dfits = new TreeMap<>();

	public DFITs(PSI parentPSI) {
		super(parentPSI);
		// TODO Auto-generated constructor stub
	}

	
	public void update(final DFITSection section){

		final int pid = section.getParentPID().getPid();
		DFIT dfit = dfits.computeIfAbsent(pid, p -> new DFIT(parentPSI));
		dfit.update(section);
		
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("DFITs (Downloadable Font Information Tables)"));
		for(Entry<Integer, DFIT> dfitEntry:dfits.entrySet()) {
			t.add(dfitEntry.getValue().getJTreeNode(modus));
		}
		return t;
	}

	public Map<Integer, DFIT> getDfits() {
		return dfits;
	}

}

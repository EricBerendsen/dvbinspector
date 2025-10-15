package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
 * based on ETSI ES 201 812 V1.1.1, ETSI TS 102 809 V1.1.1, ETSI TS 102 796 V1.1.1
 */

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

public class UNTs extends AbstractPSITabel{


	public UNTs(PSI parentPSI) {
		super(parentPSI);

	}

	private final Map<Integer, UNT> unts = new HashMap<>();

	public void update(UNTsection section){

		int pid = section.getParentPID().getPid();
		
		UNT unt= unts.computeIfAbsent(pid, k -> new UNT(parentPSI));
		unt.update(section);
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = new KVP("UNTs");

		SortedSet<Integer> s = new TreeSet<>(unts.keySet());

		for (Integer pid : s) {
			UNT unt = unts.get(pid);
			t.add(unt.getJTreeNode(modus));

		}
		return t;
	}




}

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
 */

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.util.Utils.getAppTypeIDString;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

public class RCT extends AbstractPSITabel{

	private final Map<Integer, RCTsection []> rcts = new HashMap<>();
	private int pid = 0;

	public RCT(final PSI parent){
		super(parent);
	}

	public void update(RCTsection section){
		pid=section.getParentPID().getPid();

		int key = section.getTableIdExtension();
		RCTsection[] sections = rcts.computeIfAbsent(key, k -> new RCTsection[section.getSectionLastNumber() + 1]);

		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = new KVP("RCT (Related Content Table) PID="+pid );
		TreeSet<Integer> s = new TreeSet<>(rcts.keySet());

		for (Integer type : s) {
			RCTsection[] sections = rcts.get(type);
			KVP n =new KVP("RCT", type, getAppTypeIDString(type));
			for (RCTsection tsection : sections) {
				if (tsection != null) {
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}
		return t;
	}


}

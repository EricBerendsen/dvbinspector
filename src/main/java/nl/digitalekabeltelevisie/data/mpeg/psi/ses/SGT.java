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

package nl.digitalekabeltelevisie.data.mpeg.psi.ses;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * 
 */
public class SGT extends AbstractPSITabel {
	
	private final Map<Integer,HashMap<Integer, SGTsection []>> service_guides = new HashMap<>();

	public void update(SGTsection section) {
		
		int pid = section.getParentPID().getPid();
		
		HashMap<Integer, SGTsection []> l =  service_guides.computeIfAbsent(pid,HashMap::new);

		int key = section.getServiceListId();
		SGTsection[] sections = l.computeIfAbsent(key, k -> new SGTsection[section.getSectionLastNumber() + 1]);
		if (sections[section.getSectionNumber()] == null) {
			sections[section.getSectionNumber()] = section;
		} else {
			TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}


	public SGT(PSI parentPSI) {
		super(parentPSI);
	}
	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = new KVP("SGT");
		
		for(Entry<Integer, HashMap<Integer, SGTsection[]>> guide:service_guides.entrySet()) {
			
			KVP pid = new KVP("pid",guide.getKey());
			t.add(pid);
		
			for(int service_list_id:new TreeSet<>(guide.getValue().keySet())) {
	
				KVP kvp = new KVP("service_list_id",service_list_id);
				SGTsection [] sections = guide.getValue().get(service_list_id);
				for (SGTsection tsection : sections) {
					if(tsection!= null){
						kvp.add(tsection.getJTreeNode(modus));
	
					}
				}
				pid.add(kvp);
	
			}
		}
		return t;
	}

}

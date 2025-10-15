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
*/

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

//based on EN 303 560 V1.1.1 (2018-05) 

public class DFIT extends AbstractPSITabel {

	private final Map<Integer, DFITSection []> dfits = new TreeMap<>();

	private int pid = 0;

	protected DFIT(PSI parentPSI) {
		super(parentPSI);
	}

	public void update(DFITSection section) {
		pid=section.getParentPID().getPid();
		
		int fontId = section.getFont_id();
		DFITSection [] sections= dfits.computeIfAbsent(fontId, f -> new DFITSection[section.getSectionLastNumber()+1]);

		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}

	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = new KVP("DFIT PID="+pid);
		
		for(Entry<Integer, DFITSection[]> dfit:dfits.entrySet()) {
			
			int font_id= dfit.getKey();
			DFITSection [] sections = dfit.getValue();
			KVP n = new KVP("DFIT, fontId",font_id);
			for (DFITSection tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);
		}

		return t;
	}


}

/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import java.util.Map.Entry;
import java.util.TreeMap;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class ATSCETT extends AbstractPSITabel {

	private final TreeMap<Integer, TreeMap<Long, ATSCETTsection[]>> tables = new TreeMap<>();

	public ATSCETT(final PSI parentPSI) {
		super(parentPSI);
	}

	public void update(final ATSCETTsection section, final int tableType) {
		TreeMap<Long, ATSCETTsection[]> etms = tables.computeIfAbsent(tableType, k -> new TreeMap<>());
		ATSCETTsection[] sections = etms.computeIfAbsent(section.getEtmId(),
				k -> new ATSCETTsection[section.getSectionLastNumber() + 1]);
		if (sections.length <= section.getSectionNumber()) {
			ATSCETTsection[] resized = new ATSCETTsection[section.getSectionNumber() + 1];
			System.arraycopy(sections, 0, resized, 0, sections.length);
			sections = resized;
			etms.put(section.getEtmId(), sections);
		}
		if (sections[section.getSectionNumber()] == null) {
			sections[section.getSectionNumber()] = section;
		} else {
			TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP kvp = new KVP("ETT");
		for (Entry<Integer, TreeMap<Long, ATSCETTsection[]>> tableEntry : tables.entrySet()) {
			KVP tableNode = new KVP("table_type", tableEntry.getKey(), MGTsection.getTableTypeDescription(tableEntry.getKey()));
			kvp.add(tableNode);
			for (Entry<Long, ATSCETTsection[]> etmEntry : tableEntry.getValue().entrySet()) {
				KVP etmNode = new KVP("ETM_id", etmEntry.getKey());
				tableNode.add(etmNode);
				for (ATSCETTsection section : etmEntry.getValue()) {
					if (section != null) {
						if (Utils.simpleModus(modus)) {
							etmNode.add(section.getJTreeNode(modus));
						} else {
							addSectionVersionsToJTree(etmNode, section, modus);
						}
					}
				}
			}
		}
		return kvp;
	}

	public TreeMap<Integer, TreeMap<Long, ATSCETTsection[]>> getTables() {
		return tables;
	}
}

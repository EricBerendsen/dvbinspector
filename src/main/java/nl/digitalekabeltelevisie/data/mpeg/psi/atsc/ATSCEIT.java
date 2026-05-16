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

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class ATSCEIT extends AbstractPSITabel {

	private final TreeMap<Integer, TreeMap<Integer, ATSCEITsection[]>> tables = new TreeMap<>();

	public ATSCEIT(final PSI parentPSI) {
		super(parentPSI);
	}

	public void update(final ATSCEITsection section, final int tableType) {
		section.setTableType(tableType);
		section.setGpsUtcOffset(getGpsUtcOffset());
		TreeMap<Integer, ATSCEITsection[]> sources = tables.computeIfAbsent(tableType, k -> new TreeMap<>());
		ATSCEITsection[] sections = sources.computeIfAbsent(section.getSourceId(),
				k -> new ATSCEITsection[section.getSectionLastNumber() + 1]);
		if (sections.length <= section.getSectionNumber()) {
			ATSCEITsection[] resized = new ATSCEITsection[section.getSectionNumber() + 1];
			System.arraycopy(sections, 0, resized, 0, sections.length);
			sections = resized;
			sources.put(section.getSourceId(), sections);
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
		KVP kvp = new KVP("EIT");
		kvp.addTableSource(this::getTableModel, "EPG Events");
		for (Entry<Integer, TreeMap<Integer, ATSCEITsection[]>> tableEntry : tables.entrySet()) {
			KVP tableNode = new KVP("table_type", tableEntry.getKey(), MGTsection.getTableTypeDescription(tableEntry.getKey()));
			tableNode.addTableSource(() -> getTableModel(tableEntry.getValue()), "EPG Events");
			kvp.add(tableNode);
			for (Entry<Integer, ATSCEITsection[]> sourceEntry : tableEntry.getValue().entrySet()) {
				KVP sourceNode = new KVP("source_id", sourceEntry.getKey());
				sourceNode.addTableSource(() -> getTableModel(sourceEntry.getValue()), "EPG Events");
				tableNode.add(sourceNode);
				for (ATSCEITsection section : sourceEntry.getValue()) {
					if (section != null) {
						if (Utils.simpleModus(modus)) {
							sourceNode.add(section.getJTreeNode(modus));
						} else {
							addSectionVersionsToJTree(sourceNode, section, modus);
						}
					}
				}
			}
		}
		return kvp;
	}

	public TreeMap<Integer, TreeMap<Integer, ATSCEITsection[]>> getTables() {
		return tables;
	}

	public TableModel getTableModel() {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		for (TreeMap<Integer, ATSCEITsection[]> sources : tables.values()) {
			addSectionsToTableModel(tableModel, sources);
		}
		tableModel.process();
		return tableModel;
	}

	private static TableModel getTableModel(final TreeMap<Integer, ATSCEITsection[]> sources) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		addSectionsToTableModel(tableModel, sources);
		tableModel.process();
		return tableModel;
	}

	private static TableModel getTableModel(final ATSCEITsection[] sections) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		addSectionsToTableModel(tableModel, sections);
		tableModel.process();
		return tableModel;
	}

	private static void addSectionsToTableModel(final FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel,
			final TreeMap<Integer, ATSCEITsection[]> sources) {
		for (ATSCEITsection[] sections : sources.values()) {
			addSectionsToTableModel(tableModel, sections);
		}
	}

	private static void addSectionsToTableModel(final FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel,
			final ATSCEITsection[] sections) {
		for (ATSCEITsection section : sections) {
			if (section != null) {
				tableModel.addData(section, section.getEvents());
			}
		}
	}

	private int getGpsUtcOffset() {
		if ((parentPSI == null) || parentPSI.getAtsc().getStt().getSttSectionList().isEmpty()) {
			return 0;
		}
		return parentPSI.getAtsc().getStt().getSttSectionList().getFirst().getGpsUtcOffset();
	}
}

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

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class ATSCETT extends AbstractPSITabel {

	private final TreeMap<Integer, TreeMap<Long, ATSCETTsection[]>> tables = new TreeMap<>();

	public ATSCETT(final PSI parentPSI) {
		super(parentPSI);
	}

	public void update(final ATSCETTsection section, final int tableType) {
		section.setTableType(tableType);
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
		kvp.addTableSource(this::getTableModel, "EPG Text");
		for (Entry<Integer, TreeMap<Long, ATSCETTsection[]>> tableEntry : tables.entrySet()) {
			KVP tableNode = new KVP("table_type", tableEntry.getKey(), MGTsection.getTableTypeDescription(tableEntry.getKey()));
			tableNode.addTableSource(() -> getTableModel(tableEntry.getValue()), "EPG Text");
			kvp.add(tableNode);
			for (Entry<Long, ATSCETTsection[]> etmEntry : tableEntry.getValue().entrySet()) {
				KVP etmNode = new KVP("ETM_id", etmEntry.getKey());
				etmNode.addTableSource(() -> getTableModel(etmEntry.getValue()), "EPG Text");
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

	public String getChannelText(final int sourceId) {
		return findText(section -> section.getSourceId() == sourceId
				&& ((section.getTableType() == 0x0004) || (section.getEventId() == 0)));
	}

	public String getEventText(final int sourceId, final int eventId) {
		return findText(section -> section.getSourceId() == sourceId
				&& section.getEventId() == eventId
				&& section.getTableType() != 0x0004);
	}

	public TableModel getTableModel() {
		FlexTableModel<ATSCETTsection, ATSCETTsection> tableModel = new FlexTableModel<>(ATSCETTsection.buildEttTableHeader());
		for (TreeMap<Long, ATSCETTsection[]> etms : tables.values()) {
			addSectionsToTableModel(tableModel, etms);
		}
		tableModel.process();
		return tableModel;
	}

	private static TableModel getTableModel(final TreeMap<Long, ATSCETTsection[]> etms) {
		FlexTableModel<ATSCETTsection, ATSCETTsection> tableModel = new FlexTableModel<>(ATSCETTsection.buildEttTableHeader());
		addSectionsToTableModel(tableModel, etms);
		tableModel.process();
		return tableModel;
	}

	private static TableModel getTableModel(final ATSCETTsection[] sections) {
		FlexTableModel<ATSCETTsection, ATSCETTsection> tableModel = new FlexTableModel<>(ATSCETTsection.buildEttTableHeader());
		addSectionsToTableModel(tableModel, sections);
		tableModel.process();
		return tableModel;
	}

	private static void addSectionsToTableModel(final FlexTableModel<ATSCETTsection, ATSCETTsection> tableModel,
			final TreeMap<Long, ATSCETTsection[]> etms) {
		for (ATSCETTsection[] sections : etms.values()) {
			addSectionsToTableModel(tableModel, sections);
		}
	}

	private static void addSectionsToTableModel(final FlexTableModel<ATSCETTsection, ATSCETTsection> tableModel,
			final ATSCETTsection[] sections) {
		for (ATSCETTsection section : sections) {
			if (section != null) {
				tableModel.addData(section, List.of(section));
			}
		}
	}

	private String findText(final java.util.function.Predicate<ATSCETTsection> predicate) {
		for (TreeMap<Long, ATSCETTsection[]> etms : tables.values()) {
			for (ATSCETTsection[] sections : etms.values()) {
				String text = joinText(sections, predicate);
				if (text != null) {
					return text;
				}
			}
		}
		return null;
	}

	private static String joinText(final ATSCETTsection[] sections,
			final java.util.function.Predicate<ATSCETTsection> predicate) {
		StringBuilder text = new StringBuilder();
		for (ATSCETTsection section : sections) {
			ATSCETTsection sectionVersion = section;
			while (sectionVersion != null) {
				if (predicate.test(sectionVersion)) {
					String sectionText = sectionVersion.getExtendedText();
					if ((sectionText != null) && !sectionText.isBlank()) {
						if (!text.isEmpty()) {
							text.append(' ');
						}
						text.append(sectionText);
					}
				}
				sectionVersion = (ATSCETTsection) sectionVersion.getNextVersion();
			}
		}
		return text.isEmpty() ? null : text.toString();
	}
}

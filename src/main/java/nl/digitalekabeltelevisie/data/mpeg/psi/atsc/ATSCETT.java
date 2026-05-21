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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Predicate;

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
		int requiredLength = Math.max(section.getSectionLastNumber() + 1, section.getSectionNumber() + 1);
		ATSCETTsection[] sections = etms.computeIfAbsent(section.getEtmId(),
				k -> new ATSCETTsection[requiredLength]);
		if (sections.length < requiredLength) {
			ATSCETTsection[] resized = new ATSCETTsection[requiredLength];
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
		return findText(0x0004, section -> section.getSourceId() == sourceId);
	}

	public String getEventText(final int eitTableType, final int sourceId, final int eventId) {
		return findText(getEventEttTableType(eitTableType),
				section -> section.getSourceId() == sourceId && section.getEventId() == eventId);
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

	private String findText(final int tableType, final Predicate<ATSCETTsection> predicate) {
		TreeMap<Long, ATSCETTsection[]> etms = tables.get(tableType);
		return etms == null ? null : findText(etms, predicate);
	}

	private String findText(final Predicate<ATSCETTsection> predicate) {
		for (TreeMap<Long, ATSCETTsection[]> etms : tables.values()) {
			String text = findText(etms, predicate);
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	private static String findText(final TreeMap<Long, ATSCETTsection[]> etms,
			final Predicate<ATSCETTsection> predicate) {
		for (ATSCETTsection[] sections : etms.values()) {
			String text = joinText(sections, predicate);
			if (text != null) {
				return text;
			}
		}
		return null;
	}

	private static String joinText(final ATSCETTsection[] sections, final Predicate<ATSCETTsection> predicate) {
		Entry<Integer, ATSCETTsection[]> latestCompleteVersion =
				getLatestCompleteVersionEntry(getVersionSections(sections));
		if (latestCompleteVersion == null) {
			return null;
		}
		StringBuilder text = new StringBuilder();
		for (ATSCETTsection section : latestCompleteVersion.getValue()) {
			if ((section != null) && predicate.test(section)) {
				String sectionText = section.getExtendedText();
				if ((sectionText != null) && !sectionText.isBlank()) {
					if (!text.isEmpty()) {
						text.append(' ');
					}
					text.append(sectionText);
				}
			}
		}
		return text.isEmpty() ? null : text.toString();
	}

	private static Map<Integer, ATSCETTsection[]> getVersionSections(final ATSCETTsection[] sections) {
		Map<Integer, ATSCETTsection[]> versionSections = new LinkedHashMap<>();
		for (ATSCETTsection section : sections) {
			ATSCETTsection sectionVersion = section;
			while (sectionVersion != null) {
				int requiredLength = Math.max(sectionVersion.getSectionLastNumber() + 1,
						sectionVersion.getSectionNumber() + 1);
				ATSCETTsection[] sectionsForVersion = versionSections.get(sectionVersion.getVersion());
				if (sectionsForVersion == null) {
					sectionsForVersion = new ATSCETTsection[requiredLength];
					versionSections.put(sectionVersion.getVersion(), sectionsForVersion);
				} else if (sectionsForVersion.length < requiredLength) {
					ATSCETTsection[] resizedSections = new ATSCETTsection[requiredLength];
					System.arraycopy(sectionsForVersion, 0, resizedSections, 0, sectionsForVersion.length);
					sectionsForVersion = resizedSections;
					versionSections.put(sectionVersion.getVersion(), sectionsForVersion);
				}
				sectionsForVersion[sectionVersion.getSectionNumber()] = sectionVersion;
				sectionVersion = (ATSCETTsection) sectionVersion.getNextVersion();
			}
		}
		return versionSections;
	}

	private static Entry<Integer, ATSCETTsection[]> getLatestCompleteVersionEntry(
			final Map<Integer, ATSCETTsection[]> versionSections) {
		Entry<Integer, ATSCETTsection[]> latestAvailableVersion = null;
		Entry<Integer, ATSCETTsection[]> latestCompleteVersion = null;
		for (Entry<Integer, ATSCETTsection[]> versionEntry : versionSections.entrySet()) {
			latestAvailableVersion = versionEntry;
			if (isCompleteVersion(versionEntry.getValue())) {
				latestCompleteVersion = versionEntry;
			}
		}
		return latestCompleteVersion != null ? latestCompleteVersion : latestAvailableVersion;
	}

	private static boolean isCompleteVersion(final ATSCETTsection[] sectionsForVersion) {
		if ((sectionsForVersion == null) || (sectionsForVersion.length == 0)) {
			return false;
		}
		for (ATSCETTsection section : sectionsForVersion) {
			if ((section == null) || (section.getSectionLastNumber() != (sectionsForVersion.length - 1))) {
				return false;
			}
		}
		return true;
	}

	private static int getEventEttTableType(final int eitTableType) {
		if ((0x0100 <= eitTableType) && (eitTableType <= 0x017F)) {
			return 0x0200 + (eitTableType - 0x0100);
		}
		if ((0x0200 <= eitTableType) && (eitTableType <= 0x027F)) {
			return eitTableType;
		}
		return 0x0200;
	}
}

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;

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
		int requiredLength = Math.max(section.getSectionLastNumber() + 1, section.getSectionNumber() + 1);
		ATSCEITsection[] sections = sources.computeIfAbsent(section.getSourceId(),
				k -> new ATSCEITsection[requiredLength]);
		if (sections.length < requiredLength) {
			ATSCEITsection[] resized = new ATSCEITsection[requiredLength];
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
		kvp.addTableSource(this::getTableModel, "EPG Events (latest complete versions)");
		kvp.addTableSource(this::getAllVersionsTableModel, "EPG Events (all versions)");
		for (Entry<Integer, TreeMap<Integer, ATSCEITsection[]>> tableEntry : tables.entrySet()) {
			KVP tableNode = new KVP("table_type", tableEntry.getKey(), MGTsection.getTableTypeDescription(tableEntry.getKey()));
			tableNode.addTableSource(() -> getTableModel(tableEntry.getValue()), "EPG Events (latest complete versions)");
			tableNode.addTableSource(() -> getAllVersionsTableModel(tableEntry.getValue()), "EPG Events (all versions)");
			kvp.add(tableNode);
			for (Entry<Integer, ATSCEITsection[]> sourceEntry : tableEntry.getValue().entrySet()) {
				KVP sourceNode = new KVP("source_id", sourceEntry.getKey());
				sourceNode.addTableSource(() -> getTableModel(sourceEntry.getValue()), "EPG Events (latest complete version)");
				sourceNode.addTableSource(() -> getAllVersionsTableModel(sourceEntry.getValue()), "EPG Events (all versions)");
				tableNode.add(sourceNode);
				Map<Integer, ATSCEITsection[]> versionSections = getVersionSections(sourceEntry.getValue());
				if (Utils.simpleModus(modus)) {
					Entry<Integer, ATSCEITsection[]> latestCompleteVersion = getLatestCompleteVersionEntry(versionSections);
					if (latestCompleteVersion != null) {
						addVersionToJTree(sourceNode, sourceEntry.getValue(), latestCompleteVersion, modus);
					}
				} else {
					for (Entry<Integer, ATSCEITsection[]> versionEntry : versionSections.entrySet()) {
						addVersionToJTree(sourceNode, sourceEntry.getValue(), versionEntry, modus);
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
			addLatestCompleteSectionsToTableModel(tableModel, sources);
		}
		tableModel.process();
		return tableModel;
	}

	public TableModel getAllVersionsTableModel() {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		for (TreeMap<Integer, ATSCEITsection[]> sources : tables.values()) {
			addAllVersionSectionsToTableModel(tableModel, sources);
		}
		tableModel.process();
		return tableModel;
	}

	private static TableModel getTableModel(final TreeMap<Integer, ATSCEITsection[]> sources) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		addLatestCompleteSectionsToTableModel(tableModel, sources);
		tableModel.process();
		return tableModel;
	}

	private static TableModel getAllVersionsTableModel(final TreeMap<Integer, ATSCEITsection[]> sources) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		addAllVersionSectionsToTableModel(tableModel, sources);
		tableModel.process();
		return tableModel;
	}

	private static TableModel getTableModel(final ATSCEITsection[] sections) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		Entry<Integer, ATSCEITsection[]> latestCompleteVersion = getLatestCompleteVersionEntry(getVersionSections(sections));
		if (latestCompleteVersion != null) {
			addSectionsToTableModel(tableModel, latestCompleteVersion.getValue());
		}
		tableModel.process();
		return tableModel;
	}

	private static TableModel getAllVersionsTableModel(final ATSCEITsection[] sections) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		for (ATSCEITsection[] sectionsForVersion : getVersionSections(sections).values()) {
			addSectionsToTableModel(tableModel, sectionsForVersion);
		}
		tableModel.process();
		return tableModel;
	}

	private static TableModel getVersionTableModel(final ATSCEITsection[] sections, final int version) {
		FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel = new FlexTableModel<>(ATSCEITsection.buildEitTableHeader());
		addSectionsToTableModel(tableModel, getVersionSections(sections).get(version));
		tableModel.process();
		return tableModel;
	}

	private static void addLatestCompleteSectionsToTableModel(final FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel,
			final TreeMap<Integer, ATSCEITsection[]> sources) {
		for (ATSCEITsection[] sections : sources.values()) {
			Entry<Integer, ATSCEITsection[]> latestCompleteVersion = getLatestCompleteVersionEntry(getVersionSections(sections));
			if (latestCompleteVersion != null) {
				addSectionsToTableModel(tableModel, latestCompleteVersion.getValue());
			}
		}
	}

	private static void addAllVersionSectionsToTableModel(final FlexTableModel<ATSCEITsection, ATSCEITsection.Event> tableModel,
			final TreeMap<Integer, ATSCEITsection[]> sources) {
		for (ATSCEITsection[] sections : sources.values()) {
			for (ATSCEITsection[] sectionsForVersion : getVersionSections(sections).values()) {
				addSectionsToTableModel(tableModel, sectionsForVersion);
			}
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

	private static Map<Integer, ATSCEITsection[]> getVersionSections(final ATSCEITsection[] sections) {
		Map<Integer, ATSCEITsection[]> versionSections = new LinkedHashMap<>();
		forEachSectionVersion(sections, section -> {
			int requiredLength = Math.max(section.getSectionLastNumber() + 1, section.getSectionNumber() + 1);
			ATSCEITsection[] sectionsForVersion = versionSections.get(section.getVersion());
			if (sectionsForVersion == null) {
				sectionsForVersion = new ATSCEITsection[requiredLength];
				versionSections.put(section.getVersion(), sectionsForVersion);
			} else if (sectionsForVersion.length < requiredLength) {
				ATSCEITsection[] resizedSections = new ATSCEITsection[requiredLength];
				System.arraycopy(sectionsForVersion, 0, resizedSections, 0, sectionsForVersion.length);
				sectionsForVersion = resizedSections;
				versionSections.put(section.getVersion(), sectionsForVersion);
			}
			sectionsForVersion[section.getSectionNumber()] = section;
		});
		return versionSections;
	}

	private static void forEachSectionVersion(final ATSCEITsection[] sections, final Consumer<ATSCEITsection> consumer) {
		for (ATSCEITsection section : sections) {
			ATSCEITsection sectionVersion = section;
			while (sectionVersion != null) {
				consumer.accept(sectionVersion);
				sectionVersion = (ATSCEITsection) sectionVersion.getNextVersion();
			}
		}
	}

	private static void addVersionToJTree(final KVP kvp, final ATSCEITsection[] sourceSections,
			final Entry<Integer, ATSCEITsection[]> versionEntry, final int modus) {
		String versionLabel = "version " + versionEntry.getKey();
		if (!isCompleteVersion(versionEntry.getValue())) {
			versionLabel += " (incomplete)";
		}
		KVP versionNode = new KVP(versionLabel);
		versionNode.addTableSource(() -> getVersionTableModel(sourceSections, versionEntry.getKey()), "EPG Events");
		for (ATSCEITsection section : versionEntry.getValue()) {
			if (section != null) {
				addSectionToJTree(versionNode, sourceSections, section, modus);
			}
		}
		kvp.add(versionNode);
	}

	private static void addSectionToJTree(final KVP kvp, final ATSCEITsection[] sourceSections,
			final ATSCEITsection section, final int modus) {
		KVP sectionNode = section.getJTreeNode(modus);
		sectionNode.addTableSourceFirst(() -> getVersionTableModel(sourceSections, section.getVersion()),
				"EPG Events (version " + section.getVersion() + ", all sections)");
		kvp.add(sectionNode);
	}

	private static Entry<Integer, ATSCEITsection[]> getLatestCompleteVersionEntry(
			final Map<Integer, ATSCEITsection[]> versionSections) {
		Entry<Integer, ATSCEITsection[]> latestAvailableVersion = null;
		Entry<Integer, ATSCEITsection[]> latestCompleteVersion = null;
		for (Entry<Integer, ATSCEITsection[]> versionEntry : versionSections.entrySet()) {
			latestAvailableVersion = versionEntry;
			if (isCompleteVersion(versionEntry.getValue())) {
				latestCompleteVersion = versionEntry;
			}
		}
		return latestCompleteVersion != null ? latestCompleteVersion : latestAvailableVersion;
	}

	private static boolean isCompleteVersion(final ATSCEITsection[] sectionsForVersion) {
		if ((sectionsForVersion == null) || (sectionsForVersion.length == 0)) {
			return false;
		}
		for (ATSCEITsection section : sectionsForVersion) {
			if ((section == null) || (section.getSectionLastNumber() != (sectionsForVersion.length - 1))) {
				return false;
			}
		}
		return true;
	}

	private int getGpsUtcOffset() {
		if ((parentPSI == null) || parentPSI.getAtsc().getStt().getSttSectionList().isEmpty()) {
			return 0;
		}
		return parentPSI.getAtsc().getStt().getSttSectionList().getFirst().getGpsUtcOffset();
	}
}

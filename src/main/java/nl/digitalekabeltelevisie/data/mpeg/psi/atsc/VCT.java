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
import java.util.function.Consumer;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class VCT<T extends VCTsection> extends AbstractPSITabel {

	private final String label;
	private VCTsection[] sections;

	public VCT(final PSI parentPSI, final String label) {
		super(parentPSI);
		this.label = label;
	}

	public void update(final T section) {
		int requiredLength = Math.max(section.getSectionLastNumber() + 1, section.getSectionNumber() + 1);
		if (sections == null) {
			sections = new VCTsection[requiredLength];
		} else if (sections.length < requiredLength) {
			VCTsection[] resizedSections = new VCTsection[requiredLength];
			System.arraycopy(sections, 0, resizedSections, 0, sections.length);
			sections = resizedSections;
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
		KVP kvp = new KVP(label);
		kvp.addTableSource(this::getTableModel, "Virtual Channels (latest complete version)");
		kvp.addTableSource(this::getAllVersionsTableModel, "Virtual Channels (all versions)");
		Map<Integer, VCTsection[]> versionSections = getVersionSections();
		if (Utils.simpleModus(modus)) {
			Entry<Integer, VCTsection[]> latestCompleteVersion = getLatestCompleteVersionEntry(versionSections);
			if (latestCompleteVersion != null) {
				addVersionToJTree(kvp, latestCompleteVersion, modus);
			}
		} else {
			for (Entry<Integer, VCTsection[]> versionEntry : versionSections.entrySet()) {
				addVersionToJTree(kvp, versionEntry, modus);
			}
		}
		return kvp;
	}

	public VCTsection[] getSections() {
		return sections;
	}

	public VCTsection[] getLatestCompleteSections() {
		Entry<Integer, VCTsection[]> latestCompleteVersion = getLatestCompleteVersionEntry(getVersionSections());
		return latestCompleteVersion == null ? new VCTsection[0] : latestCompleteVersion.getValue();
	}

	public TableModel getTableModel() {
		FlexTableModel<VCTsection, VCTsection.VirtualChannel> tableModel = new FlexTableModel<>(VCTsection.buildVctTableHeader());
		Entry<Integer, VCTsection[]> latestCompleteVersion = getLatestCompleteVersionEntry(getVersionSections());
		if (latestCompleteVersion != null) {
			addSectionsToTableModel(tableModel, latestCompleteVersion.getValue());
		}
		tableModel.process();
		return tableModel;
	}

	public TableModel getAllVersionsTableModel() {
		FlexTableModel<VCTsection, VCTsection.VirtualChannel> tableModel = new FlexTableModel<>(VCTsection.buildVctTableHeader());
		for (VCTsection[] sectionsForVersion : getVersionSections().values()) {
			addSectionsToTableModel(tableModel, sectionsForVersion);
		}
		tableModel.process();
		return tableModel;
	}

	private TableModel getVersionTableModel(final int version) {
		FlexTableModel<VCTsection, VCTsection.VirtualChannel> tableModel = new FlexTableModel<>(VCTsection.buildVctTableHeader());
		addSectionsToTableModel(tableModel, getVersionSections().get(version));
		tableModel.process();
		return tableModel;
	}

	private Map<Integer, VCTsection[]> getVersionSections() {
		Map<Integer, VCTsection[]> versionSections = new LinkedHashMap<>();
		forEachSectionVersion(section -> {
			int requiredLength = Math.max(section.getSectionLastNumber() + 1, section.getSectionNumber() + 1);
			VCTsection[] sectionsForVersion = versionSections.get(section.getVersion());
			if (sectionsForVersion == null) {
				sectionsForVersion = new VCTsection[requiredLength];
				versionSections.put(section.getVersion(), sectionsForVersion);
			} else if (sectionsForVersion.length < requiredLength) {
				VCTsection[] resizedSections = new VCTsection[requiredLength];
				System.arraycopy(sectionsForVersion, 0, resizedSections, 0, sectionsForVersion.length);
				sectionsForVersion = resizedSections;
				versionSections.put(section.getVersion(), sectionsForVersion);
			}
			sectionsForVersion[section.getSectionNumber()] = section;
		});
		return versionSections;
	}

	private void forEachSectionVersion(final Consumer<VCTsection> consumer) {
		if (sections != null) {
			for (VCTsection section : sections) {
				VCTsection sectionVersion = section;
				while (sectionVersion != null) {
					consumer.accept(sectionVersion);
					sectionVersion = (VCTsection) sectionVersion.getNextVersion();
				}
			}
		}
	}

	private void addVersionToJTree(final KVP kvp, final Entry<Integer, VCTsection[]> versionEntry, final int modus) {
		String versionLabel = "version " + versionEntry.getKey();
		if (!isCompleteVersion(versionEntry.getValue())) {
			versionLabel += " (incomplete)";
		}
		KVP versionNode = new KVP(versionLabel);
		versionNode.addTableSource(() -> getVersionTableModel(versionEntry.getKey()), "Virtual Channels");
		for (VCTsection section : versionEntry.getValue()) {
			if (section != null) {
				addSectionToJTree(versionNode, section, modus);
			}
		}
		kvp.add(versionNode);
	}

	private void addSectionToJTree(final KVP kvp, final VCTsection section, final int modus) {
		KVP sectionNode = section.getJTreeNode(modus);
		sectionNode.addTableSourceFirst(() -> getVersionTableModel(section.getVersion()),
				"Virtual Channels (version " + section.getVersion() + ", all sections)");
		kvp.add(sectionNode);
	}

	private static void addSectionsToTableModel(final FlexTableModel<VCTsection, VCTsection.VirtualChannel> tableModel,
			final VCTsection[] sectionsForVersion) {
		if (sectionsForVersion == null) {
			return;
		}
		for (VCTsection section : sectionsForVersion) {
			if (section != null) {
				tableModel.addData(section, section.getVirtualChannels());
			}
		}
	}

	private static Entry<Integer, VCTsection[]> getLatestCompleteVersionEntry(
			final Map<Integer, VCTsection[]> versionSections) {
		Entry<Integer, VCTsection[]> latestAvailableVersion = null;
		Entry<Integer, VCTsection[]> latestCompleteVersion = null;
		for (Entry<Integer, VCTsection[]> versionEntry : versionSections.entrySet()) {
			latestAvailableVersion = versionEntry;
			if (isCompleteVersion(versionEntry.getValue())) {
				latestCompleteVersion = versionEntry;
			}
		}
		return latestCompleteVersion != null ? latestCompleteVersion : latestAvailableVersion;
	}

	private static boolean isCompleteVersion(final VCTsection[] sectionsForVersion) {
		if ((sectionsForVersion == null) || (sectionsForVersion.length == 0)) {
			return false;
		}
		for (VCTsection section : sectionsForVersion) {
			if ((section == null) || (section.getSectionLastNumber() != (sectionsForVersion.length - 1))) {
				return false;
			}
		}
		return true;
	}
}

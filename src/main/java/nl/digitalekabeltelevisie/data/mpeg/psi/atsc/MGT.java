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
import java.util.TreeMap;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class MGT extends AbstractPSITabel {

	private MGTsection mgtSection;

	public MGT(final PSI parentPSI) {
		super(parentPSI);
	}

	public void update(final MGTsection section) {
		if (mgtSection == null) {
			mgtSection = section;
		} else {
			updateSectionVersion(section, mgtSection);
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP kvp = new KVP("MGT");
		kvp.addTableSource(this::getGuideTableModel, "PSIP Guide");
		if (mgtSection != null) {
			addSectionVersionsToJTree(kvp, mgtSection, modus);
		}
		return kvp;
	}

	public MGTsection getMgtSection() {
		MGTsection latest = mgtSection;
		while ((latest != null) && (latest.getNextVersion() != null)) {
			latest = (MGTsection) latest.getNextVersion();
		}
		return latest;
	}

	public TableModel getGuideTableModel() {
		FlexTableModel<MGT, GuideRow> tableModel = new FlexTableModel<>(buildGuideTableHeader());
		tableModel.addData(this, getGuideRows());
		tableModel.process();
		return tableModel;
	}

	private List<GuideRow> getGuideRows() {
		MGTsection latestMgtSection = getMgtSection();
		if (latestMgtSection == null) {
			return List.of();
		}
		return latestMgtSection.getTableTypeEntries().stream()
				.map(entry -> new GuideRow(entry, getParentPSI() == null ? null : getParentPSI().getAtsc()))
				.toList();
	}

	static TableHeader<MGT, GuideRow> buildGuideTableHeader() {
		return new TableHeaderBuilder<MGT, GuideRow>()
				.addRequiredRowColumn("table_type", GuideRow::getTableType, Integer.class)
				.addRequiredRowColumn("description", GuideRow::getDescription, String.class)
				.addRequiredRowColumn("PID", GuideRow::getPid, Integer.class)
				.addRequiredRowColumn("MGT_version", GuideRow::getMgtVersion, Integer.class)
				.addRequiredRowColumn("number_bytes", GuideRow::getNumberBytes, Long.class)
				.addRequiredRowColumn("status", GuideRow::getStatus, String.class)
				.addOptionalRowColumn("parsed_version", GuideRow::getParsedVersion, Integer.class)
				.addRequiredRowColumn("parsed_sections", GuideRow::getParsedSections, Integer.class)
				.addOptionalRowColumn("parsed_items", GuideRow::getParsedItems, Integer.class)
				.addOptionalRowColumn("item_type", GuideRow::getItemType, String.class)
				.addOptionalRowColumn("target", GuideRow::getTarget, String.class)
				.addOptionalRowColumn("descriptors_length", GuideRow::getDescriptorsLength, Integer.class)
				.build();
	}

	public static class GuideRow {

		private final MGTsection.TableTypeEntry entry;
		private final ATSCTables atscTables;
		private final int parsedSections;
		private final Integer parsedVersion;
		private final Integer parsedItems;
		private final String itemType;
		private final String target;

		GuideRow(final MGTsection.TableTypeEntry entry, final ATSCTables atscTables) {
			this.entry = entry;
			this.atscTables = atscTables;
			GuideStats stats = getGuideStats(entry.getTableType(), atscTables);
			parsedSections = stats.sections();
			parsedVersion = stats.version();
			parsedItems = stats.items();
			itemType = stats.itemType();
			target = stats.target();
		}

		public int getTableType() {
			return entry.getTableType();
		}

		public String getDescription() {
			return MGTsection.getTableTypeDescription(entry.getTableType());
		}

		public int getPid() {
			return entry.getTableTypePid();
		}

		public int getMgtVersion() {
			return entry.getTableTypeVersionNumber();
		}

		public long getNumberBytes() {
			return entry.getNumberBytes();
		}

		public String getStatus() {
			if (atscTables == null) {
				return "declared";
			}
			if ((entry.getTableType() == 0x0001) || (entry.getTableType() == 0x0003)) {
				return "not tracked separately";
			}
			if (parsedSections == 0) {
				return isImplementedTableType(entry.getTableType()) ? "declared, not parsed" : "not implemented";
			}
			if (parsedVersion == null || parsedVersion == getMgtVersion()) {
				return "parsed";
			}
			return "parsed (version " + parsedVersion + ", MGT " + getMgtVersion() + ")";
		}

		public Integer getParsedVersion() {
			return parsedVersion;
		}

		public int getParsedSections() {
			return parsedSections;
		}

		public Integer getParsedItems() {
			return parsedItems;
		}

		public String getItemType() {
			return itemType;
		}

		public String getTarget() {
			return target;
		}

		public int getDescriptorsLength() {
			return entry.getTableTypeDescriptorsLength();
		}
	}

	private record GuideStats(int sections, Integer version, Integer items, String itemType, String target) {
	}

	private static GuideStats getGuideStats(final int tableType, final ATSCTables atscTables) {
		if (atscTables == null) {
			return new GuideStats(0, null, null, null, getTarget(tableType));
		}
		if (tableType == 0x0000) {
			return getVctStats(atscTables.getTvct(), "channels", getTarget(tableType));
		}
		if (tableType == 0x0002) {
			return getVctStats(atscTables.getCvct(), "channels", getTarget(tableType));
		}
		if ((0x0100 <= tableType) && (tableType <= 0x017F)) {
			return getEitStats(atscTables.getEit(), tableType);
		}
		if ((tableType == 0x0004) || ((0x0200 <= tableType) && (tableType <= 0x027F))) {
			return getEttStats(atscTables.getEtt(), tableType);
		}
		if ((0x0301 <= tableType) && (tableType <= 0x03FF)) {
			return getRrtStats(atscTables.getRrt(), tableType);
		}
		return new GuideStats(0, null, null, null, getTarget(tableType));
	}

	private static GuideStats getVctStats(final VCT<? extends VCTsection> vct, final String itemType,
			final String target) {
		VCTsection[] sections = vct.getLatestCompleteSections();
		int parsedSections = 0;
		int items = 0;
		Integer version = null;
		for (VCTsection section : sections) {
			if (section != null) {
				parsedSections++;
				items += section.getVirtualChannels().size();
				if (version == null) {
					version = section.getVersion();
				}
			}
		}
		return new GuideStats(parsedSections, version, items, itemType, target);
	}

	private static GuideStats getEitStats(final ATSCEIT eit, final int tableType) {
		TreeMap<Integer, ATSCEITsection[]> sources = eit.getTables().get(tableType);
		int parsedSections = 0;
		Integer version = null;
		if (sources != null) {
			for (ATSCEITsection[] sections : sources.values()) {
				for (ATSCEITsection section : sections) {
					if (section != null) {
						parsedSections++;
						if (version == null) {
							version = section.getVersion();
						}
					}
				}
			}
		}
		int events = eit.getEventsBySource(tableType).values().stream().mapToInt(List::size).sum();
		return new GuideStats(parsedSections, version, events, "events", getTarget(tableType));
	}

	private static GuideStats getEttStats(final ATSCETT ett, final int tableType) {
		TreeMap<Long, ATSCETTsection[]> etms = ett.getTables().get(tableType);
		int parsedSections = 0;
		int texts = 0;
		Integer version = null;
		if (etms != null) {
			for (ATSCETTsection[] sections : etms.values()) {
				for (ATSCETTsection section : sections) {
					if (section != null) {
						parsedSections++;
						texts++;
						if (version == null) {
							version = section.getVersion();
						}
					}
				}
			}
		}
		return new GuideStats(parsedSections, version, texts, "text sections", getTarget(tableType));
	}

	private static GuideStats getRrtStats(final RRT rrt, final int tableType) {
		int ratingRegion = tableType - 0x0300;
		RRTsection section = rrt.getRatingRegion(ratingRegion);
		if (section == null) {
			return new GuideStats(0, null, 0, "rating values", getTarget(tableType));
		}
		int values = section.getDimensions().stream().mapToInt(d -> d.getRatingValues().size()).sum();
		return new GuideStats(1, section.getVersion(), values, "rating values", getTarget(tableType));
	}

	private static boolean isImplementedTableType(final int tableType) {
		return (0x0000 <= tableType && tableType <= 0x0004)
				|| ((0x0100 <= tableType) && (tableType <= 0x017F))
				|| ((0x0200 <= tableType) && (tableType <= 0x027F))
				|| ((0x0301 <= tableType) && (tableType <= 0x03FF));
	}

	private static String getTarget(final int tableType) {
		if ((tableType == 0x0000) || (tableType == 0x0001)) {
			return "TVCT";
		}
		if ((tableType == 0x0002) || (tableType == 0x0003)) {
			return "CVCT";
		}
		if (tableType == 0x0004) {
			return "ETT / channel ETT";
		}
		if ((0x0100 <= tableType) && (tableType <= 0x017F)) {
			return "EIT-" + (tableType - 0x0100);
		}
		if ((0x0200 <= tableType) && (tableType <= 0x027F)) {
			return "ETT-" + (tableType - 0x0200);
		}
		if ((0x0301 <= tableType) && (tableType <= 0x03FF)) {
			return "RRT-" + (tableType - 0x0300);
		}
		if ((0x1400 <= tableType) && (tableType <= 0x14FF)) {
			return "DCCT-" + (tableType - 0x1400);
		}
		return null;
	}
}

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

import java.util.TreeMap;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class RRT extends AbstractPSITabel {

	private final TreeMap<Integer, RRTsection> sections = new TreeMap<>();

	public RRT(final PSI parentPSI) {
		super(parentPSI);
	}

	public void update(final RRTsection section) {
		int ratingRegion = section.getRatingRegion();
		RRTsection last = sections.get(ratingRegion);
		if (last == null) {
			sections.put(ratingRegion, section);
		} else {
			updateSectionVersion(section, last);
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP kvp = new KVP("RRT");
		kvp.addTableSource(this::getTableModel, "Rating Values");
		for (RRTsection section : sections.values()) {
			addSectionVersionsToJTree(kvp, section, modus);
		}
		return kvp;
	}

	public TreeMap<Integer, RRTsection> getSections() {
		return sections;
	}

	public RRTsection getRatingRegion(final int ratingRegion) {
		return sections.get(ratingRegion);
	}

	public String getRatingDimensionName(final int ratingRegion, final int ratingDimension) {
		RRTsection section = sections.get(ratingRegion);
		if (section == null) {
			return null;
		}
		RRTsection.Dimension dimension = section.getDimension(ratingDimension);
		return dimension == null ? null : dimension.getDimensionName();
	}

	public String getRatingValueText(final int ratingRegion, final int ratingDimension, final int ratingValue) {
		RRTsection section = sections.get(ratingRegion);
		if (section == null) {
			return null;
		}
		RRTsection.Dimension dimension = section.getDimension(ratingDimension);
		if (dimension == null) {
			return null;
		}
		RRTsection.RatingValue value = dimension.getRatingValue(ratingValue);
		return value == null ? null : value.getRatingValueTextString();
	}

	public TableModel getTableModel() {
		FlexTableModel<RRTsection.Dimension, RRTsection.RatingValue> tableModel =
				new FlexTableModel<>(RRTsection.buildRrtTableHeader());
		for (RRTsection section : sections.values()) {
			RRTsection sectionVersion = section;
			while (sectionVersion != null) {
				for (RRTsection.Dimension dimension : sectionVersion.getDimensions()) {
					tableModel.addData(dimension, dimension.getRatingValues());
				}
				sectionVersion = (RRTsection) sectionVersion.getNextVersion();
			}
		}
		tableModel.process();
		return tableModel;
	}
}

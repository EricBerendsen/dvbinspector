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
		if (sections == null) {
			sections = new VCTsection[section.getSectionLastNumber() + 1];
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
		kvp.addTableSource(this::getTableModel, "Virtual Channels (latest version)");
		kvp.addTableSource(this::getAllVersionsTableModel, "Virtual Channels (all versions)");
		if (sections != null) {
			for (VCTsection section : sections) {
				if (section != null) {
					if (Utils.simpleModus(modus)) {
						kvp.add(section.getJTreeNode(modus));
					} else {
						addSectionVersionsToJTree(kvp, section, modus);
					}
				}
			}
		}
		return kvp;
	}

	public VCTsection[] getSections() {
		return sections;
	}

	public TableModel getTableModel() {
		FlexTableModel<VCTsection, VCTsection.VirtualChannel> tableModel = new FlexTableModel<>(VCTsection.buildVctTableHeader());
		if (sections != null) {
			for (VCTsection section : sections) {
				if (section != null) {
					VCTsection latestSection = getLatestVersion(section);
					tableModel.addData(latestSection, latestSection.getVirtualChannels());
				}
			}
		}
		tableModel.process();
		return tableModel;
	}

	public TableModel getAllVersionsTableModel() {
		FlexTableModel<VCTsection, VCTsection.VirtualChannel> tableModel = new FlexTableModel<>(VCTsection.buildVctTableHeader());
		if (sections != null) {
			for (VCTsection section : sections) {
				VCTsection sectionVersion = section;
				while (sectionVersion != null) {
					tableModel.addData(sectionVersion, sectionVersion.getVirtualChannels());
					sectionVersion = (VCTsection) sectionVersion.getNextVersion();
				}
			}
		}
		tableModel.process();
		return tableModel;
	}

	private static VCTsection getLatestVersion(final VCTsection section) {
		VCTsection latestSection = section;
		VCTsection nextSection = (VCTsection) latestSection.getNextVersion();
		while (nextSection != null) {
			latestSection = nextSection;
			nextSection = (VCTsection) latestSection.getNextVersion();
		}
		return latestSection;
	}
}

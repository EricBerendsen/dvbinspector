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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;

public class STT extends AbstractPSITabel {

	private final List<STTsection> sttSectionList = new ArrayList<>();

	public STT(final PSI parentPSI) {
		super(parentPSI);
	}

	public void update(final STTsection section) {
		sttSectionList.add(section);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP kvp = new KVP("STT");
		kvp.addTableSource(this::getTableModel, "System Time");
		for (STTsection sttSection : sttSectionList) {
			kvp.add(sttSection.getJTreeNode(modus));
		}
		return kvp;
	}

	public TableModel getTableModel() {
		FlexTableModel<STTsection, STTsection> tableModel = new FlexTableModel<>(STTsection.buildSttTableHeader());
		for (STTsection section : sttSectionList) {
			tableModel.addData(section, List.of(section));
		}
		tableModel.process();
		return tableModel;
	}

	public List<STTsection> getSttSectionList() {
		return sttSectionList;
	}

	public STTsection getLatestSttSection() {
		if (sttSectionList.isEmpty()) {
			return null;
		}
		return sttSectionList.getLast();
	}
}

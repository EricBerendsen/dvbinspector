/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.LocalTimeOffsetDescriptor;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class TOT extends AbstractPSITabel{

	private final List<TOTsection> totSectionList = new ArrayList<>();

	public TOT(final PSI parent){
		super(parent);
	}

	public void update(final TOTsection section){
		totSectionList.add(section);
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP kvp = new KVP("TOT");
		if(!totSectionList.isEmpty()) {
			kvp.setTableSource(this::getTableModel);
		}
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);

		for (TOTsection toTsection : totSectionList) {
			t.add(toTsection.getJTreeNode(modus));
		}
		return t;
	}

	public List<TOTsection> getTotSectionList() {
		return totSectionList;
	}


	
	static TableHeader<TOTsection,LocalTimeOffsetDescriptor>  buildTotTableHeader() {

		return new TableHeaderBuilder<TOTsection,LocalTimeOffsetDescriptor>().
				addRequiredBaseColumn("UTC_time",totSection -> Utils.getUTCFormattedString(totSection.getUTC_time()), Number.class).

				addOptionalRepeatingGroupedColumn("country_code",
						ltod -> ltod.getOffsetList().
									stream().
									map(LocalTimeOffsetDescriptor.LocalTimeOffset::getCountryCode).
									collect(Collectors.toList()),
						Number.class,
						"offset").

				addOptionalRepeatingGroupedColumn("local_time_offset",
						ltod -> ltod.getOffsetList().
									stream().
									map(LocalTimeOffsetDescriptor.LocalTimeOffset::getLocalOffsetString).
									collect(Collectors.toList()),
						Number.class,
						"offset").

				addOptionalRepeatingGroupedColumn("time_of_change",
						ltod -> ltod.getOffsetList().
									stream().
									map(LocalTimeOffsetDescriptor.LocalTimeOffset::getTimeOfChangeString).
									collect(Collectors.toList()),
						Number.class,
						"offset").

				addOptionalRepeatingGroupedColumn("next_time_offset",
						ltod -> ltod.getOffsetList().
									stream().
									map(LocalTimeOffsetDescriptor.LocalTimeOffset::getNextTimeOffsetString).
									collect(Collectors.toList()),
						Number.class,
						"offset").
				build();
	}
	
	
	public TableModel getTableModel() {
		FlexTableModel<TOTsection,LocalTimeOffsetDescriptor> tableModel =  new FlexTableModel<>(buildTotTableHeader());

		for (TOTsection element : totSectionList) {
			if(element!= null){
				tableModel.addData(element, findGenericDescriptorsInList(element.getDescriptorList(), LocalTimeOffsetDescriptor.class));
			}
		}

		tableModel.process();
		return tableModel;
	}


}

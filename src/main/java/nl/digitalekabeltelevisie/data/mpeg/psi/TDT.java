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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class TDT extends AbstractPSITabel{

	private final List<TDTsection> tdtSectionList = new ArrayList<>();

	public TDT(final PSI parent){
		super(parent);
	}

	public void update(final TDTsection section){
		tdtSectionList.add(section);
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP kvp = new KVP("TDT");
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);
		if(!tdtSectionList.isEmpty()) {
			kvp.setTableSource(this::getTableModel);
		}

		for (TDTsection tdTsection : tdtSectionList) {
			t.add(tdTsection.getJTreeNode(modus));
		}
		return t;
	}

	public List<TDTsection> getTdtSectionList() {
		return tdtSectionList;
	}

	/**
	 * Using TDTsection for base and row type is a hack. Every element needs at least one row to render data.
	 * See we create an artificial list of one TDTsection.
	 * 
	 * @return TableHeader<TDTsection,TDTsection > tableHeader
	 */
	static TableHeader<TDTsection,TDTsection>  buildTdtTableHeader() {

		return new TableHeaderBuilder<TDTsection,TDTsection>().
				addRequiredRowColumn("UTC_time", TDTsection::getUTC_timeString, String.class).
				build();
	}


	
	public TableModel getTableModel() {
		FlexTableModel<TDTsection,TDTsection> tableModel =  new FlexTableModel<>(buildTdtTableHeader());

		for (TDTsection element : tdtSectionList) {
			if(element!= null){
				List<TDTsection> lst = new ArrayList<>();
				lst.add(element);
				tableModel.addData(element, lst);
			}
		}

		tableModel.process();
		return tableModel;
	}


}

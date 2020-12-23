/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.gui;

import javax.swing.*;
import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.util.tablemodel.cellrenderer.StreamTypeTableCellRenderer;

public class TablePanel extends JScrollPane{
	
	JTable table;
	TableColumnAdjuster tca;

	public TablePanel(JTable table) {
		super(table);
		this.table = table;
		
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		table.setDefaultRenderer(StreamTypeTableCellRenderer.class, new StreamTypeTableCellRenderer());
		tca = new TableColumnAdjuster(table);
		tca.adjustColumns();
	}
	

	public void setModel(TableModel tableModel) {
		table.setModel(tableModel);
		tca.adjustColumns();
		table.getRowSorter().toggleSortOrder(0); // sort on first column
		getVerticalScrollBar().setValue(0); // scroll to top
	}

	public TableModel getModel() {
		return table.getModel();
	}

}

package nl.digitalekabeltelevisie.gui;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractNonEditableTableModel extends AbstractTableModel {

	@Override
	public boolean isCellEditable(int row, int col) {
		 return false;
	 }

}

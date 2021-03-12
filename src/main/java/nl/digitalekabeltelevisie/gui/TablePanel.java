/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2020-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.gui.utils.WrapLayout;
import nl.digitalekabeltelevisie.util.tablemodel.cellrenderer.StreamTypeTableCellRenderer;

public class TablePanel extends JPanel implements MouseListener {
	
	private final JTable table;
	private final TableColumnAdjuster tca;

	private final JScrollPane tableScrollPane;
	private final JCheckBox includeHeadersCheckBox;


	public TablePanel(JTable table){
		super(new BorderLayout());
		
		setFocusable(true);
		tableScrollPane = new JScrollPane(table);
		tableScrollPane.addMouseListener(this);
		
		this.table = table;
		table.setFocusable(true);

		JPanel buttonToolbar = new JPanel(new WrapLayout(FlowLayout.LEFT));
		buttonToolbar.addMouseListener(this);

		TableCopyAction copyAction = new TableCopyAction(this, "Copy");
		JButton copyButton = new JButton(copyAction);

		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK);
		getInputMap().put(copyKey, "copy");
		getActionMap().put("copy", copyAction);
		table.getActionMap().put("copy", copyAction);
		buttonToolbar.getActionMap().put("copy", copyAction);

		buttonToolbar.add(copyButton);

		TableSaveAction saveAction = new TableSaveAction(this, "Save As...");
		JButton saveButton = new JButton(saveAction);

		KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK);
		table.getInputMap().put(saveKey, "save");
		table.getActionMap().put("save", saveAction);

		buttonToolbar.add(saveButton);
		
		buttonToolbar.add(Box.createHorizontalStrut(10));

		includeHeadersCheckBox = new JCheckBox("Include Headers in Copy/Save");
		buttonToolbar.add(includeHeadersCheckBox);
		
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		table.setDefaultRenderer(StreamTypeTableCellRenderer.class, new StreamTypeTableCellRenderer());
		tca = new TableColumnAdjuster(table);
		tca.adjustColumns();
		
		
		
		add(buttonToolbar,BorderLayout.PAGE_START);
		add(tableScrollPane,BorderLayout.CENTER);

	}
	

	public void setModel(TableModel tableModel) {
		table.setModel(tableModel);
		tca.adjustColumns();
		table.getRowSorter().toggleSortOrder(0); // sort on first column
		tableScrollPane.getVerticalScrollBar().setValue(0); // scroll to top
	}

	public String getTableAsHtml() {
		
		int[] rows;
        int[] cols;
        
		rows = getRows();
        cols = getColumns();
        
        StringBuilder htmlStr = new StringBuilder();
        htmlStr.append("<html>\n<body>\n<table>\n");

		if (getIncludeHeadersCheckBox().isSelected()) {
			htmlStr.append("<tr>\n");

			for (int col = 0; col < cols.length; col++) {
				String colName = table.getColumnName(col);
				htmlStr.append("  <th>").append(colName).append("</th>\n");
			}
			htmlStr.append("</tr>\n");
		}
		for (int i : rows) {
			htmlStr.append("<tr>\n");
			for (int j : cols) {
				Object obj = table.getValueAt(i, j);
				String val = ((obj == null) ? "" : obj.toString());
				htmlStr.append("  <td>").append(val).append("</td>\n");
			}
			htmlStr.append("</tr>\n");
		}

        htmlStr.append("</table>\n</body>\n</html>");
        return htmlStr.toString();
	}


	private int[] getColumns() {
		int[] cols;
		int colCount = table.getColumnCount();

        cols = new int[colCount];
        for (int counter = 0; counter < colCount; counter++) {
            cols[counter] = counter;
        }
		return cols;
	}


	private int[] getRows() {
		int[] rows;
		rows = table.getSelectedRows();
		
        if (rows.length == 0) { // nothing selected, include all rows
            int rowCount = table.getRowCount();

            rows = new int[rowCount];
            for (int counter = 0; counter < rowCount; counter++) {
                rows[counter] = counter;
            }
        }
		return rows;
	}

	public JTable getTable() {
		return table;
	}


	public JScrollPane getTableScrollPane() {
		return tableScrollPane;
	}


	public JCheckBox getIncludeHeadersCheckBox() {
		return includeHeadersCheckBox;
	}


	@Override
	public void mouseClicked(MouseEvent e) {
	    table.requestFocusInWindow();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// ignore
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// ignore
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// ignore
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// ignore
	}


	public String getTableAsText() {
		
		int[] rows;
	    int[] cols;
	    
		rows = getRows();
	    cols = getColumns();
	    
	    StringBuilder plainStr = new StringBuilder();
	
	
		if (getIncludeHeadersCheckBox().isSelected()) {
			for (int col = 0; col < cols.length; col++) {
				String colName = table.getColumnName(col);
				plainStr.append(colName).append('\t');
			}
			// we want a newline at the end of each line and not a tab
			plainStr.deleteCharAt(plainStr.length() - 1).append('\n');
		}
		for (int i : rows) {
			for (int j : cols) {
				Object obj = table.getValueAt(i, j);
				String val = ((obj == null) ? "" : obj.toString());
				plainStr.append(val).append('\t');
			}
			// we want a newline at the end of each line and not a tab
			plainStr.deleteCharAt(plainStr.length() - 1).append('\n');
		}
	
	    // remove the last newline
	    plainStr.deleteCharAt(plainStr.length() - 1);
	    return plainStr.toString();
	
	}

}

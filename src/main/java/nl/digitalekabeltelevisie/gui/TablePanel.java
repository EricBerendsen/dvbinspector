/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2020-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.table.TableModel;

import com.opencsv.CSVWriter;

import nl.digitalekabeltelevisie.gui.utils.WrapLayout;
import nl.digitalekabeltelevisie.util.tablemodel.cellrenderer.StreamTypeTableCellRenderer;

public class TablePanel extends JPanel{
	

	private static final Logger logger = Logger.getLogger(TablePanel.class.getName());

	private final JTable table;
	private final TableColumnAdjuster tca;

	private final JScrollPane tableScrollPane;
	private final JCheckBox includeHeadersCheckBox;


	public TablePanel(JTable table){
		super(new BorderLayout());
		
		setFocusable(true);
		tableScrollPane = new JScrollPane(table);
		MouseAdapter mouseAdapter = new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
			    table.requestFocusInWindow();
			}

		};
		tableScrollPane.addMouseListener(mouseAdapter);
		
		this.table = table;
		table.setFocusable(true);

		JPanel buttonToolbar = new JPanel(new WrapLayout(FlowLayout.LEFT));
		buttonToolbar.addMouseListener(mouseAdapter);

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
		
		int[] rows = getSelectedRows();
		int columnCount = table.getColumnCount();
		
        StringBuilder htmlStr = new StringBuilder();
        htmlStr.append("<html>\n<body>\n<table>\n");

		if (includeHeadersCheckBox.isSelected()) {
			
			htmlStr.append("<tr>\n");
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				String colName = table.getColumnName(columnIndex);
				htmlStr.append("  <th>").append(colName).append("</th>\n");
			}
			htmlStr.append("</tr>\n");
		}
		for (int row : rows) {
			htmlStr.append("<tr>\n");
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				htmlStr.append("  <td>").append(Objects.toString(table.getValueAt(row, columnIndex), "")).append("</td>\n");
			}
			htmlStr.append("</tr>\n");
		}

        htmlStr.append("</table>\n</body>\n</html>");
        return htmlStr.toString();
	}


	public String getTableAsText() {
		
		int[] rows = getSelectedRows();
		int columnCount = table.getColumnCount();
		
	    StringBuilder plainStr = new StringBuilder();
	
	
		if (includeHeadersCheckBox.isSelected()) {
			for (int col = 0; col < columnCount; col++) {
				String colName = table.getColumnName(col);
				plainStr.append(colName).append('\t');
			}
			// we want a newline at the end of each line and not a tab
			plainStr.deleteCharAt(plainStr.length() - 1).append('\n');
		}
		for (int row : rows) {
			for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
				plainStr.append(Objects.toString(table.getValueAt(row, columnIndex), "")).append('\t');
			}
			// we want a newline at the end of each line and not a tab
			plainStr.deleteCharAt(plainStr.length() - 1).append('\n');
		}
	
	    // remove the last newline
	    plainStr.deleteCharAt(plainStr.length() - 1);
	    return plainStr.toString();
	
	}
	
	public String getTableAsCsv() {

		int[] rows = getSelectedRows();
		String res = "";
		try (StringWriter sw = new StringWriter(); CSVWriter csvWriter = new CSVWriter(sw)) {

			if (includeHeadersCheckBox.isSelected()) {
				csvWriter.writeNext(getHeaders());
			}
			for (int row : rows) {
				csvWriter.writeNext(getRow(row));
			}
			res = sw.toString();
		} catch (IOException e) {
			logger.log(Level.INFO, "error building csv content", e);
		}
		return res;
	}

	private String[] getRow(int row) {
		int columnCount = table.getColumnCount();
		String[] res = new String[columnCount];
		for (int col = 0; col < columnCount; col++) {
			res[col] = Objects.toString(table.getValueAt(row, col), "");
		}
		return res;
	}


	private String[] getHeaders() {
		int columnCount = table.getColumnCount();
		String[] res = new String[columnCount];
		for (int col = 0; col < columnCount; col++) {
			res[col] = table.getColumnName(col);
		}
		return res;
	}


	private int[] getSelectedRows() {
		int[] rows = table.getSelectedRows();
		
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


}

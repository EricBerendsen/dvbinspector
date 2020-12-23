/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.*;
import javax.swing.table.TableModel;

import com.opencsv.CSVWriter;

import org.apache.commons.text.TextStringBuilder;
import org.apache.commons.text.translate.CsvTranslators;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Put image on clipboard
 *
 * @author Eric
 *
 */
public class TableCopyAction extends AbstractAction {

	/**
	 * 
	 */
	private final JPanel panel;
	private TableSource tableSource;

	/**
	 *
	 */
	public TableCopyAction(JPanel panel, String name, TableSource tableSource) {
		super(name);
		this.panel = panel;
		this.tableSource = tableSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		TableModel image = tableSource.getTableModel();
		if (image != null) {
			TableTransferable it = new TableTransferable(image);
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(it, new ClipboardOwner() {
				@Override
				public void lostOwnership(Clipboard clipboard, Transferable contents) {
					System.out.println("You lose :(");
				}
			});
		}
		this.panel.setCursor(Cursor.getDefaultCursor());
	}

	public static class TableTransferable implements Transferable {

		public static final DataFlavor TABLE_DATA_FLAVOR = new DataFlavor(TableModel.class,
				"binary/x-java-tablemodel; class=<javax.swing.TableModel>");
		public static final DataFlavor HTML_DATA_FLAVOR = new DataFlavor("text/html", "HTML");
		public static final DataFlavor CSV_DATA_FLAVOR = new DataFlavor("text/csv", "CVS");
		public static final DataFlavor PLAIN_DATA_FLAVOR = new DataFlavor("text/plain", "Plain text");
		public static final DataFlavor SERIALIZED_DATA_FLAVOR = new DataFlavor(String.class,
				"application/x-java-serialized-object; Plain text");
		private final TableModel model;

		public TableTransferable(TableModel model) {
			this.model = model;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { CSV_DATA_FLAVOR, PLAIN_DATA_FLAVOR };
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			boolean supported = false;
			for (DataFlavor mine : getTransferDataFlavors()) {
				if (mine.equals(flavor)) {
					supported = true;
					break;
				}
			}
			return supported;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			Object data = null;
			if (TABLE_DATA_FLAVOR.equals(flavor)) {
				data = model;
			} else if (HTML_DATA_FLAVOR.equals(flavor)) {
				data = new ByteArrayInputStream(formatAsHTML().getBytes());
			} else if (SERIALIZED_DATA_FLAVOR.equals(flavor)) {
				data = formatAsHTML();
			} else if (CSV_DATA_FLAVOR.equals(flavor)) {
				data = new ByteArrayInputStream(formatAsCsv().getBytes());
			} else if (PLAIN_DATA_FLAVOR.equals(flavor)) {
				data = new ByteArrayInputStream(formatAsCsv().getBytes());
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
			return data;
		}

		private String formatAsCsv() {
			try {
				String[] headers = new String[model.getColumnCount()];
				for (int i = 0; i < model.getColumnCount(); i++) {
					headers[i] = model.getColumnName(i);
				}
				TextStringBuilder build = new TextStringBuilder();
				CSVWriter writer = new CSVWriter(build.asWriter());
				writer.writeNext(headers);
				for (int row = 0; row < model.getRowCount(); row++) {
					String[] line = new String[model.getColumnCount()];
					for (int col = 0; col < model.getColumnCount(); col++) {
						String cell = model.getValueAt(row, col).toString();
						line[col] = cell;
					}
					writer.writeNext(line);
				}
				writer.close();
				return build.toString();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return "";
		}

		private String formatAsHTML() {
			StringBuilder sb = new StringBuilder(128);
			sb.append("<html><body>");
			sb.append("<table>");
			sb.append("<tr>");
			for (int index = 0; index < model.getColumnCount(); index++) {
				sb.append("<th>").append(model.getColumnName(index)).append("</th>");
			}
			sb.append("</tr>");
			for (int rowIndex = 0; rowIndex < model.getRowCount(); rowIndex++) {
				sb.append("<tr>");
				for (int colIndex = 0; colIndex < model.getColumnCount(); colIndex++) {
					Object o = model.getValueAt(rowIndex, colIndex);
					// You will want to format the value...
					String value = o == null ? "" : o.toString();
					sb.append("<td>").append(value).append("</td>");
				}
				sb.append("</tr>");
			}
			sb.append("</table>");

			return sb.toString();
		}
	}

}
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

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

import com.opencsv.CSVWriter;

import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * Start dialog to save image as PNG or JPG.
 *
 * @author Eric
 *
 */
public class TableSaveAction extends AbstractAction {

	/**
	 * 
	 */
	private final JPanel panel;
	private TableSource tableSource;

	/**
	 *
	 */
	public TableSaveAction(JPanel panel, String name, TableSource tableModel) {
		super(name);
		this.panel = panel;
		this.tableSource = tableModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		final TableModel model = tableSource.getTableModel();
		if (model != null) {
			final Preferences prefs = Preferences.userNodeForPackage(DVBtree.class);
			JFileChooser chooser = createFileChooser(prefs);

			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			File saveFile = new File("dvb_inspector_tree_" + df.format(new Date()));
			chooser.setSelectedFile(saveFile);

			int rval = chooser.showSaveDialog(panel);
			if (rval == JFileChooser.APPROVE_OPTION) {
				saveFile = chooser.getSelectedFile();
				PreferencesManager.setSaveDir(saveFile.getParent());

				FileNameExtensionFilter filter = (FileNameExtensionFilter) chooser.getFileFilter();
				String extension = filter.getExtensions()[0];
				if (!saveFile.getName().endsWith('.' + extension)) {
					saveFile = new File(saveFile.getPath() + "." + extension);
				}
				boolean write = true;
				if (saveFile.exists()) {
					final int n = JOptionPane.showConfirmDialog(panel,
							"File " + saveFile + " already exists, want to overwrite?", "File already exists",
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.NO_OPTION) {
						write = false;
					}
				}
				if (write) {
					saveToCsv(model, saveFile);
				}
			}
		}
	}

	private void saveToCsv(TableModel model, File file) {
		String[] headers = new String[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			headers[i] = model.getColumnName(i);
		}
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(file));
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
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static JFileChooser createFileChooser(final Preferences prefs) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("csv", "CSV");
		chooser.addChoosableFileFilter(fileFilter);
		chooser.setAcceptAllFileFilterUsed(false);
		final String defaultDir = PreferencesManager.getSaveDir();
		if (defaultDir != null) {
			final File defDir = new File(defaultDir);
			chooser.setCurrentDirectory(defDir);
		}
		return chooser;
	}
}
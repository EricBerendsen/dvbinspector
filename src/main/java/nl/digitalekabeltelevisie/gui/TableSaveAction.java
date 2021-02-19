/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * @author Eric
 *
 */
public class TableSaveAction extends AbstractAction {
	
	private static final Logger	logger	= Logger.getLogger(TableSaveAction.class.getName());

	
	private static final String TEXT = "TEXT";
	private static final String HTML = "HTML";
	
	final TablePanel tablePanel;

	/**
	 * @param tablePanel
	 * @param string
	 */
	public TableSaveAction(TablePanel tablePanel, String label) {
		super(label);
		this.tablePanel = tablePanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		JFileChooser chooser = createFileChooser();

		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		File saveFile = new File("dvb_inspector_table_" + df.format(new Date()));
		chooser.setSelectedFile(saveFile);

		int rval = chooser.showSaveDialog(tablePanel);
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
				final int n = JOptionPane.showConfirmDialog(tablePanel,
						"File " + saveFile + " already exists, want to overwrite?",
						"File already exists",
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION) {
					write = false;
				}
			}
			if (write) {
				String content;
				if(HTML.equals(filter.getDescription())){
					content = tablePanel.getTableAsHtml();
				}else {
					content = tablePanel.getTableAsText();
				}
				try {
					Files.writeString(saveFile.toPath(), content);
				} catch (IOException e1) {
					logger.warning(()->"IOException while saving; "+e1);
				}
			}
		}
	}
        
	
	private static JFileChooser createFileChooser() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter(HTML, "html", "htm");
		chooser.addChoosableFileFilter(jpgFilter);
		FileNameExtensionFilter pngFilter = new FileNameExtensionFilter(TEXT, "txt", "text");
		chooser.addChoosableFileFilter(pngFilter);
		chooser.setAcceptAllFileFilterUsed(false);
		final String defaultDir = PreferencesManager.getSaveDir();
		if (defaultDir != null) {
			final File defDir = new File(defaultDir);
			chooser.setCurrentDirectory(defDir);
		}
		return chooser;
	}


}

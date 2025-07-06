/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jfree.data.category.CategoryDataset;

import com.opencsv.CSVWriter;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * 
 */
public class BarChartExportAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(BarChartExportAction.class.getName());

	BarChart barchart;

	public BarChartExportAction(String label, BarChart barchart) {
		super(label);
		this.barchart = barchart;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser chooser = GuiUtils.createFileChooser();

		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		File saveFile = new File("dvb_inspector_bitrates_" + df.format(new Date())+".csv");
		chooser.setSelectedFile(saveFile);

		int rval = chooser.showSaveDialog(barchart);
		if (rval == JFileChooser.APPROVE_OPTION) {
			saveFile = chooser.getSelectedFile();
			PreferencesManager.setSaveDir(saveFile.getParent());
			boolean write = true;
			if (saveFile.exists()) {
				final int n = JOptionPane.showConfirmDialog(barchart, "File " + saveFile + " already exists, want to overwrite?",
						"File already exists", JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.NO_OPTION) {
					write = false;
				}
			}
			if (write) {
				String content = getCsv(barchart.getDataSet());
				try {
					Files.writeString(saveFile.toPath(), content);
				} catch (IOException e1) {
					logger.warning(() -> "IOException while saving; " + e1);
				}

			}
		}

	}

	public static String getCsv(CategoryDataset categoryDataset) {

		String res = "";
		try (StringWriter sw = new StringWriter(); CSVWriter csvWriter = new CSVWriter(sw)) {

			csvWriter.writeNext(getHeaders(categoryDataset));
			
			for (int col = 0; col < categoryDataset.getColumnCount(); col++) {
				String[] r = new String[5];
				ChartLabel key = (ChartLabel)categoryDataset.getColumnKey(col);
				
				r[0]= Short.toString(key.getPid());
				r[1]=key.getLabel();
				for(int row = 0; row<3;row++) {
					r[row+2] = Double.toString(categoryDataset.getValue(row, col).doubleValue());
				}
				csvWriter.writeNext(r);
			}
			res = sw.toString();
		} catch (IOException e) {
			logger.log(Level.INFO, "error building csv content", e);
		}
		return res;
	}
	
	private static String[] getHeaders(CategoryDataset categoryDataset) {
		String[] res = new String[5];
		res[0]= "pid";
		res[1]=	"label";
		for(int row = 0; row<3;row++) {
			res[2+row] = ((ChartLabel)categoryDataset.getRowKey(row)).getLabel();
		}
		return res;
	}

}

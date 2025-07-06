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
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.*;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * Start dialog to save image as PNG or JPG.
 *
 * @author Eric
 *
 */
public class ImageSaveAction extends AbstractAction{

	private static final Logger	logger	= Logger.getLogger(ImageSaveAction.class.getName());

	private final JPanel panel;
	private final ImageSource imageSource;

	public ImageSaveAction(JPanel panel, String name, ImageSource imageSource) {
		super(name);
		this.panel = panel;
		this.imageSource = imageSource;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		final BufferedImage image = imageSource.getImage();
		if (image != null) {
			JFileChooser chooser = GuiUtils.createFileChooser(List.of(
					new FileNameExtensionFilter("JPG", "jpg", "jpeg"),
					new FileNameExtensionFilter("PNG", "png")
					));

			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			File saveFile = new File("dvb_inspector_image_" + df.format(new Date()));
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
							"File " + saveFile + " already exists, want to overwrite?",
							"File already exists",
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.NO_OPTION) {
						write = false;
					}
				}
				if (write) {
					try {
						ImageIO.write(image, extension, saveFile);
					} catch (IOException ex) {
						logger.warning(() -> "IOException while saving image; " + ex);
					}
				}
			}
		}
	}

}
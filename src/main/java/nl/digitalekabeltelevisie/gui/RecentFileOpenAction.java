/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import nl.digitalekabeltelevisie.main.DVBinspector;

/**
 * @author Eric
 *
 */
public class RecentFileOpenAction extends AbstractAction {
	


	private DVBinspector controller = null;
	private String fileName;
	 
	public RecentFileOpenAction(final DVBinspector controller, String fileName, int no) {
		super(no + ": " + fileName);
		this.controller = controller;
		this.fileName = fileName;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		final File file = new File(fileName);
		
		if (file.isFile()) {
			final TSLoader tsLoader = new TSLoader(file,controller);
			tsLoader.execute();
		}else {
			JOptionPane.showMessageDialog(controller.getFrame(),
					"File "+fileName + " does not exist (anymore).",
					"Error DVB Inspector",
					JOptionPane.ERROR_MESSAGE);
			controller.removeRecentFile(fileName);
		}
	}

}

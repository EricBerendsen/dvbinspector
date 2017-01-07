/**
 *
 * http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 * This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 *
 * This file is part of DVB Inspector.
 *
 * DVB Inspector is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * DVB Inspector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DVB Inspector. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * The author requests that he be notified of any application, applet, or other binary that makes use of this code, but
 * that's more out of curiosity than anything and is not required.
 *
 */

package nl.digitalekabeltelevisie.gui;

import java.awt.event.ActionEvent;

import javax.swing.*;

import nl.digitalekabeltelevisie.main.DVBinspector;
import nl.digitalekabeltelevisie.util.DefaultMutableTreeNodePreorderEnumaration;

public class FindNextAction extends AbstractAction {

	public FindNextAction(DVBinspector controller) {
		super("Find Next");
		this.controller = controller;
	}

	DVBinspector controller;

	@Override
	public void actionPerformed(ActionEvent e) {

		String searchText = controller.getSearchString();
		DefaultMutableTreeNodePreorderEnumaration enummeration = controller.getSearchEnumeration();
		if ((searchText != null) && (enummeration != null)) {
			boolean found = controller.getTreeView().findAndShow(searchText, enummeration);
			if (!found) {
				JOptionPane.showMessageDialog(controller.getFrame(),
						"No (more) instances of '" + searchText + "' found",
						"No (more) instances of '" + searchText + "' found",
						JOptionPane.INFORMATION_MESSAGE);
				controller.setSearchEnumeration(null);
				controller.setSearchString(null);
				controller.getFindNextAction().setEnabled(false);
			}
		}
	}
}

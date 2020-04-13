/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.main.DVBinspector;
import nl.digitalekabeltelevisie.util.PreferencesManager;

public class SetG0DefaultAction extends AbstractSetPreferenceAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 7867550384009016903L;
	private int default_g0_set=0;

    public SetG0DefaultAction(final DVBinspector controller, final int spec) {
	super(controller);
	default_g0_set = spec;
    }

    public void actionPerformed(final ActionEvent e) {
	PreferencesManager.setDefaultG0CharacterSet(default_g0_set);
	askReloadStream();
    }
}
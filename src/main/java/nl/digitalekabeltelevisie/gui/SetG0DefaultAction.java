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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.main.DVBinspector;

public class SetG0DefaultAction extends AbstractAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 7867550384009016903L;
	private DVBinspector contr;
	private int default_g0_set=0;
	private static final Logger	logger	= Logger.getLogger(SetG0DefaultAction.class.getName());

	public SetG0DefaultAction(final DVBinspector controller, final int spec) {
		super();

		contr=controller;
		default_g0_set=spec;
	}
	public void actionPerformed(final ActionEvent e) {
		final Preferences prefs = Preferences.userNodeForPackage(contr.getClass());
		prefs.putInt(DVBinspector.DEFAULT_G0_CHARACTER_SET, default_g0_set);
		contr.setDefaultG0CharacterSet(default_g0_set);

		final TransportStream ts = contr.getTransportStream();
		if(ts!=null){
			Object[] options = {"Yes, reload stream (may take some time)",
                    "No, setting only takes effect after next load"
                    };
			int n = JOptionPane.showOptionDialog(contr.getFrame(),
				    "For this option to take effect the stream has to be reloaded.\n "
				    + "Do you want to reload the stream now?",
				    "reload stream?",
				    JOptionPane.YES_NO_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[1]); // default to no
			if(n==0){
				try {
					ts.setDefaultG0CharacterSet(default_g0_set);
					// TODO use swingworker to enable progressIndicator (see FileOpenAction)
					//ts.parseStream((JRadioButtonMenuItem)e.getSource());
					ts.parseStream();
					contr.setTransportStream(ts);
					contr.resetSearch();
				} catch (final IOException e1) {
					logger.log (Level.SEVERE,"Error (re)reading transport stream: ",e1);
				}
			}
		}

	}
}
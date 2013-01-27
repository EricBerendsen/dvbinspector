/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2012 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.main.DVBinspector;

public class SetPrivateDataSpecifierAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7867550384009016903L;
	private DVBinspector contr;
	private long specifier=0;
	private static final Logger	logger	= Logger.getLogger(SetPrivateDataSpecifierAction.class.getName());

	public SetPrivateDataSpecifierAction(final DVBinspector controller, final long spec) {
		super();

		contr=controller;
		specifier=spec;
	}
	public void actionPerformed(final ActionEvent e) {
		final TransportStream ts = contr.getTransportStream();
		if(ts!=null){
			final Preferences prefs = Preferences.userNodeForPackage(contr.getClass());
			prefs.putLong(DVBinspector.DEFAULT_PRIVATE_DATA_SPECIFIER, specifier);

			ts.setDefaultPrivateDataSpecifier(specifier);
			contr.setDefaultPrivateDataSpecifier(specifier);
			try {
				ts.parseStream();
				contr.setTransportStream(ts);
			} catch (final IOException e1) {
				logger.log (Level.SEVERE,"Error (re)reading transport stream: ",e1);
			}
		}

	}
}
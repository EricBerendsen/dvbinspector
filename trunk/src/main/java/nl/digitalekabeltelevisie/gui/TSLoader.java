/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.Cursor;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.main.DVBinspector;

public class TSLoader extends SwingWorker<TransportStream, Void>{

	private static final Logger logger = Logger.getLogger(TSLoader.class.getName());
	
	DVBinspector control = null;
	/**
	 * @param file
	 */
	public TSLoader(final File file, DVBinspector controller) {
		super();
		this.file = file;
		this.control = controller;
		this.control.setTransportStream(null);
		this.control.getFrame().repaint();
	}

	File file = null;

	@Override
	protected void done() {
		try {

			final TransportStream ts = get();
			if(ts!=null){
				control.setTransportStream(get());
			}
		} catch (final Throwable t) {
			logger.log(Level.SEVERE, "Error displaying stream", t);
			final String msg =
					"Ooops. \n\n" + "While displaying your stream an error occured " + "from which DVB Inspector can not recover.\n\n" + "Error message: " + t.toString();

			showMessage(msg);

		}finally {
			control.getFrame().setCursor(Cursor.getDefaultCursor());
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected TransportStream doInBackground() throws Exception {
		TransportStream transportStream = null;
		try {
			transportStream = new TransportStream(file);
			transportStream.setDefaultPrivateDataSpecifier(control.getDefaultPrivateDataSpecifier());
			transportStream.setDefaultG0CharacterSet(control.getDefaultG0CharacterSet());
			transportStream.parseStream(control.getFrame());

		} catch (final NotAnMPEGFileException e) {
			logger.log(Level.WARNING, "could not determine packet size stream");
			final String msg =
					"DVB Inspector could not determine packetsize for this file. \n" +
							"DVB Inspector supports packet sizes of 188, 192, 204 and 208 bytes.\n\n " +
					"Are you sure this file contains a valid MPEG Transport Stream?\n\n ";
			showMessage(msg);
			
		} catch (final InterruptedIOException t) {
			logger.log(Level.INFO, "Interrupted while loading stream", t);
			final String msg ="Loading file was interrupted.";
			showMessage(msg);
			
		} catch (final Throwable t) {
			transportStream = null;
			logger.log(Level.WARNING, "error parsing transport stream",t);
			final String improveMsg = GuiUtils.getImproveMsg();
			final String msg =
					"Ooops. \n\n" + "While parsing your stream an error occured " + "from which DVB Inspector can not recover.\n\n" + "Error message: " + t.toString() + "\n\n" + improveMsg;

			showMessage(msg);
		}


		control.getFrame().setCursor(Cursor.getDefaultCursor());

		return transportStream;
	}

	/**
	 * @param msg
	 */
	public void showMessage(final String msg) {
		control.getFrame().setCursor(Cursor.getDefaultCursor());

		JOptionPane.showMessageDialog(control.getFrame(),
				msg,
				"Error DVB Inspector",
				JOptionPane.ERROR_MESSAGE);
	}

}
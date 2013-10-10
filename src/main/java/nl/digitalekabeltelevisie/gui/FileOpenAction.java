/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2013 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InterruptedIOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.main.DVBinspector;

public class FileOpenAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(FileOpenAction.class.getName());

	private static final String DIR = "stream_directory";

	private JFileChooser	fileChooser;
	private JFrame			frame;
	private DVBinspector	contr;


	class TSLoader extends SwingWorker<TransportStream, Void>{

		/**
		 * @param file
		 */
		private TSLoader(File file) {
			super();
			this.file = file;
		}

		File file = null;

	      @Override
	       protected void done() {
	           try {

	        	   TransportStream ts = get();
	        	   if(ts!=null){
	        		   contr.setTransportStream(get());
	        	   }
	           } catch (Exception ignore) {
	           }
	       }

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected TransportStream doInBackground() throws Exception {
			TransportStream transportStream = new TransportStream(file);
			transportStream.setDefaultPrivateDataSpecifier(contr.getDefaultPrivateDataSpecifier());
			transportStream.setEnableTSPackets(contr.isEnableTSPackets());

			try {
				transportStream.parseStream(contr.getFrame());
			} catch (final InterruptedIOException ioe) {
				transportStream = null;
			} catch (final Throwable t) {
				logger.log(Level.WARNING, "error parsing transport stream",t);

				frame.setCursor(Cursor.getDefaultCursor());
				final Package p = getClass().getPackage();
				String version = p.getImplementationVersion();
				if(version==null){
					version="development version (unreleased)";
				}
				transportStream = null;


				JOptionPane.showMessageDialog(frame,
						"Ooops. \n\n" +
						"While parsing your stream an error occured " +
						"from which DVB Inspector can not recover.\n\n" +
						"Error message: "+t.toString()+"\n\n"+
						"You can help to improve DVB Inspector by making this stream available " +
						"to Eric Berendsen\n(e_ber"+"endsen@digitalekabeltel"+"evisie.nl)\n\n" +
						"Please include the version of DVB Inspector: "+version,
						"Error DVB Inspector",
						JOptionPane.ERROR_MESSAGE);
			}


			frame.setCursor(Cursor.getDefaultCursor());

			return transportStream;
		}

	}

	public FileOpenAction(final JFileChooser jf, final JFrame fr, final DVBinspector controller) {
		super();
		fileChooser = jf;
		frame = fr;
		contr = controller;
	}

	public void actionPerformed(final ActionEvent e) {

		final Preferences prefs = Preferences.userNodeForPackage(FileOpenAction.class);

		final String defaultDir = prefs.get(DIR, null);
		if(defaultDir!=null){
			final File defDir = new File(defaultDir);
			fileChooser.setCurrentDirectory(defDir);
		}

		final int returnVal = fileChooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			File file = fileChooser.getSelectedFile();
			prefs.put(DIR,file.getParent());

			TSLoader tsLoader = new TSLoader(file);
			tsLoader.execute();

			frame.setCursor(Cursor.getDefaultCursor());
		}
	}
}
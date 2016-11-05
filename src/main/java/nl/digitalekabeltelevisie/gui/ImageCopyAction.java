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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Put image on clipboard
 *
 * @author Eric
 *
 */
public class ImageCopyAction extends AbstractAction{

	/**
	 * 
	 */
	private final JPanel panel;
	private ImageSource imageSource;
	/**
	 *
	 */
	public ImageCopyAction(JPanel panel, String name, ImageSource imageSource) {
		super(name);
		this.panel = panel;
		this.imageSource = imageSource;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Image image = imageSource.getImage();
		if(image!=null){
			ImageTransferable it = new ImageTransferable(image);
			final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents( it, it );
		}
		this.panel.setCursor(Cursor.getDefaultCursor());
	}
}
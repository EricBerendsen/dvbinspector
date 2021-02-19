/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.Image;
import java.awt.event.ActionEvent;

import javax.swing.*;

import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.Utils;

public class AboutAction extends AbstractAction {
	/**
	 *
	 */
	private static final long serialVersionUID = -6710642864878320650L;
	private final JFrame frame;

	public AboutAction(final JFrame fr) {
		super("About...");
		frame = fr;
	}
	
	@Override
	public void actionPerformed(final ActionEvent e) {
		final String javaVersion=System.getProperty("java.version");
		final String javaVendor=System.getProperty("java.vendor");
		final String javaHome=System.getProperty("java.home");


		final String osName=System.getProperty("os.name");
		final Image image = Utils.readIconImage("magnifying_glass.bmp");
		final ImageIcon icon = new ImageIcon(image);

		JOptionPane.showMessageDialog(frame, "DVB Inspector Version: "+GuiUtils.getVersionString()+"\n\n"+
				"This code is Copyright 2009,2010,2011,2012,2013,2014,2015,2016,2017,2018,2019,2020,2021 by Eric Berendsen\n(e_ber"+"endsen@digitalekabeltel"+"evisie.nl)\n\n"+
				"http://www.digitalekabeltelevisie.nl/dvb_inspector\n\n"+
				"DVB Inspector is free software: you can redistribute it and/or modify \n"+
				"it under the terms of the GNU General Public License as published by\n"+
				"the Free Software Foundation, either version 3 of the License, or\n"+
				"(at your option) any later version.\n\n"+
				"DVB and MHP are registered trademarks of the DVB Project.\n\n"+
				"Java Version: "+javaVersion +"\n"+
				"Java Vendor: "+javaVendor+"\n"+
				"Java Home: "+javaHome+"\n"+
				"OS Name: "+osName+"\n"+
				""
				,
				"About DVB Inspector",
				JOptionPane.PLAIN_MESSAGE,
				icon);

	}
}
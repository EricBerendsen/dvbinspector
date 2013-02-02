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

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;


/**
 *
 */
public class GridView extends JPanel implements TransportStreamView{

	/**
	 *
	 */
	private Grid grid;
	private JScrollPane scrollGrid;

	/**
	 * @param transportStream
	 * @param viewContext
	 */
	public GridView(final TransportStream transportStream, final ViewContext viewContext) {
		super(new GridLayout(1,0));

		grid = new Grid(transportStream,viewContext);
		scrollGrid = new JScrollPane(grid);
		scrollGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//scrollGrid.setPreferredSize(new Dimension(200,200));

		add(scrollGrid);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	public void setTransportStream(final TransportStream transportStream, final ViewContext viewContext) {

		grid.setTransportStream(transportStream,viewContext);
		validate();
		repaint();

	}




}

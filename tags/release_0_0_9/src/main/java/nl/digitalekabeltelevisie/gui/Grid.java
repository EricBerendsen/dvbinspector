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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;


/**
 *
 */
public class Grid extends JPanel implements ComponentListener
{

	/**
	 *
	 */
	private static final long serialVersionUID = 7881015434582215246L;
	private static final int blockW = 12;
	private static final int blockH = 12;

	private int blockPerLine = 100;

	private int lines = 0;

	private TransportStream stream;
	private Map<Short, Paint> colors;
	private int startPacket;
	private int endPacket;
	private int noPackets;


	/**
	 *
	 */
	public Grid(final TransportStream stream, final ViewContext viewContext) {
		super();
		this.addComponentListener(this);

		setTransportStream(stream, viewContext);
	}

	public void setTransportStream(final TransportStream stream, final ViewContext viewContext) {
		this.stream = stream;
		this.colors=new HashMap<Short, Paint>();

		if(stream!=null){
			startPacket = viewContext.getStartPacket();
			endPacket = viewContext.getEndPacket();
			noPackets = endPacket - startPacket;

			lines = (noPackets/blockPerLine);
			if((noPackets%blockPerLine)!=0){
				lines++;
			}

			for(final ChartLabel l:viewContext.getShown()){
				colors.put(l.getPid(), l.getColor());
			}


		}

		// exact text does not matter. getToolTipText overriden. This is only needed to activate tool tips
		setToolTipText("Test");
		revalidate();
	}


	@Override
	public void paintComponent(final Graphics g) {
		setBackground(Color.WHITE);
		super.paintComponent(g);    // paints background
		if(stream!=null){
			for (int i = 0; i < lines; i++) {
				for (int j = 0; j < blockPerLine; j++) {
					final int r = (i*blockPerLine)+j;
					if(r<noPackets){
						final short pid = stream.getPacket_pid(r+startPacket);
						final Color c = (Color)colors.get(pid);
						if(c!=null){
							g.setColor(c);
							g.fillRect(j*blockW, i*blockH, blockW, blockH);
						}
					}
				}
			}
		}
	}
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		int w =0;
		int h = 0;
		if(stream!=null){
			w = blockPerLine * blockW;
			h = lines * blockH;
		}
		return new Dimension(w,h);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */

	@Override
	public String getToolTipText(final MouseEvent e){
		String r=null;
		if(stream!=null){
			final int x=e.getX();
			if((x/blockW)<blockPerLine){ // empty space to the right
				final int y=e.getY();
				final int packetNo = ((y/blockH)*blockPerLine)+(x/blockW);
				if(packetNo<noPackets){
					final int realPacketNo = packetNo +startPacket;
					final short pid = stream.getPacket_pid(realPacketNo);
					if(colors.containsKey(pid)){ // don't care about actual color, just want to know is this pid shown
						r = "Packet: "+realPacketNo+", PID: "+pid+" - "+stream.getShortLabel(pid)+", Time: "+stream.getPacketTime(realPacketNo);
					}
				}
			}
		}
		return r;
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(final ComponentEvent e) {
		// empty block

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(final ComponentEvent e) {
		// empty block

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(final ComponentEvent e) {
		final int wid = (int)getVisibleRect().getWidth();
		blockPerLine = wid/blockW;
		if(stream!=null){
			lines = (noPackets/blockPerLine);
			if((noPackets%blockPerLine)!=0){
				lines++;
			}
		}
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(final ComponentEvent e) {
		repaint();
	}

}

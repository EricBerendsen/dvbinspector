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

import static nl.digitalekabeltelevisie.util.Utils.escapeHtmlBreakLines;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Grid to show individual TS-packets color coded. Mouse over will display PID.
 *
 * @author Eric
 *
 */
public class Grid extends JPanel implements ComponentListener, Scrollable, FocusListener, MouseListener
{
	
	class GridCopyAction extends AbstractAction{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			Point windowLocation = getLocationOnScreen();
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			int x=mouseLocation.x - windowLocation.x;
			int y=mouseLocation.y - windowLocation.y;
			
			final int realPacketNo = getPacketNumber(x,y);
			if(realPacketNo>=0) {
				final short pid = stream.getPacket_pid(realPacketNo);
				if(colors.containsKey(pid)){ // don't care about actual color, just want to know is this pid shown
					String htmlString = getPacketHTML(realPacketNo);
					String plainData = Utils.extractTextFromHTML(htmlString);
					TextHTMLTransferable transferable = new TextHTMLTransferable(plainData, htmlString);
					final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(transferable, null);
				}
			}
		}
	}

	private final static float DASH1[] = {3.0f};
	private final static BasicStroke DASHED =
        new BasicStroke(2.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        3.0f, DASH1, 0.0f);

	/**
	 *
	 */
	private static final long serialVersionUID = 7881015434582215246L;
	private int blockW = 20;
	private int blockH = 20;

	private int gridLines = 0;

	private int blocksPerRow = 100;

	private int lines = 0;

	private TransportStream stream;
	private Map<Short, Paint> colors;
	private int startPacket;
	private int noPacketsInView;
	private boolean showAdaptationField = false;
	private boolean showPayloadStart = false;
	private boolean showErrorIndicator = false;


	/**
	 *
	 */
	public Grid(final TransportStream stream, final ViewContext viewContext) {
		super();
		//setFocusable(true);
	    addMouseListener(this);
	    addFocusListener(this);

		this.addComponentListener(this);

		setTransportStream(stream, viewContext);
		

	}

	public final void setTransportStream(final TransportStream stream, final ViewContext viewContext) {
		this.stream = stream;
		this.colors= new HashMap<>();

		if(stream!=null){
			startPacket = viewContext.getStartPacket();
			int endPacket = viewContext.getEndPacket();
			noPacketsInView = endPacket - startPacket;

			lines = (noPacketsInView/blocksPerRow);
			if((noPacketsInView%blocksPerRow)!=0){
				lines++;
			}

			for(final ChartLabel l:viewContext.getShown()){
				colors.put(l.getPid(), l.getColor());
			}
		}

		// exact text does not matter. getToolTipText overridden. This is only needed to activate tool tips
		setToolTipText("Test");
		GridCopyAction copyAction = new GridCopyAction();

		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK);
		getInputMap().put(copyKey, "copy");
		getActionMap().put("copy", copyAction);

		revalidate();
	}


	@Override
	public void paintComponent(final Graphics g) {

		final Graphics2D graphics2d = (Graphics2D) g;
		graphics2d.setStroke(DASHED);
		setBackground(Color.WHITE);
		super.paintComponent(graphics2d);    // paints background
		if(stream!=null){
			final int startline = graphics2d.getClipBounds().y/blockH;
			final int endLine = startline+1+(graphics2d.getClipBounds().height/blockH) ;
			for (int row = startline; row <= endLine; row++) {
				for (int column = 0; column < blocksPerRow; column++) {
					paintPacket(graphics2d, row, column);
				}
			}
		}
	}

	/**
	 * @param graphics2d
	 * @param row
	 * @param column
	 */
	private void paintPacket(final Graphics2D graphics2d, final int row, final int column) {
		final int packetNo = (row*blocksPerRow)+column;
		if(packetNo<noPacketsInView){
			final short pidFlags = stream.getPacketPidFlags(packetNo+startPacket);
			final short pid = (short) (pidFlags & 0x1fff);
			final Color packetPidColor = (Color)colors.get(pid);
			if(packetPidColor==null){
				paintGridLines(graphics2d, row, column, Color.WHITE);
			}else{
				graphics2d.setColor(packetPidColor);
				graphics2d.fillRect(column*blockW, row*blockH, blockW, blockH);
				paintAdaptationFieldMarker(graphics2d, row, column, pidFlags, packetPidColor);
				paintPayloadStartMarker(graphics2d, row, column, pidFlags, packetPidColor);
				paintErrorFlagMarker(graphics2d, row, column, pidFlags, packetPidColor);
				paintGridLines(graphics2d, row, column, packetPidColor);
			}
		}
	}

	/**
	 * @param g2
	 * @param row
	 * @param column
	 * @param packetPidColor
	 */
	private void paintGridLines(final Graphics2D g2, final int row, final int column, final Color packetPidColor) {
		if(gridLines !=0){
			if(((column%gridLines)==0)&&(column!=0)){
				g2.setColor(getContrastingColor(packetPidColor));
				g2.setStroke(new BasicStroke(2));
				g2.drawLine((column*blockW), (row*blockH)+1, (column*blockW), ((row+1)*blockH)-1);
			}
			if(((row%gridLines)==0)&&(row!=0)){
				g2.setColor(getContrastingColor(packetPidColor));
				g2.setStroke(new BasicStroke(2));
				g2.drawLine((column*blockW), (row*blockH), ((column+1)*blockW)-1, (row*blockH));
			}
		}
	}

	/**
	 * @param g2
	 * @param row
	 * @param column
	 * @param pidFlags
	 * @param packetPidColor
	 */
	private void paintErrorFlagMarker(final Graphics2D g2, final int row, final int column, final short pidFlags,
			final Color packetPidColor) {
		final short errorFlag = (short) (pidFlags & TransportStream.TRANSPORT_ERROR_FLAG);
		if(showErrorIndicator && (errorFlag!=0)){
			g2.setColor(getContrastingColor(packetPidColor));
			g2.setStroke(new BasicStroke(2));

			g2.drawLine((column*blockW)+1, (row*blockH)+1, ((column+1)*blockW)-1, ((row+1)*blockH)-1);
			g2.drawLine((column*blockW)+1, ((row+1)*blockH)-1, ((column+1)*blockW)-1, (row*blockH)+1);
		}
	}

	/**
	 * @param g2
	 * @param row
	 * @param column
	 * @param pidFlags
	 * @param packetPidColor
	 */
	private void paintPayloadStartMarker(final Graphics2D g2, final int row, final int column, final short pidFlags,
			final Color packetPidColor) {
		final short payloadStartFlag = (short) (pidFlags & TransportStream.PAYLOAD_UNIT_START_FLAG);
		if(showPayloadStart && (payloadStartFlag!=0)){
			g2.setColor(getContrastingColor(packetPidColor));
			g2.setStroke(DASHED);

			g2.drawRect((column*blockW)+1, (row*blockH)+1, blockW-2, blockH-2);
		}
	}

	/**
	 * @param g
	 * @param row
	 * @param column
	 * @param pidFlags
	 * @param packetPidColor
	 */
	private void paintAdaptationFieldMarker(final Graphics2D g, final int row, final int column, final short pidFlags,
			final Color packetPidColor) {
		final short adaptationFlag = (short) (pidFlags & TransportStream.ADAPTATION_FIELD_FLAG);
		if(showAdaptationField&& (adaptationFlag!=0)){
			g.setColor(getContrastingColor(packetPidColor));
			g.fillRect((column*blockW)+2, (row*blockH)+2, blockW/2, blockH/2);
		}
	}

	/**
	 * @param c
	 * @return
	 */
	private static Color getContrastingColor(final Color c) {
		if((c.getGreen()+c.getRed()+c.getBlue())<384){
			return Color.WHITE;
		}else{
			return Color.BLACK;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		if(stream==null){
			return new Dimension(0,0);
		}else{
			return new Dimension(blocksPerRow * blockW,lines * blockH);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */

	@Override
	public String getToolTipText(final MouseEvent e){
		
		if(stream!=null){
			final int x=e.getX();
			final int y=e.getY();
			
			final int realPacketNo = getPacketNumber(x,y);
			if(realPacketNo>=0) {
				final short pid = stream.getPacket_pid(realPacketNo);
				if(colors.containsKey(pid)){ // don't care about actual color, just want to know is this pid shown
					return getPacketHTML(realPacketNo);
				}
			}
		}
		return "";
	}

	
	private int getPacketNumber(int x, int y) {
		if((x>=0)&&(y>=0)){
			if((x/blockW)<blocksPerRow){ // empty space to the right
				final int packetNo = ((y/blockH)*blocksPerRow)+(x/blockW);
				if(packetNo<noPacketsInView){
					return packetNo +startPacket;
				}
			}
		}
		return -1;
	}
	/**
	 * @param realPacketNo
	 * @return
	 */
	private String getPacketHTML(final int realPacketNo) {
		final StringBuilder r=new StringBuilder();
		final TSPacket packet = stream.getTSPacket(realPacketNo);
		r.append("<html>");
		if(packet==null){ // no packets loaded, just show pid
			// should not happen now, as packets always should be loaded.
			final short pid = stream.getPacket_pid(realPacketNo);
			r.append("Packet: ").append(realPacketNo);
			r.append("<br>PID: ").append(pid);
			r.append("<br>Time: ").append(stream.getPacketTime(realPacketNo));
			r.append("<br>").append(escapeHtmlBreakLines(stream.getShortLabel(pid)));
		}else{
			r.append(packet.getHTML());
		}
		r.append("</html>");
		return r.toString();
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
		blocksPerRow = wid/blockW;
		if(stream!=null){
			lines = (noPacketsInView/blocksPerRow);
			if((noPacketsInView%blocksPerRow)!=0){
				lines++;
			}
		}
		revalidate();
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(final ComponentEvent e) {
		repaint();
	}

	/**
	 * @param b
	 */
	public void setShowAdaptationField(final boolean b) {
		showAdaptationField = b;
		repaint();

	}

	/**
	 * @param b
	 */
	public void setShowPayloadStart(final boolean b) {
		showPayloadStart = b;
		repaint();

	}

	/**
	 * @param b
	 */
	public void setShowErrorIndicator(final boolean b) {
		showErrorIndicator = b;
		repaint();

	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableUnitIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableUnitIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
		if (orientation == SwingConstants.HORIZONTAL){
			// actual value not relevant, no horizontal scrolling
			return blockW;
		}else{
			// single line
			return blockH;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableBlockIncrement(final Rectangle visibleRect, final int orientation, final int direction) {
		if (orientation == SwingConstants.HORIZONTAL){
			// actual value not relevant, no horizontal scrolling
			return blockW;
		}else{
			// round down to integer number of blocks
			final int h = (int)getVisibleRect().getHeight();
			return h - (h % blockH);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		// We will fit as many blocks on line as Width allows
		return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public void setBlockSize(final int s){
		blockW = s;
		blockH = s;
		componentResized(null);
	}

	public int getGridLines() {
		return gridLines;
	}

	public void setGridLines(final int gridLines) {
		this.gridLines = gridLines;
		repaint();
	}

/*//	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		 //Since the user clicked on us, let us get focus!
	    requestFocusInWindow();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// ignore
	}
}

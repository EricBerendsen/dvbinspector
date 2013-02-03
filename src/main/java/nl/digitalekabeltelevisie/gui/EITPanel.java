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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JPanel;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.psi.EIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection;


/**
 *
 */
public class EITPanel extends JPanel implements ComponentListener
{

	private static final int LINE_HEIGTH = 20;

	private static int legendHeight = 40;

	private int SERVICE_NAME_WIDTH = 150;

	private TransportStream stream;
	HashMap<Integer, EITsection[]> combinedSchedule;

	private EIT eit;
	EITableImage tableImage;
	//private TreeSet<Integer> serviceOrder;
	private int translatedX;
	private int translatedY;
	private int viewWidth;
	private int viewHeight;
	private boolean selectedSchedule = true;

	/**
	 *
	 */
	public EITPanel(final TransportStream stream, final ViewContext viewContext) {
		super();
		this.addComponentListener(this);
		tableImage = new EITableImage();
		tableImage.setmSecP(15*1000L); // default

		setTransportStream(stream, viewContext);
	}

	public void setTransportStream(final TransportStream stream, final ViewContext viewContext) {
		this.stream = stream;

		if(stream!=null){
			eit = stream.getPsi().getEit();
			if(selectedSchedule ){
				combinedSchedule = eit.getCombinedSchedule();
			}else{
				combinedSchedule = eit.getCombinedPresentFollowing();
			}

			tableImage.setEit(eit);
			tableImage.setServicesTableAndOrder(combinedSchedule, new TreeSet<Integer>(combinedSchedule.keySet()));
			//tableImage.setmSecP(15*1000L);
			setSize(tableImage.getDimension());
			repaint();

		}

		//setPreferredSize(getPreferredSize());
		// exact text does not matter. getToolTipText overridden. This is only needed to activate tool tips
		setToolTipText("Test");
		revalidate();

		//setMinimumSize(new Dimension(400,300));
	}


	@Override
	public void paintComponent(final Graphics g) {
		System.out.println("EITPanel paintComponent g:"+g);
		setBackground(Color.BLUE);
		super.paintComponent(g);    // paints background

		Graphics2D gd = (Graphics2D)g;
		gd.setColor(Color.BLACK);


		Rectangle rect = getVisibleRect();
		translatedX = rect.x;
		translatedY = rect.y;
		viewWidth = rect.width;
		viewHeight = rect.height;

		System.out.println("transx: "+translatedX+", translatedY:"+translatedY+", rect "+rect);

		if((tableImage!=null)&&(tableImage.getInterval()!=null)){ // there are services in the EIT
			Date startDate = roundHourDown(tableImage.getInterval().getStart());
			Date endDate = roundHourUp(tableImage.getInterval().getEnd());

			gd.setColor(Color.WHITE);

			final Font font = new Font("SansSerif", Font.PLAIN, 14);
			final Font nameFont = new Font("SansSerif", Font.BOLD, 14);
			gd.setFont(font);

			BasicStroke basicStroke = new BasicStroke( 3.0f);
			gd.setStroke(basicStroke);


			BasicStroke basicStroke1 = new BasicStroke( 1.0f);
			gd.setStroke(basicStroke1);

			int offset = legendHeight;
			int char_descend = 16;
			tableImage.drawLegend(gd, startDate, endDate,SERVICE_NAME_WIDTH,translatedY, legendHeight);
			tableImage.drawActualTime(gd, startDate, SERVICE_NAME_WIDTH, translatedY,legendHeight);

			// draw labels
			tableImage.drawLabels(gd, tableImage.getServiceOrder(), nameFont, translatedX, offset, char_descend);

			gd.setColor(Color.BLUE);
			gd.fillRect(translatedX, translatedY, SERVICE_NAME_WIDTH, legendHeight);

			// draw grid
			offset=legendHeight;
			Graphics2D gd2 = (Graphics2D)gd.create();

			gd2.setFont(font);
			gd2.clipRect(translatedX+SERVICE_NAME_WIDTH, translatedY+legendHeight, viewWidth-SERVICE_NAME_WIDTH, viewHeight- legendHeight);

			SortedSet<Integer> order = tableImage.getServiceOrder();
			for(final Integer serviceNo : order){
				EITsection[] eiTsections = combinedSchedule.get(serviceNo);
				tableImage.drawServiceEvents(gd2, startDate, SERVICE_NAME_WIDTH, offset, char_descend, eiTsections);
				offset+=LINE_HEIGTH;
			}

			gd2.dispose();

			//tableImage.drawLabels(gd, tableImage.getServiceOrder(), nameFont, translatedX, offset, char_descend);

			//tableImage.drawLegend(gd, startDate, endDate, ERROR, ALLBITS, ABORT, WIDTH, HEIGHT);

		}else{
			gd.setColor(Color.WHITE);
			final Font nameFont = new Font("SansSerif", Font.BOLD, 14);
			gd.setFont(nameFont);
			gd.drawString("No EIT present (or empty)", 20, 20);
		}

	}
	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getPreferredScrollableViewportSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		System.out.println("EITPanel getPreferredSize");
		if(tableImage!=null){
			return tableImage.getDimension();
		}
		// empty
		return new Dimension(200,200);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setToolTipText(java.lang.String)
	 */

	@Override
	public String getToolTipText(final MouseEvent e){
		String r=null;
		if(stream!=null){

			final int x=e.getX();
			final int y=e.getY();
			if((x>(translatedX+SERVICE_NAME_WIDTH))&&
					(y>(translatedY+legendHeight))){
				r = tableImage.getToolTipText(x, y);
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
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(final ComponentEvent e) {

		System.out.println("EITPanel componentResized");
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(final ComponentEvent e) {
		System.out.println("EITPanel componentShown");
		repaint();
	}

	public void selectPresentFollowing() {
		combinedSchedule = eit.getCombinedPresentFollowing();
		TreeSet<Integer> serviceOrder = new TreeSet<Integer>(combinedSchedule.keySet());
		tableImage.setServicesTableAndOrder(combinedSchedule,serviceOrder);
		setSize(tableImage.getDimension());
		selectedSchedule = false;

		repaint();


	}

	public void selectSchedule() {
		combinedSchedule = eit.getCombinedSchedule();
		TreeSet<Integer> serviceOrder = new TreeSet<Integer>(combinedSchedule.keySet());
		tableImage.setServicesTableAndOrder(combinedSchedule,serviceOrder);
		setSize(tableImage.getDimension());
		selectedSchedule = true;
		repaint();


	}

	public void setZoom(long l) {
		tableImage.setmSecP(l);
		setSize(tableImage.getDimension());
		repaint();

	}

}

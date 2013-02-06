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
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.JPanel;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ContentDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ContentDescriptor.ContentItem;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtendedEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ParentalRatingDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ParentalRatingDescriptor.Rating;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ShortEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.EIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDTsection;
import nl.digitalekabeltelevisie.util.Interval;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Class to create a grid image of EIT information, like the EPG in decoders.
 *
 *  Can be used from DVBTree as ImageSource, to show the contents of a single tableID (like 0x4E for present/following actual),
 *  or as a JPanel with combined EIT information (current and other streams combined) , complete with scrollbars, mouse overs, zooming, switching between p/f and schedule.
 * @author Eric
 *
 */
public class EITableImage extends JPanel implements ComponentListener,ImageSource{

	private static final String FONT_NAME = "SansSerif";
	private static final int LINE_HEIGHT = 20;
	private static final long DEFAULT_MILLI_SECS_PER_PIXEL = 30*1000;
	private static final int SERVICE_NAME_WIDTH = 150;
	private static final int LEGEND_HEIGHT = 40;


	private EIT eit;
	private long milliSecsPerPixel = DEFAULT_MILLI_SECS_PER_PIXEL;
	private Map<Integer, EITsection[]> servicesTable;
	private SortedSet<Integer> serviceOrder;
	private Interval interval;
	private boolean selectedSchedule = true;

	private static SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");

	private int translatedX;
	private int translatedY;
	private int viewWidth;
	private int viewHeight;



	/**
	 *  Constructor for use from DVBTree, for use as ImageSource
	 *
	 * @param eit
	 * @param table
	 */
	public EITableImage(EIT eit, Map<Integer, EITsection[]> table){
		this.eit = eit;
		this.servicesTable = table;
		serviceOrder = new TreeSet<Integer>(table.keySet());
		this.interval = EIT.getSpanningInterval(serviceOrder, table);
		this.milliSecsPerPixel = DEFAULT_MILLI_SECS_PER_PIXEL;
	}


	/**
	 * Constructor for use from EITPanel, for use as JPanel
	 *
	 * @param stream
	 * @param viewContext
	 */
	public EITableImage(final TransportStream stream, final ViewContext viewContext) {
		super();
		this.addComponentListener(this);
		this.milliSecsPerPixel =15*1000L; // default for Jpanel, zoomlevel = 2

		setTransportStream(stream, viewContext);
		setToolTipText("");
		revalidate();

	}

	/**
	 * To load a new TransportStream, forces update
	 * @param stream
	 * @param viewContext
	 */
	public final void setTransportStream(final TransportStream stream, final ViewContext viewContext) {

		if(stream!=null){
			eit = stream.getPsi().getEit();
			if(selectedSchedule ){
				servicesTable = eit.getCombinedSchedule();
			}else{
				servicesTable = eit.getCombinedPresentFollowing();
			}

			this.serviceOrder = new TreeSet<Integer>(servicesTable.keySet());
			this.interval = EIT.getSpanningInterval(serviceOrder, servicesTable);

			setSize(getDimension());
			repaint();
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.ImageSource#getImage()
	 */
	@Override
	public BufferedImage getImage() {

		// determines selection and order of services to be rendered

		if(interval==null){ // There is a set of EIT sections, but no events found. nothing to draw
			return null;
		}

		// Round up/down to nearest hour
		Date startDate = roundHourDown(interval.getStart());
		Date endDate = roundHourUp(interval.getEnd());

		int height = (serviceOrder.size()*LINE_HEIGHT)+1 + LEGEND_HEIGHT;
		int legendWidth = (int)((endDate.getTime() - startDate.getTime())/milliSecsPerPixel);
		int width = 1+SERVICE_NAME_WIDTH + legendWidth;
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gd = img.createGraphics();
		gd.setColor(Color.BLUE);
		gd.fillRect(0, 0, width, height);
		gd.setColor(Color.WHITE);

		final Font font = new Font(FONT_NAME, Font.PLAIN, 14);
		final Font nameFont = new Font(FONT_NAME, Font.BOLD, 14);
		gd.setFont(font);

		BasicStroke basicStroke = new BasicStroke( 3.0f);
		gd.setStroke(basicStroke);

		drawLegend(gd, startDate, endDate,SERVICE_NAME_WIDTH,0, LEGEND_HEIGHT);
		drawActualTime(gd, startDate, SERVICE_NAME_WIDTH, 0, LEGEND_HEIGHT);

		BasicStroke basicStroke1 = new BasicStroke( 1.0f);
		gd.setStroke(basicStroke1);

		int offset = LEGEND_HEIGHT;
		int char_descend = 16;

		// draw labels
		drawLabels(gd, serviceOrder, nameFont, 0, offset, char_descend);

		// draw grid
		offset=LEGEND_HEIGHT;

		gd.setFont(font);
		for(final Integer serviceNo : serviceOrder){
			EITsection[] eiTsections = servicesTable.get(serviceNo);
			drawServiceEvents(gd, startDate, SERVICE_NAME_WIDTH, offset, char_descend, eiTsections);
			offset+=LINE_HEIGHT;
		}
		return img;
	}


	/**
	 * Draw all events for a single service
	 *
	 * @param gd
	 * @param startDate
	 * @param x
	 * @param y
	 * @param char_descend
	 * @param eiTsections
	 */
	private void drawServiceEvents(final Graphics2D gd, Date startDate, int x, int y, int char_descend,
			EITsection[] eiTsections) {
		for(final EITsection section :eiTsections){
			if(section!= null){
				List<Event> eventList = section.getEventList();
				for(Event event:eventList){
					drawEvent(gd, startDate, event, x, y, char_descend);
				}
			}
		}
	}


	/**
	 * Draw single event
	 *
	 * @param gd
	 * @param startDate
	 * @param event
	 * @param x
	 * @param y
	 * @param char_descend
	 */
	private void drawEvent(final Graphics2D gd, Date startDate, Event event, int x, int y, int char_descend) {
		Date eventStart = getUTCDate( event.getStartTime());

		int w = (int)(getDurationMillis(event.getDuration())/milliSecsPerPixel);
		int eventX = x+(int)((eventStart.getTime()-startDate.getTime())/milliSecsPerPixel);
		String eventName= event.getEventName();

		// FIll gray
		gd.setColor(Color.GRAY);
		gd.fillRect(eventX, y, w, LINE_HEIGHT);

		// black border
		gd.setColor(Color.BLACK);
		gd.drawRect(eventX, y, w, LINE_HEIGHT);
		// title


		Graphics2D gd2 = (Graphics2D)gd.create();
		gd2.clipRect(eventX+5, y, w-10, LINE_HEIGHT);

		gd2.setColor(Color.WHITE);
		gd2.drawString(eventName, eventX+5,y+char_descend);
		gd2.dispose();
	}


	/**
	 * Draws a column with service names at the left of the image
	 * If no name found in SDT put "Service " + service ID.
	 *
	 * @param gd
	 * @param serviceSet
	 * @param nameFont
	 * @param x
	 * @param y
	 * @param char_descend
	 */
	private void drawLabels(final Graphics2D gd, final SortedSet<Integer> serviceSet, final Font nameFont,
			int x,  int y, int char_descend) {
		int labelY = y;
		gd.setFont(nameFont);

		for(final Integer serviceNo : serviceSet){
			String serviceName = this.eit.getParentPSI().getSdt().getServiceName(serviceNo);
			if(serviceName==null){
				serviceName = "Service "+serviceNo;
			}
			gd.setColor(Color.BLUE);
			gd.fillRect(x, labelY, SERVICE_NAME_WIDTH, LINE_HEIGHT);
			gd.setColor(Color.WHITE);
			Graphics2D gd2 = (Graphics2D)gd.create();

			gd2.clipRect(x, labelY, SERVICE_NAME_WIDTH-10, LINE_HEIGHT);
			gd2.drawString(serviceName, x+5, labelY+char_descend);
			gd2.dispose();

			labelY+=LINE_HEIGHT;
		}
	}


	/**
	 * draws a vertical red line in the legend at the location of the 'current' (first TDT) time of the stream.
	 *
	 * @param gd
	 * @param startDate
	 * @param x
	 * @param y
	 * @param legendHeight
	 */
	private void drawActualTime(final Graphics2D gd, Date startDate,int x, int y, int legendHeight) {
		// do we have a current time in the TDT?
		if(this.eit.getParentPSI().getTdt()!=null){
			final List<TDTsection> tdtSectionList  = this.eit.getParentPSI().getTdt().getTdtSectionList();
			if(tdtSectionList.size()>=1){
				final TDTsection first = tdtSectionList.get(0);
				final Date startTime = getUTCCalender(first.getUTC_time()).getTime();
				gd.setColor(Color.RED);
				int labelX = x+(int)((startTime.getTime()-startDate.getTime())/milliSecsPerPixel);
				gd.drawLine(labelX, y, labelX, (y+legendHeight)-1);
			}
		}
	}


	/**
	 * The legend is the black horizontal bar at the top of the image, with the vertical white lines and printed time/date at every hour
	 *
	 * @param gd Graphics2D to draw on
	 * @param startDate
	 * @param endDate
	 * @param x position on image
	 * @param y position on image
	 * @param legendHeight height of bar
	 */
	private void drawLegend(final Graphics2D gd, Date startDate, Date endDate, int x, int y, int legendHeight) {
		gd.setColor(Color.BLACK);
		int w = (int)((endDate.getTime() - startDate.getTime())/milliSecsPerPixel);
		gd.fillRect(x, y, w, legendHeight);

		gd.setColor(Color.WHITE);

		Date hourMark = new Date(startDate.getTime());
		while(hourMark.before(endDate)){
			int labelX = x+(int)((hourMark.getTime()-startDate.getTime())/milliSecsPerPixel);
			gd.drawLine(labelX, y, labelX, (legendHeight-1)+y);
			String timeString =   tf.format(hourMark);
			String dateString =   df.format(hourMark);

			gd.drawString(dateString, labelX+5, y+ 17);
			gd.drawString(timeString, labelX+5, y+ 37);
			hourMark = new Date(hourMark.getTime() + (1000L*60*60)); //advance 1 hour
		}
	}

	/**
	 * Size of image (including labels and legend) to be drawn, depending on interval covered by events, number of services and milliseconds per pixel
	 *
	 * @return
	 */
	public Dimension getDimension(){
		if((eit!=null)&&(interval!=null)){
			// Round up/down to nearest hour
			Date startDate = roundHourDown(interval.getStart());
			Date endDate = roundHourUp(interval.getEnd());

			int legendHeight = 40;
			int height = (serviceOrder.size()*LINE_HEIGHT)+1 + legendHeight;
			int width = 1+SERVICE_NAME_WIDTH + (int)((endDate.getTime() - startDate.getTime())/milliSecsPerPixel);
			return new Dimension(width,height);
		}else{
			return new Dimension(0,0);
		}

	}


	/**
	 * Determines width in pixels of events.
	 * width = event duration (in milliseconds) /  milliSecsPerPixel
	 *
	 * So if event lasts 45 minutes (45*60*1000 = 2700000 milliseconds) and milliSecsPerPixel == 30*1000 (30000)
	 * the event will display with a width of 90 pixels.
	 *
	 * @param milliSecsPerPixel
	 */
	public void setMilliSecsPerPixel(long milliSecsPerPixel) {
		this.milliSecsPerPixel = milliSecsPerPixel;
	}

	@Override
	public String getToolTipText(final MouseEvent e){
		StringBuilder r1=new StringBuilder();
		if((eit!=null)&&(interval!=null)){
			final int x=e.getX();
			final int y=e.getY();
			if((x>(translatedX+SERVICE_NAME_WIDTH))&& // mouse not over labels or legend?
					(y>(translatedY+LEGEND_HEIGHT))){

				int row = (y-LEGEND_HEIGHT)/LINE_HEIGHT;
				if(row<serviceOrder.size()){
					int serviceId = (Integer) serviceOrder.toArray()[row];
					String name = eit.getParentPSI().getSdt().getServiceName(serviceId);
					if(name==null){
						name = "Service "+serviceId;
					}
					r1.append("<html><b>").append(name).append("</b><br>");

					Date thisDate = new Date(roundHourDown(interval.getStart()).getTime()+(milliSecsPerPixel *(x-SERVICE_NAME_WIDTH)));
					Event event = findEvent(serviceId, thisDate);
					if(event!=null){
						addEventDetails(r1, event);
					}else{ // NO event found, just display time
						String timeString =   tf.format(thisDate);
						String dateString =   df.format(thisDate);
						r1.append(dateString).append(" ").append(timeString);
					}
					r1.append("</html>");
				}
			}
		}
		return r1.toString();
	}

	/**
	 * @param r1
	 * @param event
	 */
	private void addEventDetails(StringBuilder r1, Event event) {
		r1.append("Start:&nbsp;").append(Utils.getUTCFormattedString(event.getStartTime())).append("&nbsp;Duration: ");
		r1.append(event.getDuration().substring(0, 2)).append(":");
		r1.append(event.getDuration().substring(2, 4)).append(":");
		r1.append(event.getDuration().substring(4)).append("<br>");
		final List<Descriptor> descList = event.getDescriptorList();
		final List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(descList, ShortEventDescriptor.class);
		if(shortDesc.size()>0){
			r1.append("<br><b><span style=\"background-color: white\">");
			final ShortEventDescriptor shortEventDescriptor = shortDesc.get(0);
			r1.append(Utils.escapeHTML(shortEventDescriptor.getEventName().toString())).append("</span></b><br>");
			String shortText = shortEventDescriptor.getText().toString();
			if((shortText!=null)&&!shortText.isEmpty()){
				r1.append(breakLinesEscapeHtml(shortText)).append("<br>");
			}
		}
		final List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(descList, ExtendedEventDescriptor.class);
		StringBuilder t = new StringBuilder();
		for(final ExtendedEventDescriptor extEvent: extendedDesc){ // no check whether we have all extended event descriptors
			t.append(extEvent.getText().toString());
		}
		String extended = t.toString();
		if(!extended.isEmpty()){
			r1.append("<br>").append(breakLinesEscapeHtml(extended)).append("<br>");
		}
		final List<ContentDescriptor> contentDescList = Descriptor.findGenericDescriptorsInList(descList, ContentDescriptor.class);
		if(!contentDescList.isEmpty()){
			ContentDescriptor contentDesc = contentDescList.get(0);
			List<ContentItem> contentList = contentDesc.getContentList();
			for(ContentItem c:contentList){
				r1.append("<br>Content type: ").append(ContentDescriptor.getContentNibbleLevel1String(c.getContentNibbleLevel1()));
				r1.append(ContentDescriptor.getContentNibbleLevel2String(c.getContentNibbleLevel1(),c.getContentNibbleLevel2())).append("<br>");
			}

		}
		final List<ParentalRatingDescriptor> ratingDescList = Descriptor.findGenericDescriptorsInList(descList, ParentalRatingDescriptor.class);
		if(!ratingDescList.isEmpty()){
			ParentalRatingDescriptor ratingDesc = ratingDescList.get(0);
			List<Rating> ratingList = ratingDesc.getRatingList();
			for(Rating c:ratingList){
				r1.append("<br>Rating: ").append(c.getCountryCode()).append(": ").append(ParentalRatingDescriptor.getRatingTypeAge(c.getRating())).append("<br>");
			}
		}
	}



	private String breakLinesEscapeHtml(String t) {
		 StringTokenizer st = new StringTokenizer(t);
		 int len = 0;
		 StringBuilder res = new StringBuilder();
	     while (st.hasMoreTokens()) {
	         String s = st.nextToken();
	         if((len+s.length())>80){
	        	 res.append("<br>").append(Utils.escapeHTML(s));
	        	 len=s.length();
	         }else{
	        	 res.append(' ').append(Utils.escapeHTML(s));
	        	 len+=1+s.length();
	         }
	     }
		return res.toString();
	}


	/**
	 * Find event in servicesTable, based on serviceID, and date
	 *
	 * @param serviceID
	 * @param date
	 * @return
	 */
	private Event findEvent(int serviceID, Date date){
		EITsection[] list = servicesTable.get(serviceID);
		for(EITsection section:list){
			if(section!=null){
				List<Event> eventList = section.getEventList();
				for(Event event: eventList){
					Date eventStart = getUTCDate( event.getStartTime());
					if(date.after(eventStart)||date.equals(eventStart)){
						Date eventEnd = new Date(eventStart.getTime()+ getDurationMillis(event.getDuration()));
						if(eventEnd.after(date)){
							return event;
						}
					}
				}
			}
		}

		return null;
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
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(final ComponentEvent e) {
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(final ComponentEvent e) {
		repaint();
	}

	/**
	 * Paints the table for the JPanel usage, with the legend and labels always in view.
	 *
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		setBackground(Color.BLUE);
		super.paintComponent(g);    // paints background

		Graphics2D gd = (Graphics2D)g;
		gd.setColor(Color.BLACK);

		Rectangle rect = getVisibleRect();
		translatedX = rect.x;
		translatedY = rect.y;
		viewWidth = rect.width;
		viewHeight = rect.height;

		if((eit!=null)&&(interval!=null)){ // there are services in the EIT
			Date startDate = roundHourDown(interval.getStart());
			Date endDate = roundHourUp(interval.getEnd());

			gd.setColor(Color.WHITE);

			final Font font = new Font(FONT_NAME, Font.PLAIN, 14);
			final Font nameFont = new Font(FONT_NAME, Font.BOLD, 14);
			gd.setFont(font);

			BasicStroke basicStroke = new BasicStroke( 3.0f);
			gd.setStroke(basicStroke);


			BasicStroke basicStroke1 = new BasicStroke( 1.0f);
			gd.setStroke(basicStroke1);

			int offset = LEGEND_HEIGHT;
			int char_descend = 16;
			drawLegend(gd, startDate, endDate,SERVICE_NAME_WIDTH,translatedY, LEGEND_HEIGHT);
			drawActualTime(gd, startDate, SERVICE_NAME_WIDTH, translatedY,LEGEND_HEIGHT);

			// draw labels
			drawLabels(gd, serviceOrder, nameFont, translatedX, offset, char_descend);

			gd.setColor(Color.BLUE);
			gd.fillRect(translatedX, translatedY, SERVICE_NAME_WIDTH, LEGEND_HEIGHT);

			// draw grid
			offset=LEGEND_HEIGHT;
			Graphics2D gd2 = (Graphics2D)gd.create();

			gd2.setFont(font);
			gd2.clipRect(translatedX+SERVICE_NAME_WIDTH, translatedY+LEGEND_HEIGHT, viewWidth-SERVICE_NAME_WIDTH, viewHeight- LEGEND_HEIGHT);

			SortedSet<Integer> order = serviceOrder;
			for(final Integer serviceNo : order){
				EITsection[] eiTsections = servicesTable.get(serviceNo);
				drawServiceEvents(gd2, startDate, SERVICE_NAME_WIDTH, offset, char_descend, eiTsections);
				offset+=LINE_HEIGHT;
			}

			gd2.dispose();

		}else{
			gd.setColor(Color.WHITE);
			final Font nameFont = new Font(FONT_NAME, Font.BOLD, 14);
			gd.setFont(nameFont);
			gd.drawString("No EIT present (or empty)", 20, 20);
		}

	}


	/**
	 *
	 * Causes display to switch to Present/following information for all services in entire EIT.
	 *
	 *  Used for display as JPanel.
	 *
	 */
	public void selectPresentFollowing() {
		selectedSchedule = false;
		if(eit!=null){
			servicesTable = eit.getCombinedPresentFollowing();
			serviceOrder = new TreeSet<Integer>(servicesTable.keySet());
			interval = EIT.getSpanningInterval(serviceOrder, servicesTable);
			setSize(getDimension());
			repaint();
		}
	}

	/**
	 *
	 * Causes display to switch to schedule information for all services in entire EIT.
	 *
	 *  Used for display as JPanel.
	 *
	 */
	public void selectSchedule() {
		selectedSchedule = true;
		if(eit!=null){
			servicesTable = eit.getCombinedSchedule();
			serviceOrder = new TreeSet<Integer>(servicesTable.keySet());
			interval = EIT.getSpanningInterval(serviceOrder, servicesTable);
			setSize(getDimension());
			repaint();
		}
	}

	/**
	 * Determines width of events
	 *
	 * @param l milliseconds per pixel
	 */
	public void setZoom(long l) {
		milliSecsPerPixel = l;
		setSize(getDimension());
		repaint();
	}

	@Override
	public Dimension getPreferredSize() {
		return getDimension();
	}

}
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.getDurationSeconds;
import static nl.digitalekabeltelevisie.util.Utils.getUTCLocalDateTime;
import static nl.digitalekabeltelevisie.util.Utils.isUndefined;
import static nl.digitalekabeltelevisie.util.Utils.roundHourDown;
import static nl.digitalekabeltelevisie.util.Utils.roundHourUp;
import static nl.digitalekabeltelevisie.util.Utils.escapeHTML;

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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.psi.EIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDTsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.atsc.ATSCEITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.atsc.ATSCTables;
import nl.digitalekabeltelevisie.data.mpeg.psi.atsc.STTsection;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.util.Interval;
import nl.digitalekabeltelevisie.util.ServiceIdentification;

/**
 * Class to create a grid image of EIT information, like the EPG in decoders.
 *
 *  Can be used from DVBTree as ImageSource, to show the contents of a single tableID (like 0x4E for present/following actual),
 *  or as a JPanel with combined EIT information (current and other streams combined) , complete with scrollbars, mouse overs, zooming, switching between p/f and schedule.
 * @author Eric
 *
 */
public class EITableImage extends JPanel implements ComponentListener,ImageSource, Scrollable{

	private static final Logger	logger	= Logger.getLogger(EITableImage.class.getName());
	private static final String FONT_NAME = "SansSerif";
	private static final int LINE_HEIGHT = 20;
	private static final long DEFAULT_MILLI_SECS_PER_PIXEL = 30*1000;
	private static final int SERVICE_NAME_WIDTH = 150;
	private static final int LEGEND_HEIGHT = 40;
	private static final String TIME_FORMAT = "%1$tH:%1$tM:%1$tS";
	private static final String DATE_FORMAT = "%1$tY/%1$tm/%1$td";


	private EIT eit;
	private TransportStream transportStream;
	private boolean atscMode;
	private long milliSecsPerPixel = DEFAULT_MILLI_SECS_PER_PIXEL;
	private Map<ServiceIdentification, EITsection[]> servicesTable = null;
	private Map<ServiceIdentification, List<AtscEitEvent>> atscServicesTable = Map.of();
	private Map<ServiceIdentification, String> serviceNames = Map.of();
	private SortedSet<ServiceIdentification> serviceOrder = null;
	private Interval interval;
	private boolean selectedSchedule = true;

	private int translatedX = 0;
	private int translatedY = 0;


	/**
	 *  Constructor for use from DVBTree, for use as ImageSource
	 *
	 * @param eit
	 * @param table
	 */
	public EITableImage(EIT eit, Map<ServiceIdentification, EITsection[]> table){
		this.eit = eit;
		this.servicesTable = table;
		serviceOrder = new TreeSet<>(table.keySet());
		this.interval = EIT.getSpanningInterval(serviceOrder, table);
		this.milliSecsPerPixel = DEFAULT_MILLI_SECS_PER_PIXEL;
	}

	/**
	 * Constructor for use from DVBTree, for ATSC EIT ImageSource detail views.
	 *
	 * @param atsc
	 * @param sourceEvents
	 */
	public EITableImage(ATSCTables atsc, Map<Integer, List<ATSCEITsection.Event>> sourceEvents){
		this.milliSecsPerPixel = DEFAULT_MILLI_SECS_PER_PIXEL;
		loadAtscEit(atsc, getTransportStreamId(atsc), sourceEvents);
	}


	/**
	 * Constructor for use from EITPanel, for use as JPanel
	 *
	 * @param stream
     */
	public EITableImage(TransportStream stream) {
        this.addComponentListener(this);
		this.milliSecsPerPixel =15*1000L; // default for Jpanel, zoomlevel = 2

		setTransportStream(stream);
		setToolTipText("");
		revalidate();

	}

	/**
	 * To load a new TransportStream, forces update
	 * @param stream
     */
	public final void setTransportStream(TransportStream stream) {

		transportStream = stream;
		atscMode = false;
		atscServicesTable = Map.of();
		serviceNames = Map.of();
		if(stream!=null){
			eit = stream.getPsi().getEit();
			if(selectedSchedule ){
				servicesTable = eit.getCombinedSchedule();
			}else{
				servicesTable = eit.getCombinedPresentFollowing();
			}

			if (hasDvbEvents(servicesTable)) {
				this.serviceOrder = new TreeSet<>(servicesTable.keySet());
				this.interval = EIT.getSpanningInterval(serviceOrder, servicesTable);
			} else if (!loadAtscEit(stream.getPsi().getAtsc(), getTransportStreamId(stream),
					stream.getPsi().getAtsc().getEit().getEventsBySource())) {
				this.serviceOrder = new TreeSet<>();
				this.interval = null;
			}
		} else {
			eit = null;
			servicesTable = null;
			serviceOrder = new TreeSet<>();
			interval = null;
		}
		setSize(getDimension());
		repaint();
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
		LocalDateTime startDate = roundHourDown(interval.start());
		LocalDateTime endDate = roundHourUp(interval.end());

		int height = (serviceOrder.size()*LINE_HEIGHT)+1 + LEGEND_HEIGHT;
		int legendWidth = (int) (startDate.until(endDate,ChronoUnit.SECONDS) *1000L /milliSecsPerPixel);
		int width = 1+SERVICE_NAME_WIDTH + legendWidth;
		
		long size = (long)width * height;
		if (size > Integer.MAX_VALUE) {
			return GuiUtils.getErrorImage("The combination of number of services and time interval\n"+
										"is too large to display.");
		}
		
		
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gd = img.createGraphics();
		gd.setColor(Color.BLUE);
		gd.fillRect(0, 0, width, height);
		gd.setColor(Color.WHITE);

		Font font = new Font(FONT_NAME, Font.PLAIN, 14);
		Font nameFont = new Font(FONT_NAME, Font.BOLD, 14);
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
		gd.setFont(font);
		for(ServiceIdentification serviceNo : serviceOrder){
			drawEventsForService(gd, startDate, SERVICE_NAME_WIDTH, offset, char_descend, serviceNo);
			offset+=LINE_HEIGHT;
		}
		return img;
	}

	private boolean loadAtscEit(final ATSCTables atsc, final int transportStreamId,
			final Map<Integer, List<ATSCEITsection.Event>> sourceEventsBySource) {
		Map<ServiceIdentification, List<AtscEitEvent>> atscTable = new HashMap<>();
		Map<ServiceIdentification, String> names = new HashMap<>();
		SortedSet<ServiceIdentification> order = new TreeSet<>();
		long currentTime = getAtscCurrentTime(atsc);
		for (Map.Entry<Integer, List<ATSCEITsection.Event>> sourceEntry : sourceEventsBySource.entrySet()) {
			Integer sourceId = sourceEntry.getKey();
			List<ATSCEITsection.Event> sourceEvents = sourceEntry.getValue();
			if (!selectedSchedule) {
				sourceEvents = getAtscPresentFollowingEvents(sourceEvents, currentTime);
			}
			List<AtscEitEvent> events = new ArrayList<>();
			for (ATSCEITsection.Event event : sourceEvents) {
				AtscEitEvent atscEvent = toAtscEitEvent(event);
				if (atscEvent != null) {
					events.add(atscEvent);
				}
			}
			if (!events.isEmpty()) {
				ServiceIdentification service = new ServiceIdentification(0, transportStreamId, sourceId);
				atscTable.put(service, events);
				names.put(service, atsc.getChannelNameOptional(sourceId).orElse("Source " + sourceId));
				order.add(service);
			}
		}
		Interval atscInterval = getAtscSpanningInterval(order, atscTable);
		if (atscInterval == null) {
			return false;
		}
		atscMode = true;
		servicesTable = null;
		atscServicesTable = atscTable;
		serviceNames = names;
		serviceOrder = order;
		interval = atscInterval;
		return true;
	}

	private static boolean hasDvbEvents(final Map<ServiceIdentification, EITsection[]> table) {
		if (table == null) {
			return false;
		}
		for (EITsection[] sections : table.values()) {
			if (sections == null) {
				continue;
			}
			for (EITsection section : sections) {
				if ((section != null) && !section.getEventList().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	private static int getTransportStreamId(final TransportStream stream) {
		try {
			return stream.getPsi().getPat().getTransportStreamId();
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private static int getTransportStreamId(final ATSCTables atsc) {
		try {
			return atsc.getParentPSI().getPat().getTransportStreamId();
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private static long getAtscCurrentTime(final ATSCTables atsc) {
		STTsection latestStt = atsc.getStt().getLatestSttSection();
		return latestStt == null ? -1 : latestStt.getSystemTime();
	}

	private static List<ATSCEITsection.Event> getAtscPresentFollowingEvents(
			final List<ATSCEITsection.Event> events, final long currentTime) {
		List<ATSCEITsection.Event> presentFollowing = new ArrayList<>();
		if (currentTime < 0) {
			for (int i = 0; (i < events.size()) && (i < 2); i++) {
				presentFollowing.add(events.get(i));
			}
			return presentFollowing;
		}
		for (ATSCEITsection.Event event : events) {
			long start = event.getStartTime();
			long end = start + event.getLengthInSeconds();
			if ((start <= currentTime) && (currentTime < end)) {
				presentFollowing.add(event);
			} else if (start >= currentTime) {
				presentFollowing.add(event);
			}
			if (presentFollowing.size() == 2) {
				break;
			}
		}
		return presentFollowing;
	}

	private static AtscEitEvent toAtscEitEvent(final ATSCEITsection.Event event) {
		try {
			LocalDateTime start = Instant.parse(event.getUtcStartTimeString()).atZone(ZoneOffset.UTC).toLocalDateTime();
			return new AtscEitEvent(event, start);
		} catch (DateTimeParseException e) {
			logger.log(Level.WARNING, "ATSC EIT event start_time is not a valid UTC instant;", e);
			return null;
		}
	}

	private static Interval getAtscSpanningInterval(final SortedSet<ServiceIdentification> order,
			final Map<ServiceIdentification, List<AtscEitEvent>> table) {
		LocalDateTime start = null;
		LocalDateTime end = null;
		for (ServiceIdentification service : order) {
			for (AtscEitEvent event : table.get(service)) {
				if ((start == null) || event.start().isBefore(start)) {
					start = event.start();
				}
				LocalDateTime eventEnd = event.end();
				if ((end == null) || eventEnd.isAfter(end)) {
					end = eventEnd;
				}
			}
		}
		return start == null ? null : new Interval(start, end);
	}

	private void drawEventsForService(Graphics2D gd, LocalDateTime startDate, int x, int y, int char_descend,
			ServiceIdentification serviceNo) {
		if (atscMode) {
			List<AtscEitEvent> events = atscServicesTable.get(serviceNo);
			if (events != null) {
				for (AtscEitEvent event : events) {
					drawEventBlock(gd, startDate, event.start(), event.durationSeconds(), event.title(), x, y, char_descend);
				}
			}
		} else {
			EITsection[] eiTsections = servicesTable.get(serviceNo);
			drawServiceEvents(gd, startDate, x, y, char_descend, eiTsections);
		}
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
	private void drawServiceEvents(Graphics2D gd, LocalDateTime startDate, int x, int y, int char_descend,
                                   EITsection[] eiTsections) {
		for(EITsection section :eiTsections){
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
	private void drawEvent(Graphics2D gd, LocalDateTime startDate, Event event, int x, int y, int char_descend) {
		byte[] startTime = event.getStartTime();
		if(isUndefined(startTime)){
			return;
		}
		LocalDateTime eventStart = getUTCLocalDateTime(startTime);

		try{
			drawEventBlock(gd, startDate, eventStart, getDurationSeconds(event.getDuration()),
					event.getEventName(), x, y, char_descend);
		}catch(NumberFormatException nfe){
			logger.log(Level.WARNING, "drawEvent: Event.duration is not a valid BCD number;", nfe);

		}
	}

	private void drawEventBlock(Graphics2D gd, LocalDateTime startDate, LocalDateTime eventStart,
			long durationSeconds, String eventName, int x, int y, int char_descend) {
		int w = (int)(durationSeconds*1000L/milliSecsPerPixel);
		int eventX = x+(int)(startDate.until(eventStart, ChronoUnit.MILLIS)/milliSecsPerPixel);

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
		gd2.drawString(eventName == null ? "" : eventName, eventX+5,y+char_descend);
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
	private void drawLabels(Graphics2D gd, SortedSet<ServiceIdentification> serviceSet, Font nameFont,
                            int x, int y, int char_descend) {
		int labelY = y; 
		gd.setFont(nameFont);

		for(ServiceIdentification serviceNo : serviceSet){
			String serviceName = getServiceName(serviceNo);
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
	private void drawActualTime(Graphics2D gd, LocalDateTime startDate, int x, int y, int legendHeight) {
		LocalDateTime sectionStart = getActualTime();
		if(sectionStart!=null){
			gd.setColor(Color.RED);
			int labelX = x+(int)(startDate.until(sectionStart,ChronoUnit.SECONDS) * 1000L/milliSecsPerPixel);
			gd.drawLine(labelX, y, labelX, (y+legendHeight)-1);
		}
	}

	private LocalDateTime getActualTime() {
		if (atscMode && (transportStream != null)) {
			STTsection latestStt = transportStream.getPsi().getAtsc().getStt().getLatestSttSection();
			if (latestStt != null) {
				try {
					return Instant.parse(latestStt.getUtcTimeString()).atZone(ZoneOffset.UTC).toLocalDateTime();
				} catch (DateTimeParseException e) {
					logger.log(Level.WARNING, "ATSC STT UTC_time is not a valid UTC instant;", e);
				}
			}
			return null;
		}
		// do we have a current time in the TDT?
		if((this.eit != null) && (this.eit.getParentPSI().getTdt()!=null)){
			List<TDTsection> tdtSectionList  = this.eit.getParentPSI().getTdt().getTdtSectionList();
			if(!tdtSectionList.isEmpty()){
				TDTsection first = tdtSectionList.getFirst();
				return getUTCLocalDateTime(first.getUTC_time());
			}
		}
		return null;
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
	private void drawLegend(Graphics2D gd, LocalDateTime startDate, LocalDateTime endDate, int x, int y, int legendHeight) {
		gd.setColor(Color.BLACK);
		int w = (int)(startDate.until(endDate,ChronoUnit.SECONDS) *1000L/milliSecsPerPixel);
		gd.fillRect(x, y, w, legendHeight);

		gd.setColor(Color.WHITE);

		LocalDateTime hourMark = startDate;
		while(hourMark.isBefore(endDate)){
			int labelX = x+(int)(startDate.until(hourMark,ChronoUnit.SECONDS) *1000L/milliSecsPerPixel);
			gd.drawLine(labelX, y, labelX, (legendHeight-1)+y);

			String timeString =   String.format(TIME_FORMAT,hourMark);
			String dateString =   String.format(DATE_FORMAT,hourMark);
			gd.drawString(dateString, labelX+5, y+ 17);
			gd.drawString(timeString, labelX+5, y+ 37);
			hourMark = hourMark.plusHours(1);
		}
	}

	/**
	 * Size of image (including labels and legend) to be drawn, depending on interval covered by events, number of services and milliseconds per pixel
	 *
	 * @return
	 */
	public final Dimension getDimension(){
		if(interval!=null){
			// Round up/down to nearest hour
			LocalDateTime startDate = roundHourDown(interval.start());
			LocalDateTime endDate = roundHourUp(interval.end());

			int legendHeight = 40;
			int height = (serviceOrder.size()*LINE_HEIGHT)+1 + legendHeight;
			int width = 1+SERVICE_NAME_WIDTH + (int)(startDate.until(endDate,ChronoUnit.SECONDS) * 1000L/milliSecsPerPixel);
			return new Dimension(width,height);
		}
		return new Dimension(0,0);
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
	public String getToolTipText(MouseEvent e){
		StringBuilder r1=new StringBuilder();
		if(interval!=null){
			int x=e.getX();
			int y=e.getY();
			if( y>(translatedY+LEGEND_HEIGHT)){ // mouse not over legend?

				int row = (y-LEGEND_HEIGHT)/LINE_HEIGHT;
				if(row<serviceOrder.size()){ // not below last line
					r1.append("<html><b>");
					ServiceIdentification serviceIdent = serviceOrder.toArray(new ServiceIdentification[0])[row];

					if(x>(translatedX+SERVICE_NAME_WIDTH)) { // over event line
						String name = getServiceNameHTML(serviceIdent);
						r1.append(name).append("</b><br><br>");
						LocalDateTime thisDate =roundHourDown(interval.start()).plusSeconds(milliSecsPerPixel *(x-SERVICE_NAME_WIDTH) / 1000L);
						if (atscMode) {
							AtscEitEvent event = findAtscEvent(serviceIdent, thisDate);
							if(event!=null){
								r1.append(event.getHTML());
							}else{ // NO event found, just display time
								appendTime(r1, thisDate);
							}
						} else {
							Event event = findEvent(serviceIdent, thisDate);
							if(event!=null){
								r1.append(event.getHTML());
							}else{ // NO event found, just display time
								appendTime(r1, thisDate);
							}
						}
					}else { // over service names
						if (atscMode) {
							r1.append(getServiceNameHTML(serviceIdent));
							r1.append("</b><br><br>transport_stream_id:").
							append(serviceIdent.transportStreamId()).
							append("<br>source_id:").
							append(serviceIdent.serviceId());
						}else{
							r1.append(getDvbServiceNameHTMLForLabel(serviceIdent)).
							append("</b><br><br>original_network_id:").
							append(serviceIdent.originalNetworkId()).
							append("<br>transport_stream_id:").
							append(serviceIdent.transportStreamId()).
							append("<br>service_id:").
							append(serviceIdent.serviceId());
						}
					}
					r1.append("</html>");
				}
			}
		}
		return r1.toString();
	}

	private String getServiceName(final ServiceIdentification serviceNo) {
		if (atscMode) {
			return serviceNames.getOrDefault(serviceNo, "Source " + serviceNo.serviceId());
		}
		return this.eit.
				getParentPSI().
				getSdt().
				getServiceNameDVBString(serviceNo).
				map(DVBString::toString).
				orElse("Service " + serviceNo.serviceId());
	}

	private String getServiceNameHTML(final ServiceIdentification serviceNo) {
		if (atscMode) {
			return escapeHTML(getServiceName(serviceNo));
		}
		return eit.getParentPSI().
				getSdt().
				getServiceNameDVBString(serviceNo).
				map(DVBString::toEscapedHTML).
				orElse("Service "+serviceNo.serviceId());
	}

	private String getDvbServiceNameHTMLForLabel(final ServiceIdentification serviceNo) {
		return eit.getParentPSI().
				getSdt().
				getServiceNameDVBString(serviceNo).
				map(DVBString::toEscapedHTML).
				orElse("[Name not in SDT]<br><br>");
	}

	private static void appendTime(final StringBuilder target, final LocalDateTime thisDate) {
		String timeString = String.format(TIME_FORMAT,thisDate);
		String dateString = String.format(DATE_FORMAT,thisDate);
		target.append(dateString).append("&nbsp;").append(timeString);
	}

	/**
	 * Find event in servicesTable, based on serviceID, and date
	 *
	 * @param serviceID
	 * @param date
	 * @return
	 */
	private Event findEvent(ServiceIdentification serviceID, LocalDateTime date){
		EITsection[] list = servicesTable.get(serviceID);
		for(EITsection section:list){
			if(section!=null){
				List<Event> eventList = section.getEventList();
				for(Event event: eventList){
					byte[] startTime = event.getStartTime();
					if(!isUndefined(startTime)) {
						LocalDateTime eventStart = getUTCLocalDateTime(startTime);
						if(date.isAfter(eventStart)||date.equals(eventStart)){
							LocalDateTime eventEnd = eventStart.plusSeconds(getDurationSeconds(event.getDuration()));
							if(eventEnd.isAfter(date)){
								return event;
							}
						}
					}
				}
			}
		}

		return null;
	}

	private AtscEitEvent findAtscEvent(ServiceIdentification serviceID, LocalDateTime date){
		List<AtscEitEvent> list = atscServicesTable.get(serviceID);
		if (list == null) {
			return null;
		}
		for(AtscEitEvent event: list){
			if(date.isAfter(event.start())||date.equals(event.start())){
				if(event.end().isAfter(date)){
					return event;
				}
			}
		}

		return null;
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
		// empty block

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
		repaint();
	}

	/**
	 * Paints the table for the JPanel usage, with the legend and labels always in view.
	 *
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		setBackground(Color.BLUE);
		super.paintComponent(g);    // paints background

		Graphics2D gd = (Graphics2D)g;
		gd.setColor(Color.BLACK);

		Rectangle rect = getVisibleRect();
		translatedX = rect.x;
		translatedY = rect.y;
		int viewWidth = rect.width;
		int viewHeight = rect.height;

		if(interval!=null){ // there are services in the EIT
			LocalDateTime startDate = roundHourDown(interval.start());
			LocalDateTime endDate = roundHourUp(interval.end());

			gd.setColor(Color.WHITE);

			Font font = new Font(FONT_NAME, Font.PLAIN, 14);
			Font nameFont = new Font(FONT_NAME, Font.BOLD, 14);
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
			Graphics2D gd2 = (Graphics2D)gd.create();

			gd2.setFont(font);
			gd2.clipRect(translatedX+SERVICE_NAME_WIDTH, translatedY+LEGEND_HEIGHT, viewWidth -SERVICE_NAME_WIDTH, viewHeight - LEGEND_HEIGHT);

			SortedSet<ServiceIdentification> order = serviceOrder;
			for(ServiceIdentification serviceNo : order){
				drawEventsForService(gd2, startDate, SERVICE_NAME_WIDTH, offset, char_descend, serviceNo);
				offset+=LINE_HEIGHT;
			}

			gd2.dispose();

		}else{
			gd.setColor(Color.WHITE);
			Font nameFont = new Font(FONT_NAME, Font.BOLD, 14);
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
		if(transportStream!=null){
			setTransportStream(transportStream);
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
		if(transportStream!=null){
			setTransportStream(transportStream);
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
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL){
			// 1 hour
			return (int) ((60 * 60 * 1000) / milliSecsPerPixel);
		}
		// single line
		return LINE_HEIGHT;
	}


	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableBlockIncrement(java.awt.Rectangle, int, int)
	 */
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL){
			//
			int w = (int)getVisibleRect().getWidth()-SERVICE_NAME_WIDTH;
			// round down to integer number of hours, at least 1 hour
			int pixelsHour = (int) ((60 * 60 * 1000) / milliSecsPerPixel);
			return Math.max(pixelsHour, w-(w%pixelsHour));
		}
		int h = (int)getVisibleRect().getHeight()-LEGEND_HEIGHT;
		// round down to integer number of services
		return h-(h%LINE_HEIGHT);
	}


	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportWidth()
	 */
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return interval==null;
	}


	/* (non-Javadoc)
	 * @see javax.swing.Scrollable#getScrollableTracksViewportHeight()
	 */
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return interval==null;
	}

	private record AtscEitEvent(ATSCEITsection.Event event, LocalDateTime start) {

		long durationSeconds() {
			return event.getLengthInSeconds();
		}

		LocalDateTime end() {
			return start.plusSeconds(durationSeconds());
		}

		String title() {
			return event.getTitle();
		}

		String getHTML() {
			StringBuilder html = new StringBuilder();
			html.append("event_id: ").append(event.getEventId()).
					append("<br>start_time: ").append(escapeHTML(event.getUtcStartTimeString())).
					append("<br>duration_sec: ").append(event.getLengthInSeconds()).
					append("<br>title: ").append(escapeHTML(event.getTitle()));
			String extendedText = event.getExtendedText();
			if ((extendedText != null) && !extendedText.isBlank()) {
				html.append("<br>extended_text_message: ").append(escapeHTML(extendedText));
			}
			String contentAdvisory = event.getContentAdvisory();
			if ((contentAdvisory != null) && !contentAdvisory.isBlank()) {
				html.append("<br>content_advisory: ").append(escapeHTML(contentAdvisory));
			}
			html.append("<br>ETM_location: ").append(escapeHTML(event.getEtmLocationString()));
			return html.toString();
		}
	}

}

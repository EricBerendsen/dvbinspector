package nl.digitalekabeltelevisie.gui;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtendedEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ShortEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.EIT;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.data.mpeg.psi.TDTsection;
import nl.digitalekabeltelevisie.util.Interval;
import nl.digitalekabeltelevisie.util.Utils;

public class EITableImage implements ImageSource{

	public static final int LINE_HEIGHT = 20;
	/**
	 *
	 */
	private final EIT eit;
	private long mSecP = 30*1000;
	private static long DEFAULT_MILLI_SECS_PER_PIXEL = 30*1000;
	private static final int SERVICE_NAME_WIDTH = 150;
	private Map<Integer, EITsection[]> servicesTable;
	private SortedSet<Integer> serviceOrder;
	private Interval interval;
	int legendHeight = 40;

	SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
	SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");


	/**
	 * Services are ordered by their SericeID.
	 * @param servicesTable
	 * @param eit TODO
	 */
	public EITableImage(EIT eit, Map<Integer, EITsection[]> table, long pixMilliSec){
		this.eit = eit;
		this.servicesTable = table;
		serviceOrder = new TreeSet<Integer>(table.keySet());
		this.interval = EIT.getSpanningInterval(serviceOrder, table);
		this.mSecP = pixMilliSec;
	}

	public EITableImage(EIT eit, Map<Integer, EITsection[]> table){
		this.eit = eit;
		this.servicesTable = table;
		serviceOrder = new TreeSet<Integer>(table.keySet());
		this.interval = EIT.getSpanningInterval(serviceOrder, table);
		this.mSecP = DEFAULT_MILLI_SECS_PER_PIXEL;
	}

	/**
	 * @param serviceOrder determines order in which EPG services are displayed
	 * @param servicesTable
	 * @param eit TODO
	 */
	public EITableImage(EIT eit, SortedSet<Integer> serviceOrder, Map<Integer, EITsection[]> table){
		this.eit = eit;
		this.servicesTable = table;
		this.serviceOrder = serviceOrder;
		this.interval = EIT.getSpanningInterval(serviceOrder, table);
	}
	@Override
	public BufferedImage getImage() {

		// determines selection and order of services to be rendered

		if(interval==null){ // There is a set of EIT sections, but no events found. Return null
			return null;
		}

		// Round up/down to nearest hour

		Date startDate = roundHourDown(interval.getStart());
		Date endDate = roundHourUp(interval.getEnd());


		int height = (serviceOrder.size()*LINE_HEIGHT)+1 + legendHeight;
		int legendWidth = (int)((endDate.getTime() - startDate.getTime())/mSecP);
		int width = 1+SERVICE_NAME_WIDTH + legendWidth;
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gd = img.createGraphics();
		gd.setColor(Color.BLUE);
		gd.fillRect(0, 0, width, height);
		gd.setColor(Color.WHITE);

		final Font font = new Font("SansSerif", Font.PLAIN, 14);
		final Font nameFont = new Font("SansSerif", Font.BOLD, 14);
		gd.setFont(font);

		BasicStroke basicStroke = new BasicStroke( 3.0f);
		gd.setStroke(basicStroke);

		drawLegend(gd, startDate, endDate,SERVICE_NAME_WIDTH,0, legendHeight);
		drawActualTime(gd, startDate, SERVICE_NAME_WIDTH, 0, legendHeight);

		BasicStroke basicStroke1 = new BasicStroke( 1.0f);
		gd.setStroke(basicStroke1);

		int offset = legendHeight;
		int char_descend = 16;

		// draw labels
		drawLabels(gd, serviceOrder, nameFont, 0, offset, char_descend);

		// draw grid
		offset=legendHeight;

		gd.setFont(font);
		for(final Integer serviceNo : serviceOrder){
			EITsection[] eiTsections = servicesTable.get(serviceNo);
			drawServiceEvents(gd, startDate, SERVICE_NAME_WIDTH, offset, char_descend, eiTsections);
			offset+=LINE_HEIGHT;
		}
		return img;
	}


	public void drawServiceEvents(final Graphics2D gd, Date startDate, int x, int y, int char_descend,
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


	private void drawEvent(final Graphics2D gd, Date startDate, Event event, int x, int y, int char_descend) {
		Date eventStart = getUTCDate( event.getStartTime());

		int w = (int)(getDurationMillis(event.getDuration())/mSecP);
		int eventX = x+(int)((eventStart.getTime()-startDate.getTime())/mSecP);
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


	public void drawLabels(final Graphics2D gd, final SortedSet<Integer> serviceSet, final Font nameFont,
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


	public void drawActualTime(final Graphics2D gd, Date startDate,int x, int y, int legendHeight) {
		// do we have a current time in the TDT?
		if(this.eit.getParentPSI().getTdt()!=null){
			final List<TDTsection> tdtSectionList  = this.eit.getParentPSI().getTdt().getTdtSectionList();
			if(tdtSectionList.size()>=1){
				final TDTsection first = tdtSectionList.get(0);
				final Date startTime = getUTCCalender(first.getUTC_time()).getTime();
				gd.setColor(Color.RED);
				int labelX = x+(int)((startTime.getTime()-startDate.getTime())/mSecP);
				gd.drawLine(labelX, y, labelX, (y+legendHeight)-1);
			}
		}
	}


	public void drawLegend(final Graphics2D gd, Date startDate, Date endDate, int x, int y, int legendHeight) {
		gd.setColor(Color.BLACK);
		int w = (int)((endDate.getTime() - startDate.getTime())/mSecP);
		gd.fillRect(x, y, w, legendHeight);

		gd.setColor(Color.WHITE);

		Date hourMark = new Date(startDate.getTime());
		while(hourMark.before(endDate)){
			int labelX = x+(int)((hourMark.getTime()-startDate.getTime())/mSecP);
			gd.drawLine(labelX, y, labelX, (legendHeight-1)+y);
			String timeString =   tf.format(hourMark);
			String dateString =   df.format(hourMark);

			gd.drawString(dateString, labelX+5, y+ 17);
			gd.drawString(timeString, labelX+5, y+ 37);
			hourMark = new Date(hourMark.getTime() + (1000L*60*60)); //advance 1 hour
		}
	}

	public Dimension getDimension(){
		if(interval!=null){
			// Round up/down to nearest hour
			Date startDate = roundHourDown(interval.getStart());
			Date endDate = roundHourUp(interval.getEnd());

			int legendHeight = 40;
			int height = (serviceOrder.size()*LINE_HEIGHT)+1 + legendHeight;
			int width = 1+SERVICE_NAME_WIDTH + (int)((endDate.getTime() - startDate.getTime())/mSecP);
			return new Dimension(width,height);
		}else{
			return new Dimension(0,0);
		}

	}

	public EIT getEit() {
		return eit;
	}



	public static int getServiceNameWidth() {
		return SERVICE_NAME_WIDTH;
	}

	public Map<Integer, EITsection[]> getTable() {
		return servicesTable;
	}

	public SortedSet<Integer> getServiceOrder() {
		return serviceOrder;
	}

	public Interval getInterval() {
		return interval;
	}

	public long getmSecP() {
		return mSecP;
	}

	public void setmSecP(long mSecP) {
		this.mSecP = mSecP;
	}

	String getToolTipText(int x, int y){
		StringBuilder r = new StringBuilder();
		if(y>legendHeight){
			int row = (y-legendHeight)/LINE_HEIGHT;
			int serviceId = (Integer) serviceOrder.toArray()[row];
			String name = eit.getParentPSI().getSdt().getServiceName(serviceId);
			if(name==null){
				name = "Service "+serviceId;
			}

			r.append("<html><b>").append(name).append("</b>");
			if(x>SERVICE_NAME_WIDTH){
				Date thisDate = new Date(interval.getStart().getTime()+(mSecP *(x-SERVICE_NAME_WIDTH)));

				Event event = findEvent(serviceId, thisDate);
				if(event!=null){
					//r.append("<br><br><b>").append(event.getEventName()).append("</b>");
					r.append("&nbsp;").append(Utils.getUTCFormattedString(event.getStartTime())).append("&nbsp;");
					r.append(event.getDuration()).append("&nbsp;");

					final List<Descriptor> descList = event.getDescriptorList();
					//List<Descriptor> shortDesc = Descriptor.findDescriptorsInList(descList, 0x4D);
					final List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(descList, ShortEventDescriptor.class);
					if(shortDesc.size()>0){
						r.append("<br><b><span style=\"background-color: white\">");
						final ShortEventDescriptor shortEventDescriptor = shortDesc.get(0);
						r.append(Utils.escapeHTML(shortEventDescriptor.getEventName().toString())).append("</span></b><br>");
						r.append(Utils.escapeHTML(shortEventDescriptor.getText().toString()));
					}
					//List<Descriptor> extendedDesc = Descriptor.findDescriptorsInList(descList, 0x4E);
					final List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(descList, ExtendedEventDescriptor.class);
					for(final ExtendedEventDescriptor extEvent: extendedDesc){ // no check whether we have all extended event descriptors
						r.append(Utils.escapeHTML(extEvent.getText().toString()));
					}

				}else{ // NO event found, just display time
					String timeString =   tf.format(thisDate);
					String dateString =   df.format(thisDate);
					r.append("<br>").append(dateString);
					r.append("<br>").append(timeString);

				}

			}
			r.append("</html>");

		}

		return r.toString();

	}

	Event findEvent(int serviceID, Date date){
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

	public Map<Integer, EITsection[]> getServicesTable() {
		return servicesTable;
	}

	public void setServicesTableAndOrder(Map<Integer, EITsection[]> servicesTable, SortedSet<Integer> serviceOrder) {
		this.servicesTable = servicesTable;
		this.serviceOrder = serviceOrder;
		this.interval = EIT.getSpanningInterval(serviceOrder, servicesTable);
	}

}
package nl.digitalekabeltelevisie.data.mpeg.psi;
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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.Interval;


/**
 *
 * Based on 5.2.4 Event Information Table (EIT) of ETSI EN 300 468 V1.11.1 (2010-04)
 *
 * @author Eric
 *
 */
public class EIT extends AbstractPSITabel{

	private final Map<Integer, HashMap<Integer,EITsection []>> eit = new HashMap<Integer, HashMap<Integer, EITsection []>>();


	public class EITableImage implements ImageSource{

		private final long MILLI_SECS_PER_PIXEL = 30*1000;
		private static final int SERVICE_NAME_WIDTH = 150;
		private Map<Integer, EITsection[]> table;
		final SortedSet<Integer> serviceOrder;

		/**
		 * Services are ordered by their SericeID.
		 * @param table
		 */
		public EITableImage(Map<Integer, EITsection[]> table){
			this.table = table;
			serviceOrder = new TreeSet<Integer>(table.keySet());
		}

		/**
		 * @param serviceOrder determines order in which EPG services are displayed
		 * @param table
		 */
		public EITableImage(SortedSet<Integer> serviceOrder, Map<Integer, EITsection[]> table){
			this.table = table;
			this.serviceOrder = serviceOrder;
		}
		@Override
		public BufferedImage getImage() {

			// determines selection and order of services to be rendered

			Interval interval = getSpanningInterval(serviceOrder, table);

			// Round up/down to nearest hour

			Date startDate = roundHourDown(interval.getStart());
			Date endDate = roundHourUp(interval.getEnd());

			int legendHeight = 40;
			int height = (serviceOrder.size()*20)+1 + legendHeight;
			int width = 1+SERVICE_NAME_WIDTH + (int)((endDate.getTime() - startDate.getTime())/MILLI_SECS_PER_PIXEL);
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

			drawLegend(gd, startDate, endDate,MILLI_SECS_PER_PIXEL,SERVICE_NAME_WIDTH, 0, width,legendHeight);
			drawActualTime(gd, startDate, MILLI_SECS_PER_PIXEL, SERVICE_NAME_WIDTH, 0,width,legendHeight);

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
				EITsection[] eiTsections = table.get(serviceNo);
				drawServiceEvents(gd, startDate, MILLI_SECS_PER_PIXEL, SERVICE_NAME_WIDTH, offset, char_descend,eiTsections);
				offset+=20;
			}
			return img;
		}


		private void drawServiceEvents(final Graphics2D gd, Date startDate, long mSecsPixel, int x, int y,
				int char_descend,EITsection[] eiTsections) {
			for(final EITsection section :eiTsections){
				if(section!= null){
					List<Event> eventList = section.getEventList();
					for(Event event:eventList){
						drawEvent(gd, startDate, mSecsPixel, event, x, y, char_descend);
					}
				}
			}
		}


		private void drawEvent(final Graphics2D gd, Date startDate, long mSecsPixel, Event event, int x, int y, int char_descend) {
			Date eventStart = getUTCDate( event.getStartTime());

			int w = (int)(getDurationMillis(event.getDuration())/mSecsPixel);
			int eventX = x+(int)((eventStart.getTime()-startDate.getTime())/mSecsPixel);
			String eventName= event.getEventName();
			gd.setClip(null);

			// FIll gray
			gd.setColor(Color.GRAY);
			gd.fillRect(eventX, y, w, 20);

			// black border
			gd.setColor(Color.BLACK);
			gd.drawRect(eventX, y, w, 20);
			// title

			Shape clip = new Rectangle(eventX+5, y, w-10, 20);
			gd.setClip(clip);
			gd.setColor(Color.WHITE);
			gd.drawString(eventName, eventX+5,y+char_descend);
		}


		private void drawLabels(final Graphics2D gd, final SortedSet<Integer> serviceSet, final Font nameFont,
				int x,  int y, int char_descend) {
			int labelY = y;
			gd.setFont(nameFont);
			gd.setColor(Color.WHITE);
			for(final Integer serviceNo : serviceSet){
				String serviceName = getParentPSI().getSdt().getServiceName(serviceNo);
				if(serviceName==null){
					serviceName = "Service "+serviceNo;
				}
				Shape clipn = new Rectangle(x, labelY, SERVICE_NAME_WIDTH-5, 20);
				gd.setClip(clipn);
				gd.drawString(serviceName, x+5, labelY+char_descend);

				labelY+=20;
			}
		}


		private void drawActualTime(final Graphics2D gd, Date startDate,long msecsPixel, int x, int y, int width, int legendHeight) {
			// do we have a current time in the TDT?
			if(getParentPSI().getTdt()!=null){
				final List<TDTsection> tdtSectionList  = getParentPSI().getTdt().getTdtSectionList();
				if(tdtSectionList.size()>=1){
					final TDTsection first = tdtSectionList.get(0);
					final Date startTime = getUTCCalender(first.getUTC_time()).getTime();
					gd.setColor(Color.RED);
					int labelX = x+(int)((startTime.getTime()-startDate.getTime())/msecsPixel);
					gd.drawLine(labelX, 0, labelX, legendHeight-1);
				}
			}
		}


		private void drawLegend(final Graphics2D gd, Date startDate, Date endDate, long msecsPixel, int x, int y, int w, int legendHeight) {
			gd.setColor(Color.BLACK);
			gd.fillRect(x, y, w, legendHeight);

			gd.setColor(Color.WHITE);

			SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
			Date hourMark = new Date(startDate.getTime());
			while(hourMark.before(endDate)){
				int labelX = x+(int)((hourMark.getTime()-startDate.getTime())/msecsPixel);
				gd.drawLine(labelX, 0, labelX, legendHeight-1);
				String timeString =   tf.format(hourMark);
				String dateString =   df.format(hourMark);

				gd.drawString(dateString, labelX+5, 17);
				gd.drawString(timeString, labelX+5, 37);
				hourMark = new Date(hourMark.getTime() + (1000L*60*60)); //advance 1 hour
			}
		}

	}

	/**
	 * Helper to implement a HTMLSource for the program information for a single service (channel)
	 * in a single tableID (now/next actual stream, now/next other stream, schedule current stream,
	 * schedule other stream).
	 *
	 * @author Eric
	 *
	 */

	 // TODO as this is pure presentation logic, move to gui package ??

	public class ServiceListing implements HTMLSource {
		private final int tableID;
		private final int serviceNo;

		public ServiceListing(final int tableID, final int serviceNo){
			this.tableID = tableID;
			this.serviceNo=serviceNo;
		}

		public String getHTML() {
			final StringBuilder b = new StringBuilder();
			b.append("<code>");

			for(final EITsection section :eit.get(tableID).get(serviceNo)){
				if(section!=null){
					b.append(section.getHTMLLines());
				}
			}

			b.append("</code>");
			return b.toString();
		}
	}


	public EIT(final PSI parent){
		super(parent);
	}

	public void update(final EITsection section){


		if(section.isCrc_error()){
			return;
		}
		count++;

		final int tableId = section.getTableId();
		HashMap<Integer, EITsection []>  table= eit.get(tableId);

		if(table==null){
			table = new HashMap<Integer, EITsection []>();
			eit.put(tableId, table);
		}

		EITsection [] serviceSection = table.get(section.getServiceID());
		if(serviceSection==null){
			serviceSection = new EITsection [section.getSectionLastNumber()+1];
			table.put(section.getServiceID(),serviceSection);
		}
		if(serviceSection[section.getSectionNumber()]==null){
			serviceSection[section.getSectionNumber()] = section;
		}else{
			final TableSection last = serviceSection[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP eitKVP = new KVP("EIT");
		eitKVP.setImageSource(new EITableImage(getCombinedSchedule()));
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(eitKVP);
		final TreeSet<Integer> tableSet = new TreeSet<Integer>(eit.keySet());


		for(final Integer tableID : tableSet ){
			final KVP tableKVP = new KVP("table_id",tableID, TableSection.getTableType(tableID));
			tableKVP.setImageSource(new EITableImage(eit.get(tableID)));
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(tableKVP);
			final HashMap<Integer, EITsection []> table= eit.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				final KVP kvp = new KVP("service_id",serviceNo, getParentPSI().getSdt().getServiceName(serviceNo));
				kvp.setHtmlSource(new ServiceListing(tableID, serviceNo)); // set listing for whole service

				final DefaultMutableTreeNode o = new DefaultMutableTreeNode(kvp);
				for(final EITsection section :table.get(serviceNo)){
					if(section!= null){
						if(!simpleModus(modus)){
							addSectionVersionsToJTree(o, section, modus);
						}else{
							addListJTree(o,section.getEventList(),modus,"events");
						}
					}
				}
				n.add(o);
			}
			t.add(n);
		}
		return t;
	}

	public boolean exists(final int tableId, final int tableIdExtension, final int section){
		return ((eit.get(tableId)!=null) &&
				(eit.get(tableId).get(tableIdExtension)!=null) &&
				(eit.get(tableId).get(tableIdExtension).length >section) &&
				(eit.get(tableId).get(tableIdExtension)[section]!=null));
	}

	public Map<Integer, HashMap<Integer, EITsection[]>> getEITsectionsMap() {
		return eit;
	}

	/**
	 * Returns the start time of the first, and the end time of the last event of the services in this EIT table.
	 * Only the services contained in serviceSet are used in the calculation
	 *
	 * @param serviceSet service ID of services to be included in calculation
	 * @param eitTable map of service IDs to EITSection[] Can contain sections from different Table IDs, like 0x50 and 0x51, etc... (for very long EPGs)
	 * @return Interval that covers all events in eitTable
	 */
	public static Interval getSpanningInterval(final Set<Integer> serviceSet, Map<Integer, EITsection[]> eitTable) {
		Date startDate1 = null;
		Date endDate1 = null;
		// services to be displayed, in order

		for(final Integer serviceNo : serviceSet){
			for(final EITsection section :eitTable.get(serviceNo)){
				if(section!= null){
					List<Event> eventList = section.getEventList();
					for(Event event:eventList){
						Date eventStart = getUTCDate( event.getStartTime());
						if((startDate1==null)||(startDate1.after(eventStart))){
							startDate1 = eventStart;
						}
						Date eventEnd = new Date(eventStart.getTime()+ getDurationMillis(event.getDuration()));
						if((endDate1==null)||(endDate1.before(eventEnd))){
							endDate1 = eventEnd;
						}
					}
				}
			}
		}
		Interval interval = new Interval(startDate1,endDate1);
		return interval;
	}

	public HashMap<Integer, EITsection[]> getCombinedSchedule(){

		HashMap<Integer, EITsection[]> res = new HashMap<Integer, EITsection[]>();
		// actual TS
		for (int tableID = 0x50; tableID < 0x60; tableID++) {
			addSections(res, tableID);
		}
		// other TS
		for (int tableID = 0x60; tableID < 0x70; tableID++) {
			addSections(res, tableID);
		}
		return res;
	}

	/**
	 * @param res
	 * @param tableID
	 */
	private void addSections(HashMap<Integer, EITsection[]> res, int tableID) {
		HashMap<Integer, EITsection []> table= eit.get(tableID);
		if(table!=null){
			HashSet<Integer> serviceSet = new HashSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				EITsection[] eitArray = table.get(serviceNo); // array to be added to already found EITSections
				if((eitArray!=null)&&(eitArray.length>0)){
					EITsection[] resArray = res.get(serviceNo); //already found EITSections
					if(resArray==null){ // nothing yet, so put in new found
						res.put(serviceNo, eitArray);
					}else{
						EITsection[] combinedArray = new EITsection[resArray.length + eitArray.length];
						System.arraycopy(resArray, 0, combinedArray, 0, resArray.length);
						System.arraycopy(eitArray, 0, combinedArray, resArray.length, eitArray.length);
						res.put(serviceNo, combinedArray);
					}
				}
			}
		}
	}

}

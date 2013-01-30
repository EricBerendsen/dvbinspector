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
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.EITsection.Event;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.gui.ImageSource;
import nl.digitalekabeltelevisie.util.Utils;

/**
 *
 * Based on 5.2.4 Event Information Table (EIT) of ETSI EN 300 468 V1.11.1 (2010-04)
 *
 * @author Eric
 *
 */
public class EIT extends AbstractPSITabel{

	private final Map<Integer, HashMap<Integer,EITsection []>> eit = new HashMap<Integer, HashMap<Integer, EITsection []>>();
	private final int							charWidth	= 15;
	private final int							charHeight	= 19;

	private final long MILLI_SECS_PER_PIXEL = 15*1000;

	public class EITableImage implements ImageSource{
		private static final int SERVICE_NAME_WIDTH = 150;
		private int tableID;

		public EITableImage(final int tableID){
			this.tableID = tableID;
		}

		@Override
		public BufferedImage getImage() {
			HashMap<Integer, EITsection[]> table = eit.get(tableID);
			Date startDate = null;
			Date endDate = null;

			final TreeSet<Integer> serviceSet = new TreeSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				for(final EITsection section :table.get(serviceNo)){
					if(section!= null){
						List<Event> eventList = section.getEventList();
						for(Event event:eventList){
							Date eventStart = Utils.getUTCDate( event.getStartTime());
							if((startDate==null)||(startDate.after(eventStart))){
								startDate = eventStart;
							}
							Date eventEnd = new Date(eventStart.getTime()+ Utils.getDurationMillis(event.getDuration()));
							if((endDate==null)||(endDate.before(eventEnd))){
								endDate = eventEnd;
							}
						}
					}
				}
			}

			System.out.println("Startdate :"+startDate);
			System.out.println("endDate :"+endDate);
			// ROUND UP/DOWN TO NEAREST HOUR

			Calendar c = new GregorianCalendar();
			c.setTime(startDate);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MINUTE, 0);

			startDate = c.getTime();

			c.setTime(endDate);
			if((c.get(Calendar.SECOND)!=0) || (c.get(Calendar.MINUTE)!=0)){ //  no need to round if xx:00:00
				c.set(Calendar.SECOND, 0);
				c.set(Calendar.MINUTE, 0);
				c.add(Calendar.HOUR, 1);
			}

			endDate = c.getTime();

			System.out.println("Rounded Startdate :"+startDate);
			System.out.println("Rounded endDate :"+endDate);

			int rows = table.keySet().size();


			int legendHeight = 40;
			int height = (rows*20)+1 + legendHeight;
			int width = 1+SERVICE_NAME_WIDTH + (int)((endDate.getTime() - startDate.getTime())/MILLI_SECS_PER_PIXEL);
			final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D gd = img.createGraphics();
			gd.setColor(Color.BLUE);
			gd.fillRect(0, 0, width, height);
			gd.setColor(Color.WHITE);

			gd.setColor(Color.BLACK);
			gd.fillRect(0, 0, width, legendHeight);


			final BufferedImage charImg = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
			final Graphics2D charGD = charImg.createGraphics();
			final Font font = new Font("SansSerif", Font.PLAIN, 14);
			final Font nameFont = new Font("SansSerif", Font.BOLD, 14);
			//charGD.setFont(font);
			gd.setFont(font);

			gd.setColor(Color.WHITE);
			BasicStroke basicStroke = new BasicStroke( 3.0f);
			gd.setStroke(basicStroke);

			SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
			Date hourMark = new Date(startDate.getTime());
			while(hourMark.before(endDate)){
				int labelX = SERVICE_NAME_WIDTH+(int)((hourMark.getTime()-startDate.getTime())/MILLI_SECS_PER_PIXEL);
				gd.drawLine(labelX, 0, labelX, legendHeight);
				String timeString =   tf.format(hourMark);
				String dateString =   df.format(hourMark);
				gd.drawChars(dateString.toCharArray(), 0, dateString.length(), labelX+5, 17);
				gd.drawChars(timeString.toCharArray(), 0, timeString.length(), labelX+5, 37);
				hourMark = new Date(hourMark.getTime() + (1000L*60*60)); //advance 1 hour
			}

			// do we have a current time in the TDT?
			if(getParentPSI().getTdt()!=null){
				final List<TDTsection> tdtSectionList  = getParentPSI().getTdt().getTdtSectionList();
				if(tdtSectionList.size()>=1){
					final TDTsection first = tdtSectionList.get(0);
					final Date startTime = getUTCCalender(first.getUTC_time()).getTime();
					gd.setColor(Color.RED);
					int labelX = SERVICE_NAME_WIDTH+(int)((startTime.getTime()-startDate.getTime())/MILLI_SECS_PER_PIXEL);
					gd.drawLine(labelX, 0, labelX, legendHeight);

					gd.setColor(Color.WHITE);
				}
			}


			BasicStroke basicStroke1 = new BasicStroke( 1.0f);
			gd.setStroke(basicStroke1);

			Image targetChar = null;
			int offset = legendHeight;

			int char_descend = 16;

			for(final Integer serviceNo : serviceSet){
				String serviceName = getParentPSI().getSdt().getServiceName(serviceNo);
				if(serviceName==null){
					serviceName = "Service "+serviceNo;
				}
				gd.setFont(nameFont);
				Shape clipn = new Rectangle(0, offset, SERVICE_NAME_WIDTH-5, 20);
				gd.setClip(clipn);
				gd.drawChars(serviceName.toCharArray() , 0, serviceName.length(),5,offset+char_descend);

				gd.setFont(font);

				for(final EITsection section :table.get(serviceNo)){
					if(section!= null){
						List<Event> eventList = section.getEventList();
						for(Event event:eventList){
							Date eventStart = Utils.getUTCDate( event.getStartTime());
							Date eventEnd = new Date(eventStart.getTime()+ Utils.getDurationMillis(event.getDuration()));

							int w = (int)(Utils.getDurationMillis(event.getDuration())/MILLI_SECS_PER_PIXEL);
							int x = SERVICE_NAME_WIDTH+(int)((eventStart.getTime()-startDate.getTime())/MILLI_SECS_PER_PIXEL);
							String eventName= event.getEventName();
//							Shape clip = new Rectangle(x, offset, w, 20);
							gd.setClip(null);

							// FIll blue
							gd.setColor(Color.GRAY);
							gd.fillRect(x, offset, w, 20);

							// black border
							gd.setColor(Color.BLACK);

							gd.drawRect(x, offset, w, 20);
							// title

							Shape clip = new Rectangle(x+5, offset, w-10, 20);
							gd.setClip(clip);
							gd.setColor(Color.WHITE);
							gd.drawChars(eventName.toCharArray() , 0, eventName.length(), x+5,offset+char_descend);

							gd.setClip(null);

						}
					}
				}



				//gd.drawBytes(serviceName.getBytes() , 0, serviceName.length(), 0,offset);
				offset+=20;


			}


			return img;
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

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("EIT"));
		final TreeSet<Integer> tableSet = new TreeSet<Integer>(eit.keySet());


		for(final Integer tableID : tableSet ){
			final KVP tableKVP = new KVP("table_id",tableID, TableSection.getTableType(tableID));
			tableKVP.setImageSource(new EITableImage(tableID));
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(tableKVP);
			final HashMap<Integer, EITsection []> table= eit.get(tableID);

			final TreeSet<Integer> serviceSet = new TreeSet<Integer>(table.keySet());
			for(final Integer serviceNo : serviceSet){
				final KVP kvp = new KVP("service_id",serviceNo, getParentPSI().getSdt().getServiceName(serviceNo));
				kvp.setHtmlSource(new ServiceListing(tableID, serviceNo)); // set listing for whole service

				final DefaultMutableTreeNode o = new DefaultMutableTreeNode(kvp);
				for(final EITsection section :table.get(serviceNo)){
					if(section!= null){
						if(!Utils.simpleModus(modus)){
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


}

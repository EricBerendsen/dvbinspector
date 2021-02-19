package nl.digitalekabeltelevisie.data.mpeg.psi;
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

import static nl.digitalekabeltelevisie.util.Utils.formatDuration;
import static nl.digitalekabeltelevisie.util.Utils.getEscapedHTML;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ComponentDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ContentDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ContentDescriptor.ContentItem;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtendedEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.LanguageDependentEitDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ParentalRatingDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ParentalRatingDescriptor.Rating;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ShortEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.TimeShiftedEventDescriptor;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.Utils;


public class EITsection extends TableSectionExtendedSyntax implements HTMLSource {

	private List<Event> eventList;
	private final int transportStreamID;
	private final int originalNetworkID;
	private final int segmentLastSectionNumber;
	private final int lastTableID;

	public static class Event implements TreeNode, HTMLSource{
		private int eventID;
		private byte[] startTime;
		private String duration;
		private int runningStatus;
		private int freeCAMode;

		private int descriptorsLoopLength;

		private List<Descriptor> descriptorList;

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		public void setDescriptorList(final List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}
		public int getEventID() {
			return eventID;
		}

		public void setEventID(final int transportStreamID) {
			this.eventID = transportStreamID;
		}

		@Override
		public String toString(){
			final StringBuilder b = new StringBuilder("Event, ID=");
			b.append(getEventID()).
			append(", start_time:").
			append(Utils.getEITStartTimeAsString(startTime)).
			append(", duration:").
			append(formatDuration(duration));
			for (Descriptor d : descriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}

		public String getEventName(){


			for (Descriptor d : descriptorList) {
				if (d instanceof ShortEventDescriptor) {
					return ((ShortEventDescriptor) d).getEventName().toString();
				}
			}
			//  no ShortEventDescriptor, give up
			return "";

		}
		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus){

			final KVP kvp = new KVP("event",eventID,Utils.getEITStartTimeAsString(startTime)+" "+getEventName());
			kvp.setHtmlSource(this);
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);

			t.add(new DefaultMutableTreeNode(new KVP("event_id",eventID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("start_time",startTime,Utils.getEITStartTimeAsString(startTime))));
			t.add(new DefaultMutableTreeNode(new KVP("duration",duration,formatDuration(duration))));
			t.add(new DefaultMutableTreeNode(new KVP("running_status",runningStatus,getRunningStatusString(runningStatus))));
			t.add(new DefaultMutableTreeNode(new KVP("free_CA_mode",freeCAMode,getFreeCAmodeString(freeCAMode))));
			t.add(new DefaultMutableTreeNode(new KVP("descriptors_loop_length",descriptorsLoopLength,null)));

			Utils.addListJTree(t,descriptorList,modus,"event_descriptors");

			return t;
		}

		public int getDescriptorsLoopLength() {
			return descriptorsLoopLength;
		}

		public void setDescriptorsLoopLength(final int descriptorsLoopLength) {
			this.descriptorsLoopLength = descriptorsLoopLength;
		}

		public String getDuration() {
			return duration;
		}

		public void setDuration(final String duration) {
			this.duration = duration;
		}

		public int getFreeCAMode() {
			return freeCAMode;
		}

		public void setFreeCAMode(final int freeCAMode) {
			this.freeCAMode = freeCAMode;
		}

		public int getRunningStatus() {
			return runningStatus;
		}

		public void setRunningStatus(final int runningStatus) {
			this.runningStatus = runningStatus;
		}

		public byte[] getStartTime() {
			return startTime;
		}

		public void setStartTime(final byte[] startTime) {
			this.startTime = startTime;
		}


		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
		 */
		@Override
		public String getHTML() {
			final StringBuilder r1 = new StringBuilder();
			r1.append("Start:&nbsp;").
				append(Utils.getEITStartTimeAsString(getStartTime())).
				append("&nbsp;Duration: ").
				append(formatDuration(getDuration())).
				append("<br><hr><br>");
			final List<Descriptor> descList = getDescriptorList();
			
			addTimeShiftDetails(r1, Descriptor.findGenericDescriptorsInList(descList, TimeShiftedEventDescriptor.class));
			
			LinkedHashMap<String, ArrayList<LanguageDependentEitDescriptor>> languageDependentDescriptorsMap = mapDescriptorsByLanguage(descList);
			
			for(String isoLanguage:languageDependentDescriptorsMap.keySet()) {
				ArrayList<LanguageDependentEitDescriptor> languageList = languageDependentDescriptorsMap.get(isoLanguage);
				
				r1.append("ISO_639_language_code: ").
					append(isoLanguage).
					append("<br><br>");
			
				addShortEventDetails(r1, languageList);
				addExtendedEventDetails(r1, languageList);
				
				addComponentDetails(r1, languageList);
				
				r1.append("<hr><br>");
			}

			// end of language dependent part
			
			addContentDetails(r1, Descriptor.findGenericDescriptorsInList(descList, ContentDescriptor.class));
			addParentalRatingDetails(r1, Descriptor.findGenericDescriptorsInList(descList, ParentalRatingDescriptor.class));
			
			return r1.toString();
		}

		private static void addTimeShiftDetails(final StringBuilder r1, List<TimeShiftedEventDescriptor> timeShiftedEventDescriptorList) {
			for(TimeShiftedEventDescriptor timeShiftedEventDescriptor:timeShiftedEventDescriptorList) {
				r1.append("Event is a time shifted copy of other event; service_id:").
					append(timeShiftedEventDescriptor.getReference_service_id()).
					append(", event_id:").
					append(timeShiftedEventDescriptor.getReference_event_id()).
					append("<br><br>");
			}
		}

		private static void addParentalRatingDetails(final StringBuilder r1, final List<ParentalRatingDescriptor> ratingDescList) {
			for(final ParentalRatingDescriptor ratingDesc :ratingDescList){
				final List<Rating> ratingList = ratingDesc.getRatingList();
				for(final Rating c:ratingList){
					r1.append("Rating: ").
						append(c.getCountryCode()).
						append(": ").
						append(ParentalRatingDescriptor.getRatingTypeAge(c.getRating())).
						append("<br>");
				}
				r1.append("<br>");
			}
		}

		private static void addContentDetails(final StringBuilder r1, final List<ContentDescriptor> contentDescList) {
			if(!contentDescList.isEmpty()){
				for(final ContentDescriptor contentDesc : contentDescList){
					final List<ContentItem> contentList = contentDesc.getContentList();
					for(final ContentItem c:contentList){
						r1.append("Content type: ").
							append(ContentDescriptor.getContentNibbleLevel1String(c.getContentNibbleLevel1())).
							append(ContentDescriptor.getContentNibbleLevel2String(c.getContentNibbleLevel1(),c.getContentNibbleLevel2())).
							append("<br>");
					}
				}
				r1.append("<br>");

			}
		}

		private static void addComponentDetails(final StringBuilder r1, ArrayList<LanguageDependentEitDescriptor> languageList) {
			List<ComponentDescriptor> componentDescriptorList = Descriptor.findGenericDescriptorsInList(languageList, ComponentDescriptor.class);
			for(ComponentDescriptor componentDescriptor:componentDescriptorList) {
				r1.append("Component: ").
					append(componentDescriptor.getStreamTypeString()).
					append(", component_tag: ").
					append(componentDescriptor.getComponentTag()).
					append(", text: ").
					append(componentDescriptor.getText().toEscapedHTML()).
					append("<br>");
			}
		}

		private static void addShortEventDetails(final StringBuilder r1, ArrayList<LanguageDependentEitDescriptor> languageList) {
			final List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(languageList, ShortEventDescriptor.class);
			if(shortDesc.size()>0){
				for(final ShortEventDescriptor shortEventDescriptor : shortDesc){
					
					r1.append("Event name: <b>").
						append(shortEventDescriptor.getEventName().toEscapedHTML()).
						append("</b><br>");
					final String shortText = shortEventDescriptor.getText().toEscapedHTML();
					if((shortText!=null)&&!shortText.isEmpty()){
						r1.append("Short description: ").
							append(shortText).
							append("<br>");
					}
					r1.append("<br>");
				}
			}
		}

		private static void addExtendedEventDetails(final StringBuilder r1, ArrayList<LanguageDependentEitDescriptor> languageList) {
			final List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(languageList, ExtendedEventDescriptor.class);
			final ArrayList<DVBString> extendedEventStrings = new ArrayList<>();
			for(final ExtendedEventDescriptor extEvent: extendedDesc){ // no check whether we have all extended event descriptors

				if(!extEvent.getItemList().isEmpty()){ // this extended Event has items
					r1.append("<br><table>");
					for(final ExtendedEventDescriptor.Item item :extEvent.getItemList()){
						r1.append("<tr><td>").
							append(item.getItemDescription().toEscapedHTML()).
							append("</td><td>").
							append(item.getItem().toEscapedHTML()).
							append("</td></tr>");
					}
					r1.append("</table>");
				}
				extendedEventStrings.add(extEvent.getText());
			}
			
			if(!extendedEventStrings.isEmpty()){
				r1.append("<br>Extended description:<br><br>").append(getEscapedHTML(extendedEventStrings,80));
				r1.append("<br><br>");
			}
		}

		private static LinkedHashMap<String, ArrayList<LanguageDependentEitDescriptor>> mapDescriptorsByLanguage(final List<Descriptor> descList) {
			LinkedHashMap<String, ArrayList<LanguageDependentEitDescriptor>> languageDependentDescriptorsMap = new LinkedHashMap<>();
			
			for(Descriptor d:descList) {
				if(d instanceof LanguageDependentEitDescriptor) {
					LanguageDependentEitDescriptor languageDependentEitDescriptor = (LanguageDependentEitDescriptor) d;
					String iso639LanguageCode = languageDependentEitDescriptor.getIso639LanguageCode();
					List<LanguageDependentEitDescriptor> lst = languageDependentDescriptorsMap.computeIfAbsent(iso639LanguageCode, s -> new ArrayList<>());
					
					lst.add(languageDependentEitDescriptor);
				}
			}
			return languageDependentDescriptorsMap;
		}
	}



	public EITsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);

		transportStreamID = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_16BITS);
		originalNetworkID = Utils.getInt(raw_data.getData(), 10, 2, Utils.MASK_16BITS);
		segmentLastSectionNumber= Utils.getInt(raw_data.getData(), 12, 1, Utils.MASK_8BITS);
		lastTableID= Utils.getInt(raw_data.getData(), 13, 1, Utils.MASK_8BITS);

		eventList = buildEventList(raw_data.getData(), 14, sectionLength-14-4); //start and CRC(4)
	}


	public int getServiceID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("EITsection section=");
		b.append(getSectionNumber())
		.append(", OrgNetworkId=").append(getOriginalNetworkID())
		.append(", TransportStreamID=").append(getTransportStreamID())
		.append(", ServiceD=").append(getServiceID())
		.append(", tableType=").append(getTableType(getTableId()))
		.append(", lastSection=").append(getSectionLastNumber());



		return b.toString();
	}



	public List<Event> getEventList() {
		return eventList;
	}


	public void setEventList(
			final List<Event> transportStreamList) {
		this.eventList = transportStreamList;
	}

	public int noEvents() {
		return eventList.size();
	}


	private List<Event> buildEventList(final byte[] data, final int offset, final int programInfoLength) {
		final List<Event> r = new ArrayList<>();
		int t =0;
		while(t<programInfoLength){
			final Event c = new Event();
			c.setEventID(Utils.getInt(data, offset+t, 2, Utils.MASK_16BITS));
			c.setStartTime(Arrays.copyOfRange(data,offset+t+2,offset+t+7));
			c.setDuration(Utils.getBCD(data, (offset+t+7)*2,6));
			c.setRunningStatus(Utils.getInt(data, offset+t+10, 1, 0xE0)>>5);
			c.setFreeCAMode(Utils.getInt(data, offset+t+10, 1, 0x10)>>4);
			c.setDescriptorsLoopLength(Utils.getInt(data, offset+t+10, 2, Utils.MASK_12BITS));
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,offset+t+12,c.getDescriptorsLoopLength(),this));
			t+=12+c.getDescriptorsLoopLength();
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus,this);

		t.add(new DefaultMutableTreeNode(new KVP("service_id",getServiceID(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamID,null)));
		t.add(new DefaultMutableTreeNode(new KVP("original_network_id",originalNetworkID,Utils.getOriginalNetworkIDString(originalNetworkID))));
		t.add(new DefaultMutableTreeNode(new KVP("segment_last_section_number",segmentLastSectionNumber,null)));
		t.add(new DefaultMutableTreeNode(new KVP("last_table_id",lastTableID,null)));

		Utils.addListJTree(t,eventList,modus,"events");

		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "service_id";
	}

	public StringBuilder getHTMLLines(){
		final StringBuilder b = new StringBuilder();
		for(final Event event:eventList){
			b.append(Utils.escapeHTML(Utils.getEITStartTimeAsString(event.getStartTime()))).append("&nbsp;");
			b.append(formatDuration(event.getDuration())).append("&nbsp;");
			final List<Descriptor> descList = event.getDescriptorList();
			final List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(descList, ShortEventDescriptor.class);
			if(shortDesc.size()>0){
				for(final ShortEventDescriptor shortEventDescriptor : shortDesc){
					b.append("<b><span style=\"background-color: white\">");
					b.append(Utils.escapeHTML(shortEventDescriptor.getEventName().toString())).append("</span></b>&nbsp;");
					b.append(Utils.escapeHTML(shortEventDescriptor.getText().toString()));
				}
			}
			final List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(descList, ExtendedEventDescriptor.class);
			for(final ExtendedEventDescriptor extEvent: extendedDesc){
				b.append(Utils.escapeHTML(extEvent.getText().toString()));
			}

			b.append("<br>");
		}
		return b;
	}


	@Override
	public String getHTML() {
		final StringBuilder b = new StringBuilder();
		b.append("<code>");
		b.append(getHTMLLines());
		b.append("</code>");
		return b.toString();
	}


	public int getTransportStreamID() {
		return transportStreamID;
	}


	public int getOriginalNetworkID() {
		return originalNetworkID;
	}
}

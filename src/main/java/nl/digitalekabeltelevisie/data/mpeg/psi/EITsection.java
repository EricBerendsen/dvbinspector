package nl.digitalekabeltelevisie.data.mpeg.psi;
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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.table.TableModel;
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
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;


public class EITsection extends TableSectionExtendedSyntax{

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

		public void setDescriptorList(List<Descriptor> descriptorList) {
			this.descriptorList = descriptorList;
		}

		public int getEventID() {
			return eventID;
		}

		public void setEventID(int eventID) {
			this.eventID = eventID;
		}

		@Override
		public String toString(){
			StringBuilder b = new StringBuilder("Event, ID=");
			b.append(eventID).
			append(", start_time:").
			append(getEITStartTimeAsString(startTime)).
			append(", duration:").
			append(formatDuration(duration));
			for (Descriptor d : descriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}

		public String getEventName(){


			for (Descriptor d : descriptorList) {
				if (d instanceof ShortEventDescriptor shortEventDescriptor) {
					return shortEventDescriptor.getEventName().toString();
				}
			}
			//  no ShortEventDescriptor, give up
			return "";

		}
		@Override
		public KVP getJTreeNode(int modus){

			KVP t = new KVP("event",eventID).setDescription(getEITStartTimeAsString(startTime)+" "+getEventName());
			t.addHTMLSource(this,"Event details");
			
			t.add(new KVP("event_id", eventID));
			t.add(new KVP("start_time", startTime).setDescription(getEITStartTimeAsString(startTime)));
			t.add(new KVP("duration", duration).setDescription(formatDuration(duration)));
			t.add(new KVP("running_status", runningStatus).setDescription(getRunningStatusString(runningStatus)));
			t.add(new KVP("free_CA_mode", freeCAMode).setDescription(getFreeCAmodeString(freeCAMode)));
			t.add(new KVP("descriptors_loop_length", descriptorsLoopLength));

			Utils.addListJTree(t,descriptorList,modus,"event_descriptors");

			return t;
		}

		public int getDescriptorsLoopLength() {
			return descriptorsLoopLength;
		}

		public void setDescriptorsLoopLength(int descriptorsLoopLength) {
			this.descriptorsLoopLength = descriptorsLoopLength;
		}

		public String getDuration() {
			return duration;
		}

		public void setDuration(String duration) {
			this.duration = duration;
		}

		public int getFreeCAMode() {
			return freeCAMode;
		}

		public void setFreeCAMode(int freeCAMode) {
			this.freeCAMode = freeCAMode;
		}

		public int getRunningStatus() {
			return runningStatus;
		}

		public void setRunningStatus(int runningStatus) {
			this.runningStatus = runningStatus;
		}

		public byte[] getStartTime() {
			return startTime;
		}

		public void setStartTime(byte[] startTime) {
			this.startTime = startTime;
		}


		/* (non-Javadoc)
		 * @see nl.digitalekabeltelevisie.gui.HTMLSource#getHTML()
		 */
		@Override
		public String getHTML() {
			StringBuilder r1 = new StringBuilder();
			r1.append("Start:&nbsp;").
				append(getEITStartTimeAsString(getStartTime())).
				append("&nbsp;Duration: ").
				append(formatDuration(duration)).
				append("<br><hr><br>");
			List<Descriptor> descList = descriptorList;
			
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

		private static void addTimeShiftDetails(StringBuilder r1, List<TimeShiftedEventDescriptor> timeShiftedEventDescriptorList) {
			for(TimeShiftedEventDescriptor timeShiftedEventDescriptor:timeShiftedEventDescriptorList) {
				r1.append("Event is a time shifted copy of other event; service_id:").
					append(timeShiftedEventDescriptor.getReference_service_id()).
					append(", event_id:").
					append(timeShiftedEventDescriptor.getReference_event_id()).
					append("<br><br>");
			}
		}

		private static void addParentalRatingDetails(StringBuilder r1, List<ParentalRatingDescriptor> ratingDescList) {
			for(ParentalRatingDescriptor ratingDesc :ratingDescList){
				List<Rating> ratingList = ratingDesc.getRatingList();
				for(Rating c:ratingList){
					r1.append("Rating: ").
						append(c.getCountryCode()).
						append(": ").
						append(ParentalRatingDescriptor.getRatingTypeAge(c.getRating())).
						append("<br>");
				}
				r1.append("<br>");
			}
		}

		private static void addContentDetails(StringBuilder r1, List<ContentDescriptor> contentDescList) {
			if(!contentDescList.isEmpty()){
				for(ContentDescriptor contentDesc : contentDescList){
					List<ContentItem> contentList = contentDesc.getContentList();
					for(ContentItem c:contentList){
						r1.append("Content type: ").
							append(ContentDescriptor.getContentNibbleLevel1String(c.contentNibbleLevel1())).
							append(ContentDescriptor.getContentNibbleLevel2String(c.contentNibbleLevel1(),c.contentNibbleLevel2())).
							append("<br>");
					}
				}
				r1.append("<br>");

			}
		}

		private static void addComponentDetails(StringBuilder r1, ArrayList<LanguageDependentEitDescriptor> languageList) {
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

		private static void addShortEventDetails(StringBuilder r1, ArrayList<LanguageDependentEitDescriptor> languageList) {
			List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(languageList, ShortEventDescriptor.class);
			if(!shortDesc.isEmpty()){
				for(ShortEventDescriptor shortEventDescriptor : shortDesc){
					
					r1.append("Event name: <b>").
						append(shortEventDescriptor.getEventName().toEscapedHTML()).
						append("</b><br>");
					String shortText = shortEventDescriptor.getText().toEscapedHTML();
					if((shortText!=null)&&!shortText.isEmpty()){
						r1.append("Short description: ").
							append(shortText).
							append("<br>");
					}
					r1.append("<br>");
				}
			}
		}

		private static void addExtendedEventDetails(StringBuilder r1, ArrayList<LanguageDependentEitDescriptor> languageList) {
			List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(languageList, ExtendedEventDescriptor.class);
			ArrayList<DVBString> extendedEventStrings = new ArrayList<>();
			for(ExtendedEventDescriptor extEvent: extendedDesc){ // no check whether we have all extended event descriptors

				if(!extEvent.getItemList().isEmpty()){ // this extended Event has items
					r1.append("<br><table>");
					for(ExtendedEventDescriptor.Item item :extEvent.getItemList()){
						r1.append("<tr><td>").
							append(item.itemDescription().toEscapedHTML()).
							append("</td><td>").
							append(item.item().toEscapedHTML()).
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

		private static LinkedHashMap<String, ArrayList<LanguageDependentEitDescriptor>> mapDescriptorsByLanguage(List<Descriptor> descList) {
			LinkedHashMap<String, ArrayList<LanguageDependentEitDescriptor>> languageDependentDescriptorsMap = new LinkedHashMap<>();
			
			for(Descriptor d:descList) {
				if(d instanceof LanguageDependentEitDescriptor languageDependentEitDescriptor) {
					String iso639LanguageCode = languageDependentEitDescriptor.getIso639LanguageCode();
					List<LanguageDependentEitDescriptor> lst = languageDependentDescriptorsMap.computeIfAbsent(iso639LanguageCode, s -> new ArrayList<>());
					
					lst.add(languageDependentEitDescriptor);
				}
			}
			return languageDependentDescriptorsMap;
		}
	}



	public EITsection(PsiSectionData raw_data, PID parent){
		super(raw_data,parent);

		transportStreamID = getInt(raw_data.getData(), 8, 2, Utils.MASK_16BITS);
		originalNetworkID = getInt(raw_data.getData(), 10, 2, Utils.MASK_16BITS);
		segmentLastSectionNumber= getInt(raw_data.getData(), 12, 1, Utils.MASK_8BITS);
		lastTableID= getInt(raw_data.getData(), 13, 1, Utils.MASK_8BITS);

		eventList = buildEventList(raw_data.getData(), 14, sectionLength-14-4); //start and CRC(4)
	}


	public int getServiceID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){
		StringBuilder b = new StringBuilder("EITsection section=");
		b.append(getSectionNumber())
		.append(", OrgNetworkId=").append(originalNetworkID)
		.append(", TransportStreamID=").append(transportStreamID)
		.append(", ServiceD=").append(getServiceID())
		.append(", tableType=").append(getTableType(getTableId()))
		.append(", lastSection=").append(getSectionLastNumber());



		return b.toString();
	}



	public List<Event> getEventList() {
		return eventList;
	}


	public void setEventList(
			List<Event> eventList) {
		this.eventList = eventList;
	}

	public int noEvents() {
		return eventList.size();
	}


	private List<Event> buildEventList(byte[] data, int offset, int programInfoLength) {
		List<Event> r = new ArrayList<>();
		int t = 0;
		while (t < programInfoLength) {
			Event c = new Event();
			c.setEventID(getInt(data, offset + t, 2, Utils.MASK_16BITS));
			c.setStartTime(Arrays.copyOfRange(data, offset + t + 2, offset + t + 7));
			c.setDuration(getBCD(data, (offset + t + 7) * 2, 6));
			c.setRunningStatus(getInt(data, offset + t + 10, 1, 0xE0) >> 5);
			c.setFreeCAMode(getInt(data, offset + t + 10, 1, 0x10) >> 4);
			c.setDescriptorsLoopLength(getInt(data, offset + t + 10, 2, Utils.MASK_12BITS));
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data, offset + t + 12, c.getDescriptorsLoopLength(), this));
			t += 12 + c.getDescriptorsLoopLength();
			r.add(c);

		}

		return r;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus){

		DefaultMutableTreeNode t = super.getJTreeNode(modus);

		KVP kvp = (KVP)t.getUserObject();
		kvp.addHTMLSource(()->getHtmlForEit(modus), "List");
		kvp.addTableSource(this::getTableModel, "Events");
		t.add(new KVP("service_id",getServiceID()));
		t.add(new KVP("transport_stream_id",transportStreamID));
		t.add(new KVP("original_network_id",originalNetworkID).setDescription(Utils.getOriginalNetworkIDString(originalNetworkID)));
		t.add(new KVP("segment_last_section_number",segmentLastSectionNumber));
		t.add(new KVP("last_table_id",lastTableID));

		Utils.addListJTree(t,eventList,modus,"events");

		return t;
	}

	/**
	 * @param modus
	 * @return
	 */
	String getHtmlForEit(int modus) {
		StringBuilder b = new StringBuilder();
		b.append("<code>");
		b.append(getHTMLLines(modus));
		b.append("</code>");
		return b.toString();

	}


	@Override
	protected String getTableIdExtensionLabel() {
		return "service_id";
	}

	public StringBuilder getHTMLLines(int modus){
		StringBuilder b = new StringBuilder();
		for(Event event:eventList){
			b.append(Utils.escapeHTML(getEITStartTimeAsString(event.getStartTime()))).
				append("&nbsp;").
				append(formatDuration(event.getDuration())).
				append("&nbsp;");
			List<Descriptor> descList = event.getDescriptorList();
			List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(descList, ShortEventDescriptor.class);
			if(!shortDesc.isEmpty()){
				for(ShortEventDescriptor shortEventDescriptor : shortDesc){
					b.append("<b><span style=\"background-color: white\">").
					append("<a href=\"root/psi/eit/original_network_id:").
					append(originalNetworkID).
					append("/transport_stream_id:").
					append(transportStreamID).
					append("/service_id:").
					append(getServiceID()).
					append("/tableid:").
					append(tableId);
					if(!Utils.simpleModus(modus)) {
						b.append("/tablesection:").
						append(getSectionNumber()).
						append("/events");
						
					}
					b.append("/event:").
					append(event.eventID).
					append("\">").
					append(Utils.escapeHTML(shortEventDescriptor.getEventName().toString())).
					append("</a>").
					append("</span></b>&nbsp;").
					append(Utils.escapeHTML(shortEventDescriptor.getText().toString()));
				}
			}
			List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(descList, ExtendedEventDescriptor.class);
			for(ExtendedEventDescriptor extEvent: extendedDesc){
				b.append(Utils.escapeHTML(extEvent.getText().toString()));
			}

			b.append("<br>");
		}
		return b;
	}


	public int getTransportStreamID() {
		return transportStreamID;
	}


	public int getOriginalNetworkID() {
		
		
		return originalNetworkID;
	}
	
	public String findServiceName() {
		return getPSI().getSdt().getServiceName(originalNetworkID, transportStreamID, getServiceID());
	}

	
	public TableModel getTableModel() {
		FlexTableModel<EITsection,Event> tableModel =  new FlexTableModel<>(EIT.buildEitTableHeader());

		tableModel.addData(this, eventList);

		tableModel.process();
		return tableModel;
	}
	


}

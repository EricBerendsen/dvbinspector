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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ExtendedEventDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.ShortEventDescriptor;
import nl.digitalekabeltelevisie.gui.HTMLSource;
import nl.digitalekabeltelevisie.util.Utils;


public class EITsection extends TableSection implements HTMLSource {

	private List<Event> eventList;
	private int transportStreamID;
	private int originalNetworkID;
	private int segmentLastSectionNumber;
	private int lastTableID;

	public class Event implements TreeNode{
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
			final StringBuilder b = new StringBuilder("Service, transportStreamID=");
			b.append(getEventID());
			final Iterator<Descriptor> j=descriptorList.iterator();
			while (j.hasNext()) {
				final Descriptor d = j.next();
				b.append(d).append(", ");

			}
			return b.toString();

		}

		public String getEventName(){


			final Iterator<Descriptor> descs=descriptorList.iterator();
			while(descs.hasNext()){
				final Descriptor d=descs.next();
				if(d instanceof ShortEventDescriptor) {
					return ((ShortEventDescriptor)d).getEventName().toString();
				}
			}
			//  no ShortEventDescriptor, give up
			return "";

		}
		public DefaultMutableTreeNode getJTreeNode(final int modus){

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("event",eventID,Utils.getUTCFormattedString(startTime)+" "+getEventName()));

			t.add(new DefaultMutableTreeNode(new KVP("event_id",eventID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("start_time",startTime,Utils.getUTCFormattedString(startTime))));
			t.add(new DefaultMutableTreeNode(new KVP("duration",duration,null)));
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



	}



	public EITsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		if(!isCrc_error()){
			transportStreamID = Utils.getInt(raw_data.getData(), 8, 2, Utils.MASK_16BITS);
			originalNetworkID = Utils.getInt(raw_data.getData(), 10, 2, Utils.MASK_16BITS);
			segmentLastSectionNumber= Utils.getInt(raw_data.getData(), 12, 1, Utils.MASK_8BITS);
			lastTableID= Utils.getInt(raw_data.getData(), 13, 1, Utils.MASK_8BITS);

			eventList = buildEventList(raw_data.getData(), 14, sectionLength-14-4); //start and CRC(4)
		}
	}


	public int getServiceID(){
		return getTableIdExtension();
	}

	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("EITsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", tableType=").append(getTableType(tableId)). append(", ServiceD=").append(getServiceID()).append(", ");



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


	public List<Event> buildEventList(final byte[] data, final int i, final int programInfoLength) {
		final List<Event> r = new ArrayList<Event>();
		int t =0;
		while(t<programInfoLength){
			final Event c = new Event();
			c.setEventID(Utils.getInt(data, i+t, 2, Utils.MASK_16BITS));
			c.setStartTime(Utils.copyOfRange(data,i+t+2,i+t+7));
			c.setDuration(Utils.getBCD(data, (i+t+7)*2,6));
			c.setRunningStatus(Utils.getInt(data, i+t+10, 1, 0xE0)>>5);
			c.setFreeCAMode(Utils.getInt(data, i+t+10, 1, 0x10)>>4);
			c.setDescriptorsLoopLength(Utils.getInt(data, i+t+10, 2, Utils.MASK_12BITS));
			c.setDescriptorList(DescriptorFactory.buildDescriptorList(data,i+t+12,c.getDescriptorsLoopLength(),this));
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

	public StringBuilder getHTMLLines(){
		final StringBuilder b = new StringBuilder();
		for(final Event event:eventList){
			b.append("&nbsp;").append(Utils.getUTCFormattedString(event.getStartTime())).append("&nbsp;");
			b.append(event.getDuration()).append("&nbsp;");
			final List<Descriptor> descList = event.getDescriptorList();
			//List<Descriptor> shortDesc = Descriptor.findDescriptorsInList(descList, 0x4D);
			final List<ShortEventDescriptor> shortDesc = Descriptor.findGenericDescriptorsInList(descList, ShortEventDescriptor.class);
			if(shortDesc.size()>0){
				b.append("<b><span style=\"background-color: white\">");
				final ShortEventDescriptor shortEventDescriptor = shortDesc.get(0);
				b.append(Utils.escapeHTML(shortEventDescriptor.getEventName().toString())).append("</span></b>&nbsp;");
				b.append(Utils.escapeHTML(shortEventDescriptor.getText().toString()));
			}
			//List<Descriptor> extendedDesc = Descriptor.findDescriptorsInList(descList, 0x4E);
			final List<ExtendedEventDescriptor> extendedDesc = Descriptor.findGenericDescriptorsInList(descList, ExtendedEventDescriptor.class);
			for(final ExtendedEventDescriptor extEvent: extendedDesc){ // no check whether we have all extended event descriptors
				b.append(Utils.escapeHTML(extEvent.getText().toString()));
			}

			b.append("<br>");
		}
		return b;
	}


	public String getHTML() {
		final StringBuilder b = new StringBuilder();
		b.append("<code>");
		b.append(getHTMLLines());
		b.append("</code>");
		return b.toString();
	}
}

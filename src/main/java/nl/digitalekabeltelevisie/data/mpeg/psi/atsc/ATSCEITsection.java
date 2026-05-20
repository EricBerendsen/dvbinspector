/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.psi.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_12BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_14BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_20BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.AtscMultipleString;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ContentAdvisoryDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class ATSCEITsection extends TableSectionExtendedSyntax {

	private static final Instant GPS_EPOCH = Instant.parse("1980-01-06T00:00:00Z");

	private final int protocolVersion;
	private final int numEventsInSection;
	private final List<Event> events = new ArrayList<>();
	private int tableType = -1;
	private int gpsUtcOffset;

	public ATSCEITsection(final PsiSectionData rawData, final PID parent) {
		super(rawData, parent);

		byte[] data = rawData.getData();
		protocolVersion = getInt(data, 8, 1, MASK_8BITS);
		numEventsInSection = getInt(data, 9, 1, MASK_8BITS);

		int offset = 10;
		for (int i = 0; i < numEventsInSection; i++) {
			Event event = new Event(data, offset, this);
			events.add(event);
			offset += event.getLength();
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.addTableSource(this::getTableModel, "EPG Events");
		t.add(new KVP("protocol_version", protocolVersion));
		t.add(new KVP("num_events_in_section", numEventsInSection));
		addListJTree(t, events, modus, "events");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "source_id";
	}

	public int getSourceId() {
		return getTableIdExtension();
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public int getNumEventsInSection() {
		return numEventsInSection;
	}

	public List<Event> getEvents() {
		return events;
	}

	public int getTableType() {
		return tableType;
	}

	void setTableType(final int tableType) {
		this.tableType = tableType;
	}

	public int getGpsUtcOffset() {
		return gpsUtcOffset;
	}

	void setGpsUtcOffset(final int gpsUtcOffset) {
		this.gpsUtcOffset = gpsUtcOffset;
	}

	public String getTableTypeDescription() {
		return MGTsection.getTableTypeDescription(tableType);
	}

	public TableModel getTableModel() {
		FlexTableModel<ATSCEITsection, Event> tableModel = new FlexTableModel<>(buildEitTableHeader());
		tableModel.addData(this, events);
		tableModel.process();
		return tableModel;
	}

	static TableHeader<ATSCEITsection, Event> buildEitTableHeader() {
		return new TableHeaderBuilder<ATSCEITsection, Event>()
				.addRequiredBaseColumn("table_type", ATSCEITsection::getTableTypeDescription, String.class)
				.addRequiredBaseColumn("source_id", ATSCEITsection::getSourceId, Integer.class)
				.addRequiredBaseColumn("channel", ATSCEITsection::getChannelName, String.class)
				.addRequiredBaseColumn("section", ATSCEITsection::getSectionNumber, Integer.class)
				.addRequiredRowColumn("event_id", Event::getEventId, Integer.class)
				.addRequiredRowColumn("start_time", Event::getUtcStartTimeString, String.class)
				.addRequiredRowColumn("duration_sec", Event::getLengthInSeconds, Integer.class)
				.addRequiredRowColumn("title", Event::getTitle, String.class)
				.addOptionalRowColumn("extended_text_message", Event::getExtendedText, String.class)
				.addOptionalRowColumn("content_advisory", Event::getContentAdvisory, String.class)
				.addOptionalRowColumn("ETM_location", Event::getEtmLocationString, String.class)
				.addOptionalRowColumn("descriptors_length", Event::getDescriptorsLength, Integer.class)
				.build();
	}

	static TableHeader<ATSCEITsection, ATSCEITsection> buildEitSectionTableHeader() {
		return new TableHeaderBuilder<ATSCEITsection, ATSCEITsection>()
				.addRequiredRowColumn("table_type", ATSCEITsection::getTableTypeDescription, String.class)
				.addRequiredRowColumn("source_id", ATSCEITsection::getSourceId, Integer.class)
				.addRequiredRowColumn("channel", ATSCEITsection::getChannelName, String.class)
				.addRequiredRowColumn("version", ATSCEITsection::getVersion, Integer.class)
				.addRequiredRowColumn("current_next", ATSCEITsection::getCurrentNext, Integer.class)
				.addRequiredRowColumn("section", ATSCEITsection::getSectionNumber, Integer.class)
				.addRequiredRowColumn("last_section", ATSCEITsection::getSectionLastNumber, Integer.class)
				.addRequiredRowColumn("num_events", ATSCEITsection::getNumEventsInSection, Integer.class)
				.addRequiredRowColumn("protocol_version", ATSCEITsection::getProtocolVersion, Integer.class)
				.addOptionalRowColumn("first_packet_no", ATSCEITsection::getFirst_packet_no, Integer.class)
				.addOptionalRowColumn("last_packet_no", ATSCEITsection::getLast_packet_no, Integer.class)
				.addOptionalRowColumn("occurrence_count", ATSCEITsection::getOccurrence_count, Integer.class)
				.build();
	}

	public String getChannelName() {
		try {
			return getPSI().getAtsc().getChannelNameOptional(getSourceId()).orElse(null);
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static String getUtcTimeString(final long gpsSeconds, final int gpsUtcOffset) {
		return GPS_EPOCH.plusSeconds(gpsSeconds - gpsUtcOffset).toString();
	}

	public static class Event implements TreeNode {

		private final int eventId;
		private final long startTime;
		private final int etmLocation;
		private final int lengthInSeconds;
		private final int titleLength;
		private final AtscMultipleString titleText;
		private final int descriptorsLength;
		private final List<Descriptor> descriptorList;
		private final int length;
		private final ATSCEITsection parent;

		Event(final byte[] data, final int offset, final ATSCEITsection parent) {
			this.parent = parent;
			eventId = getInt(data, offset, 2, MASK_14BITS);
			startTime = getLong(data, offset + 2, 4, 0xFFFF_FFFFL);
			long flagsAndLength = getLong(data, offset + 6, 3, 0xFF_FFFFL);
			etmLocation = (int) ((flagsAndLength >> 20) & 0x03);
			lengthInSeconds = (int) (flagsAndLength & MASK_20BITS);
			titleLength = getInt(data, offset + 9, 1, MASK_8BITS);
			titleText = new AtscMultipleString(data, offset + 10, titleLength);
			int descriptorOffset = offset + 10 + titleLength;
			descriptorsLength = getInt(data, descriptorOffset, 2, MASK_12BITS);
			descriptorList = DescriptorFactory.buildDescriptorList(data, descriptorOffset + 2, descriptorsLength, parent);
			length = 12 + titleLength + descriptorsLength;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("event", eventId, getTitle());
			t.add(new KVP("event_id", eventId));
			t.add(new KVP("start_time", startTime));
			t.add(new KVP("ETM_location", etmLocation, VCTsection.getEtmLocationString(etmLocation)));
			t.add(new KVP("length_in_seconds", lengthInSeconds));
			t.add(new KVP("title_length", titleLength));
			t.add(new KVP("title", getTitle()));
			t.add(titleText.getJTreeNode(modus));
			t.add(new KVP("descriptors_length", descriptorsLength));
			addListJTree(t, descriptorList, modus, "descriptors");
			return t;
		}

		public int getEventId() {
			return eventId;
		}

		public long getStartTime() {
			return startTime;
		}

		public int getEtmLocation() {
			return etmLocation;
		}

		public String getEtmLocationString() {
			return VCTsection.getEtmLocationString(etmLocation);
		}

		public String getUtcStartTimeString() {
			return getUtcTimeString(startTime, parent.getGpsUtcOffset());
		}

		public int getLengthInSeconds() {
			return lengthInSeconds;
		}

		public int getTitleLength() {
			return titleLength;
		}

		public AtscMultipleString getTitleText() {
			return titleText;
		}

		public String getTitle() {
			return titleText.getText();
		}

		public String getExtendedText() {
			try {
				return parent.getPSI().getAtsc().getEtt().getEventText(parent.getSourceId(), eventId);
			} catch (RuntimeException e) {
				return null;
			}
		}

		public String getContentAdvisory() {
			List<String> ratings = new ArrayList<>();
			for (Descriptor descriptor : descriptorList) {
				if (descriptor instanceof ContentAdvisoryDescriptor contentAdvisoryDescriptor) {
					String rating = contentAdvisoryDescriptor.getRatingSummaryString();
					if ((rating != null) && !rating.isBlank()) {
						ratings.add(rating);
					}
				}
			}
			return ratings.isEmpty() ? null : String.join("; ", ratings);
		}

		public int getDescriptorsLength() {
			return descriptorsLength;
		}

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}

		int getLength() {
			return length;
		}
	}
}

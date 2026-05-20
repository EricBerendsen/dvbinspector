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

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.ExtendedChannelNameDescriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.AbstractPSITabel;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class ATSCTables extends AbstractPSITabel {

	public static final int BASE_PID = 0x1FFB;

	private final STT stt;
	private final MGT mgt;
	private final VCT<TVCTsection> tvct;
	private final VCT<CVCTsection> cvct;
	private final ATSCEIT eit;
	private final ATSCETT ett;
	private final RRT rrt;

	public ATSCTables(final PSI parentPSI) {
		super(parentPSI);
		stt = new STT(parentPSI);
		mgt = new MGT(parentPSI);
		tvct = new VCT<>(parentPSI, "TVCT");
		cvct = new VCT<>(parentPSI, "CVCT");
		eit = new ATSCEIT(parentPSI);
		ett = new ATSCETT(parentPSI);
		rrt = new RRT(parentPSI);
	}

	public void update(final STTsection section) {
		stt.update(section);
	}

	public void update(final MGTsection section) {
		mgt.update(section);
	}

	public void update(final TVCTsection section) {
		tvct.update(section);
	}

	public void update(final CVCTsection section) {
		cvct.update(section);
	}

	public void update(final ATSCEITsection section) {
		int tableType = 0x0100;
		if (section.getParentPID() != null) {
			tableType = getTableTypeForPid(section.getParentPID().getPid(), 0x0100, 0x017F).orElse(tableType);
		}
		eit.update(section, tableType);
	}

	public void update(final ATSCETTsection section) {
		int tableType = section.getEventId() == 0 ? 0x0004 : 0x0200;
		if (section.getParentPID() != null) {
			tableType = getTableTypeForPid(section.getParentPID().getPid(), 0x0004, 0x0004)
					.or(() -> getTableTypeForPid(section.getParentPID().getPid(), 0x0200, 0x027F))
					.orElse(tableType);
		}
		ett.update(section, tableType);
	}

	public void update(final RRTsection section) {
		rrt.update(section);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP kvp = new KVP("ATSC PSIP");
		kvp.addTableSource(this::getProgramsTableModel, "Programs / Channels");
		kvp.add(stt.getJTreeNode(modus));
		kvp.add(mgt.getJTreeNode(modus));
		kvp.add(tvct.getJTreeNode(modus));
		kvp.add(cvct.getJTreeNode(modus));
		kvp.add(eit.getJTreeNode(modus));
		kvp.add(ett.getJTreeNode(modus));
		kvp.add(rrt.getJTreeNode(modus));
		return kvp;
	}

	public TableModel getProgramsTableModel() {
		FlexTableModel<ATSCTables, ProgramChannel> tableModel = new FlexTableModel<>(buildProgramTableHeader());
		tableModel.addData(this, getProgramChannels());
		tableModel.process();
		return tableModel;
	}

	static TableHeader<ATSCTables, ProgramChannel> buildProgramTableHeader() {
		return new TableHeaderBuilder<ATSCTables, ProgramChannel>()
				.addRequiredRowColumn("VCT", ProgramChannel::getVctName, String.class)
				.addRequiredRowColumn("channel", ProgramChannel::getChannelNumber, String.class)
				.addRequiredRowColumn("short_name", ProgramChannel::getShortName, String.class)
				.addRequiredRowColumn("program_number", ProgramChannel::getProgramNumber, Integer.class)
				.addRequiredRowColumn("source_id", ProgramChannel::getSourceId, Integer.class)
				.addRequiredRowColumn("service_type", ProgramChannel::getServiceType, String.class)
				.addOptionalRowColumn("PMT_PID", ProgramChannel::getPmtPid, Integer.class)
				.addOptionalRowColumn("PCR_PID", ProgramChannel::getPcrPid, Integer.class)
				.addOptionalRowColumn("video_PIDs", ProgramChannel::getVideoPids, String.class)
				.addOptionalRowColumn("audio_PIDs", ProgramChannel::getAudioPids, String.class)
				.addOptionalRowColumn("elementary_streams", ProgramChannel::getElementaryStreams, String.class)
				.addOptionalRowColumn("channel_text", ProgramChannel::getChannelText, String.class)
				.addOptionalRowColumn("events", ProgramChannel::getEventCount, Integer.class)
				.addOptionalRowColumn("current_event", ProgramChannel::getCurrentEventTitle, String.class)
				.addOptionalRowColumn("current_event_text", ProgramChannel::getCurrentEventText, String.class)
				.addOptionalRowColumn("next_event", ProgramChannel::getNextEventTitle, String.class)
				.addOptionalRowColumn("next_event_start", ProgramChannel::getNextEventStart, String.class)
				.build();
	}

	public STT getStt() {
		return stt;
	}

	public MGT getMgt() {
		return mgt;
	}

	public VCT<TVCTsection> getTvct() {
		return tvct;
	}

	public VCT<CVCTsection> getCvct() {
		return cvct;
	}

	public ATSCEIT getEit() {
		return eit;
	}

	public ATSCETT getEtt() {
		return ett;
	}

	public RRT getRrt() {
		return rrt;
	}

	public boolean hasPsipTables() {
		return (stt.getLatestSttSection() != null) || (mgt.getMgtSection() != null)
				|| hasSections(tvct) || hasSections(cvct) || !eit.getTables().isEmpty() || !ett.getTables().isEmpty();
	}

	public boolean isAtscEitPid(final int pid) {
		return getTableTypeForPid(pid, 0x0100, 0x017F).isPresent();
	}

	public boolean isAtscEttPid(final int pid) {
		return getTableTypeForPid(pid, 0x0004, 0x0004).isPresent()
				|| getTableTypeForPid(pid, 0x0200, 0x027F).isPresent();
	}

	public Optional<Integer> getTableTypeForPid(final int pid, final int lowerInclusive, final int upperInclusive) {
		MGTsection mgtSection = mgt.getMgtSection();
		if (mgtSection == null) {
			return Optional.empty();
		}
		return mgtSection.getTableTypeEntries().stream()
				.filter(e -> e.getTableTypePid() == pid)
				.map(MGTsection.TableTypeEntry::getTableType)
				.filter(tableType -> (lowerInclusive <= tableType) && (tableType <= upperInclusive))
				.findFirst();
	}

	private static boolean hasSections(final VCT<? extends VCTsection> vct) {
		VCTsection[] sections = vct.getSections();
		if (sections == null) {
			return false;
		}
		for (VCTsection section : sections) {
			if (section != null) {
				return true;
			}
		}
		return false;
	}

	private List<ProgramChannel> getProgramChannels() {
		List<ProgramChannel> rows = new ArrayList<>();
		addProgramChannels(rows, "TVCT", tvct);
		addProgramChannels(rows, "CVCT", cvct);
		rows.sort(Comparator.comparing(ProgramChannel::getVctName)
				.thenComparing(ProgramChannel::getMajorChannelNumber)
				.thenComparing(ProgramChannel::getMinorChannelNumber)
				.thenComparing(ProgramChannel::getProgramNumber));
		return rows;
	}

	private void addProgramChannels(final List<ProgramChannel> rows, final String vctName,
			final VCT<? extends VCTsection> vct) {
		for (VCTsection section : vct.getLatestCompleteSections()) {
			if (section == null) {
				continue;
			}
			for (VCTsection.VirtualChannel channel : section.getVirtualChannels()) {
				rows.add(new ProgramChannel(vctName, channel, this));
			}
		}
	}

	public Optional<String> getServiceNameOptional(final int programNumber) {
		return findServiceName(tvct, programNumber).or(() -> findServiceName(cvct, programNumber));
	}

	public Optional<String> getChannelNameOptional(final int sourceId) {
		return findChannelName(tvct, sourceId).or(() -> findChannelName(cvct, sourceId));
	}

	private static Optional<String> findServiceName(final VCT<? extends VCTsection> vct, final int programNumber) {
		VCTsection[] sections = vct.getSections();
		if (sections == null) {
			return Optional.empty();
		}
		for (VCTsection section : sections) {
			if (section == null) {
				continue;
			}
			for (VCTsection.VirtualChannel channel : section.getVirtualChannels()) {
				if (channel.getProgramNumber() == programNumber) {
					return Optional.of(getChannelLabel(channel));
				}
			}
		}
		return Optional.empty();
	}

	private static Optional<String> findChannelName(final VCT<? extends VCTsection> vct, final int sourceId) {
		VCTsection[] sections = vct.getSections();
		if (sections == null) {
			return Optional.empty();
		}
		for (VCTsection section : sections) {
			if (section == null) {
				continue;
			}
			for (VCTsection.VirtualChannel channel : section.getVirtualChannels()) {
				if (channel.getSourceId() == sourceId) {
					return Optional.of(getChannelLabel(channel));
				}
			}
		}
		return Optional.empty();
	}

	private static String getChannelLabel(final VCTsection.VirtualChannel channel) {
		String name = findGenericDescriptorsInList(channel.getDescriptorList(), ExtendedChannelNameDescriptor.class)
				.stream()
				.findFirst()
				.map(ExtendedChannelNameDescriptor::getLongChannelName)
				.filter(s -> !s.isBlank())
				.orElse(channel.getShortName());
		if (name == null || name.isBlank()) {
			return channel.getChannelNumberString();
		}
		return channel.getChannelNumberString() + " " + name;
	}

	public static class ProgramChannel {

		private final String vctName;
		private final VCTsection.VirtualChannel channel;
		private final ATSCTables atscTables;
		private final List<ATSCEITsection.Event> events;
		private final ATSCEITsection.Event currentEvent;
		private final ATSCEITsection.Event nextEvent;

		ProgramChannel(final String vctName, final VCTsection.VirtualChannel channel,
				final ATSCTables atscTables) {
			this.vctName = vctName;
			this.channel = channel;
			this.atscTables = atscTables;
			events = atscTables.getEit().getEventsForSource(channel.getSourceId());
			long currentTime = -1;
			STTsection latestStt = atscTables.getStt().getLatestSttSection();
			if (latestStt != null) {
				currentTime = latestStt.getSystemTime();
			}
			currentEvent = findCurrentEvent(events, currentTime);
			nextEvent = findNextEvent(events, currentTime);
		}

		public String getVctName() {
			return vctName;
		}

		public String getChannelNumber() {
			return channel.getChannelNumberString();
		}

		public int getMajorChannelNumber() {
			return channel.getMajorChannelNumber();
		}

		public int getMinorChannelNumber() {
			return channel.getMinorChannelNumber();
		}

		public String getShortName() {
			return channel.getShortName();
		}

		public int getProgramNumber() {
			return channel.getProgramNumber();
		}

		public int getSourceId() {
			return channel.getSourceId();
		}

		public String getServiceType() {
			return channel.getServiceTypeString();
		}

		public Integer getPmtPid() {
			return channel.getPmtPid();
		}

		public Integer getPcrPid() {
			return channel.getPcrPid();
		}

		public String getVideoPids() {
			return channel.getVideoPidsString();
		}

		public String getAudioPids() {
			return channel.getAudioPidsString();
		}

		public String getElementaryStreams() {
			return channel.getElementaryStreamsString();
		}

		public String getChannelText() {
			return atscTables.getEtt().getChannelText(channel.getSourceId());
		}

		public int getEventCount() {
			return events.size();
		}

		public String getCurrentEventTitle() {
			return currentEvent == null ? null : currentEvent.getTitle();
		}

		public String getCurrentEventText() {
			return currentEvent == null ? null : currentEvent.getExtendedText();
		}

		public String getNextEventTitle() {
			return nextEvent == null ? null : nextEvent.getTitle();
		}

		public String getNextEventStart() {
			return nextEvent == null ? null : nextEvent.getUtcStartTimeString();
		}

		private static ATSCEITsection.Event findCurrentEvent(final List<ATSCEITsection.Event> events,
				final long currentTime) {
			if (currentTime < 0) {
				return events.isEmpty() ? null : events.getFirst();
			}
			for (ATSCEITsection.Event event : events) {
				if ((event.getStartTime() <= currentTime)
						&& (currentTime < (event.getStartTime() + event.getLengthInSeconds()))) {
					return event;
				}
			}
			return null;
		}

		private static ATSCEITsection.Event findNextEvent(final List<ATSCEITsection.Event> events,
				final long currentTime) {
			if (currentTime < 0) {
				return events.size() > 1 ? events.get(1) : null;
			}
			for (ATSCEITsection.Event event : events) {
				if (event.getStartTime() >= currentTime) {
					return event;
				}
			}
			return null;
		}
	}
}

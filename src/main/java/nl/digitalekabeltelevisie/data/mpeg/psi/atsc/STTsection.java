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

import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import java.time.Instant;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class STTsection extends TableSectionExtendedSyntax {

	private static final Instant GPS_EPOCH = Instant.parse("1980-01-06T00:00:00Z");

	private final int protocolVersion;
	private final long systemTime;
	private final int gpsUtcOffset;
	private final int daylightSaving;
	private final int dsStatus;
	private final int dsDayOfMonth;
	private final int dsHour;
	private final List<Descriptor> descriptorList;

	public STTsection(final PsiSectionData rawData, final PID parent) {
		super(rawData, parent);

		byte[] data = rawData.getData();
		protocolVersion = getInt(data, 8, 1, MASK_8BITS);
		systemTime = getLong(data, 9, 4, 0xFFFF_FFFFL);
		gpsUtcOffset = getInt(data, 13, 1, MASK_8BITS);
		daylightSaving = getInt(data, 14, 2, MASK_16BITS);
		dsStatus = (daylightSaving & 0x8000) >> 15;
		dsDayOfMonth = (daylightSaving & 0x1F00) >> 8;
		dsHour = daylightSaving & MASK_8BITS;
		int descriptorsLength = Math.max(0, sectionLength - 17);
		descriptorList = DescriptorFactory.buildDescriptorList(data, 16, descriptorsLength, this);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.addTableSource(this::getTableModel, "System Time");
		t.add(new KVP("protocol_version", protocolVersion));
		t.add(new KVP("system_time", systemTime, getUtcTimeString()));
		t.add(new KVP("GPS_UTC_offset", gpsUtcOffset));
		t.add(new KVP("daylight_saving", daylightSaving));
		t.add(new KVP("DS_status", dsStatus));
		t.add(new KVP("DS_day_of_month", dsDayOfMonth));
		t.add(new KVP("DS_hour", dsHour));
		addListJTree(t, descriptorList, modus, "descriptors");
		return t;
	}

	public TableModel getTableModel() {
		FlexTableModel<STTsection, STTsection> tableModel = new FlexTableModel<>(buildSttTableHeader());
		tableModel.addData(this, List.of(this));
		tableModel.process();
		return tableModel;
	}

	static TableHeader<STTsection, STTsection> buildSttTableHeader() {
		return new TableHeaderBuilder<STTsection, STTsection>()
				.addRequiredRowColumn("UTC_time", STTsection::getUtcTimeString, String.class)
				.addRequiredRowColumn("system_time", STTsection::getSystemTime, Long.class)
				.addRequiredRowColumn("GPS_UTC_offset", STTsection::getGpsUtcOffset, Integer.class)
				.addRequiredRowColumn("DS_status", STTsection::getDsStatus, Integer.class)
				.addRequiredRowColumn("DS_day_of_month", STTsection::getDsDayOfMonth, Integer.class)
				.addRequiredRowColumn("DS_hour", STTsection::getDsHour, Integer.class)
				.addRequiredRowColumn("protocol_version", STTsection::getProtocolVersion, Integer.class)
				.addOptionalRowColumn("first_packet_no", STTsection::getFirst_packet_no, Integer.class)
				.addOptionalRowColumn("last_packet_no", STTsection::getLast_packet_no, Integer.class)
				.addOptionalRowColumn("occurrence_count", STTsection::getOccurrence_count, Integer.class)
				.build();
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public long getSystemTime() {
		return systemTime;
	}

	public int getGpsUtcOffset() {
		return gpsUtcOffset;
	}

	public int getDaylightSaving() {
		return daylightSaving;
	}

	public int getDsStatus() {
		return dsStatus;
	}

	public int getDsDayOfMonth() {
		return dsDayOfMonth;
	}

	public int getDsHour() {
		return dsHour;
	}

	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public String getUtcTimeString() {
		return GPS_EPOCH.plusSeconds(systemTime - gpsUtcOffset).toString();
	}
}

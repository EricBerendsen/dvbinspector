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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc.AtscMultipleString;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeader;
import nl.digitalekabeltelevisie.util.tablemodel.TableHeaderBuilder;

public class ATSCETTsection extends TableSectionExtendedSyntax {

	private final int protocolVersion;
	private final long etmId;
	private final int extendedTextMessageLength;
	private final AtscMultipleString extendedTextMessage;
	private int tableType = -1;

	public ATSCETTsection(final PsiSectionData rawData, final PID parent) {
		super(rawData, parent);

		byte[] data = rawData.getData();
		protocolVersion = getInt(data, 8, 1, MASK_8BITS);
		etmId = getLong(data, 9, 4, 0xFFFF_FFFFL);
		extendedTextMessageLength = Math.max(0, getSectionLength() - 14);
		extendedTextMessage = new AtscMultipleString(data, 13, extendedTextMessageLength);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.addTableSource(this::getTableModel, "EPG Text");
		t.add(new KVP("protocol_version", protocolVersion));
		t.add(new KVP("ETM_id", etmId));
		t.add(new KVP("source_id", getSourceId()));
		t.add(new KVP("event_id", getEventId()));
		t.add(new KVP("extended_text_message_length", extendedTextMessageLength));
		t.add(new KVP("extended_text_message", getExtendedText()));
		t.add(extendedTextMessage.getJTreeNode(modus));
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "ETT_table_id_extension";
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public long getEtmId() {
		return etmId;
	}

	public int getTableType() {
		return tableType;
	}

	void setTableType(final int tableType) {
		this.tableType = tableType;
	}

	public String getTableTypeDescription() {
		return MGTsection.getTableTypeDescription(tableType);
	}

	public int getSourceId() {
		return (int) ((etmId >> 16) & 0xFFFF);
	}

	public int getEventId() {
		return (int) ((etmId >> 2) & 0x3FFF);
	}

	public int getExtendedTextMessageLength() {
		return extendedTextMessageLength;
	}

	public AtscMultipleString getExtendedTextMessage() {
		return extendedTextMessage;
	}

	public String getExtendedText() {
		return extendedTextMessage.getText();
	}

	public TableModel getTableModel() {
		FlexTableModel<ATSCETTsection, ATSCETTsection> tableModel = new FlexTableModel<>(buildEttTableHeader());
		tableModel.addData(this, java.util.List.of(this));
		tableModel.process();
		return tableModel;
	}

	static TableHeader<ATSCETTsection, ATSCETTsection> buildEttTableHeader() {
		return new TableHeaderBuilder<ATSCETTsection, ATSCETTsection>()
				.addRequiredRowColumn("table_type", ATSCETTsection::getTableTypeDescription, String.class)
				.addRequiredRowColumn("ETM_id", ATSCETTsection::getEtmId, Long.class)
				.addRequiredRowColumn("source_id", ATSCETTsection::getSourceId, Integer.class)
				.addRequiredRowColumn("event_id", ATSCETTsection::getEventId, Integer.class)
				.addRequiredRowColumn("extended_text_message", ATSCETTsection::getExtendedText, String.class)
				.addOptionalRowColumn("message_length", ATSCETTsection::getExtendedTextMessageLength, Integer.class)
				.addOptionalRowColumn("section", ATSCETTsection::getSectionNumber, Integer.class)
				.build();
	}
}

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
import static nl.digitalekabeltelevisie.util.Utils.MASK_13BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_16BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_5BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getLong;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSectionExtendedSyntax;

public class MGTsection extends TableSectionExtendedSyntax {

	private final int protocolVersion;
	private final int tablesDefined;
	private final List<TableTypeEntry> tableTypeEntries;
	private final int descriptorsLength;
	private final List<Descriptor> descriptorList;

	public MGTsection(final PsiSectionData rawData, final PID parent) {
		super(rawData, parent);

		byte[] data = rawData.getData();
		protocolVersion = getInt(data, 8, 1, MASK_8BITS);
		tablesDefined = getInt(data, 9, 2, MASK_16BITS);
		tableTypeEntries = new ArrayList<>();

		int offset = 11;
		for (int i = 0; i < tablesDefined; i++) {
			int tableType = getInt(data, offset, 2, MASK_16BITS);
			int tableTypePid = getInt(data, offset + 2, 2, MASK_13BITS);
			int tableTypeVersionNumber = getInt(data, offset + 4, 1, MASK_5BITS);
			long numberBytes = getLong(data, offset + 5, 4, 0xFFFF_FFFFL);
			int tableTypeDescriptorsLength = getInt(data, offset + 9, 2, MASK_12BITS);
			List<Descriptor> tableTypeDescriptorList = DescriptorFactory.buildDescriptorList(data, offset + 11, tableTypeDescriptorsLength, this);

			tableTypeEntries.add(new TableTypeEntry(tableType, tableTypePid, tableTypeVersionNumber, numberBytes,
					tableTypeDescriptorsLength, tableTypeDescriptorList));
			offset += 11 + tableTypeDescriptorsLength;
		}

		descriptorsLength = getInt(data, offset, 2, MASK_12BITS);
		descriptorList = DescriptorFactory.buildDescriptorList(data, offset + 2, descriptorsLength, this);
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("protocol_version", protocolVersion));
		t.add(new KVP("tables_defined", tablesDefined));
		addListJTree(t, tableTypeEntries, modus, "tables");
		t.add(new KVP("descriptors_length", descriptorsLength));
		addListJTree(t, descriptorList, modus, "descriptors");
		return t;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public int getTablesDefined() {
		return tablesDefined;
	}

	public List<TableTypeEntry> getTableTypeEntries() {
		return tableTypeEntries;
	}

	public int getDescriptorsLength() {
		return descriptorsLength;
	}

	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public static String getTableTypeDescription(final int tableType) {
		return switch (tableType) {
			case 0x0000 -> "Terrestrial Virtual Channel Table, current";
			case 0x0001 -> "Terrestrial Virtual Channel Table, next";
			case 0x0002 -> "Cable Virtual Channel Table, current";
			case 0x0003 -> "Cable Virtual Channel Table, next";
			case 0x0004 -> "Channel Extended Text Table";
			case 0x0005 -> "DCC Selection Code Table";
			default -> {
				if ((0x0100 <= tableType) && (tableType <= 0x017F)) {
					yield "Event Information Table " + (tableType - 0x0100);
				}
				if ((0x0200 <= tableType) && (tableType <= 0x027F)) {
					yield "Event Extended Text Table " + (tableType - 0x0200);
				}
				if ((0x0301 <= tableType) && (tableType <= 0x03FF)) {
					yield "Rating Region Table " + (tableType - 0x0300);
				}
				if ((0x0400 <= tableType) && (tableType <= 0x0FFF)) {
					yield "User private table";
				}
				if ((0x1400 <= tableType) && (tableType <= 0x14FF)) {
					yield "Directed Channel Change Table " + (tableType - 0x1400);
				}
				yield "reserved";
			}
		};
	}

	public static class TableTypeEntry implements TreeNode {

		private final int tableType;
		private final int tableTypePid;
		private final int tableTypeVersionNumber;
		private final long numberBytes;
		private final int tableTypeDescriptorsLength;
		private final List<Descriptor> descriptorList;

		TableTypeEntry(final int tableType, final int tableTypePid, final int tableTypeVersionNumber,
				final long numberBytes, final int tableTypeDescriptorsLength, final List<Descriptor> descriptorList) {
			this.tableType = tableType;
			this.tableTypePid = tableTypePid;
			this.tableTypeVersionNumber = tableTypeVersionNumber;
			this.numberBytes = numberBytes;
			this.tableTypeDescriptorsLength = tableTypeDescriptorsLength;
			this.descriptorList = descriptorList;
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("table", tableType, getTableTypeDescription(tableType));
			t.add(new KVP("table_type", tableType, getTableTypeDescription(tableType)));
			t.add(new KVP("table_type_PID", tableTypePid));
			t.add(new KVP("table_type_version_number", tableTypeVersionNumber));
			t.add(new KVP("number_bytes", numberBytes));
			t.add(new KVP("table_type_descriptors_length", tableTypeDescriptorsLength));
			addListJTree(t, descriptorList, modus, "table_type_descriptors");
			return t;
		}

		public int getTableType() {
			return tableType;
		}

		public int getTableTypePid() {
			return tableTypePid;
		}

		public int getTableTypeVersionNumber() {
			return tableTypeVersionNumber;
		}

		public long getNumberBytes() {
			return numberBytes;
		}

		public int getTableTypeDescriptorsLength() {
			return tableTypeDescriptorsLength;
		}

		public List<Descriptor> getDescriptorList() {
			return descriptorList;
		}
	}
}

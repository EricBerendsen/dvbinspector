/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class MosaicDescriptor extends Descriptor {

	private List<LogicalCell> logicalCellList = new ArrayList<>();

	private int mosaicEntryPoint = 0;
	private int numberOfHorizontalElementaryCells = 0;
	private int numberOfVerticalElementaryCells = 0;


	public class LogicalCell implements TreeNode{
		/**
		 *
		 */
		private int logicalCellId ;
		private int logicalCellPresentationInfo ;
		private int elementaryCellFieldLength;
		private byte [] elementaryCellIds;
		private int cellLinkageInfo;

		private int bouquetId;
		private int originalNetworkId;
		private int transportStreamId;
		private int serviceId;
		private int eventId;



		/**
		 * @param logical_cell_id
		 * @param logical_cell_presentation_info
		 * @param elementary_cell_field_length
		 * @param elementary_cell_ids
		 * @param cell_linkage_info
		 */
		public LogicalCell(final int logical_cell_id, final int logical_cell_presentation_info, final int elementary_cell_field_length, final byte[] elementary_cell_ids, final int cell_linkage_info) {
			super();
			logicalCellId = logical_cell_id;
			logicalCellPresentationInfo = logical_cell_presentation_info;
			elementaryCellFieldLength = elementary_cell_field_length;
			elementaryCellIds = elementary_cell_ids;
			cellLinkageInfo = cell_linkage_info;
		}



		@Override
		public KVP getJTreeNode(int modus){
			KVP s = new KVP("logical_cell");
			s.add(new KVP("logical_cell_id",logicalCellId));
			s.add(new KVP("logical_cell_presentation_info",logicalCellPresentationInfo,getCodingOfLogicalCellPresentationInfo(logicalCellPresentationInfo)));
			s.add(new KVP("elementary_cell_field_length",elementaryCellFieldLength));
			s.add(new KVP("elementary_cell_ids",elementaryCellIds));
			s.add(new KVP("cell_linkage_info",cellLinkageInfo,getCodingOfCellLinkageInfo(cellLinkageInfo)));


			if(cellLinkageInfo==0x01){
				s.add(new KVP("bouquet_id",bouquetId));
			}
			if(cellLinkageInfo==0x02){
				s.add(new KVP("original_network_id",originalNetworkId));
				s.add(new KVP("transport_stream_id",transportStreamId));
				s.add(new KVP("service_id",serviceId,parentTableSection.getParentTransportStream().getPsi().getSdt().getServiceName(originalNetworkId,transportStreamId,serviceId)));
			}
			if(cellLinkageInfo==0x03){
				s.add(new KVP("original_network_id",originalNetworkId));
				s.add(new KVP("transport_stream_id",transportStreamId));
				s.add(new KVP("service_id",serviceId,parentTableSection.getParentTransportStream().getPsi().getSdt().getServiceName(originalNetworkId,transportStreamId,serviceId)));
			}
			if(cellLinkageInfo==0x04){
				s.add(new KVP("original_network_id",originalNetworkId));
				s.add(new KVP("transport_stream_id",transportStreamId));
				s.add(new KVP("service_id",serviceId,parentTableSection.getParentTransportStream().getPsi().getSdt().getServiceName(originalNetworkId,transportStreamId,serviceId)));
				s.add(new KVP("event",eventId));
			}

			return s;
		}



		public int getBouquetId() {
			return bouquetId;
		}



		public void setBouquetId(final int bouquetId) {
			this.bouquetId = bouquetId;
		}



		public int getEventId() {
			return eventId;
		}



		public void setEventId(final int eventId) {
			this.eventId = eventId;
		}



		public int getOriginalNetworkId() {
			return originalNetworkId;
		}



		public void setOriginalNetworkId(final int originalNetworkId) {
			this.originalNetworkId = originalNetworkId;
		}



		public int getServiceId() {
			return serviceId;
		}



		public void setServiceId(final int serviceId) {
			this.serviceId = serviceId;
		}



		public int getTransportStreamId() {
			return transportStreamId;
		}



		public void setTransportStreamId(final int transportStreamId) {
			this.transportStreamId = transportStreamId;
		}



	}

	public MosaicDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		mosaicEntryPoint = Utils.getInt(b, 2, 1, 0x80) >> 7;
		numberOfHorizontalElementaryCells = Utils.getInt(b, 2, 1, 0x70) >> 4;
		numberOfVerticalElementaryCells = Utils.getInt(b, 2, 1, 0x07);
		int t = 1;
		while (t < descriptorLength) {
			int logical_cell_id = Utils.getInt(b, t + 2, 1, 0xFC) >> 2;
			int logical_cell_presentation_info = Utils.getInt(b, t + 3, 1, Utils.MASK_3BITS);
			int elementary_cell_field_length = Utils.getInt(b, t + 4, 1, Utils.MASK_8BITS);
			byte[] elementary_cell_ids = Utils.getBytes(b, t + 5, elementary_cell_field_length);
			int cell_linkage_info = Utils.getInt(b, t + 5 + elementary_cell_field_length, 1, Utils.MASK_8BITS);
			LogicalCell logicalCell = new LogicalCell(logical_cell_id, logical_cell_presentation_info, elementary_cell_field_length,
					elementary_cell_ids, cell_linkage_info);
			t = t + 4 + elementary_cell_field_length;
			if (cell_linkage_info == 0x01) {
				logicalCell.setBouquetId(Utils.getInt(b, t + 2, 2, Utils.MASK_16BITS));
				t = t + 2;
			}
			if (cell_linkage_info == 0x02) {
				logicalCell.setOriginalNetworkId(Utils.getInt(b, t + 2, 2, Utils.MASK_16BITS));
				logicalCell.setTransportStreamId(Utils.getInt(b, t + 4, 2, Utils.MASK_16BITS));
				logicalCell.setServiceId(Utils.getInt(b, t + 6, 2, Utils.MASK_16BITS));
				t = t + 6;
			}
			if (cell_linkage_info == 0x03) {
				logicalCell.setOriginalNetworkId(Utils.getInt(b, t + 2, 2, Utils.MASK_16BITS));
				logicalCell.setTransportStreamId(Utils.getInt(b, t + 4, 2, Utils.MASK_16BITS));
				logicalCell.setServiceId(Utils.getInt(b, t + 6, 2, Utils.MASK_16BITS));
				t = t + 6;
			}
			if (cell_linkage_info == 0x04) {
				logicalCell.setOriginalNetworkId(Utils.getInt(b, t + 2, 2, Utils.MASK_16BITS));
				logicalCell.setTransportStreamId(Utils.getInt(b, t + 4, 2, Utils.MASK_16BITS));
				logicalCell.setServiceId(Utils.getInt(b, t + 6, 2, Utils.MASK_16BITS));
				logicalCell.setEventId(Utils.getInt(b, t + 8, 2, Utils.MASK_16BITS));
				t = t + 8;
			}
			logicalCellList.add(logicalCell);
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (LogicalCell logicalCell : logicalCellList) {
			buf.append(logicalCell.toString());
		}


		return buf.toString();
	}


	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("mosaic_entry_point",mosaicEntryPoint ,mosaicEntryPoint==1?"entry point":"sub tree"));
		t.add(new KVP("number_of_horizontal_elementary_cells",numberOfHorizontalElementaryCells ,getNumberOfElementaryCells(numberOfHorizontalElementaryCells)));
		t.add(new KVP("number_of_vertical_elementary_cells",numberOfVerticalElementaryCells ,getNumberOfElementaryCells(numberOfVerticalElementaryCells)));
		Utils.addListJTree(t,logicalCellList,modus,"logical_cell_list");
		return t;
	}

	public static String getNumberOfElementaryCells(int cells) {
		return switch (cells) {
		case 0x0 -> "one cell";
		case 0x1 -> "two cells";
		case 0x2 -> "three cells";
		case 0x3 -> "four cells";
		case 0x4 -> "five cells";
		case 0x5 -> "six cells";
		case 0x6 -> "seven cells";
		case 0x7 -> "eight cells";
		default -> "illegal value";
		};
	}

	public static String getCodingOfLogicalCellPresentationInfo(int info) {
		return switch (info) {
		case 0x0 -> "undefined";
		case 0x1 -> "video";
		case 0x2 -> "still picture";
		case 0x3 -> "graphics/text";
		default -> "reserved for future use";
		};
	}

	public static String getCodingOfCellLinkageInfo(int info) {
		return switch (info) {
		case 0x0 -> "undefined";
		case 0x1 -> "bouquet related";
		case 0x2 -> "service related";
		case 0x3 -> "other mosaic related";
		case 0x4 -> "event related";
		default -> "reserved for future use";
		};
	}
}

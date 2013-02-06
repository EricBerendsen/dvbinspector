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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class MosaicDescriptor extends Descriptor {

	private List<LogicalCell> logicalCellList = new ArrayList<LogicalCell>();

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



		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("logical_cell"));
			s.add(new DefaultMutableTreeNode(new KVP("logical_cell_id",logicalCellId,null)));
			s.add(new DefaultMutableTreeNode(new KVP("logical_cell_presentation_info",logicalCellPresentationInfo,getCodingOfLogicalCellPresentationInfo(logicalCellPresentationInfo))));
			s.add(new DefaultMutableTreeNode(new KVP("elementary_cell_field_length",elementaryCellFieldLength,null)));
			s.add(new DefaultMutableTreeNode(new KVP("elementary_cell_ids",elementaryCellIds,null)));
			s.add(new DefaultMutableTreeNode(new KVP("cell_linkage_info",cellLinkageInfo,getCodingOfCellLinkageInfo(cellLinkageInfo))));


			if(cellLinkageInfo==0x01){
				s.add(new DefaultMutableTreeNode(new KVP("bouquet_id",bouquetId,null)));
			}
			if(cellLinkageInfo==0x02){
				s.add(new DefaultMutableTreeNode(new KVP("original_network_id",bouquetId,null)));
				s.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamId,null)));
				s.add(new DefaultMutableTreeNode(new KVP("service_id",serviceId,parentTableSection.getParentTransportStream().getPsi().getSdt().getServiceName(serviceId))));
			}
			if(cellLinkageInfo==0x03){
				s.add(new DefaultMutableTreeNode(new KVP("original_network_id",bouquetId,null)));
				s.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamId,null)));
				s.add(new DefaultMutableTreeNode(new KVP("service_id",serviceId,parentTableSection.getParentTransportStream().getPsi().getSdt().getServiceName(serviceId))));
			}
			if(cellLinkageInfo==0x04){
				s.add(new DefaultMutableTreeNode(new KVP("original_network_id",bouquetId,null)));
				s.add(new DefaultMutableTreeNode(new KVP("transport_stream_id",transportStreamId,null)));
				s.add(new DefaultMutableTreeNode(new KVP("service_id",serviceId,parentTableSection.getParentTransportStream().getPsi().getSdt().getServiceName(serviceId))));
				s.add(new DefaultMutableTreeNode(new KVP("event",eventId,null)));
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

	public MosaicDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);

		mosaicEntryPoint = Utils.getInt(b, offset+2, 1, 0x80)>>7;
		numberOfHorizontalElementaryCells = Utils.getInt(b, offset+2, 1, 0x70)>>4;
		numberOfVerticalElementaryCells= Utils.getInt(b, offset+2, 1, 0x07);
		int t=1;
		while (t<descriptorLength) {
			final int logical_cell_id = Utils.getInt(b, offset+t+2, 1, 0xFC)>>2;
		final int logical_cell_presentation_info = Utils.getInt(b, offset+t+3, 1, Utils.MASK_3BITS);
		final int elementary_cell_field_length = Utils.getInt(b, offset+t+4, 1, Utils.MASK_8BITS);
		final byte [] elementary_cell_ids = Utils.getBytes(b, offset+t+5, elementary_cell_field_length);
		final int cell_linkage_info=Utils.getInt(b, offset+t+5+elementary_cell_field_length, 1, Utils.MASK_8BITS);
		final LogicalCell s = new LogicalCell(logical_cell_id,logical_cell_presentation_info,elementary_cell_field_length,elementary_cell_ids,cell_linkage_info);
		t=t+4+elementary_cell_field_length;
		if(cell_linkage_info==0x01){
			s.setBouquetId(Utils.getInt(b, offset+t+2, 2, Utils.MASK_16BITS));
			t=t+2;
		}
		if(cell_linkage_info==0x02){
			s.setOriginalNetworkId(Utils.getInt(b, offset+t+2, 2, Utils.MASK_16BITS));
			s.setTransportStreamId(Utils.getInt(b, offset+t+4, 2, Utils.MASK_16BITS));
			s.setServiceId(Utils.getInt(b, offset+t+6, 2, Utils.MASK_16BITS));
			t=t+6;
		}
		if(cell_linkage_info==0x03){
			s.setOriginalNetworkId(Utils.getInt(b, offset+t+2, 2, Utils.MASK_16BITS));
			s.setTransportStreamId(Utils.getInt(b, offset+t+4, 2, Utils.MASK_16BITS));
			s.setServiceId(Utils.getInt(b, offset+t+6, 2, Utils.MASK_16BITS));
			t=t+6;
		}
		if(cell_linkage_info==0x04){
			s.setOriginalNetworkId(Utils.getInt(b, offset+t+2, 2, Utils.MASK_16BITS));
			s.setTransportStreamId(Utils.getInt(b, offset+t+4, 2, Utils.MASK_16BITS));
			s.setServiceId(Utils.getInt(b, offset+t+6, 2, Utils.MASK_16BITS));
			s.setEventId(Utils.getInt(b, offset+t+8, 2, Utils.MASK_16BITS));
			t=t+8;
		}
		logicalCellList.add(s);
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
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("mosaic_entry_point",mosaicEntryPoint ,mosaicEntryPoint==1?"entry point":"sub tree")));
		t.add(new DefaultMutableTreeNode(new KVP("number_of_horizontal_elementary_cells",numberOfHorizontalElementaryCells ,getNumberOfElementaryCells(numberOfHorizontalElementaryCells))));

		t.add(new DefaultMutableTreeNode(new KVP("number_of_vertical_elementary_cells",numberOfVerticalElementaryCells ,getNumberOfElementaryCells(numberOfVerticalElementaryCells))));
		Utils.addListJTree(t,logicalCellList,modus,"logical_cell_list");
		return t;
	}

	public static String getNumberOfElementaryCells(final int cells) {
		switch (cells) {
		case 0x0: return "one cell";
		case 0x1: return "two cells";
		case 0x2: return "three cells";
		case 0x3: return "four cells";
		case 0x4: return "five cells";
		case 0x5: return "six cells";
		case 0x6: return "seven cells";
		case 0x7: return "eight cells";
		default: return "illegal value";
		}
	}

	public static String getCodingOfLogicalCellPresentationInfo(final int info) {
		switch (info) {
		case 0x0: return "undefined";
		case 0x1: return "video";
		case 0x2: return "still picture";
		case 0x3: return "graphics/text";
		default: return "reserved for future use";
		}
	}

	public static String getCodingOfCellLinkageInfo(final int info) {
		switch (info) {
		case 0x0: return "undefined";
		case 0x1: return "bouquet related";
		case 0x2: return "service related";
		case 0x3: return "other mosaic related";
		case 0x4: return "event related";
		default: return "reserved for future use";
		}
	}
}

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
 *
 * represents single line of VBI data. Subclassed for Teletext, VPS en WSS signallling
 * See ETSI EN 301 775,
 * Digital Video Broadcasting (DVB);
 * Specification for the carriage of Vertical Blanking
 * Information (VBI) data in DVB bitstreams
 * 
 */
package nl.digitalekabeltelevisie.data.mpeg.pes.ebu;

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

public class EBUDataField implements TreeNode{


	/**
	 * this 8-bit field identifies the type of data unit. It is coded as in table 4:
	 * Table 4: data_unit_id
	 * data_unit_id value
	 * 0x00 to 0x01 reserved for future use
	 * 0x02 EBU Teletext non-subtitle data
	 * 0x03 EBU Teletext subtitle data
	 * 0x04 to 0x7F reserved for future use
	 * 0x80 to 0xFE user defined
	 * 0xFF data_unit for stuffing
	 */
	protected int dataUnitId;
	/**
	 * this 8-bit field indicates the number of bytes in the data unit following the length field.
	 * For data units carrying EBU Teletext data, this field shall always be set to 0x2
	 */
	protected int dataUnitLength;
	/**
	 * 
	 */


	protected int reserved_future_use;
	protected int field_parity;
	protected int line_offset;
	protected byte[] data_block;

	protected int offset;
	protected int len;
	protected long pts;
	protected int count = 1;



	public EBUDataField(final byte[] data,final int offset,final int len,final long pts) {
		dataUnitId = getInt(data, offset, 1, MASK_8BITS);
		dataUnitLength = getInt(data, offset+1, 1, MASK_8BITS);

		reserved_future_use = getInt(data, offset+2, 1, 0xC0)>>6;
		field_parity = getInt(data, offset+2, 1, 0x20)>>5;
		line_offset = getInt(data, offset+2, 1, MASK_5BITS);
		this.data_block = data;
		this.offset = offset;
		this.len = len;
		this.pts = pts;

	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP(EBUPESDataField.getDataUnitIdString(dataUnitId)));

		addDetailsToJTree(s,modus);
		return s;
	}

	/**
	 * @param s
	 */
	protected void addDetailsToJTree(final DefaultMutableTreeNode s, final int modus) {
		s.add(new DefaultMutableTreeNode(new KVP("data_unit_id",dataUnitId,EBUPESDataField.getDataUnitIdString(dataUnitId))));
		s.add(new DefaultMutableTreeNode(new KVP("data_unit_length",dataUnitLength,null)));
		s.add(new DefaultMutableTreeNode(new KVP("reserved_future_use",reserved_future_use,null)));
		s.add(new DefaultMutableTreeNode(new KVP("field_parity",field_parity,null)));
		s.add(new DefaultMutableTreeNode(new KVP("line_offset",line_offset,null)));
		s.add(new DefaultMutableTreeNode(new KVP("pts",pts, printTimebase90kHz(pts))));
		s.add(new DefaultMutableTreeNode(new KVP("count",count, null)));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = (PRIME * result) + dataUnitId;
		result = (PRIME * result) + dataUnitLength;
		result = (PRIME * result) + Arrays.hashCode(data_block);
		result = (PRIME * result) + field_parity;
		result = (PRIME * result) + line_offset;
		//result = PRIME * result + offset;
		result = (PRIME * result) + reserved_future_use;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		final EBUDataField other = (EBUDataField) obj;
		if (dataUnitId != other.dataUnitId){
			return false;
		}
		if (dataUnitLength != other.dataUnitLength){
			return false;
		}
		if (!Utils.equals(data_block,offset,len, other.data_block,other.offset, other.len)){
			return false;
		}
		if (field_parity != other.field_parity){
			return false;
		}
		if (line_offset != other.line_offset){
			return false;
		}
		if (reserved_future_use != other.reserved_future_use){
			return false;
		}
		return true;
	}


	/**
	 * @return the count
	 */
	public int getCount() {
		return count;
	}


	/**
	 * @param count the count to set
	 */
	public void setCount(final int count) {
		this.count = count;
	}

	public void incCount(final int t) {
		this.count += t;
	}


	/**
	 * @return the dataUnitId
	 */
	public int getDataUnitId() {
		return dataUnitId;
	}


	/**
	 * @return the dataUnitLength
	 */
	public int getDataUnitLength() {
		return dataUnitLength;
	}


	/**
	 * @return the field_parity
	 */
	public int getField_parity() {
		return field_parity;
	}


	/**
	 * @return the line_offset
	 */
	public int getLine_offset() {
		return line_offset;
	}


	/**
	 * @return the pts
	 */
	public long getPts() {
		return pts;
	}


	/**
	 * @return the data_block
	 */
	protected byte[] getData_block() {
		return data_block;
	}


	/**
	 * @return the len
	 */
	protected int getLen() {
		return len;
	}


	/**
	 * @return the offset
	 */
	protected int getOffset() {
		return offset;
	}


	/**
	 * @return the reserved_future_use
	 */
	protected int getReserved_future_use() {
		return reserved_future_use;
	}

}
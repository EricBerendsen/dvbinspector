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
package nl.digitalekabeltelevisie.data.mpeg.pes.audio;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Represents Ancillary Data for MPEG Audio
 * as described in annex C of ETSI TS 101 154 V1.9.1 (2009-09)
 * 
 * In the MPEG frame these bytes are located from the end in reverse order. Here the order is back to normal.
 * 
 * There is no need to align the UECP_data_byte field with the UECP frames. Consequently, one or more
 * complete UECP frames and/or only parts of UECP frames may be contained in one UECP_data_byte field.
 * 
 * @author Eric
 *
 */
public class AncillaryData implements TreeNode{

	private byte[] data_byte;
	private final int sync;
	private final int data_field_length;

	/**
	 * @param data
	 * @param offset
	 * @param len points to the first position after the MPEG frame, so the first byte of this is at data[offset+len-1], working back
	 */
	public AncillaryData(final byte[] data, final int offset, final int len) {
		super();
		sync= Utils.getInt(data, (offset+len)-1, 1, Utils.MASK_8BITS);
		data_field_length= Utils.getInt(data, (offset+len)-2, 1, Utils.MASK_8BITS);
		if(data_field_length>0){
			data_byte = new byte[data_field_length];
			for (int i = 0; i < data_field_length; i++) {
				data_byte[i]=data[(offset+len)-3-i];
			}
		}

	}


	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("AncillaryData"));
		s.add(new DefaultMutableTreeNode(new KVP("sync",sync,getSync(sync))));
		s.add(new DefaultMutableTreeNode(new KVP("data_field_length",data_field_length,null)));
		if(data_byte!=null){
			s.add(new DefaultMutableTreeNode(new KVP("data_byte",data_byte,null)));

		}

		return s;
	}

	private static String getSync(final int s){
		switch (s) {
		case 0xAD: return "announcement_switching_data_sync";
		case 0xFD: return "UECP_data_sync";
		case 0xFE: return "scale_factor_error_check data_sync";
		default:
			return "illegal value";
		}
	}


	public byte[] getDataByte() {
		return data_byte;
	}


	public int getSync() {
		return sync;
	}


	public int getDataFieldLength() {
		return data_field_length;
	}

}

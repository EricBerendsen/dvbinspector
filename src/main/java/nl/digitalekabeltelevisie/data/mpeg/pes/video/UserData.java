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

package nl.digitalekabeltelevisie.data.mpeg.pes.video;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;


/**
 * @author Eric Berendsen
 *
 */
public class UserData extends VideoMPEG2Section {

	private final byte[] data;
	private final int offset;
	private final int len;

	private int active_format_flag;
	private int active_format;
	private boolean isAFD=false;
	// TODO ??? support ATSC , containing afd and cc data ??
	// see ATSC Digital Television Standard: Part 4 – MPEG-2 Video System Characteristics
	// Document A/53 Part 4:2009, 7 August 2009 http://www.atsc.org/cms/standards/a53/a_53-Part-4-2009.pdf
	// 6.2.3 ATSC Picture User Data Semantics


	/**
	 * @param data
	 * @param offset
	 */
	public UserData(final byte[] data, final int offset) {
		super(data, offset);
		this.data = data;
		this.offset = offset;
		final int end = indexOf(data, new byte[]{0,0,1}, offset+1);
		len = end - offset-1;
		if(indexOf(data, new byte[]{0x44,0x54,0x47,0x31}, offset+1)==(offset+1)){ // DTG1
			isAFD=true;
			active_format_flag = getInt(data,offset+5,1,0x40)>>6;
			if(active_format_flag==1){
				active_format = getInt(data,offset+6,1,MASK_4BITS);
			}
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("data",data,offset+1, len,isAFD?"Active Format Description":null)));
		if(isAFD){
			t.add(new DefaultMutableTreeNode(new KVP("active_format_flag",active_format_flag,null)));
			t.add(new DefaultMutableTreeNode(new KVP("active_format",active_format,getActiveFormatString(active_format))));
		}
		return t;
	}



	public static String getActiveFormatString(final int active_format) {
		switch (active_format) {
		case 0x00:
			return "reserved";
		case 0x01:
			return "reserved";
		case 0x02:
			return "box 16:9 (top)";
		case 0x03:
			return "box 14:9 (top)";
		case 0x04:
			return "box > 16:9 (centre)";
		case 0x05:
			return "reserved";
		case 0x06:
			return "reserved";
		case 0x07:
			return "reserved";
		case 0x08:
			return "Active format is the same as the coded frame";
		case 0x09:
			return "4:3 (centre)";
		case 0x0A:
			return "16:9 (centre)";
		case 0x0B:
			return "14:9 (centre)";
		case 0x0C:
			return "reserved";
		case 0x0D:
			return "4:3 (with shoot & protect 14:9 centre)";
		case 0x0E:
			return "16:9 (with shoot & protect 14:9 centre)";
		case 0x0F:
			return "16:9 (with shoot & protect 4:3 centre)";
		default:
			return "unknown/error";
		}
	}


	/**
	 * @return the active_format
	 */
	public int getActive_format() {
		return active_format;
	}


	/**
	 * @return the active_format_flag
	 */
	public int getActive_format_flag() {
		return active_format_flag;
	}


	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}


	/**
	 * @return the isAFD
	 */
	public boolean isAFD() {
		return isAFD;
	}


	/**
	 * @return the len
	 */
	public int getLen() {
		return len;
	}


	/**
	 * @return the offset
	 */
	public int getOffset() {
		return offset;
	}
}

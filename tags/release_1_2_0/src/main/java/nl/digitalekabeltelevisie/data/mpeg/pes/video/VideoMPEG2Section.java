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

import static nl.digitalekabeltelevisie.util.Utils.indexOf;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.util.BitSource;

/**
 * A VideoMPEG2Section is a section as defined in 6.2 of ISO/IEC 13818-2,
 * So either a slice, or sequence header, picture start, etc...
 *
 * @author Eric
 *
 */
public class VideoMPEG2Section implements TreeNode{

	/**
	 * startCode of this section as defined in ISO13818-2 6.2.1
	 */
	protected int startCode;
	private int offset;
	private byte[]data;
	// helper to get bits instead of bytes
	protected BitSource bs;

	public VideoMPEG2Section(final byte[]data, final int offset) {
		super();
		bs = new BitSource(data, offset);
		this.data = data;
		this.offset = offset;
		this.startCode = bs.readBits(8);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP(VideoPESDataField.getSectionTypeString(startCode)));

		t.add(new DefaultMutableTreeNode(new KVP("length",getLength(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("start_code",startCode,VideoPESDataField.getStartCodeString(startCode))));
		t.add(new DefaultMutableTreeNode(new KVP("data",data,offset,getLength(),null)));
		return t;
	}


	/**
	 * @return the startCode
	 */
	public int getStartCode() {
		return startCode;
	}

	protected int getLength(){
		int i = indexOf(data, new byte[]{0,0,1},offset); // look for start code
		if(i>=0){ // next section in same byte[]
			return (i-offset);
		}
		i = indexOf(data, new byte[]{0,0,0},offset); // look for stuffing
		if(i>=0){ //
			return (i-offset);
		}
		// fall back to length of array
		return data.length - offset;
	}

	public String toString(){
		if(startCode==0){
			return VideoPESDataField.getSectionTypeString(startCode)+" ("+((PictureHeader)this).getPictureCodingTypeShortString()+")";
		}else{
			return VideoPESDataField.getSectionTypeString(startCode);
		}
	}
}
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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.Utils;


/**
 * @author Eric Berendsen
 *
 */
public class GroupOfPicturesHeader extends VideoMPEG2Section {

	private final int time_code;
	private final int closed_gop;
	private final int broken_link;


	/**
	 * @param data
	 * @param offset
	 */
	public GroupOfPicturesHeader(final byte[] data, final int offset) {
		super(data, offset);
		time_code = Utils.getInt(data, offset+1, 4, 0xFFFFFF80)>>7;
		closed_gop = Utils.getInt(data, offset+4, 1, 0x40)>>6;
		broken_link = Utils.getInt(data, offset+4, 1, 0x20)>>5;

	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("time_code",time_code,getTimeCodeString(time_code))));
		t.add(new DefaultMutableTreeNode(new KVP("closed_gop",closed_gop,closed_gop==1?"following B-pictures have been encoded using only backward prediction or intra coding":null)));
		t.add(new DefaultMutableTreeNode(new KVP("broken_link",broken_link,broken_link==1?"consecutive B-Pictures (if any) immediately following the first coded I-frame following the group of picture header may not be correctly decoded because the reference frame which is used for prediction is not available":null)));
		return t;
	}

	public static String getTimeCodeString(final int tc){

		final StringBuilder b = new StringBuilder("drop_frame_flag: ");
		final int drop_frame_flag = (tc&0x1000000)>>24 ;
		b.append(drop_frame_flag).append(", time: ");
		final int hours = (tc&0x0F80000)>>19;
		b.append(hours).append(":");
		final int minutes = (tc&0x007E000)>>13;
		b.append(minutes).append(":");
		final int seconds = (tc&0x0000FC0)>>6;
		b.append(seconds).append(":");
		final int pictures = (tc&0x000003F);
		b.append(pictures);
		return b.toString();
	}


	/**
	 * @return the broken_link
	 */
	public int getBroken_link() {
		return broken_link;
	}


	/**
	 * @return the closed_gop
	 */
	public int getClosed_gop() {
		return closed_gop;
	}


	/**
	 * @return the time_code
	 */
	public int getTime_code() {
		return time_code;
	}

}

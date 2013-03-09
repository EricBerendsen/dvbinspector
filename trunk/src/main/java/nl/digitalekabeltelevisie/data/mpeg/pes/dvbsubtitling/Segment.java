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

package nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.ImageSource;

/**
 * @author Eric Berendsen
 *
 */
public class Segment implements TreeNode{

	/**
	 *
	 */
	protected byte[] data_block;

	/**
	 *
	 */
	protected int offset;

	/**
	 * @return
	 */
	public int getSegmentType(){
		return getSegmentType(1);
	}

	/**
	 * @param localOffset
	 * @return
	 */
	private int getSegmentType(final int localOffset) {

		return getInt(data_block, offset+ localOffset, 1, MASK_8BITS);
	}

	/**
	 * @param data
	 * @param offset
	 */
	public Segment(final byte[] data,final int offset) {
		data_block = data;
		this.offset = offset;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus, final ImageSource imgSource) {
		final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("Segment (" +DVBSubtitlingPESDataField.getSegmentTypeString(getSegmentType())+")", imgSource));
		s.add(new DefaultMutableTreeNode(new KVP("raw_data",data_block,offset,getSegmentLength()+6,null)));
		s.add(new DefaultMutableTreeNode(new KVP("segment_type",getSegmentType(),DVBSubtitlingPESDataField.getSegmentTypeString(getSegmentType()))));
		s.add(new DefaultMutableTreeNode(new KVP("page_id",getPageID(),null)));
		s.add(new DefaultMutableTreeNode(new KVP("segment_length",getSegmentLength(),null)));

		return s;
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		return getJTreeNode(modus, null);
	}

	/**
	 * @return
	 */
	public int getPageID() {
		return getInt(data_block, offset+2, 2, MASK_16BITS);
	}

	/**
	 * @return
	 */
	public int getSegmentLength() {
		return getInt(data_block, offset+4, 2, MASK_16BITS);
	}
}
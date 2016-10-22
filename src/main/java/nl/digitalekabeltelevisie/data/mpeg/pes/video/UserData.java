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

import nl.digitalekabeltelevisie.data.mpeg.pes.AuxiliaryData;


/**
 * @author Eric Berendsen
 *
 */
public class UserData extends VideoMPEG2Section {

	private final byte[] data;
	private final int offset;
	private final int len;

	private final AuxiliaryData auxData;


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

		auxData = new AuxiliaryData(data, offset+1, len);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(auxData.getJTreeNode(modus));
		return t;
	}


	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
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

	public AuxiliaryData getAuxData() {
		return auxData;
	}
}

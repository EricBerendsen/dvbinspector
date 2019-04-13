/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2013 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.util;

import javax.swing.tree.MutableTreeNode;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

/**
 *
 * Enables display of all TS-packets belonging to one TransportStream
 *
 * @author Eric
 *
 */
public class TSPacketGetter implements LazyListItemGetter {


	TransportStream transportStream = null;
	int modus;
	/**
	 *
	 */
	public TSPacketGetter(TransportStream ts, int modus) {
		transportStream = ts;
		this.modus=modus;
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.util.LazyListItemGetter#getTreeNode(int)
	 */
	@Override
	public MutableTreeNode getTreeNode(int i) {

		return transportStream.getTSPacket(i).getJTreeNode(modus);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.util.LazyListItemGetter#getNoItems()
	 */
	@Override
	public int getNoItems() {

		return transportStream.getNo_packets();
	}

}

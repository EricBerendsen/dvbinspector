/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.Arrays;

import javax.swing.tree.MutableTreeNode;

import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

/**
 * Enables display of all TS-packets belonging to one PID.
 *
 * @author Eric
 *
 */
public class PIDPacketGetter implements LazyListItemGetter {


	TransportStream transportStream = null;
	int modus;
	int pid;
	int[] packetMapping = null;
	/**
	 *
	 */
	public PIDPacketGetter(TransportStream ts, int pid, int modus) {
		transportStream = ts;
		this.pid = pid;
		this.modus = modus;
	}

	/**
	 * returns i-th TS-packet of this PID. first need to do a mapping from index in PID to index in transportstream
	 * @see nl.digitalekabeltelevisie.util.LazyListItemGetter#getTreeNode(int)
	 */
	@Override
	public MutableTreeNode getTreeNode(int i) {
		if(packetMapping==null){
			buildPacketMapping();
		}

		return transportStream.getTSPacket(packetMapping[i]).getJTreeNode(modus);
	}

	private void buildPacketMapping() {
		int k = 0;
		packetMapping = new int [getNoItems()];
		for (int j = 0; j < transportStream.getNo_packets(); j++) {
			int p = transportStream.getPacket_pid(j);
			if(p==pid){
				packetMapping[k++]=j;
			}
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.util.LazyListItemGetter#getNoItems()
	 */
	@Override
	public int getNoItems() {

		 PID p = transportStream.getPID(pid);
		 if(p!=null){
			 return p.getPackets();
		 }
		 return 0;

	}
	
	@Override
	public int getActualNumberForIndex(int i) {
		if(packetMapping==null){
			buildPacketMapping();
		}

		return transportStream.getTSPacket(packetMapping[i]).getPacketNo();
	}
	
	@Override
	public int getIndexForActualNumber(int a) {
		return Arrays.binarySearch(packetMapping, a);
		
	}


}

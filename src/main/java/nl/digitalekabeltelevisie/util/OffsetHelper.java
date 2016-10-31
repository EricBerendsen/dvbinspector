/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

public class OffsetHelper {

	private RangeHashMap<Integer, Long> rangeHashMap = new RangeHashMap<>();
	private int maxPackets = -1;
	private int packetLength = 0;
	
	RangeHashMap<Integer, Long>.Entry currentEntry = null;

	public OffsetHelper(int max_packets, int packetLength) {
		this.maxPackets = max_packets;
		this.packetLength = packetLength;
	}

	public void addPacket(int packetNo, long offset) {
		if(currentEntry == null){
			currentEntry = rangeHashMap.new Entry(0,maxPackets, offset); 
			rangeHashMap.put(0, currentEntry);
		}else if(calculateOffset(packetNo, currentEntry) != offset){
			currentEntry.setUpper(packetNo - 1);
			currentEntry = rangeHashMap.new Entry(packetNo, maxPackets, offset);
			rangeHashMap.put(packetNo, currentEntry);
		}
	}

	public int getMaxPacket() {
		return maxPackets;
	}

	public long getOffset(int packetNo) {
		RangeHashMap<Integer, Long>.Entry entry = rangeHashMap.findEntry(packetNo);
		return calculateOffset(packetNo, entry); 
	}

	private long calculateOffset(int packetNo, RangeHashMap<Integer, Long>.Entry entry) {
		return entry.getValue() + ((long)(packetNo - entry.getLower()) * packetLength);
	}


}

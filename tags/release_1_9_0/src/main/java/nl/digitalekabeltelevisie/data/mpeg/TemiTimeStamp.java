/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
package nl.digitalekabeltelevisie.data.mpeg;

import java.math.*;

public class TemiTimeStamp {

	private final int packetNo;
	private final BigInteger media_timestamp;
	private long timescale;
	private int discontinuity;
	private int paused;

	public TemiTimeStamp(int packetNo, BigInteger time) {
		this(packetNo,time,0,0,0);
	}

	public TemiTimeStamp(int packetNo, BigInteger media_timestamp, long timescale, int discontinuity,
			int paused) {
		this.packetNo = packetNo;
		this.media_timestamp = media_timestamp;
		this.timescale = timescale;
		this.discontinuity = discontinuity;
		this.paused = paused;
	}

	public double getTime(){
		return media_timestamp.doubleValue()/timescale;
	}
	public int getPacketNo() {
		return packetNo;
	}

	public BigInteger getMediaTimeStamp() {
		return media_timestamp;
	}

	public long getTimescale() {
		return timescale;
	}

	public int getDiscontinuity() {
		return discontinuity;
	}

	public int getPaused() {
		return paused;
	}
	

}

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

import static nl.digitalekabeltelevisie.util.Utils.printTimebase90kHz;

import java.math.BigInteger;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.gui.HTMLSource;

public class TemiTimeStamp implements TreeNode, HTMLSource {

	private final int packetNo;
	private final long pts;
	private final BigInteger media_timestamp;
	private final long timescale;
	private final int discontinuity;
	private final int paused;


	public TemiTimeStamp(int packetNo, long pts, BigInteger media_timestamp, long timescale, int discontinuity,
			int paused) {
		this.packetNo = packetNo;
		this.pts = pts;
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
	
	public long getPts() {
		return pts;
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

	@Override
	public KVP getJTreeNode(int modus) {
		KVP kvp = new KVP(String.format("temi (time %.2f sec)", media_timestamp.floatValue() / timescale));
		kvp.addHTMLSource(this, "temi packet");
		kvp.add(new KVP("packetNo", packetNo));
		kvp.add(new KVP("pts", pts, printTimebase90kHz(pts)));
		kvp.add(new KVP("media_timestamp", media_timestamp));
		kvp.add(new KVP("timescale", timescale));
		kvp.add(new KVP("paused", paused));
		kvp.add(new KVP("discontinuity", discontinuity));
		return kvp;
	}

	@Override
	public String getHTML() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("packetNo: ").append(packetNo).append("<br>");
		stringBuilder.append("PTS: ").append(printTimebase90kHz(pts)).append("<br>");
		stringBuilder.append("media_timestamp: ").append(media_timestamp).append("<br>");
		stringBuilder.append("timescale: ").append(timescale).append("<br>");
		stringBuilder.append("paused: ").append(paused).append("<br>");
		stringBuilder.append("discontinuity:").append(discontinuity).append("<br>");
		stringBuilder.append("</html>");

		return stringBuilder.toString();
	}	

}

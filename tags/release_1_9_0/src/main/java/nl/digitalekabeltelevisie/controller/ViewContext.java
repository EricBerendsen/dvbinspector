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

package nl.digitalekabeltelevisie.controller;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

/**
 * @author Eric Berendsen
 *
 */

public class ViewContext {

	private List<ChartLabel> shown = new ArrayList<ChartLabel>();
	private List<ChartLabel> notShown = new ArrayList<ChartLabel>();

	private int startPacket ;
	private int endPacket;
	private int maxPacket;
	private TransportStream transportStream;

	private int graphSteps =100;

	public int getEndPacket() {
		return endPacket;
	}

	public void setEndPacket(final int endPacket) {
		this.endPacket = endPacket;
	}

	public int getGraphSteps() {
		return graphSteps;
	}

	public void setGraphSteps(final int graphSteps) {
		this.graphSteps = graphSteps;
	}

	public List<ChartLabel> getNotShown() {
		return notShown;
	}

	public void setNotShown(final List<ChartLabel> notShown) {
		this.notShown = notShown;
	}

	public List<ChartLabel> getShown() {
		return shown;
	}

	public void setShown(final List<ChartLabel> shown) {
		this.shown = shown;
	}

	public int getStartPacket() {
		return startPacket;
	}

	public void setStartPacket(final int startPacket) {
		this.startPacket = startPacket;
	}

	public int getMaxPacket() {
		return maxPacket;
	}

	public void setMaxPacket(final int maxPacket) {
		this.maxPacket = maxPacket;
	}

	public TransportStream getTransportStream() {
		return transportStream;
	}

	public void setTransportStream(final TransportStream transportStream) {
		this.transportStream = transportStream;
	}

}

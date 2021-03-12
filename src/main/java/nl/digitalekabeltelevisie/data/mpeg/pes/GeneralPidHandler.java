/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2018 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.pes;

import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TSPacket;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

public abstract class GeneralPidHandler implements TreeNode{

	private TransportStream transportStream = null;

	public abstract void processTSPacket(final TSPacket packet);
	
	public void postProcess() {
		// EMPTY default
	}

	/**
	 * by default the contents of a PES PID is not read or analyzed. After explicit user command PID is initialized
	 */
	protected boolean initialized = false;

	public TransportStream getTransportStream() {
		return transportStream;
	}

	public void setTransportStream(TransportStream transportStream) {
		this.transportStream = transportStream;
	}

	protected PID pid;

	public GeneralPidHandler() {
		super();
	}

	public void setPID(PID pid) {
		this.pid = pid;
	
	}

	public PID getPID() {
		return pid;
	
	}


	public boolean isInitialized() {
		return initialized;
	}
	
	public String getMenuDescription() {
		return "Parse data";
	}

}
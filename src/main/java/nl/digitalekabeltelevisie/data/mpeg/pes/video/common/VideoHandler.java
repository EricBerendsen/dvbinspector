package nl.digitalekabeltelevisie.data.mpeg.pes.video.common;

/**
*
*  http://www.digitalekabeltelevisie.nl/dvb_inspector
*
*  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;

public abstract class VideoHandler extends GeneralPesHandler {
	
	protected Cea608 cea608 = new Cea608();


	public VideoHandler() {
		super();
	}
	
	/**
	 * @param modus
	 * @param s
	 */
	protected void addCCDataToTree(final int modus, final DefaultMutableTreeNode s) {
		cea608.addCCDataToTree(modus, s);
	}
	
	
	
	@Override
	public void postProcess() {
		// collect sei_messages UserDataRegisteredItuT35Sei_message GA94 cc data Line 21  
		collectCEA708Data();
		cea608.handleXDSData();
	}

	


	protected abstract void collectCEA708Data() ;

	/**
	 * @param pts
	 * @param auxData
	 */
	protected void find708AuxData(long pts, AuxiliaryData auxData) {
		cea608.find708AuxData(pts, auxData);
	}

	/**
	 * @param num_units_in_tick
	 * @param time_scale
	 * @return
	 */
	public static String getClockTickString(final long num_units_in_tick, final long time_scale) {
		return String.format("clock tick:  %+4.2f  seconds, framerate: %+4.2f fps", 
				(double) num_units_in_tick / (double) time_scale,
				(double)time_scale  / (double) num_units_in_tick);
	}


}
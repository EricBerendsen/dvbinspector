/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.gui.utils;

import java.awt.Graphics2D;
import java.awt.geom.*;

import nl.digitalekabeltelevisie.controller.KVP;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;

/**
 * @author Eric
 *
 */
public final class GuiUtils {

	/**
	 * 
	 */
	public static final String NO_TRANSPORTSTREAM_LOADED = "No transportstream loaded";

	/**
	 *
	 */
	private GuiUtils() {
		// Do not instantiate
	}

	/**
	 * @param title
	 * @return
	 */
	public static JFreeChart createTitleOnlyChart(final String title) {
		final Plot plot = new Plot() {

			@Override
			public String getPlotType() {
				// don't care about actual value
				return "message";
			}

			@Override
			public void draw(final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info) {
				// empty
			}
		};
		final JFreeChart mesg = new JFreeChart(title,plot);
		return mesg;
	}

	public static KVP getNotImplementedKVP(final String feature){
		final StringBuilder message = new StringBuilder();
		message.append(feature).append(" not implemented. ").append(getImproveMsg());
		return new KVP("<span style=\"color: red;\">"+message.toString()+"</span>",message.toString());
	}

	/**
	 * @return
	 */
	public static String getImproveMsg() {
		final String version = getVersionString();

		final String improveMsg = "You can help to improve DVB Inspector by making this stream available " +
				"to Eric Berendsen\n(e_ber"+"endsen@digitalekabeltel"+"evisie.nl)\n\n" +
				"Please include the version of DVB Inspector: "+version;
		return improveMsg;
	}

	/**
	 * @return
	 */
	public static String getVersionString() {
		final Package p = GuiUtils.class.getPackage();
		String version = p.getImplementationVersion();

		if(version==null){
			version="development version (unreleased)";
		}
		return version;
	}

}

/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

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
	public static final String NO_TRANSPORTSTREAM_LOADED = "No transportstream loaded, drag a MPEG-TS (*.ts;*.mpg;*.mpeg;*.m2ts;*.mts;*.tsa;*.tsv) file here";

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

	public static KVP getErrorKVP(final String message){
		return new KVP("<span style=\"color: red;\">"+message.toString()+"</span>",message.toString());
	}

	/**
	 * @return
	 */
	public static String getImproveMsg() {
		final String version = getVersionString();

		final String improveMsg = "You can help to improve DVB Inspector by making this stream available\n" +
				"to Eric Berendsen (e_ber"+"endsen@digitalekabeltel"+"evisie.nl)\n\n" +
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

	public static BufferedImage getErrorImage(String str) {
		final int width = 800;
		final int height = 450;
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D gd = img.createGraphics();
	
		gd.setColor(Color.GRAY);
		gd.fillRect(0, 0, width, height);
	
		Color[] testBarColors = {
				Color.WHITE, 
				Color.YELLOW, 
				Color.CYAN, 
				Color.GREEN, 
				Color.MAGENTA, 
				Color.RED, 
				Color.BLUE,
				Color.BLACK 
				};
		
		int barsHeight = 40;
	
		for (int i = 0; i < testBarColors.length; i++) {
			gd.setColor(testBarColors[i]);
			gd.fillRect(i * width / 8, 0, width / 8, barsHeight);
			gd.fillRect(i * width / 8, height -barsHeight, width / 8, barsHeight);
		}		
	
		gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
		final Font font = new Font("Arial", Font.BOLD, 20);
		gd.setFont(font);
		
		
	
		gd.setColor(Color.WHITE);
		
		int x = 20;
		int y = 100;
		for (String line : str.split("\n")) {
	        gd.drawString(line, x, y += gd.getFontMetrics().getHeight());
		}
	
		return img;
	}

}

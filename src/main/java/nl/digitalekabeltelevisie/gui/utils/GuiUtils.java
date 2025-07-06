/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.util.PreferencesManager;

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

	public static JFreeChart createTitleOnlyChart(String title) {
		Plot plot = new Plot() {

			@Override
			public String getPlotType() {
				// don't care about actual value
				return "message";
			}

			@Override
			public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
				// empty
			}
		};
		return new JFreeChart(title,plot);
	}

	public static KVP getNotImplementedKVP(String feature){
		StringBuilder message = new StringBuilder();
		message.append(feature).append(" not implemented. ").append(getImproveMsg());
		return new KVP(message.toString()).setHtmlLabel("<span style=\"color: red;\">"+ message +"</span>");
	}

	public static KVP getErrorKVP(String message){
		return new KVP( message).setHtmlLabel("<span style=\"color: red;\">"+ message +"</span>");
	}

	public static String getImproveMsg() {
		String version = getVersionString();

		return "You can help to improve DVB Inspector by making this stream available\n" +
				"to Eric Berendsen (e_ber"+"endsen@digitalekabeltel"+"evisie.nl)\n\n" +
				"Please include the version of DVB Inspector: "+version;
	}

	public static String getVersionString() {
		Package p = GuiUtils.class.getPackage();
		String version = p.getImplementationVersion();

		if(version==null){
			version="development version (unreleased)";
		}
		return version;
	}

	public static BufferedImage getErrorImage(String str) {
		final int width = 800;
		final int height = 450;
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D gd = img.createGraphics();
	
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
	
		Font font = new Font("Arial", Font.BOLD, 20);
		gd.setFont(font);
		gd.setColor(Color.WHITE);
		
		int x = 20;
		int y = 100;
		for (String line : str.split("\n")) {
	        gd.drawString(line, x, y += gd.getFontMetrics().getHeight());
		}
		return img;
	}

	public static JFileChooser createFileChooser() {
		return createFileChooser(List.of());
	
	}

	public static JFileChooser createFileChooser(List<FileNameExtensionFilter> fileNameExtensionFilters) {
	JFileChooser chooser = new JFileChooser();
	for(FileNameExtensionFilter fileNameExtensionFilter :fileNameExtensionFilters) {
		chooser.addChoosableFileFilter(fileNameExtensionFilter);
	}
	chooser.setAcceptAllFileFilterUsed(fileNameExtensionFilters.isEmpty());
	final String defaultDir = PreferencesManager.getSaveDir();
	if (defaultDir != null) {
		final File defDir = new File(defaultDir);
		chooser.setCurrentDirectory(defDir);
	}
	return chooser;
}

}

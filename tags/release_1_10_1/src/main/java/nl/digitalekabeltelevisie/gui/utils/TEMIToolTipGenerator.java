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

package nl.digitalekabeltelevisie.gui.utils;

import java.text.NumberFormat;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

import nl.digitalekabeltelevisie.data.mpeg.TemiTimeStamp;

public class TEMIToolTipGenerator implements XYToolTipGenerator {
	
	private NumberFormat timeFormat;

	public TEMIToolTipGenerator(NumberFormat packetNumberFormat, NumberFormat timeFormat) {
		super();
		this.packetNumberFormat = packetNumberFormat;
		this.timeFormat = timeFormat;
	}

	NumberFormat packetNumberFormat;

	@Override
	public String generateToolTip(XYDataset  dataset, int series, int item){
		TEMIXYDataset temiDataSet = (TEMIXYDataset)dataset;
		String label = temiDataSet.getSeriesKey(series);
		TemiTimeStamp timeStamp = temiDataSet.getTimestamp(series,item);

		StringBuilder stringBuilder = new StringBuilder("<html>").append(label).append("<br>");
		stringBuilder.append(packetNumberFormat.format(timeStamp.getPacketNo())).append("<br>");
		stringBuilder.append("Time: ").append(timeFormat.format(timeStamp.getTime())).append("<br>");
		stringBuilder.append("media_timestamp: ").append(timeStamp.getMediaTimeStamp()).append("<br>");
		stringBuilder.append("time_scale: ").append(timeStamp.getTimescale()).append("<br>");
		stringBuilder.append("paused: ").append(timeStamp.getPaused()).append("<br>");
		stringBuilder.append("discontinuity:" ).append(timeStamp.getDiscontinuity()).append("<br>");
		stringBuilder.append("</html>");
				
		return stringBuilder.toString();
	}

}

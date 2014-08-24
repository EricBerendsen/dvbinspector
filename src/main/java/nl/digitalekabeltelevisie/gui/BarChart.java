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
package nl.digitalekabeltelevisie.gui;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 * @author Eric Berendsen
 */
public class BarChart extends ChartPanel implements TransportStreamView {

	/**
	 *
	 */
	private static final long serialVersionUID = -654471783180064471L;
	/**
	 *
	 */
	private JFreeChart	freeChart;

	/**
	 * @param transportStream
	 * @param viewContext
	 */
	public BarChart(final TransportStream transportStream, final ViewContext viewContext) {

		super(null);

		if (transportStream != null) {
			setTransportStream(transportStream, viewContext);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */

	public final void setTransportStream(final TransportStream transportStream, final ViewContext viewContext) {
		if(transportStream!=null){
			final int noPIDs = viewContext.getShown().size();
			final double[][] data = new double[3][noPIDs];
			final int steps = viewContext.getGraphSteps();

			final short[] used_pids = new short[noPIDs];
			final ChartLabel[] labels = new ChartLabel[noPIDs];
			for (int i = 0; i < noPIDs; i++) {
				labels[i] = new ChartLabel(transportStream.getShortLabel(viewContext.getShown().get(i).getPid()),
						viewContext.getShown().get(i).getPid());
				used_pids[i] = viewContext.getShown().get(i).getPid();
			}
			final ChartLabel[] stepLabels = new ChartLabel[3];
			stepLabels[0] = new ChartLabel("avg", (short) 0);
			stepLabels[1] = new ChartLabel("min", (short) 1);
			stepLabels[2] = new ChartLabel("max", (short) 2);

			final int startPacket = viewContext.getStartPacket();
			final int endPacket = viewContext.getEndPacket();
			final int noPackets = endPacket - startPacket;

			// AVG
			final int[] pidcount = new int[8192];
			for (int r = startPacket; r < endPacket; r++) {
				final int pid_current_packet = transportStream.getPacket_pid(r);
				pidcount[pid_current_packet]++;
			}

			for (int i = 0; i < used_pids.length; i++) {
				if (transportStream.getBitRate() != -1) {
					data[0][i] = (pidcount[used_pids[i]] * transportStream.getBitRate()) / (endPacket - startPacket);
				} else {
					data[0][i] = pidcount[used_pids[i]];
				}
			}

			// MIN/MAX
			// initialize minima at maximum value
			for (int i = 0; i < used_pids.length; i++) {
				data[1][i] = Double.MAX_VALUE;
			}

			for (int t = 0; t < steps; t++) {

				final int[] periodpidcount = new int[8192];

				final int startPacketStep = startPacket + (int) (((long) t * (long) noPackets) / steps);
				final int endPacketStep = startPacket + (int) (((long) (t + 1) * (long) noPackets) / steps);

				for (int r = startPacketStep; r < endPacketStep; r++) {
					final int pid_current_packet = transportStream.getPacket_pid(r);
					periodpidcount[pid_current_packet]++;
				}
				for (int i = 0; i < used_pids.length; i++) {
					final int periodCount = periodpidcount[used_pids[i]];
					if (transportStream.getBitRate() != -1) {
						final double bitRate = (periodCount * transportStream.getBitRate()) / (endPacketStep - startPacketStep);
						if (bitRate < data[1][i]) { // new min found
							data[1][i] = bitRate;
						}
						if (bitRate > data[2][i]) { // new max found
							data[2][i] = bitRate;
						}
					} else {
						data[1][i] = periodCount;
						data[2][i] = periodCount;
					}
				}
			}
			final CategoryDataset dataSet = DatasetUtilities.createCategoryDataset(stepLabels, labels, data);
			freeChart = ChartFactory.createBarChart(transportStream.getFile().getName() + " - stream:"
					+ transportStream.getStreamID(), "PID", "bitrate", dataSet, PlotOrientation.HORIZONTAL, true, true,
					false);
			setChart(freeChart);
		}else{ // transportstream == null
			freeChart = null;
		}
	}

}

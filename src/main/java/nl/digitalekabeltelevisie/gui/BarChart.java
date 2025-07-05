/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.AVCHD_PACKET_LENGTH;
import static nl.digitalekabeltelevisie.data.mpeg.MPEGConstants.MAX_PIDS;

import javax.swing.JMenuItem;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.MPEGConstants;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

/**
 * @author Eric Berendsen
 */
public class BarChart extends ChartPanel implements TransportStreamView {
	

	private JFreeChart	freeChart;
	private CategoryDataset dataSet;

	/**
	 * @param transportStream
	 * @param viewContext
	 */
	public BarChart(final TransportStream transportStream, final ViewContext viewContext) {

		super(null,false);
		setTransportStream(transportStream, viewContext);
		JMenuItem export = new JMenuItem(new BarChartExportAction("export as .csv", this));
		getPopupMenu().add(export);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */

	public final void setTransportStream(final TransportStream transportStream, final ViewContext viewContext) {
		if(transportStream!=null){
			dataSet = createDataSet(transportStream, viewContext);
			freeChart = ChartFactory.createBarChart(transportStream.getFile().getName() + " - stream:"
					+ transportStream.getStreamID(), "PID", "bitrate", dataSet, PlotOrientation.HORIZONTAL, true, true,
					false);
			setChart(freeChart);
		}else{ // transportstream == null
			freeChart = null;
			dataSet = null;
			setChart(GuiUtils.createTitleOnlyChart(GuiUtils.NO_TRANSPORTSTREAM_LOADED));

		}
	}

	private static double[][] createCbrData(final TransportStream transportStream, final ViewContext viewContext,
			final short[] used_pids, final int[] pidCountAvg) {
		final int steps = viewContext.getGraphSteps();
		final double[][] data = new double[3][viewContext.getShown().size()];
	
		for (int i = 0; i < used_pids.length; i++) {
			if (transportStream.getBitRate() != -1) {
				data[0][i] = (pidCountAvg[used_pids[i]] * transportStream.getBitRate()) / (viewContext.getEndPacket() - viewContext.getStartPacket());
			} else {
				data[0][i] = pidCountAvg[used_pids[i]];
			}
		}
	
		// MIN/MAX
		// initialize minima at maximum value
		for (int i = 0; i < used_pids.length; i++) {
			data[1][i] = Double.MAX_VALUE;
		}
	
		for (int t = 0; t < steps; t++) {
	
			final int[] periodpidcount = new int[MAX_PIDS];
	
			final int startPacketStep = viewContext.getStartPacket() + (int) (((long) t * (long) (viewContext.getEndPacket() - viewContext.getStartPacket())) / steps);
			final int endPacketStep = viewContext.getStartPacket() + (int) (((long) (t + 1) * (long) (viewContext.getEndPacket() - viewContext.getStartPacket())) / steps);
	
			for (int r = startPacketStep; r < endPacketStep; r++) {
				final int pid_current_packet = transportStream.getPacket_pid(r);
				periodpidcount[pid_current_packet]++;
			}
			for (int i = 0; i < used_pids.length; i++) {
				final int periodCount = periodpidcount[used_pids[i]];
				if (transportStream.getBitRate() != -1 && endPacketStep > startPacketStep) {
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
		return data;
	}

	private static CategoryDataset createDataSet(final TransportStream transportStream, final ViewContext viewContext) {

		final short[] usedPids = BitRateChart.createUsedPidsArray(viewContext, viewContext.getShown().size());
		final ChartLabel[] labels = createPidChartLabels(transportStream, viewContext, viewContext.getShown().size());
		final ChartLabel[] avgMinMaxLabels = createAvgMinMaxLabels();

		// AVG
		final int[] pidCountAvg = new int[MAX_PIDS];
		for (int r = viewContext.getStartPacket(); r < viewContext.getEndPacket(); r++) {
			final int pid_current_packet = transportStream.getPacket_pid(r);
			pidCountAvg[pid_current_packet]++;
		}

		final double[][] data;
		if(transportStream.isAVCHD()) {
			data = createAvchdData(transportStream, viewContext, usedPids, pidCountAvg);
		}else {
			data = createCbrData(transportStream, viewContext, usedPids, pidCountAvg);
		}
		
		
		final CategoryDataset dataSet = DatasetUtils.createCategoryDataset(avgMinMaxLabels, labels, data);
		return dataSet;
	}

	private static double[][] createAvchdData(final TransportStream transportStream, final ViewContext viewContext,
			final short[] usedPids, final int[] pidCountAvg) {
		final int steps = viewContext.getGraphSteps();
		final double[][] data = new double[3][viewContext.getShown().size()];

		long startSelectionTime = transportStream.getAVCHDPacketTime(viewContext.getStartPacket());
		long endSelectionTime = transportStream.getAVCHDPacketTime(viewContext.getEndPacket() - 1);
		long selectionDuration = endSelectionTime - startSelectionTime;

		for (int i = 0; i < usedPids.length; i++) {
				data[0][i] = (pidCountAvg[usedPids[i]] * (double) MPEGConstants.system_clock_frequency) * 8 * AVCHD_PACKET_LENGTH	/ selectionDuration;
		}

		// MIN/MAX
		// initialize minima at maximum value
		for (int i = 0; i < usedPids.length; i++) {
			data[1][i] = Double.MAX_VALUE;
		}


		int packetIndex = viewContext.getStartPacket();
		long startStepPacketTime = startSelectionTime;
		
		for (int step = 0; step < steps; step++) {
			final long endStepPacketTime = startSelectionTime + (selectionDuration * (step + 1) / steps);

			final int[] periodpidcount = new int[MAX_PIDS];


			while(packetIndex< viewContext.getEndPacket() && transportStream.getAVCHDPacketTime(packetIndex) <= endStepPacketTime) {
				final int pid_current_packet=transportStream.getPacket_pid(packetIndex);
				periodpidcount[pid_current_packet]++;
				packetIndex++;
			}
			
			for (int i = 0; i < usedPids.length; i++) {
				if (endStepPacketTime > startStepPacketTime) {
					final double bitRate = ((periodpidcount[usedPids[i]]) * (double) MPEGConstants.system_clock_frequency) * 8 * AVCHD_PACKET_LENGTH	/ (endStepPacketTime - startStepPacketTime) ;
					if (bitRate < data[1][i]) { // new min found
						data[1][i] = bitRate;
					}
					if (bitRate > data[2][i]) { // new max found
						data[2][i] = bitRate;
					}
				}
			}
			startStepPacketTime = endStepPacketTime;

		}
		return data;
	}

	private static ChartLabel[] createAvgMinMaxLabels() {
		final ChartLabel[] avgMinMaxLabels = new ChartLabel[3];
		avgMinMaxLabels[0] = new ChartLabel("avg", (short) 0);
		avgMinMaxLabels[1] = new ChartLabel("min", (short) 1);
		avgMinMaxLabels[2] = new ChartLabel("max", (short) 2);
		return avgMinMaxLabels;
	}

	private static ChartLabel[] createPidChartLabels(final TransportStream transportStream, final ViewContext viewContext,
			final int noPIDs) {
		final ChartLabel[] labels = new ChartLabel[noPIDs];
		for (int i = 0; i < noPIDs; i++) {
			labels[i] = new ChartLabel(transportStream.getShortLabel(viewContext.getShown().get(i).getPid()),
					viewContext.getShown().get(i).getPid());
		}
		return labels;
	}

	public CategoryDataset getDataSet() {
		return dataSet;
	}

}

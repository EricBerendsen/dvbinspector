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


import java.awt.BorderLayout;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.MPEGConstants;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.data.xy.CategoryTableXYDataset;

/**
 * Shows variation over time of the bandwidth each PID uses
 *
 * @author Eric Berendsen
 *
 */
public class BitRateChart extends JPanel implements TransportStreamView{

	/**
	 * @author Eric
	 *
	 */
	public static final class PacketTimeNumberFormat extends NumberFormat {
		/**
		 *
		 */
		private final TransportStream transportStream;

		/**
		 * @param transportStream
		 */
		public PacketTimeNumberFormat(final TransportStream transportStream) {
			super();
			this.transportStream = transportStream;
		}

		@Override
		public Number parse(final String source, final ParsePosition parsePosition) {
			// Not implemented, only used for output.
			return null;
		}

		@Override
		public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
			final String s = transportStream.getShortPacketTime(number);
			return toAppendTo.append(s);
		}

		@Override
		public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
			final String s = transportStream.getShortPacketTime((long) number);
			return toAppendTo.append(s);
		}
	}

	private JFreeChart freeChart;
	private final JPanel buttonPanel;

	private final ChartPanel chartPanel;
	private boolean legendVisible = true;


	/**
	 * Creates a new BitRateChart
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext determines which PIDs to include, what interval to use, and how many steps in the graph
	 */
	public BitRateChart(final TransportStream transportStream, final ViewContext viewContext){

		super(new BorderLayout());
		buttonPanel = new JPanel();
		addLegendRadioButtons();
		add(buttonPanel,BorderLayout.PAGE_START);

		chartPanel = new ChartPanel(null,false);
		// see http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=28118
		// Bug in ChartPanel.setMouseWheelEnabled in jfreechart 1.0.13
		chartPanel.isMouseWheelEnabled();
		chartPanel.setMouseWheelEnabled(true);


		setTransportStream(transportStream,viewContext);
		add(chartPanel,BorderLayout.CENTER);
	}

	/**
	 * Update existing BitRateChart to display a new {@link TransportStream}
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext determines which PIDs to include, what interval to use, and how many steps in the graph
	 *
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	public final void setTransportStream(final TransportStream transportStream, final ViewContext viewContext){
		if(transportStream==null){
			freeChart = null;
			chartPanel.setChart(GuiUtils.createTitleOnlyChart(GuiUtils.NO_TRANSPORTSTREAM_LOADED));
		}else{
			CategoryTableXYDataset categoryTableXYDataset;
		
			if(transportStream.isAVCHD()) {
				categoryTableXYDataset = createAvchdDataSet(transportStream, viewContext);
			}else {
				categoryTableXYDataset = createCbrDataSet(transportStream, viewContext);
			}
			//because we want custom colors, can not use ChartFactory.createStackedXYAreaChart(, this is almost literal copy

			final XYPlot plot = createXYPlot(transportStream, viewContext, categoryTableXYDataset);
	        freeChart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, legendVisible);
			plot.setRangePannable(true);
			plot.setDomainPannable(true);

			chartPanel.setChart(freeChart);

			chartPanel.setDomainZoomable(true);
			chartPanel.setRangeZoomable(true);
		}
	}

	/**
	 * @param transportStream
	 * @param viewContext
	 * @param noPIDs
	 * @param categoryTableXYDataset
	 * @return
	 */
	private static XYPlot createXYPlot(final TransportStream transportStream,
			final ViewContext viewContext, final CategoryTableXYDataset categoryTableXYDataset) {
		final NumberAxis xAxis = new NumberAxis("time");
		xAxis.setAutoRangeIncludesZero(false);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		xAxis.setNumberFormatOverride(new PacketTimeNumberFormat(transportStream));

		final NumberAxis yAxis = new NumberAxis("bitrate");
		final XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("PID: {0}, Time: {1}, bitrate: {2}", new PacketTimeNumberFormat(transportStream), NumberFormat.getNumberInstance());

		final StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(
		        toolTipGenerator, null);
		final int noPIDs=viewContext.getShown().size();
		for (int i = 0; i < noPIDs; i++) {
			renderer.setSeriesPaint(i, viewContext.getShown().get(i).getColor());
		}

		renderer.setOutline(false);
		final XYPlot plot = new XYPlot(categoryTableXYDataset, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setRangeAxis(yAxis);  // forces recalculation of the axis range
		return plot;
	}

	/**
	 * @param transportStream
	 * @param viewContext
	 * @param noPIDs
	 * @return
	 */
	private static CategoryTableXYDataset createCbrDataSet(final TransportStream transportStream,
			final ViewContext viewContext) {

		final int noPIDs=viewContext.getShown().size();
		final short[] used_pids = createUsedPidsArray(viewContext, noPIDs);
		final ChartLabel[] labels = createChartLabels(viewContext, noPIDs);
		final int numberOfSteps = viewContext.getGraphSteps();

		final CategoryTableXYDataset categoryTableXYDataset = new CategoryTableXYDataset();

		int startPacketStep = getFirstPacketNoOfStep(viewContext, 0);
		for (int step = 0; step < numberOfSteps; step++) {
			final int endPacketStep = getFirstPacketNoOfStep(viewContext, step + 1);
			final int[] pidcount = countPidOccurrencesInStep(transportStream, startPacketStep, endPacketStep);

			for (int pidIndex = 0; pidIndex < used_pids.length; pidIndex++) {
				if (transportStream.getBitRate() == -1) {
					categoryTableXYDataset.add(startPacketStep, pidcount[used_pids[pidIndex]], labels[pidIndex].getLabel());
				} else if (endPacketStep > startPacketStep) {
					categoryTableXYDataset.add(startPacketStep, ((pidcount[used_pids[pidIndex]]) * transportStream.getBitRate())	/ (endPacketStep - startPacketStep), labels[pidIndex].getLabel());
				}
			}
			startPacketStep = endPacketStep;
		}
		return categoryTableXYDataset;
	}

	static short[] createUsedPidsArray(final ViewContext viewContext, final int noPIDs) {
		final short[] used_pids = new short[noPIDs];
		for (int i = 0; i < noPIDs; i++) {
			used_pids[i] = viewContext.getShown().get(i).getPid();
		}
		return used_pids;
	}

	private static ChartLabel[] createChartLabels(final ViewContext viewContext, final int noPIDs) {
		final ChartLabel[] labels = new ChartLabel[noPIDs];
		for (int i = 0; i < noPIDs; i++) {
			labels[i] = viewContext.getShown().get(i);
		}
		return labels;
	}

	/**
	 * @param transportStream
	 * @param startPacketStep
	 * @param endPacketStep
	 * @return
	 */
	private static int[] countPidOccurrencesInStep(final TransportStream transportStream, final int startPacketStep, final int endPacketStep) {
		final int [] pidcount = new int [MAX_PIDS];
		for(int r = startPacketStep; r< endPacketStep;r++ ){
			final int pid_current_packet=transportStream.getPacket_pid(r);
			pidcount[pid_current_packet]++;
		}
		return pidcount;
	}

	/**
	 * @param viewContext
	 * @param steps
	 * @param step
	 * @return
	 */
	private static int getFirstPacketNoOfStep(final ViewContext viewContext, final int step) {

		int steps = viewContext.getGraphSteps();
		final long packetsInSelectedRange = viewContext.getEndPacket() - viewContext.getStartPacket();
		return viewContext.getStartPacket() + (int) ((step * packetsInSelectedRange) / steps);
	}

	private void addLegendRadioButtons() {
		final JLabel typeLabel = new JLabel("Legend:");
		buttonPanel.add(typeLabel);
		final JRadioButton onButton = new JRadioButton("On");
		onButton.addActionListener(e -> {
			if(!legendVisible){
				legendVisible = true;
				if(freeChart!=null){
					freeChart.getLegend().setVisible(legendVisible);
				}
			}
		});
		final JRadioButton offButton = new JRadioButton("Off");
		offButton.addActionListener(e -> {
			if(legendVisible){
				legendVisible = false;
				if(freeChart!=null){
					freeChart.getLegend().setVisible(legendVisible);
				}
			}
		});
		onButton.setSelected(true);
		final ButtonGroup group = new ButtonGroup();
		group.add(onButton);
		group.add(offButton);

		buttonPanel.add(onButton);
		buttonPanel.add(offButton);
	}

	/**
	 * @param transportStream
	 * @param viewContext
	 * @param noPIDs
	 * @return
	 */
	private static CategoryTableXYDataset createAvchdDataSet(final TransportStream transportStream,
			final ViewContext viewContext) {
	
		final int noPIDs=viewContext.getShown().size();
		final short[] used_pids = createUsedPidsArray(viewContext, noPIDs);
		final ChartLabel[] labels = createChartLabels(viewContext, noPIDs);
		final int numberOfSteps = viewContext.getGraphSteps();
	
		final CategoryTableXYDataset categoryTableXYDataset = new CategoryTableXYDataset();
	
		long startSelectionTime = transportStream.getAVCHDPacketTime(viewContext.getStartPacket());
		long endSelectionTime = transportStream.getAVCHDPacketTime(viewContext.getEndPacket() - 1);
		long selectionDuration = endSelectionTime - startSelectionTime;
		
		int packetIndex = viewContext.getStartPacket();
		long startStepPacketTime = startSelectionTime;
		for (int step = 0; step < numberOfSteps; step++) {
			final long endStepPacketTime = startSelectionTime + (selectionDuration * (step + 1) / numberOfSteps);
			final int [] pidcount = new int [MAX_PIDS];
			while(packetIndex< viewContext.getEndPacket() && transportStream.getAVCHDPacketTime(packetIndex) <= endStepPacketTime) {
				final int pid_current_packet=transportStream.getPacket_pid(packetIndex);
				pidcount[pid_current_packet]++;
				packetIndex++;
			}
	
			for (int pidIndex = 0; pidIndex < used_pids.length; pidIndex++) {
				if (endStepPacketTime > startStepPacketTime) {
					final long bitRate = ((pidcount[used_pids[pidIndex]]) * (long) MPEGConstants.system_clock_frequency) * 8 * AVCHD_PACKET_LENGTH	/ (endStepPacketTime - startStepPacketTime) ;
					categoryTableXYDataset.add(startStepPacketTime, bitRate, labels[pidIndex].getLabel());
				}
			}
			startStepPacketTime = endStepPacketTime;
		}
		return categoryTableXYDataset;
	}


}

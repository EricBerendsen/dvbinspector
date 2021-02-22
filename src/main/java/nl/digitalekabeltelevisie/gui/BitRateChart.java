/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
			final int noPIDs=viewContext.getShown().size();
			final CategoryTableXYDataset categoryTableXYDataset = createDataSet(transportStream, viewContext, noPIDs);
			//because we want custom colors, can not use ChartFactory.createStackedXYAreaChart(, this is almost literal copy

			final XYPlot plot = createXYPlot(transportStream, viewContext, noPIDs, categoryTableXYDataset);
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
			final ViewContext viewContext, final int noPIDs,
			final CategoryTableXYDataset categoryTableXYDataset) {
		final NumberAxis xAxis = new NumberAxis("time");
		xAxis.setAutoRangeIncludesZero(false);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		xAxis.setNumberFormatOverride(new PacketTimeNumberFormat(transportStream));

		final NumberAxis yAxis = new NumberAxis("bitrate");
		final XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("PID: {0}, Time: {1}, bitrate: {2}", new PacketTimeNumberFormat(transportStream), NumberFormat.getNumberInstance());

		final StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2(
		        toolTipGenerator, null);
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
	private static CategoryTableXYDataset createDataSet(final TransportStream transportStream,
			final ViewContext viewContext, final int noPIDs) {

		final short[]used_pids=new short[noPIDs];
		final ChartLabel[] labels= new ChartLabel[noPIDs];
		for (int i = 0; i < noPIDs; i++) {
			labels[i]=viewContext.getShown().get(i);
			used_pids[i]=viewContext.getShown().get(i).getPid();
		}
		final int numberOfSteps=viewContext.getGraphSteps();

		final CategoryTableXYDataset categoryTableXYDataset = new CategoryTableXYDataset();

		for (int pidIndex = 0; pidIndex < used_pids.length; pidIndex++) {
			for(int step=0; step<numberOfSteps;step++){

				final int startPacketStep = getFirstPacketNoOfStep(viewContext, numberOfSteps, step);
				final int endPacketStep = getFirstPacketNoOfStep(viewContext, numberOfSteps, step+1);
				final int[] pidcount = countPidOccurrencesInStep(transportStream, startPacketStep, endPacketStep);

				if(transportStream.getBitRate()==-1){
					categoryTableXYDataset.add(startPacketStep,pidcount[used_pids[pidIndex]],labels[pidIndex].getLabel());
				}else{
					categoryTableXYDataset.add(startPacketStep,((pidcount[used_pids[pidIndex]])*transportStream.getBitRate()) / (endPacketStep - startPacketStep),labels[pidIndex].getLabel());
				}
			}
		}
		return categoryTableXYDataset;
	}

	/**
	 * @param transportStream
	 * @param startPacketStep
	 * @param endPacketStep
	 * @return
	 */
	private static int[] countPidOccurrencesInStep(final TransportStream transportStream, final int startPacketStep,
			final int endPacketStep) {
		final int [] pidcount = new int [8192];
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
	private static int getFirstPacketNoOfStep(final ViewContext viewContext,
			final int steps, final int step) {
		return viewContext.getStartPacket() +(int)(((long)step*(long)(viewContext.getEndPacket() - viewContext.getStartPacket()))/steps);
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


}

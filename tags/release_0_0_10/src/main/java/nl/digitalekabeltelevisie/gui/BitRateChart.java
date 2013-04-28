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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedAreaRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

/**
 * Shows variation over time of the bandwidth each PID uses
 *
 * @author Eric Berendsen
 *
 */
public class BitRateChart extends JPanel implements TransportStreamView{

	private JFreeChart freeChart;
	private JPanel buttonPanel;

	private ChartPanel chartPanel;
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

		chartPanel = new ChartPanel(null);

		if(transportStream!=null){
			setTransportStream(transportStream,viewContext);
		}


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
	public void setTransportStream(final TransportStream transportStream, final ViewContext viewContext){
		if(transportStream!=null){
			final int steps=viewContext.getGraphSteps();

			final int noPIDs=viewContext.getShown().size();
			final double[][] data = new double[noPIDs][steps];

			final short[]used_pids=new short[noPIDs];
			final ChartLabel[] labels= new ChartLabel[noPIDs];
			for (int i = 0; i < noPIDs; i++) {
				labels[i]=viewContext.getShown().get(i);
				used_pids[i]=viewContext.getShown().get(i).getPid();
			}
			final ChartLabel[] stepLabels= new ChartLabel[steps];

			final int startPacket = viewContext.getStartPacket();
			final int endPacket = viewContext.getEndPacket();
			final int noPackets = endPacket - startPacket;

			for(int t=0; t<steps;t++){

				final int startPacketStep= startPacket +(int)(((long)t*(long)noPackets)/steps);
				final int endPacketStep = startPacket + (int) (((long)(t+1)*(long)noPackets)/steps);
				stepLabels[t]=new ChartLabel(transportStream.getShortPacketTime(startPacketStep),(short)t);
				final int [] pidcount = new int [8192];
				for(int r = startPacketStep; r< endPacketStep;r++ ){
					final int pid_current_packet=transportStream.getPacket_pid(r);
					pidcount[pid_current_packet]++;
				}

				for (int i = 0; i < used_pids.length; i++) {
					if(transportStream.getBitRate()!=-1)
					{
						data[i][t]=((pidcount[used_pids[i]])*transportStream.getBitRate()) / (endPacketStep - startPacketStep) ;
					}else{
						data[i][t]=pidcount[used_pids[i]];
					}
				}
			}

			final CategoryDataset dataSet = DatasetUtilities.createCategoryDataset(labels,stepLabels, data);
			//because we want custom colors, can not use ChartFactory.createStackedAreaChart, this is almost litteral copy
			final CategoryAxis categoryAxis = new CategoryAxis("time");
			categoryAxis.setCategoryMargin(0.0);
			final ValueAxis valueAxis = new NumberAxis("bitrate");
			final StackedAreaRenderer renderer = new StackedAreaRenderer();
			for (int i = 0; i < noPIDs; i++) {
				renderer.setSeriesPaint(i, viewContext.getShown().get(i).getColor());
			}

			//tooltips
			renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
			final CategoryPlot plot = new CategoryPlot(dataSet, categoryAxis, valueAxis,renderer);
			plot.setOrientation(PlotOrientation.VERTICAL);
			// always create with legend
			freeChart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT,plot, true);
			freeChart.getLegend().setVisible(legendVisible);
			plot.setBackgroundPaint(Color.white);
			plot.setRangePannable(true);
			plot.setDomainGridlinesVisible(false);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.lightGray);

			final CategoryAxis domainAxis = plot.getDomainAxis();
			domainAxis.setAxisLineVisible(true);

			chartPanel.setChart(freeChart);
			chartPanel.setDomainZoomable(true);
			chartPanel.setRangeZoomable(true);
		}else{ // transportstreaam == null
			freeChart = null;
			chartPanel.setChart(freeChart);
		}
	}

	private void addLegendRadioButtons() {
		JLabel typeLabel = new JLabel("Legend:");
		buttonPanel.add(typeLabel);
		JRadioButton onButton = new JRadioButton("On");
		onButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!legendVisible){
					legendVisible = true;
					if(freeChart!=null){
						freeChart.getLegend().setVisible(legendVisible);
					}
				}
			}
		});
		JRadioButton offButton = new JRadioButton("Off");
		offButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(legendVisible){
					legendVisible = false;
					if(freeChart!=null){
						freeChart.getLegend().setVisible(legendVisible);
					}
				}
			}
		});
		onButton.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(onButton);
		group.add(offButton);

		buttonPanel.add(onButton);
		buttonPanel.add(offButton);
	}


}

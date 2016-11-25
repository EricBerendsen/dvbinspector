/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.psi.*;
import nl.digitalekabeltelevisie.gui.utils.*;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Shows variation over time of the bandwidth each PID uses
 *
 * @author Eric Berendsen
 *
 */
public class TimeStampChart extends JPanel implements TransportStreamView, ActionListener{


	/**
	 * @author Eric
	 *
	 */
	private final class TimeStampNumberFormat extends NumberFormat {
		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			return toAppendTo.append(Utils .printTimebase90kHz(number));
		}

		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			return toAppendTo.append(Utils .printTimebase90kHz((long)number));
		}
	}

	/**
	 * @author Eric
	 *
	 */
	public final class PacketTimeNumberFormat extends NumberFormat {

		@Override
		public Number parse(final String source, final ParsePosition parsePosition) {
			return null;
		}

		@Override
		public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
			final String s = (usepacketTime?transportStream.getShortPacketTime(number): Long.toString(number));
			return toAppendTo.append(s);
		}

		@Override
		public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
			final String s = (usepacketTime?transportStream.getShortPacketTime((long)number):Long.toString((long)number));
			return toAppendTo.append(s);
		}
	}


	public final class  PacketTimeNumberFormatLabel extends NumberFormat {

		@Override
		public Number parse(final String source, final ParsePosition parsePosition) {
			return null;
		}

		@Override
		public StringBuffer format(final long number, final StringBuffer toAppendTo, final FieldPosition pos) {
			final String label = usepacketTime?"Time: ":"Packet No: ";
			final String s = packetTimeNumberFormat.format(number);
			return toAppendTo.append(label).append(s);
		}

		@Override
		public StringBuffer format(final double number, final StringBuffer toAppendTo, final FieldPosition pos) {
			final String label = usepacketTime?"Time: ":"Packet No: ";
			final String s = packetTimeNumberFormat.format(number);
			return toAppendTo.append(label).append(s);
		}
	}

	private JFreeChart freeChart;
	private final JPanel buttonPanel;

	private PacketTimeNumberFormat packetTimeNumberFormat = new PacketTimeNumberFormat();
	private PacketTimeNumberFormatLabel packetTimeNumberFormatLabel = new PacketTimeNumberFormatLabel();
	private boolean usepacketTime = true;
	private TimeStampNumberFormat timeStampNumberFormat = new TimeStampNumberFormat();

	private TransportStream transportStream;
	private List<PMTsection> pmts = new ArrayList<>();
	private final ChartPanel chartPanel;
	private boolean legendVisible = true;

	JComboBox<String> serviceChooser = new JComboBox<String>() ;
	private ViewContext viewContext;
	

	/**
	 * Creates a new TimeStampChart
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext determines which PIDs to include, what interval to use, and how many steps in the graph
	 */
	public TimeStampChart(final TransportStream transportStream, final ViewContext viewContext){
		super(new BorderLayout());
		this.transportStream = transportStream;
		buttonPanel = new JPanel();
		addServicesSelect(buttonPanel);
		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer
		addLegendRadioButtons(buttonPanel);
		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer
		addTimePacketNoRadioButtons(buttonPanel);
		add(buttonPanel,BorderLayout.PAGE_START);

		chartPanel = new ChartPanel(null);
		// see http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=28118
		// Bug in ChartPanel.setMouseWheelEnabled in jfreechart 1.0.13
		chartPanel.isMouseWheelEnabled();
		chartPanel.setMouseWheelEnabled(true);


		setTransportStream(transportStream,viewContext);
		add(chartPanel,BorderLayout.CENTER);
	}

	/**
	 * @param transportStream
	 * @param buttonPanel2
	 */
	private void addServicesSelect(JPanel buttonPanel) {

		final JLabel typeLabel = new JLabel("Service:");
		buttonPanel.add(typeLabel);
		serviceChooser.addActionListener(this);

		buttonPanel.add(serviceChooser);
	}

	/**
	 * Update existing TimeStampChart to display a new {@link TransportStream}
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext determines which PIDs to include, what interval to use, and how many steps in the graph
	 *
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	public final void setTransportStream(final TransportStream transportStream, final ViewContext viewContext){
		this.transportStream = transportStream;
		this.viewContext = viewContext;
		serviceChooser.removeActionListener(this);
		serviceChooser.removeAllItems();
		pmts.clear();
		if(transportStream==null){
			freeChart = null;
			chartPanel.setChart(GuiUtils.createTitleOnlyChart(GuiUtils.NO_TRANSPORTSTREAM_LOADED));
		}else{
			final PMTs streamPmts = transportStream.getPsi().getPmts();
			if(streamPmts.getPmts().isEmpty()){
				chartPanel.setChart(GuiUtils.createTitleOnlyChart("No PMTs found to display in this graph"));
			}else{
				for (PMTsection[] pmTsections : streamPmts) {
					PMTsection section = pmTsections[0]; //always one
					//PCR_PID If no PCR is associated with a program definition for private
					//streams, then this field shall take the value of 0x1FFF.
					if(section.getPcrPid()!=MPEGConstants.NO_PCR_PID){
						pmts.add(section);
						String name = getServiceName(transportStream, section.getProgramNumber());
						serviceChooser.addItem(name+", PCR_PID : "+section.getPcrPid());
					}
				}
				serviceChooser.addActionListener(this);
				updateChartPanel();
			}
		}
	}

	/**
	 * @param transportStream
	 * @param programNumber
	 * @return
	 */
	private static String getServiceName(final TransportStream transportStream, int programNumber) {
		String 	name = transportStream.getPsi().getSdt().getServiceName(programNumber);
		if(name==null){
			name= "Service "+ programNumber;
		}
		return name;
	}

	/**
	 *
	 */
	public void updateChartPanel() {
		freeChart = createChart(serviceChooser.getSelectedIndex());
		chartPanel.setChart(freeChart);
		freeChart.getLegend().setVisible(legendVisible);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);
	}

	/**
	 * @param selectedIndex
	 * @param transportStream
	 * @param viewContext
	 * @return
	 */
	private JFreeChart createChart(int selectedIndex) {


		final XYDataset categoryTableXYDataset = createDataSet(selectedIndex);
		PMTsection section = pmts.get(selectedIndex);
		String serviceLabel = getServiceName(transportStream, section.getProgramNumber());

		JFreeChart chart = ChartFactory.createScatterPlot(
				"PCR/PTS/DTS Graph - "+serviceLabel, // chart title
				"Time/packet no.", // domain axis label
				"pcr/pts/dts value", // range axis label
				categoryTableXYDataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // always create legend, make visible only if needed
				true, // tooltips
				false // urls
				);


		// make legend visible
		//freeChart.getLegend().setVisible(legendVisible);
		XYPlot plot = (XYPlot) chart.getPlot();

		// use larger shapes
		plot.setDrawingSupplier(new DVBInspectorDefaultDrawingSupplier());

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

		int seriesCount = categoryTableXYDataset.getSeriesCount();
		for (int i = 0; i < seriesCount; i++) {
			renderer.setSeriesOutlinePaint(i, Color.black);
		};
		renderer.setUseOutlinePaint(true);

		final XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("<htmL>{0}<br\\>{1}<br\\>value: {2}</html>",
				packetTimeNumberFormatLabel, timeStampNumberFormat);
		renderer.setBaseToolTipGenerator(toolTipGenerator);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoRangeIncludesZero(false);

		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setNumberFormatOverride(packetTimeNumberFormat);

		rangeAxis.setNumberFormatOverride(timeStampNumberFormat);

		return chart;
	}

	
	private XYDataset createDataSet(int selectedIndex) {
		
		PMTsection pmt = pmts.get(selectedIndex);
		return new TimestampXYDataset(pmt,transportStream,viewContext);
	}


	private void addLegendRadioButtons(JPanel buttonPanel) {
		final JLabel typeLabel = new JLabel("Legend:");
		buttonPanel.add(typeLabel);
		final JRadioButton onButton = new JRadioButton("On");
		onButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(!legendVisible){
					legendVisible = true;
					if(freeChart!=null){
						freeChart.getLegend().setVisible(legendVisible);
					}
				}
			}
		});
		final JRadioButton offButton = new JRadioButton("Off");
		offButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(legendVisible){
					legendVisible = false;
					if(freeChart!=null){
						freeChart.getLegend().setVisible(legendVisible);
					}
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

	private void addTimePacketNoRadioButtons(JPanel buttonPanel) {
		final JLabel typeLabel = new JLabel("X-Axis:");
		buttonPanel.add(typeLabel);
		final JRadioButton timeButton = new JRadioButton("Time");
		timeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(!usepacketTime){
					usepacketTime = true;
					if(freeChart!=null){
						freeChart.fireChartChanged();
					}
				}
			}
		});
		final JRadioButton packetNoButton = new JRadioButton("Packet No.");
		packetNoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(usepacketTime){
					usepacketTime = false;
					if(freeChart!=null){
						freeChart.fireChartChanged();
					}
				}
			}
		});
		timeButton.setSelected(true);
		final ButtonGroup group = new ButtonGroup();
		group.add(timeButton);
		group.add(packetNoButton);

		buttonPanel.add(timeButton);
		buttonPanel.add(packetNoButton);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		updateChartPanel();
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

}

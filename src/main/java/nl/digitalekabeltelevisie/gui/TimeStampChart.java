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

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

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
import nl.digitalekabeltelevisie.util.*;

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
	private static final class TimeStampNumberFormat extends NumberFormat {
		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			// not used
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

	private static final class TEMIMediaTimeStampNumberFormat extends NumberFormat {

		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			
			return  toAppendTo.append(df.format(number));
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
		
			return  toAppendTo.append(number);
		}

		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			// not used
			return null;
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
			final String label = usepacketTime?"Packet Time: ":"Packet No: ";
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
	

	private PacketTimeNumberFormat packetTimeNumberFormat = new PacketTimeNumberFormat();
	private PacketTimeNumberFormatLabel packetTimeNumberFormatLabel = new PacketTimeNumberFormatLabel();
	private boolean usepacketTime = true;
	private TimeStampNumberFormat timeStampNumberFormat = new TimeStampNumberFormat();
	private TEMIMediaTimeStampNumberFormat temiNumberFormat = new TEMIMediaTimeStampNumberFormat();

	private TransportStream transportStream;
	private List<PMTsection> pmts = new ArrayList<>();
	private final ChartPanel chartPanel;
	private boolean legendVisible = true;

	JComboBox<String> serviceChooser = new JComboBox<>() ;
	private ViewContext viewContext;
	
	static DecimalFormat df = new DecimalFormat("#0.00");
	private JCheckBox temiOptionCheckBox = new JCheckBox("enable");
	private JPanel seriesSelectionPanel;


	/**
	 * Creates a new TimeStampChart
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext determines which PIDs to include, what interval to use, and how many steps in the graph
	 */
	public TimeStampChart(final TransportStream transportStream, final ViewContext viewContext){
		super(new BorderLayout());
		this.transportStream = transportStream;
		JPanel topRowbuttonPanel = createTopRowButtonPanel();
		seriesSelectionPanel = new JPanel(new WrapLayout());
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.add(topRowbuttonPanel);
		controls.add(seriesSelectionPanel);
		add(controls,BorderLayout.PAGE_START);

		chartPanel = new ChartPanel(null,false);
		// see http://www.jfree.org/phpBB2/viewtopic.php?f=3&t=28118
		// Bug in ChartPanel.setMouseWheelEnabled in jfreechart 1.0.13
		chartPanel.isMouseWheelEnabled();
		chartPanel.setMouseWheelEnabled(true);

		setTransportStream(transportStream,viewContext);
		add(chartPanel,BorderLayout.CENTER);
	}

	/**
	 * @return
	 */
	private JPanel createTopRowButtonPanel() {
		JPanel buttonPanel = new JPanel(new WrapLayout());
		addServicesSelect(buttonPanel);
		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer
		addLegendRadioButtons(buttonPanel);
		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer
		
		addTimePacketNoRadioButtons(buttonPanel);
		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer
		addTEMIOption(buttonPanel);
		buttonPanel.invalidate();
		return buttonPanel;
	}

	/**
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
			seriesSelectionPanel.removeAll();
			temiOptionCheckBox.setEnabled(false);
		}else if(!PreferencesManager.isEnablePcrPtsView()) {
			freeChart = null;
			chartPanel.setChart(GuiUtils.createTitleOnlyChart("PCR/PTS/DTS View not enabled, select 'Settings -> Enable PCR/PTS/DTS View' to enable "));
			temiOptionCheckBox.setEnabled(false);
			
		}else{
			final PMTs streamPmts = transportStream.getPsi().getPmts();
			if(streamPmts.getPmts().isEmpty()){
				chartPanel.setChart(GuiUtils.createTitleOnlyChart("No PMTs found, nothing to display in this graph"));
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
				if(pmts.isEmpty()){
					chartPanel.setChart(GuiUtils.createTitleOnlyChart("No PMTs with PCR found, nothing to display in this graph"));
				}else{
					serviceChooser.addActionListener(this);
					updateChartPanel();
				}
			}
		}
	}

	/**
	 * @param transportStream
	 * @param programNumber
	 * @return
	 */
	private static String getServiceName(final TransportStream transportStream, int programNumber) {
		return transportStream.
				getPsi().
				getSdt().
				getServiceNameForActualTransportStreamOptional(programNumber).
				orElse("Service "+ programNumber);
	}

	/**
	 *
	 */
	private void updateChartPanel() {
		freeChart = createChart(serviceChooser.getSelectedIndex());
		chartPanel.setChart(freeChart);
		freeChart.getLegend().setVisible(legendVisible);
		chartPanel.setDomainZoomable(true);
		chartPanel.setRangeZoomable(true);
		fillSeriesSelectionPanel(seriesSelectionPanel,freeChart);
	}

	private void fillSeriesSelectionPanel(JPanel seriesSelectionPanel, JFreeChart freeChart2) {
		seriesSelectionPanel.removeAll();

		XYPlot plot = (XYPlot) freeChart.getPlot();
		XYDataset dataset = plot.getDataset();

		int count = dataset.getSeriesCount();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

		for (int i = 0; i < count; i++) {
			String label = (String) dataset.getSeriesKey(i);
			JCheckBox cb1 = new JCheckBox(label,true);
			
			cb1.addActionListener(actionEvent -> {
				JCheckBox source = (JCheckBox)actionEvent.getSource();
				int index = dataset.indexOf(label);
				renderer.setSeriesVisible(index, source.isSelected());
			});
			seriesSelectionPanel.validate();

			seriesSelectionPanel.add(cb1);
			if(i<(count-1)) {
				seriesSelectionPanel.add(Box.createHorizontalStrut(20)); // spacer
			}
		}
		
		seriesSelectionPanel.revalidate();
//		updateUI();
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

		XYPlot plot = (XYPlot) chart.getPlot();
		
		// use larger shapes
		plot.setDrawingSupplier(new DVBInspectorDefaultDrawingSupplier());
		plot.setRangePannable(true);
		plot.setDomainPannable(true);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();

		useBlackOutlinePaint(categoryTableXYDataset, renderer);
		
		// workaround for https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8213535
		// JDK-8213535 : Windows HiDPI html lightweight tooltips are truncated
		UIManager.put("ToolTip.font",  new FontUIResource("SansSerif", Font.PLAIN, 12));

		final XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("<htmL>{0}<br\\>{1}<br\\>value: {2}</html>",
				packetTimeNumberFormatLabel, timeStampNumberFormat);
		renderer.setDefaultToolTipGenerator(toolTipGenerator);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoRangeIncludesZero(false);

		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setNumberFormatOverride(packetTimeNumberFormat);

		rangeAxis.setNumberFormatOverride(timeStampNumberFormat);

		final XYDataset temiDataset = createTEMIDataset(selectedIndex);
		final boolean hasTEMIData = temiDataset.getSeriesCount()>0;
		temiOptionCheckBox.setEnabled(hasTEMIData);
		if(temiOptionCheckBox.isSelected() && hasTEMIData){
			
			final NumberAxis axis2 = new NumberAxis("TEMI");

			axis2.setAutoRangeIncludesZero(false);
			axis2.setLabelFont(rangeAxis.getLabelFont());
			axis2.setNumberFormatOverride(temiNumberFormat);
			plot.setRangeAxis(1, axis2);
			
			plot.setDataset(1, temiDataset);
			
			plot.mapDatasetToRangeAxis(1, 1);
			final XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(false,true);
			useBlackOutlinePaint(temiDataset, renderer2);

			final TEMIToolTipGenerator temiToolTipGenerator = new TEMIToolTipGenerator(	packetTimeNumberFormatLabel, temiNumberFormat);
			renderer2.setDefaultToolTipGenerator(temiToolTipGenerator);
		    plot.setRenderer(1, renderer2);
		}

		return chart;
	}

	private static void useBlackOutlinePaint(final XYDataset categoryTableXYDataset, XYLineAndShapeRenderer renderer) {
		int seriesCount = categoryTableXYDataset.getSeriesCount();
		for (int i = 0; i < seriesCount; i++) {
			renderer.setSeriesOutlinePaint(i, Color.black);
		}
		renderer.setUseOutlinePaint(true);
	}

	
	private XYDataset createDataSet(int selectedIndex) {
		
		PMTsection pmt = pmts.get(selectedIndex);
		return new TimestampXYDataset(pmt,transportStream,viewContext);
	}
	
	private XYDataset createTEMIDataset(int selectedIndex) {
		
		PMTsection pmt = pmts.get(selectedIndex);
		return new TEMIXYDataset(pmt,transportStream,viewContext);
	}

	

	private void addLegendRadioButtons(JPanel buttonPanel) {
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
	
	private void addTEMIOption(JPanel temiPanel) {
		final JLabel typeLabel = new JLabel("TEMI display:");
		temiPanel.add(typeLabel);
		temiOptionCheckBox.addActionListener(this);
		temiPanel.add(temiOptionCheckBox);
	}

	private void addTimePacketNoRadioButtons(JPanel buttonPanel) {
		final JLabel typeLabel = new JLabel("X-Axis:");
		buttonPanel.add(typeLabel);
		final JRadioButton timeButton = new JRadioButton("Time");
		timeButton.addActionListener(e -> {
			if(!usepacketTime){
				usepacketTime = true;
				if(freeChart!=null){
					freeChart.fireChartChanged();
				}
			}
		});
		final JRadioButton packetNoButton = new JRadioButton("Packet No.");
		packetNoButton.addActionListener(e -> {
			if(usepacketTime){
				usepacketTime = false;
				if(freeChart!=null){
					freeChart.fireChartChanged();
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

	
	@Override
	public void refreshView() {
		repaint();
		
	}
}

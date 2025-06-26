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

package nl.digitalekabeltelevisie.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.MPEGConstants;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTs;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection;
import nl.digitalekabeltelevisie.gui.utils.*;
import nl.digitalekabeltelevisie.util.PreferencesManager;
import nl.digitalekabeltelevisie.util.Utils;


public class TimeStampChart extends JPanel implements TransportStreamView, ActionListener{


	class HoverCopyAction extends AbstractAction{

		@Override
		public void actionPerformed(ActionEvent arg0) {
        Point p = chartPanel.getMousePosition();
        String htmlString = chartPanel.getToolTipText(new MouseEvent(chartPanel,
            0, System.currentTimeMillis(), 0, p.x, p.y, 0, false));
		String plainData = Utils.extractTextFromHTML(htmlString);
		TextHTMLTransferable transferable = new TextHTMLTransferable(plainData, htmlString);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(transferable, null);
		}
	}


	public final class PacketTimeNumberFormat extends NumberFormat {

		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			return null;
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			String s = (usepacketTime?transportStream.getShortPacketTime(number): Long.toString(number));
			return toAppendTo.append(s);
		}

		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			String s = (usepacketTime?transportStream.getShortPacketTime((long)number):Long.toString((long)number));
			return toAppendTo.append(s);
		}
	}


	public final class  PacketTimeNumberFormatLabel extends NumberFormat {

		@Override
		public Number parse(String source, ParsePosition parsePosition) {
			return null;
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			String label = usepacketTime?"Packet Time: ":"Packet No: ";
			String s = packetTimeNumberFormat.format(number);
			return toAppendTo.append(label).append(s);
		}

		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			String label = usepacketTime?"Time: ":"Packet No: ";
			String s = packetTimeNumberFormat.format(number);
			return toAppendTo.append(label).append(s);
		}
	}

	
	private JFreeChart freeChart;
	

	private final PacketTimeNumberFormat packetTimeNumberFormat = new PacketTimeNumberFormat();
	private final PacketTimeNumberFormatLabel packetTimeNumberFormatLabel = new PacketTimeNumberFormatLabel();
	private boolean usepacketTime = true;
	private final TimeStampNumberFormat timeStampNumberFormat = new TimeStampNumberFormat();
	private final TEMIMediaTimeStampNumberFormat temiNumberFormat = new TEMIMediaTimeStampNumberFormat();

	private TransportStream transportStream;
	private final List<PMTsection> pmts = new ArrayList<>();
	private final ChartPanel chartPanel;
	private boolean legendVisible = true;

	private final JComboBox<String> serviceChooser = new JComboBox<>() ;
	private ViewContext viewContext;
	
	private final JPanel seriesSelectionPanel;
	private final JPanel temiSelectionPanel;

	private JRadioButton timeButton;

	private JRadioButton packetNoButton;


	/**
	 * Creates a new TimeStampChart
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext determines which PIDs to include, what interval to use, and how many steps in the graph
	 */
	public TimeStampChart(TransportStream transportStream, ViewContext viewContext){
		super(new BorderLayout());
		this.transportStream = transportStream;
		JPanel topRowbuttonPanel = createTopRowButtonPanel();
		seriesSelectionPanel = new JPanel(new WrapLayout());
		temiSelectionPanel = new JPanel(new WrapLayout());
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		controls.add(topRowbuttonPanel);
		controls.add(seriesSelectionPanel);
		controls.add(temiSelectionPanel);
		add(controls,BorderLayout.PAGE_START);

		chartPanel = new ChartPanel(null,false);
		chartPanel.setMouseWheelEnabled(true);

		// catch keystrokes for ctrl-c
		chartPanel.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseEntered(MouseEvent e) {
	        requestFocus();}
		});

		InputMap im = getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copytip");
		
		HoverCopyAction hoverCopyAction = new HoverCopyAction();
		getActionMap().put("copytip", hoverCopyAction);
		
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
		buttonPanel.invalidate();
		return buttonPanel;
	}

	/**
	 */
	private void addServicesSelect(JPanel buttonPanel) {

		JLabel typeLabel = new JLabel("Service:");
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
	@Override
	public final void setTransportStream(TransportStream transportStream, ViewContext viewContext){
		this.transportStream = transportStream;
		this.viewContext = viewContext;
		serviceChooser.removeActionListener(this);
		serviceChooser.removeAllItems();
		pmts.clear();
		if(transportStream==null){
			freeChart = null;
			chartPanel.setChart(GuiUtils.createTitleOnlyChart(GuiUtils.NO_TRANSPORTSTREAM_LOADED));
			seriesSelectionPanel.removeAll();
			temiSelectionPanel.removeAll();
		}else if(!PreferencesManager.isEnablePcrPtsView()) {
			freeChart = null;
			chartPanel.setChart(GuiUtils.createTitleOnlyChart("PCR/PTS/DTS View not enabled, select 'Settings -> Enable PCR/PTS/DTS View' to enable "));
			
		}else{
			
			timeButton.setEnabled(!transportStream.isAVCHD());
			packetNoButton.setEnabled(!transportStream.isAVCHD());
			if(transportStream.isAVCHD()) {
				usepacketTime = true;
			}
			PMTs streamPmts = transportStream.getPsi().getPmts();
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
	private static String getServiceName(TransportStream transportStream, int programNumber) {
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
		fillSelectionPanels();
	}

	private void fillSelectionPanels() {

		XYPlot plot = (XYPlot) freeChart.getPlot();

		seriesSelectionPanel.removeAll();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		XYDataset dataset = plot.getDataset();
		createCheckBoxForSeries(seriesSelectionPanel, renderer, dataset);

		temiSelectionPanel.removeAll();
		XYDataset temiDataset = plot.getDataset(1);
		if (temiDataset != null) {
			XYLineAndShapeRenderer temiRenderer = (XYLineAndShapeRenderer) plot.getRenderer(1);
			createCheckBoxForSeries(temiSelectionPanel, temiRenderer, temiDataset);
		}

		seriesSelectionPanel.revalidate();
		temiSelectionPanel.revalidate();
	}

	private static void createCheckBoxForSeries(JPanel selectionPanel, XYLineAndShapeRenderer renderer, XYDataset dataset) {
		int count = dataset.getSeriesCount();
		for (int i = 0; i < count; i++) {
			String label = (String) dataset.getSeriesKey(i);
			JCheckBox cb1 = new JCheckBox(label,true);
			
			cb1.addActionListener(actionEvent -> {
				JCheckBox source = (JCheckBox)actionEvent.getSource();
				int index = dataset.indexOf(label);
				renderer.setSeriesVisible(index, source.isSelected());
			});
			selectionPanel.validate();

			selectionPanel.add(cb1);
			if(i<(count-1)) {
				selectionPanel.add(Box.createHorizontalStrut(20)); // spacer
			}
		}
	}

	/**
	 * @param selectedIndex
	 * @param transportStream
	 * @param viewContext
	 * @return
	 */
	private JFreeChart createChart(int selectedIndex) {

		XYDataset categoryTableXYDataset = createDataSet(selectedIndex);
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

		XYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator("<htmL>{0}<br\\>{1}<br\\>value: {2}</html>",
				packetTimeNumberFormatLabel, timeStampNumberFormat);
		renderer.setDefaultToolTipGenerator(toolTipGenerator);

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRange(true);
		rangeAxis.setAutoRangeIncludesZero(false);

		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setNumberFormatOverride(packetTimeNumberFormat);

		rangeAxis.setNumberFormatOverride(timeStampNumberFormat);

		XYDataset temiDataset = createTEMIDataset(selectedIndex);
		boolean hasTEMIData = temiDataset.getSeriesCount()>0;
		if(hasTEMIData){
			
			NumberAxis axis2 = new NumberAxis("TEMI");

			axis2.setAutoRangeIncludesZero(false);
			axis2.setLabelFont(rangeAxis.getLabelFont());
			axis2.setNumberFormatOverride(temiNumberFormat);
			plot.setRangeAxis(1, axis2);
			
			plot.setDataset(1, temiDataset);
			
			plot.mapDatasetToRangeAxis(1, 1);
			XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(false,true);
			useBlackOutlinePaint(temiDataset, renderer2);

			TEMIToolTipGenerator temiToolTipGenerator = new TEMIToolTipGenerator(	temiNumberFormat);
			renderer2.setDefaultToolTipGenerator(temiToolTipGenerator);
		    plot.setRenderer(1, renderer2);
		}

		return chart;
	}

	private static void useBlackOutlinePaint(XYDataset categoryTableXYDataset, XYLineAndShapeRenderer renderer) {
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
		JLabel typeLabel = new JLabel("Legend:");
		buttonPanel.add(typeLabel);
		JRadioButton onButton = new JRadioButton("On");
		onButton.addActionListener(e -> {
			if(!legendVisible){
				legendVisible = true;
				if(freeChart!=null){
					freeChart.getLegend().setVisible(legendVisible);
				}
			}
		});
		JRadioButton offButton = new JRadioButton("Off");
		offButton.addActionListener(e -> {
			if(legendVisible){
				legendVisible = false;
				if(freeChart!=null){
					freeChart.getLegend().setVisible(legendVisible);
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

	private void addTimePacketNoRadioButtons(JPanel buttonPanel) {
		JLabel typeLabel = new JLabel("X-Axis:");
		buttonPanel.add(typeLabel);
		timeButton = new JRadioButton("Time");
		timeButton.addActionListener(e -> {
			if(!usepacketTime){
				usepacketTime = true;
				if(freeChart!=null){
					freeChart.fireChartChanged();
				}
			}
		});
		packetNoButton = new JRadioButton("Packet No.");
		packetNoButton.addActionListener(e -> {
			if(usepacketTime){
				usepacketTime = false;
				if(freeChart!=null){
					freeChart.fireChartChanged();
				}
			}
		});
		timeButton.setSelected(true);
		ButtonGroup group = new ButtonGroup();
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

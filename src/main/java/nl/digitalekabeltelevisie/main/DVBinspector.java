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
package nl.digitalekabeltelevisie.main;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.plot.DefaultDrawingSupplier;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.AC3Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ac3.EAC3Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.Audio138183Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.aac.Audio144963Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.dvbsubtitling.DVBSubtitleHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ebu.EBUTeletextHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video.Video138182Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video264.Video14496Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.video265.H265Handler;
import nl.digitalekabeltelevisie.data.mpeg.pid.t2mi.T2miPidHandler;
import nl.digitalekabeltelevisie.gui.*;
import nl.digitalekabeltelevisie.gui.exception.NotAnMPEGFileException;
import nl.digitalekabeltelevisie.util.DefaultMutableTreeNodePreorderEnumaration;
import nl.digitalekabeltelevisie.util.PreferencesManager;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * Main class for DVB Inspector, creates and holds all GUI elements.
 *
 * @author Eric
 *
 */
public class DVBinspector implements ChangeListener, ActionListener{

	private static final Logger LOGGER = Logger.getLogger(DVBinspector.class.getName());

	private TransportStream transportStream;

	private JFrame frame;

	private final List<TransportStreamView> views = new ArrayList<>();
	private DVBtree treeView;
	private TimeStampChart timeStampChart;
	private BitRateChart bitRateView;
	private BarChart barChart;
	private GridView gridView;
	private JTabbedPane tabbedPane;
	private JMenu viewTreeMenu;
	private JMenu viewMenu;
	private PIDDialog pidDialog = null;

	private ViewContext viewContext = new ViewContext();

	private int modus;

	private FindNextAction findNextAction;


	private DefaultMutableTreeNodePreorderEnumaration searchEnummeration;
	private String searchString;


	/**
	 * Default constructor for DVBinspector
	 */
	public DVBinspector() {
		super();
	}

	enum PidHandlers{
		t2mi(T2miPidHandler.class),
		ac3(AC3Handler.class),
		eac3(EAC3Handler.class),
		mpeg2audio(Audio138183Handler.class),
		dvbsub(DVBSubtitleHandler.class),
		ttx(EBUTeletextHandler.class),
		h222(Video138182Handler.class),
		h264(Video14496Handler.class),
		h265(H265Handler.class),
		aac(Audio144963Handler.class);
		
		PidHandlers(Class<? extends GeneralPidHandler> a) {
			pidHandler = a;
		}

		private final Class<? extends GeneralPidHandler> pidHandler;

		Class<? extends GeneralPidHandler> getPidHandler() {
			return pidHandler;
		}

	}
	
	/**
	 * Starting point for DVB Inspector
	 *
	 * @param args String[] all optional, if used; arg[0] is absolute filename of transport stream to be loaded on startup.
	 *  args[1...n] pids with PES data that should be parsed on startup (equivalent to "Parse PES data" menu in Tree View)
	 *  These args are mainly intended for debugging, where you want to use the same stream and PES data over and over again.
	 *
	 */
	public static void main(final String[] args) {
		final DVBinspector inspector = new DVBinspector();
		if(args.length>=1){
			final String filename= args[0];
			try {
				final TransportStream ts = new TransportStream(filename);
				inspector.transportStream = ts;
				inspector.transportStream.parseStream();
								
				if (args.length >= 2) {
					final Map<Integer, GeneralPidHandler> pidHandlerMap = determinePidHandlers(args, ts);
					ts.parsePidStreams(pidHandlerMap);
				}
			} catch (final NotAnMPEGFileException e) {
				LOGGER.log(Level.WARNING, "error determining packetsize transportStream", e);
			} catch (final Throwable e) {
				LOGGER.log(Level.WARNING, "error parsing transportStream", e);
			}
		}
		inspector.run();
	}

	/**
	 * @param args
	 * @param ts
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static Map<Integer, GeneralPidHandler> determinePidHandlers(final String[] args, final TransportStream ts)
			throws InstantiationException, IllegalAccessException {
		Class<? extends GeneralPidHandler> forcedHandlerClass = null;
		final PID[] pids = ts.getPids();
		final Map<Integer, GeneralPidHandler> pidHandlerMap = new HashMap<>();
		for (int i = 1; i < args.length; i++) {

			if (args[i].equalsIgnoreCase("-")) { // single - resets forced handler, use normal handler according to PSI
				forcedHandlerClass = null;
			} else if (args[i].startsWith("-")) {
				forcedHandlerClass = PidHandlers.valueOf(args[i].substring(1)).getPidHandler();
			} else {
				final int pid = Integer.parseInt(args[i]);
				final PID p = pids[pid];
				if (p != null) { // pid exists
					if (forcedHandlerClass == null) {
						pidHandlerMap.put(pid, p.getPidHandler());
					} else {
						GeneralPidHandler handlerInstance = forcedHandlerClass.newInstance();
						handlerInstance.setPID(p);
						handlerInstance.setTransportStream(ts);
						p.setPidHandler(handlerInstance);
						pidHandlerMap.put(pid, handlerInstance);
					}
				}
			}
		}
		return pidHandlerMap;
	}

	public void run() {

		KVP.setNumberDisplay(KVP.NUMBER_DISPLAY_BOTH);
		KVP.setStringDisplay(KVP.STRING_DISPLAY_HTML_AWT);
		javax.swing.SwingUtilities.invokeLater(() -> createAndShowGUI(transportStream));

	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 *
	 * @param tStream Transport stream (can be <code>null</code>)
	 */
	private void createAndShowGUI(final TransportStream tStream) {
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e)
		{
			LOGGER.warning("Couldn't use system look and feel. Exception:"+e.getMessage());
		}
		
		int modus = PreferencesManager.getDefaultViewModus();

		ToolTipManager.sharedInstance().setDismissDelay(30000);
		//Create and set up the window.
		frame = new JFrame("DVB Inspector");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			/* (non-Javadoc)
			 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
			 */
			@Override
			public void windowClosing(final WindowEvent e) {
				saveWindowState();
				super.windowClosing(e);
			}
		});
		pidDialog= new PIDDialog(frame,viewContext,this);
		updatePIDLists(tStream,pidDialog);

		tabbedPane = new JTabbedPane();
		treeView = new DVBtree(tStream,modus);
		tabbedPane.addTab("Tree", treeView);
		views.add(treeView);

		final EITView eitView = new EITView(tStream,viewContext);
		tabbedPane.addTab("EIT View", eitView);
		views.add(eitView);

		bitRateView = new BitRateChart(tStream,viewContext);
		tabbedPane.addTab("BitRate View", bitRateView);
		views.add(bitRateView);

		barChart = new BarChart(tStream,viewContext);
		tabbedPane.addTab("Bar View", barChart);
		views.add(barChart);

		gridView = new GridView(tStream,viewContext);
		tabbedPane.addTab("Grid View", gridView);
		views.add(gridView);

		timeStampChart = new TimeStampChart(tStream, viewContext);
		tabbedPane.addTab("PCR/PTS/DTS View", timeStampChart);
		views.add(timeStampChart);

		tabbedPane.validate();
		tabbedPane.addChangeListener(this);
		
		tabbedPane.setTransferHandler(new FileDropHandler(this));
		frame.add(tabbedPane);

		frame.setJMenuBar(createMenuBar(modus));
		enableViewMenus();

		final Image image = Utils.readIconImage("magnifying_glass.bmp");
		frame.setIconImage(image);

		frame.setBounds(calculateBounds());

		frame.setVisible(true);
	}

	/**
	 * @param modus
	 * @return
	 */
	private JMenuBar createMenuBar(final int modus) {
		final JMenuBar menuBar = new JMenuBar();

		menuBar.add(createFileMenu());

		viewTreeMenu = createViewTreeMenu(modus);
		menuBar.add(viewTreeMenu);

		viewMenu = createViewMenu();
		menuBar.add(viewMenu);

		menuBar.add(createSettingsMenu());
		menuBar.add(createHelpMenu());
		return menuBar;
	}

	/**
	 * @return
	 */
	private static Rectangle calculateBounds() {

		int x = Math.max(0, PreferencesManager.getWindowX());
		int y = Math.max(0, PreferencesManager.getWindowY());

		// if last used on larger screen, it might open too large, or out of range
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final int screenWidth = (int) screenSize.getWidth();
		final int screenHeight = (int) screenSize.getHeight();

		x = Math.min(screenWidth-200, x); // at least 200 pix visible
		y = Math.min(screenHeight-200, y); // at least 200 pix visible
		final int w = Math.min(screenWidth, PreferencesManager.getWindowWidth());
		final int h = Math.min(screenHeight, PreferencesManager.getWindowHeight());
		return new Rectangle(x,y,w,h);
	}

	/**
	 * @return
	 */
	private JMenu createSettingsMenu() {
		final JMenu settingsMenu =new JMenu("Settings");
		settingsMenu.setMnemonic(KeyEvent.VK_S);

		final JMenu packetLengthSubMenu = new JMenu("Packet Size");
		packetLengthSubMenu.setMnemonic(KeyEvent.VK_I);
		final ButtonGroup packetLengthMenuGroup = new ButtonGroup();
		
		
		addPacketLengthMenuItem(packetLengthSubMenu, packetLengthMenuGroup, "auto (Recommended)", 0);
		 
		for(int i: TransportStream.ALLOWED_PACKET_LENGTHS) {
			addPacketLengthMenuItem(packetLengthSubMenu, packetLengthMenuGroup, ""+i, i);
		}
		settingsMenu.add(packetLengthSubMenu);


		final JMenu privateDataSubMenu = new JMenu("Private Data Specifier Default");
		privateDataSubMenu.setMnemonic(KeyEvent.VK_S);
		
		final ButtonGroup privateDataSpecifierMenuGroup = new ButtonGroup();
		
		long defaultPrivateDataSpecifier = PreferencesManager.getDefaultPrivateDataSpecifier();

		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x00, "none",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x16, "Casema",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x28, "EACEM",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x29, "Nordig",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x40, "CI Plus LLP",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x600, "UPC",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, privateDataSpecifierMenuGroup, 0x0000233A, "Independent Television Commission (DTG)",defaultPrivateDataSpecifier);

		settingsMenu.add(privateDataSubMenu);
		
		
		
		final JMenu defaultG0andG2CharacterSetDesignationMenu = new JMenu("Teletext Default G0 and G2 Character Set Designation");
		defaultG0andG2CharacterSetDesignationMenu.setMnemonic(KeyEvent.VK_T);
		final ButtonGroup g0Group = new ButtonGroup();

		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x00, "0 0 0 0 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x01, "0 0 0 1 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x02, "0 0 1 0 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x03, "0 0 1 1 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x04, "0 1 0 0 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x05, "0 1 0 1 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x06, "0 1 1 0 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x07, "0 1 1 1 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x08, "1 0 0 0 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x09, "1 0 0 1 x x x");
		addG0CharacterSet(defaultG0andG2CharacterSetDesignationMenu, g0Group, 0x0A, "1 0 1 0 x x x");

		settingsMenu.add(defaultG0andG2CharacterSetDesignationMenu);
		
		final JCheckBoxMenuItem enableM7Fastscan = new JCheckBoxMenuItem(new EnableM7FastscanAction(this));
		enableM7Fastscan.setMnemonic(KeyEvent.VK_M);
		enableM7Fastscan.setSelected(PreferencesManager.isEnableM7Fastscan());
		settingsMenu.add(enableM7Fastscan);
		
		settingsMenu.addSeparator();
		
		final JCheckBoxMenuItem enableGenericPSI = new JCheckBoxMenuItem(new EnableGenericPSIAction(this));
		enableGenericPSI.setMnemonic(KeyEvent.VK_G);
		enableGenericPSI.setSelected(PreferencesManager.isEnableGenericPSI());
		settingsMenu.add(enableGenericPSI);

		final JCheckBoxMenuItem enableDSMCC = new JCheckBoxMenuItem(new EnableDSMCCAction(this));
		enableDSMCC.setMnemonic(KeyEvent.VK_D);
		enableDSMCC.setSelected(PreferencesManager.isEnableDSMCC());
		settingsMenu.add(enableDSMCC);

		final JCheckBoxMenuItem enablePcrPtsView = new JCheckBoxMenuItem(new EnablePcrPtsViewAction(this));
		enablePcrPtsView.setMnemonic(KeyEvent.VK_P);
		enablePcrPtsView.setSelected(PreferencesManager.isEnablePcrPtsView());
		settingsMenu.add(enablePcrPtsView);
		
		final JMenu timestampPresentationMenu = new JMenu("Timestamp format");
		timestampPresentationMenu.setMnemonic(KeyEvent.VK_T);
		boolean showSeconds = PreferencesManager.getEnableSecondsTimestamp();
		final ButtonGroup tsFormatGroup = new ButtonGroup();
		
		final JMenuItem hhmmssItem = new JRadioButtonMenuItem("hh:mm:ss.SSS");
		tsFormatGroup.add(hhmmssItem);
		hhmmssItem.addActionListener(new TimestampFormatAction(this,false));
		hhmmssItem.setSelected(!showSeconds);
		timestampPresentationMenu.add(hhmmssItem);

		final JMenuItem secondsItem = new JRadioButtonMenuItem("seconds.SSS");
		tsFormatGroup.add(secondsItem);
		secondsItem.addActionListener(new TimestampFormatAction(this,true));
		secondsItem.setSelected(showSeconds);
		timestampPresentationMenu.add(secondsItem);
		
		settingsMenu.add(timestampPresentationMenu);
		return settingsMenu;
	}

	private void addPacketLengthMenuItem(final JMenu packetLengthSubMenu, final ButtonGroup packetLengthMenuGroup,
			String description, int length) {
		final JMenuItem packetLengthItem = new JRadioButtonMenuItem(description);
		packetLengthMenuGroup.add(packetLengthItem);
		packetLengthItem.addActionListener(new SetPacketLengthAction(this,length));

		packetLengthItem.setSelected(PreferencesManager.getPacketLengthModus()==length);
		packetLengthSubMenu.add(packetLengthItem);
	}

	/**
	 * @return
	 */
	private JMenu createViewMenu() {
		final JMenu viewMenu =new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);

		PIDDialogOpenAction pidOpenAction = new PIDDialogOpenAction(pidDialog, frame, this);
		final JMenuItem filterItem = new JMenuItem(pidOpenAction);
		filterItem.setMnemonic(KeyEvent.VK_F);
		viewMenu.add(filterItem);
		//filterItem.addActionListener(pidOpenAction);
		viewMenu.add(filterItem);
		return viewMenu;
	}

	/**
	 * @return
	 */
	private JMenu createHelpMenu() {
		final JMenu helpMenu =new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		AboutAction aboutAction = new AboutAction(frame);
		JMenuItem aboutMenuItem = new JMenuItem(aboutAction);
		aboutMenuItem.setMnemonic(KeyEvent.VK_A);
		helpMenu.add(aboutMenuItem);
		return helpMenu;
	}

	/**
	 * @param modus
	 */
	private JMenu createViewTreeMenu(final int modus) {
		final JMenu viewTreeMenu =new JMenu("Tree View");
		viewTreeMenu.setMnemonic(KeyEvent.VK_T);

		viewTreeMenu.add(createCheckBoxMenuItem(modus, "Simple Tree View", DVBtree.SIMPLE_MODUS,KeyEvent.VK_S));
		viewTreeMenu.add(createCheckBoxMenuItem(modus, "PSI Only",DVBtree.PSI_ONLY_MODUS,KeyEvent.VK_P));
		viewTreeMenu.add(createCheckBoxMenuItem(modus, "Packet Count",DVBtree.PACKET_MODUS,KeyEvent.VK_C));
		viewTreeMenu.add(createCheckBoxMenuItem(modus, "Number List Items",DVBtree.COUNT_LIST_ITEMS_MODUS,KeyEvent.VK_N));
		viewTreeMenu.add(createCheckBoxMenuItem(modus, "Show PTS on PES Packets",DVBtree.SHOW_PTS_MODUS,KeyEvent.VK_T));
		viewTreeMenu.add(createCheckBoxMenuItem(modus, "Show version_number on Table Sections",DVBtree.SHOW_VERSION_MODUS,KeyEvent.VK_V));

		FindAction findAction = new FindAction(this);
		
		JMenuItem findMenuItem = new JMenuItem(findAction);
		findMenuItem.setMnemonic(KeyEvent.VK_F);
		findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
		
		findNextAction = new FindNextAction(this);
		JMenuItem findNextMenuItem = new JMenuItem(findNextAction);
		findNextMenuItem.setMnemonic(KeyEvent.VK_N);
		findNextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		findNextAction.setEnabled(false);
		
		viewTreeMenu.addSeparator();
		viewTreeMenu.add(findMenuItem);
		viewTreeMenu.add(findNextMenuItem);

		return viewTreeMenu;
	}

	/**
	 * @param modus
	 * @param label
	 * @param modusBit
	 * @param vkS 
	 * @return
	 */
	private JCheckBoxMenuItem createCheckBoxMenuItem(final int modus, final String label, final int modusBit, int vkS) {
		final JCheckBoxMenuItem viewMenu = new JCheckBoxMenuItem(new ToggleViewAction(label,this, modusBit));
		viewMenu.setMnemonic(vkS);
		viewMenu.setSelected((modus&modusBit)!=0);

		return viewMenu;
	}

	/**
	 * @return
	 */
	private JMenu createFileMenu() {
		final JMenu fileMenu =new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);

		Action fileOpenAction = new FileOpenAction(this);
		final JMenuItem openMenuItem = new JMenuItem(fileOpenAction);
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		fileMenu.add(openMenuItem);

		final JMenuItem exitMenuItem = new JMenuItem("Exit",KeyEvent.VK_X);
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
		exitMenuItem.addActionListener(this);
		fileMenu.add(exitMenuItem);
		return fileMenu;
	}


	/**
	 * Add a menu item for a private data specifier to the "Private Data Specifier Default" menu
	 *
	 * @param privateDataSubMenu
	 * @param group
	 * @param spec
	 * @param name
	 * @param defaultSpecifier
	 */
	private void addPrivateDataSpecMenuItem(final JMenu privateDataSubMenu, final ButtonGroup group, final long spec, final String name, final long defaultSpecifier) {
		final JMenuItem menuItem = new JRadioButtonMenuItem(Utils.toHexString(spec, 8)+" - "+name);
		group.add(menuItem);
		menuItem.addActionListener(new SetPrivateDataSpecifierAction(this,spec));
		menuItem.setSelected(spec==defaultSpecifier);
		privateDataSubMenu.add(menuItem);
	}

	
	private void addG0CharacterSet(final JMenu privateDataSubMenu, final ButtonGroup group, final int spec, final String name) {
		final JMenuItem menuItem = new JRadioButtonMenuItem(name);
		group.add(menuItem);
		menuItem.addActionListener(new SetG0DefaultAction(this,spec));
		menuItem.setSelected(spec==PreferencesManager.getDefaultG0CharacterSet());
		privateDataSubMenu.add(menuItem);
	}
	
	/**
	 * getter for the Transport stream (can be <code>null</code>)
	 * @return
	 */
	public TransportStream getTransportStream() {
		return transportStream;
	}

	/**
	 * setter for the Transport stream (can be <code>null</code>)
	 * @param transportStream
	 */
	public void setTransportStream(final TransportStream transportStream) {
		this.transportStream = transportStream;
		if(transportStream!=null){
			updatePIDLists(transportStream,pidDialog);
			frame.setTitle(transportStream.getFile().getName()+ " - stream:"+ transportStream.getStreamID()+ " - DVB Inspector");
		}

		for(final TransportStreamView v: views) {
			v.setTransportStream(transportStream,viewContext);
		}
		enableViewMenus();

	}

	public DVBtree getTreeView() {
		return treeView;
	}

	public void setTreeView(final DVBtree treeView) {
		this.treeView = treeView;
	}



	/**
	 * Update the list of all pids when a new stream is loaded, used in the {@link PIDDialog}
	 * @param tStream
	 * @param pDialog
	 */
	private void updatePIDLists(final TransportStream tStream, final PIDDialog pDialog){

		final ViewContext viewConfig = new ViewContext();
		final ArrayList<ChartLabel> used = new ArrayList<>();
		final ArrayList<ChartLabel> notUsed = new ArrayList<>();

		if(tStream!=null){
			final short[] used_pids=tStream.getUsedPids();
			for (int i = 0; i < used_pids.length; i++) {
				final short actualPid = used_pids[i];
				used.add(new ChartLabel(actualPid+" - "+transportStream.getShortLabel(actualPid),actualPid, DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i%DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length]));
			}
			viewConfig.setStartPacket(0);
			viewConfig.setEndPacket(tStream.getNo_packets());
			viewConfig.setMaxPacket(tStream.getNo_packets());
			
			if(used_pids.length>0) {
				int graphSteps = 20_000 / used_pids.length;
				int stepsIndex =  Arrays.binarySearch(PIDDialog.STEP_OPTIONS, graphSteps);
				if(stepsIndex<0) {
					stepsIndex = -(stepsIndex+1);
					stepsIndex = Math.min(6, stepsIndex); //  6 == 100 steps, nice default
				}
				viewConfig.setGraphSteps(PIDDialog.STEP_OPTIONS[stepsIndex]);
			}
		}

		
		viewConfig.setShown(used);
		viewConfig.setNotShown(notUsed);
		viewConfig.setTransportStream(tStream);
		viewContext = viewConfig;
		pDialog.setConfig(viewConfig);
	}



	/**
	 * Called by PIDDialog, when the list of PIDs to be shown has changed. Forwards new list to all views that need it.
	 * @param vContext
	 */
	public void setPIDList(final ViewContext vContext){
		viewContext = vContext;
		if(transportStream!=null){
			bitRateView.setTransportStream(transportStream, vContext);
			barChart.setTransportStream(transportStream, vContext);
			gridView.setTransportStream(transportStream, vContext);
			timeStampChart.setTransportStream(transportStream, vContext);
			//ignore eitView on this, because it always uses only one PID.
		}
	}


	/**
	 * Listen for changes caused by selecting another tab from tabbedPane. Then enable/disable appropriate menu's.
	 *
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */

	public void stateChanged(final ChangeEvent e) {
		enableViewMenus();
	}

	/**
	 * enables menus based on which view is selected in the tabbedPane
	 */
	private void enableViewMenus() {
		final int i = tabbedPane.getSelectedIndex();
		viewTreeMenu.setEnabled((i==0)&&(transportStream!=null));
		viewMenu.setEnabled((i>1)&&(transportStream!=null));
//		exportMenuItem.setEnabled(transportStream!=null);
	}


	/**
	 * store current window position and size in preferences
	 */
	private void saveWindowState(){
		PreferencesManager.setWindowX(frame.getX());
		PreferencesManager.setWindowY(frame.getY());
		PreferencesManager.setWindowWidth(frame.getWidth());
		PreferencesManager.setWindowHeight(frame.getHeight());
	}

	/**
	 * called when "exit" is selected in menu.
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		saveWindowState();
		System.exit(0);
	}

	public JFrame getFrame() {
		return frame;
	}

	public int getModus() {
		return modus;
	}

	public void setModus(final int modus) {
		this.modus = modus;
	}


	public void setSearchEnumeration(DefaultMutableTreeNodePreorderEnumaration enummeration) {
		this.searchEnummeration = enummeration;
		if(enummeration==null){
			findNextAction.setEnabled(false);
		}else if(searchString!=null){
			findNextAction.setEnabled(true);
		}
		
	}

	public DefaultMutableTreeNodePreorderEnumaration getSearchEnumeration() {
		return searchEnummeration;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
		if(searchString==null){
			findNextAction.setEnabled(false);
		}else if(searchEnummeration!=null){
			findNextAction.setEnabled(true);
		}
	}

	public FindNextAction getFindNextAction() {
		return findNextAction;
	}


	public void resetSearch(){
		searchEnummeration = null;
		searchString = null;
		findNextAction.setEnabled(false);
	}

	/**
	 * 
	 */
	public void refreshViews() {
		
		for(final TransportStreamView view: views) {
			view.refreshView();
		}
	}


}

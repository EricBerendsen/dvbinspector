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
package nl.digitalekabeltelevisie.main;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.gui.AboutAction;
import nl.digitalekabeltelevisie.gui.BarChart;
import nl.digitalekabeltelevisie.gui.BitRateChart;
import nl.digitalekabeltelevisie.gui.DVBtree;
import nl.digitalekabeltelevisie.gui.FileOpenAction;
import nl.digitalekabeltelevisie.gui.GridView;
import nl.digitalekabeltelevisie.gui.PIDDialog;
import nl.digitalekabeltelevisie.gui.PIDDialogOpenAction;
import nl.digitalekabeltelevisie.gui.SetPrivateDataSpecifierAction;
import nl.digitalekabeltelevisie.gui.ToggleViewAction;
import nl.digitalekabeltelevisie.gui.TransportStreamView;
import nl.digitalekabeltelevisie.util.Utils;

import org.jfree.chart.plot.DefaultDrawingSupplier;

public class DVBinspector implements ChangeListener{

	private static Logger logger = Logger.getLogger(DVBinspector.class.getName());

	public static final String DEFAULT_PRIVATE_DATA_SPECIFIER = "private_data_spcifier";
	public static final String DEFAULT_VIEW_MODUS = "view_modus";

	private TransportStream transportStream;

	private JFrame frame;

	private final List<TransportStreamView> views = new ArrayList<TransportStreamView>();
	private DVBtree treeView;
	private BitRateChart bitRateView;
	private BarChart barChart;
	private GridView gridView;
	private final JFileChooser fc = new JFileChooser();
	private JTabbedPane tabbedPane;
	private JMenu viewTreeMenu;
	private JMenu viewMenu;
	private final JDialog aboutDialog = new JDialog();
	private PIDDialog pidDialog = null;

	private long defaultPrivateDataSpecifier = 0;

	private ViewContext viewContest = new ViewContext();

	private int modus;


	public DVBinspector() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final DVBinspector inspector = new DVBinspector();
		if(args.length>=1){
			final String filename= args[0];
			// TODO test if file exists
			final TransportStream ts = new TransportStream(filename);
			inspector.transportStream = ts;

			try {
				inspector.transportStream.parseStream();
				if(args.length>=2){

					PID[] pids = ts.getPids();
			        Map<Integer, GeneralPesHandler> pesHandlerMap = new HashMap<Integer, GeneralPesHandler>();
					for (int i = 1; i < args.length; i++) {
						int pid=Integer.parseInt(args[i]);
						PID p= pids[pid];
			            if (p != null) {
			                pesHandlerMap.put(Integer.valueOf(p.getPid()), p.getPesHandler());
			            }
			        }
			        ts.parseStream(pesHandlerMap);					
				}
			} catch (final IOException e) {
				logger.log(Level.WARNING, "error parsing transportStream", e);
			}
			

}

		inspector.run();
	}

	public void run() {
		final Preferences prefs = Preferences.userNodeForPackage(DVBinspector.class);
		defaultPrivateDataSpecifier = prefs.getLong(DVBinspector.DEFAULT_PRIVATE_DATA_SPECIFIER, 0);
		modus = prefs.getInt(DVBinspector.DEFAULT_VIEW_MODUS,0);

//		try {
//			if(transportStream!=null){
//				transportStream.parseStream();
//			}
//		} catch (final IOException e) {
//			logger.log(Level.WARNING, "error parsing transportStream", e);
//		}
//

		KVP.setNumberDisplay(KVP.NUMBER_DISPLAY_BOTH);
		KVP.setStringDisplay(KVP.STRING_DISPLAY_HTML_AWT);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(transportStream,modus);
			}
		});

	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 */
	private void createAndShowGUI(final TransportStream tStream,final int modus) {
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e)
		{
			logger.warning("Couldn't use system look and feel.");
		}

		//Create and set up the window.
		frame = new JFrame("DVB Inspector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// ViewContext viewContext = new ViewContext();
		pidDialog= new PIDDialog(frame,viewContest,this);
		updatePIDLists(tStream,pidDialog);

		tabbedPane = new JTabbedPane();

		treeView = new DVBtree(tStream,modus);
		tabbedPane.addTab("Tree", treeView);
		views.add(treeView);


		bitRateView = new BitRateChart(tStream,viewContest);
		tabbedPane.addTab("BitRate View", bitRateView);
		views.add(bitRateView);

		barChart = new BarChart(tStream,viewContest);
		tabbedPane.addTab("Bar View", barChart);
		views.add(barChart);

		gridView = new GridView(tStream,viewContest);
		tabbedPane.addTab("Grid View", gridView);
		views.add(gridView);
		tabbedPane.validate();

		tabbedPane.addChangeListener(this);

		frame.add(tabbedPane);
		//Add content to the window.
		//frame.add(treeView);

		JMenuBar menuBar;
		JMenu fileMenu;
		JMenu helpMenu;
		JMenu privateDataSubMenu;
		JMenuItem openMenuItem;
		JMenuItem filterItem;

		menuBar = new JMenuBar();
		fileMenu =new JMenu("File");
		menuBar.add(fileMenu);
		openMenuItem = new JMenuItem("Open",
				KeyEvent.VK_O);

		viewTreeMenu =new JMenu("Tree View");
		menuBar.add(viewTreeMenu);

		viewMenu =new JMenu("View");
		menuBar.add(viewMenu);

		helpMenu =new JMenu("Help");
		menuBar.add(helpMenu);
		JMenuItem aboutMenuItem;
		aboutMenuItem = new JMenuItem("About...");


		final Action aboutAction= new AboutAction(aboutDialog, frame,this);
		aboutMenuItem.addActionListener(aboutAction);
		helpMenu.add(aboutMenuItem);

		final JCheckBoxMenuItem simpleViewMenu = new JCheckBoxMenuItem("Simple Tree View");
		simpleViewMenu.setSelected((modus&DVBtree.SIMPLE_MODUS)!=0);
		final Action simpleViewAction= new ToggleViewAction(this, DVBtree.SIMPLE_MODUS);
		simpleViewMenu.addActionListener(simpleViewAction);
		viewTreeMenu.add(simpleViewMenu);

		final JCheckBoxMenuItem psiViewMenu = new JCheckBoxMenuItem("PSI Only");
		psiViewMenu.setSelected((modus&DVBtree.PSI_ONLY_MODUS)!=0);
		final Action psiOnlyViewAction= new ToggleViewAction(this, DVBtree.PSI_ONLY_MODUS);
		psiViewMenu.addActionListener(psiOnlyViewAction);
		viewTreeMenu.add(psiViewMenu);

		final JCheckBoxMenuItem packetViewMenu = new JCheckBoxMenuItem("Packet Count");
		packetViewMenu.setSelected((modus&DVBtree.PACKET_MODUS)!=0);
		final Action packetViewAction= new ToggleViewAction(this, DVBtree.PACKET_MODUS);
		packetViewMenu.addActionListener(packetViewAction);
		viewTreeMenu.add(packetViewMenu);

		final JCheckBoxMenuItem countListViewMenu = new JCheckBoxMenuItem("Number List Items");
		countListViewMenu.setSelected((modus&DVBtree.COUNT_LIST_ITEMS_MODUS)!=0);
		final Action countListViewAction= new ToggleViewAction(this, DVBtree.COUNT_LIST_ITEMS_MODUS);
		countListViewMenu.addActionListener(countListViewAction);
		viewTreeMenu.add(countListViewMenu);

		privateDataSubMenu = new JMenu("Private Data Specifier Default");
		final ButtonGroup group = new ButtonGroup();

		addPrivateDataSpecMenuItem(privateDataSubMenu, group, 0x00, "none",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, group, 0x16, "Casema",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, group, 0x28, "EACEM",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, group, 0x29, "Nordig",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, group, 0x40, "CI Plus LLP",defaultPrivateDataSpecifier);
		addPrivateDataSpecMenuItem(privateDataSubMenu, group, 0x600, "UPC",defaultPrivateDataSpecifier);
		viewTreeMenu.add(privateDataSubMenu);

		final Action fileOpenAction= new FileOpenAction(fc,frame,this);
		openMenuItem.addActionListener(fileOpenAction);
		fileMenu.add(openMenuItem);

		filterItem = new JMenuItem("Filter");
		viewMenu.add(filterItem);
		final Action pidOpenAction = new PIDDialogOpenAction(pidDialog,frame,this);
		filterItem.addActionListener(pidOpenAction);
		viewMenu.add(filterItem);

		frame.setJMenuBar(menuBar);

		enableViewMenus();

		final Image image = Utils.readIconImage("magnifying_glass.bmp");
		frame.setIconImage(image);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * @param privateDataSubMenu
	 * @param group
	 */
	private void addPrivateDataSpecMenuItem(final JMenu privateDataSubMenu, final ButtonGroup group, final long spec, final String name, final long defaultSpecifier) {
		final JMenuItem menuItem = new JRadioButtonMenuItem(Utils.toHexString(spec, 8)+" - "+name);
		group.add(menuItem);
		menuItem.addActionListener(new SetPrivateDataSpecifierAction(this,spec));
		menuItem.setSelected(spec==defaultSpecifier);
		privateDataSubMenu.add(menuItem);
	}

	public TransportStream getTransportStream() {
		return transportStream;
	}

	public void setTransportStream(final TransportStream transportStream) {
		this.transportStream = transportStream;
		if(transportStream!=null){
			updatePIDLists(transportStream,pidDialog);
			frame.setTitle(transportStream.getFile().getName()+ " - stream:"+ transportStream.getStreamID()+ " - DVB Inspector");
		}

		for(final TransportStreamView v: views) {
			v.setTransportStream(transportStream,viewContest);
		}
		enableViewMenus();

	}

	public DVBtree getTreeView() {
		return treeView;
	}

	public void setTreeView(final DVBtree treeView) {
		this.treeView = treeView;
	}



	private void updatePIDLists(final TransportStream tStream, final PIDDialog pDialog){

		final ViewContext viewConfig = new ViewContext();
		final ArrayList<ChartLabel> used = new ArrayList<ChartLabel>();
		final ArrayList<ChartLabel> notUsed = new ArrayList<ChartLabel>();

		if(tStream!=null){
			final short[] used_pids=tStream.getUsedPids();
			//ChartLabel[] labels= new ChartLabel[transportStream.getNoPIDS()];
			for (int i = 0; i < used_pids.length; i++) {
				used.add(new ChartLabel(used_pids[i]+" - "+transportStream.getShortLabel(used_pids[i]),used_pids[i], DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE[i%DefaultDrawingSupplier.DEFAULT_PAINT_SEQUENCE.length]));
			}
			viewConfig.setStartPacket(0);
			viewConfig.setEndPacket(tStream.getNo_packets());
			viewConfig.setMaxPacket(tStream.getNo_packets());
		}
		viewConfig.setShown(used);
		viewConfig.setNotShown(notUsed);
		viewConfig.setTransportStream(tStream);
		viewContest = viewConfig;
		pDialog.setConfig(viewConfig);
	}


	public void setPIDList(final ViewContext vContext){
		viewContest = vContext;
		if(transportStream!=null){
			bitRateView.setTransportStream(transportStream, vContext);
			barChart.setTransportStream(transportStream, vContext);
			gridView.setTransportStream(transportStream, vContext);
		}
	}

	/**
	 * @return the defaultPrivateDataSpecifier
	 */
	public long getDefaultPrivateDataSpecifier() {
		return defaultPrivateDataSpecifier;
	}

	/**
	 * @param defaultPrivateDataSpecifier the defaultPrivateDataSpecifier to set
	 */
	public void setDefaultPrivateDataSpecifier(final long defaultPrivateDataSpecifier) {
		this.defaultPrivateDataSpecifier = defaultPrivateDataSpecifier;
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 * 
	 * Listen for changes caused by selecting another tab from tabbedPane. Then enable/disable appropriate menu's.
	 */

	public void stateChanged(final ChangeEvent e) {
		enableViewMenus();
	}

	/**
	 * 
	 */
	private void enableViewMenus() {
		final int i = tabbedPane.getSelectedIndex();
		viewTreeMenu.setEnabled((i==0)&&(transportStream!=null));
		viewMenu.setEnabled((i!=0)&&(transportStream!=null));
	}

}

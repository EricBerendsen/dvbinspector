/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * <p>
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
 * <p>
 *  This file is part of DVB Inspector.
 * <p>
 *  DVB Inspector is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  DVB Inspector is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with DVB Inspector.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 *  The author requests that he be notified of any application, applet, or
 *  other binary that makes use of this code, but that's more out of curiosity
 *  than anything and is not required.
 *
 */

package nl.digitalekabeltelevisie.gui;

import static nl.digitalekabeltelevisie.util.Utils.toHexStringUnformatted;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.KVP.DetailView;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.ServiceDSMCC.DSMFile;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPidHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.audio.Audio138183Handler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ebu.EBUTeletextHandler;
import nl.digitalekabeltelevisie.data.mpeg.pes.ebu.SubPage;
import nl.digitalekabeltelevisie.data.mpeg.pid.t2mi.PlpHandler;
import nl.digitalekabeltelevisie.data.mpeg.psi.handler.GeneralPsiTableHandler;
import nl.digitalekabeltelevisie.gui.utils.GuiUtils;
import nl.digitalekabeltelevisie.main.DVBinspector;
import nl.digitalekabeltelevisie.util.KvpPreorderEnumaration;
import nl.digitalekabeltelevisie.util.JTreeLazyList;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * DVBTree is the container for the JTree (on the left side) and the image and text on the right side.
 * Also sets up pop-up menu items for the JTree, and handles events from it.
 *
 * @author Eric
 *
 */
public class DVBtree extends JPanel implements HyperlinkListener, TransportStreamView , TreeSelectionListener, ActionListener, ClipboardOwner {
	
	

	public static final String STOP = "stop";
	public static final String PLAY = "play";
	public static final String EXPORT = "export";
	public static final String SAVE = "save";
	public static final String PARSE = "parse";
	public static final String T2MI = "t2mi";
	public static final String T42 = "t42";

	private static final String EXPAND = "expand";
	private static final String EXPAND_ALL = "expand_all";

	private static final String COPY = "copy";
	private static final String VIEW = "view";
	private static final String COPY_TREE = "copy_tree";
	
	private static final String SAVE_BYTES = "save_bytes";
	
	// update when adding a menu option that is always present
	private static final int NO_DEFAULT_ELEMENTS_POP_UP_MENU = 5;
	private static final long MAX_EXPAND_ALL_TIME_MILLISECS = 500L;

	public class CopyAction extends AbstractAction implements ClipboardOwner {

		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof KVP kvp) {
				copyItemToClipboard(kvp);
			}
		}

		@Override
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// ignore
		}

	}	
	
	public class ExpandAllAction extends AbstractAction {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof KVP kvp) {
				expandAllItems(kvp);
			}
		}
	}
	
	private static final long serialVersionUID = 9200238343077897328L;
	private static final Logger logger = Logger.getLogger(DVBtree.class.getName());

	private final JTree tree;
	private final JTabbedPane detailPanel;
	private final JPopupMenu popup;

	public static final int SIMPLE_MODUS=0x1;
	public static final int PSI_ONLY_MODUS=0x2;
	public static final int PACKET_MODUS=0x4;
	public static final int COUNT_LIST_ITEMS_MODUS=0x8;
	public static final int SHOW_PTS_MODUS=0x10;
	public static final int SHOW_VERSION_MODUS=0x20;

	private int mod;
	private TransportStream ts;
	private DefaultTreeModel model;
	
	private DVBinspector controller;

	/**
	 *
	 * Creates a new DVBTree
	 *
	 * @param transportStream stream to be displayed (can be {@code null})
	 * @param modus determines options of JTree, (like simple view, number list items, etc.)
	 */
	public DVBtree(TransportStream transportStream, int modus, DVBinspector controller) {
		super(new GridLayout(1,0));
		this.mod=modus;
		this.ts=transportStream;
		this.controller = controller;

		//Create a tree that allows one selection at a time.
		if(ts!=null){
			model=new DefaultTreeModel(ts.getJTreeNode(modus));
			tree = new JTree(model);
		}else{
			tree = new JTree(new Object[] {});
		}

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(true);
		tree.addTreeSelectionListener(this);

		popup = new JPopupMenu();

		JMenuItem expandMenuItem = new JMenuItem("Expand");
		expandMenuItem.addActionListener(this);
		expandMenuItem.setActionCommand(EXPAND);
		popup.add(expandMenuItem);

		JMenuItem expandAllMenuItem = new JMenuItem("Expand All");
		expandAllMenuItem.addActionListener(this);
		expandAllMenuItem.setActionCommand(EXPAND_ALL);
		popup.add(expandAllMenuItem);

		JMenuItem copyMenuItem = new JMenuItem("Copy Item to clipboard");
		copyMenuItem.addActionListener(this);
		copyMenuItem.setActionCommand(COPY);
		popup.add(copyMenuItem);

		JMenuItem treeMenuItem = new JMenuItem("Copy Entire Sub Tree to clipboard");
		treeMenuItem.addActionListener(this);
		treeMenuItem.setActionCommand(COPY_TREE);
		popup.add(treeMenuItem);

		JMenuItem viewMenuItem = new JMenuItem("Copy Visible Sub Tree to clipboard");
		viewMenuItem.addActionListener(this);
		viewMenuItem.setActionCommand(VIEW);
		popup.add(viewMenuItem);

		tree.addMouseListener(
				new MouseAdapter() {
					@Override
					public void mouseReleased( MouseEvent e ) {
						if ( e.isPopupTrigger()) {
							showContextMenu(e);
						}
					}
					@Override
					public void mousePressed(MouseEvent e) {
						if ( e.isPopupTrigger()) {
							showContextMenu(e);
						}
					}

				});

		//Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);

		InputMap inputMap = tree.getInputMap();
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK);
		inputMap.put(key, COPY);
		tree.getActionMap().put(COPY, new CopyAction());
		
		KeyStroke keyStar = KeyStroke.getKeyStroke('*');
		inputMap.put(keyStar, EXPAND_ALL);
		tree.getActionMap().put(EXPAND_ALL, new ExpandAllAction());
		
		detailPanel = new JTabbedPane();
		
		// hide tabs when only one tab is present
		// https://stackoverflow.com/questions/942500/is-there-a-way-to-hide-the-tab-bar-of-jtabbedpane-if-only-one-tab-exists
		
		detailPanel.setUI(new BasicTabbedPaneUI() {  
		    @Override  
		    protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
		        if (detailPanel.getTabCount() > 1) {
		            return super.calculateTabAreaHeight(tabPlacement, horizRunCount, maxTabHeight);
		        }
	            return 0;  
		    }  
		});  
		

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(detailPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(12);

		Dimension minimumSize = new Dimension(300, 670);
		treeView.setMinimumSize(minimumSize);
		treeView.setPreferredSize(new Dimension(980, 670));
		splitPane.setDividerLocation(500);

		add(splitPane);
	}

	/**
	 * @param e
	 * @return
	 */
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
			MutableTreeNode node = findNodeByTrail(e.getDescription(), model);
			if(node!=null) {
				showNode(node);
			}
		}
	}

	/**
	 * @param trail
	 * @param treeModel
	 * @return
	 */
	private MutableTreeNode findNodeByTrail(String trail, DefaultTreeModel treeModel) {
		if(trail == null ||
                !trail.startsWith("root") ||
                tree == null) {
			return null;
		}
		
		List<String> pathList = Arrays.asList(trail.split("/"));
		return searchNode(pathList,(KVP)treeModel.getRoot());
	}

	/**
	 * @param crumbTrail
	 * @param node
	 * @return
	 */
	private MutableTreeNode searchNode(List<String> crumbTrail, KVP node) {
		if (crumbTrail == null || crumbTrail.isEmpty()) {
			return null;
		}
		String crumbFound = node.getCrumb();
		if (crumbTrail.getFirst().equalsIgnoreCase(crumbFound)) {
			if (crumbTrail.size() == 1) {
				return node;
			}
			List<String> subList = crumbTrail.subList(1, crumbTrail.size());
			for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
				TreeNode nextChild = e.nextElement();

				switch (nextChild) {
				case KVP dmtn:
					MutableTreeNode res = searchNode(subList, dmtn);
					if (res != null) {
						return res;
					}
					break;
				case JTreeLazyList.RangeNode rangeNode when subList.get(0).equalsIgnoreCase(rangeNode.getLabel().trim()):
					return rangeNode.findChildForActual(Integer.parseInt(subList.get(1)));
				default:
					// EMPTY
				}

			}
		}
		// name does not match
		return null;
	}

	/**
	 * Update existing DVBTree to display a new {@link TransportStream}
	 *
	 * @param transportStream stream to be displayed (can be {@code null})
	 * @param viewContext ignored, required by {@link TransportStreamView}

	 * @see TransportStreamView#setTransportStream(TransportStream, ViewContext)
	 */
	@Override
	public void setTransportStream(TransportStream transportStream, ViewContext viewContext){
		ts=transportStream;
		if(ts!=null){
			model=new DefaultTreeModel(ts.getJTreeNode(mod));
			tree.setModel(model);
		}else{
			tree.setModel(null);
		}
		tree.getSelectionModel().setSelectionMode
		(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.setRootVisible(true);
	}


	/**
	 * @return the current view modus
	 */
	public int getMod() {
		return mod;
	}

	/**
	 *  Toggle one or more bits in the current modus
	 * @param modus mask of bit to be set
	 * @return true if bit(s) was set as result of this operation
	 */
	public boolean toggleMod(int modus) {

		mod = mod ^ modus;

		PreferencesManager.setDefaultViewModus(mod);

		refreshView();
		return (this.mod&modus)!=0;
	}

	
	@Override
	public void refreshView() {
		String state = "";
		if(isNotStructuralChange(mod)) {
			state = getExpansionState();
		}
		rebuildTree();
		if(isNotStructuralChange(mod)) {
			setExpansionState(state);
		}
	}

	/**
	 * 
	 * @param modus
	 * @return true when toggling this modus will not change the number of nodes in the tree, or the structure of it
	 */
	private static boolean isNotStructuralChange(int modus) {
		return (modus != SIMPLE_MODUS) && (modus != PSI_ONLY_MODUS) && (modus != PACKET_MODUS);
	}

	private void rebuildTree(){
		if(ts!=null){
			model = new DefaultTreeModel(ts.getJTreeNode(mod));
			tree.setModel(model);
		} else {
			tree.setModel(null);
		}
	}
	
	
	
	/**
     *
     * Retrieves the expansion state as a String, defined by a comma delimited list
     * of each row node that is expanded.
     * <p>
     * Based on {@link <a href="http://www.algosome.com/articles/save-jtree-expand-state.html">...</a>}
     *
     * @return the expansion state as a String, defined by a comma delimited list
     *
     */

	private String getExpansionState() {
		StringBuilder sb = new StringBuilder();

		int rowCount = tree.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			if (tree.isExpanded(i)) {
				sb.append(i).append(",");
			}
		}
		return sb.toString();
	}
	

	/**
     * Sets the expansion state based upon a comma delimited list of row indexes that
     * are expanded.
     * <p>
     * Based on {@link <a href="http://www.algosome.com/articles/save-jtree-expand-state.html">...</a>}
     *
     * @param s the expansion state based upon a comma delimited list of row indexes that need to be expanded
     */

	private void setExpansionState(String s) {
		String[] indexes = s.split(",");

		for (String st : indexes) {
			int row = Integer.parseInt(st);
			tree.expandRow(row);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		// node1 is either a KVP (most normal items) or a
		// MutableTreeNode (for LazyList)
		MutableTreeNode node1 = (MutableTreeNode) tree.getLastSelectedPathComponent();
		detailPanel.removeAll();

		if (node1 == null) {
			return;
		}
		// not always a KVP
		if (node1 instanceof KVP kvp) {

			List<DetailView> detailViews = kvp.getDetailViews();
			if (detailViews.isEmpty()) {
				return;
			}
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			for (DetailView view : detailViews) {
				createTabForView(view);
			}
			setCursor(Cursor.getDefaultCursor());
			return;
		}

		detailPanel.removeAll();
	}

	private void createTabForView(DetailView view) {
		DetailSource detailSource = view.detailSource();
		String label = view.label();

		switch(detailSource){
			case ImageSource imageSource:
				BufferedImage img;
				try {
					img = imageSource.getImage();
				} catch (RuntimeException e1) {
					logger.log(Level.WARNING, "could not create image from getImageSource():", e1);
					img = GuiUtils.getErrorImage("Ooops.\n\n" + "Something went wrong generating this image.\n\n"  + GuiUtils.getImproveMsg());
				}
				if(img != null){
					ImagePanel imagePanel = new ImagePanel();
					imagePanel.setImage(img);
					detailPanel.addTab(label, imagePanel);
				}
				break;
			case HTMLSource htmlSource:
				HtmlPanel htmlPanel = new HtmlPanel(controller, this, htmlSource.getHTML());
				detailPanel.addTab(label, new JScrollPane(htmlPanel));
				break;
			case XMLSource xmlSource:
				XmlPanel xmlPanel = new XmlPanel();
				// hack to reset
				xmlPanel.setDocument(xmlPanel.getEditorKit().createDefaultDocument());
				xmlPanel.setText(xmlSource.getXML());
				xmlPanel.setCaretPosition(0);
				detailPanel.addTab(label, new JScrollPane(xmlPanel));
				break;
			case TableSource tableSource:
				try {
					TableModel tableModel = tableSource.getTableModel();
					if(tableModel.getColumnCount()>0 && tableModel.getRowCount()>0) {
						TablePanel tablePanel = new TablePanel(new JTable());
						tablePanel.setModel(tableModel);
						detailPanel.addTab(label, tablePanel);
					}
				}catch (RuntimeException e2) {
					logger.log(Level.WARNING, "could not create table:", e2);
				}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		TreePath path = tree.getSelectionPath();
		if(path!=null){
			KVP kvp = (KVP) path.getLastPathComponent();
			String actionCommand = actionEvent.getActionCommand();
			switch (actionCommand){
				case EXPAND -> expandItem();
				case EXPAND_ALL ->expandAllItems(kvp);
				case COPY -> copyItemToClipboard(kvp);
				case COPY_TREE -> copyEntireSubTreeToClipboard(kvp);
				case VIEW -> copyVisibleSubTreeToClipboard(kvp, path);
				case PARSE -> parsePid(kvp);
				case SAVE -> saveDsmccFile(kvp);
				case EXPORT -> saveDsmccTree(kvp);
				case PLAY -> playAudio138183(kvp);
				case STOP -> stopAudio138183(kvp);
				case T2MI -> saveT2miTs(kvp);
				case T42 -> saveT42File(kvp);
				case SAVE_BYTES -> saveBytes(kvp);
			}
		}
	}

	/**
	 * @param kvp start node from where to expand
	 */
	private void expandAllItems(KVP kvp) {
		expandItem();
		long end = System.currentTimeMillis() + MAX_EXPAND_ALL_TIME_MILLISECS;
		expandAllItemsRecursive(kvp,end);
	}
	
	/**
	 * @param kvp start node from where to expand
	 * @param end time (System.currentTimeMillis()) when this should stop expanding, to prevent long responses in the AWT Event thread
	 */
	private void expandAllItemsRecursive(KVP kvp, long end) {
		if (System.currentTimeMillis() > end) {
			return;
		}

		tree.expandPath(new TreePath(kvp.getPath()));
		Enumeration<?> children = kvp.children();

		while (children.hasMoreElements()) {
			if (children.nextElement() instanceof KVP child && !child.isLeaf()) {
				expandAllItemsRecursive(child, end);
			}
		}
	}	

	private void expandItem() {
		tree.expandPath(tree.getSelectionPath());
	}

	private void saveT2miTs(KVP kvp) {
		
		PlpHandler plpHandler = (PlpHandler)kvp.getOwner();
		String fileName = ts.getFile().getName()+"_pid_"+plpHandler.getPid()+"_plp_"+plpHandler.getPlpId()+".ts";
		
		selectFileAndSave(fileName, plpHandler);

	}

	/**
	 * @param kvp
	 */
	private static void stopAudio138183(KVP kvp) {
		Audio138183Handler audioHandler = (Audio138183Handler) kvp.getOwner();
		audioHandler.stop();
	}

	/**
	 * @param kvp
	 */
	private static void playAudio138183(KVP kvp) {
		Audio138183Handler audioHandler = (Audio138183Handler) kvp.getOwner();
		audioHandler.play();
	}

	/**
	 * @param kvp
	 */
	private void saveDsmccTree(KVP kvp) {
		DSMFile dsmFile = (DSMFile) kvp.getOwner();

		JFileChooser chooser = GuiUtils.createFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			PreferencesManager.setSaveDir(file.getAbsolutePath());

			logger.info("Preparing to save as: " + file.getName() + ", path:" + file.getAbsolutePath());
			// start at selected directory, first create new subFolder with name "label"
			File newDir = new File(file, dsmFile.getLabel());
			confirmOverwriteIfExisting(newDir, dsmFile);
		} else {
			logger.info("Open command cancelled by user.");
		}
	}


	/**
	 * @param kvp
	 */
	private void saveDsmccFile(KVP kvp) {
		DSMFile dsmFile = (DSMFile) kvp.getOwner();
		String fileName = dsmFile.getLabel();

		selectFileAndSave(fileName, dsmFile);
	}

	
	private void saveT42File(KVP kvp) {
		Object owner = kvp.getOwner();
			switch(owner){
				case SubPage subPage:
					String fileName = "Page" + subPage.getMagazineNo() + toHexStringUnformatted(subPage.getPageNo(), 2) + "-"
							+ toHexStringUnformatted(subPage.getSubPageNo(), 4) + ".t42";
					selectFileAndSave(fileName, subPage);
					break;
				case EBUTeletextHandler txtHandler:
                    selectFileAndSave("Txt Service.t42", txtHandler);
					break;
                default:
            }
	}

	/**
	 * @param dmtn
	 * @param kvp
	 */
	private void parsePid(KVP kvp) {
		PID p = (PID) kvp.getOwner();
		int pid = p.getPid();
		GeneralPidHandler generalPidHandler = p.getPidHandler();
		if(generalPidHandler.isInitialized()) return; // prevent double click

		Map<Integer, GeneralPidHandler> handlerMap = new HashMap<>();
		handlerMap.put(pid, generalPidHandler);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			ts.parsePidStreams(handlerMap);
		} catch (IOException e) {
			logger.log(Level.WARNING,"could not read file "+ts.getFile().getName()+" while parsing PES",e);
			setCursor(Cursor.getDefaultCursor());
		}catch (RuntimeException e) {
			logger.log(Level.WARNING,"could not parse PID "+pid+" using handler "+generalPidHandler.getClass().getName(),e);
			if((generalPidHandler.getClass() != GeneralPesHandler.class) &&
				(generalPidHandler.getClass() != GeneralPsiTableHandler.class)){ // only if specialized subclass of GeneralPesHandler
				logger.log(Level.WARNING,"trying again with GeneralPesHandler");
				JOptionPane.showMessageDialog(this,
						"Error parsing PID PES Packets for "+p.getLabelMaker()+", falling back to general PES packets",
						"DVB Inspector",
						JOptionPane.WARNING_MESSAGE);
				p.setPidHandler(new GeneralPesHandler());
				generalPidHandler = p.getPidHandler();
				handlerMap = new HashMap<>();
				handlerMap.put(pid, generalPidHandler);
				try {
					ts.parsePidStreams(handlerMap);
				} catch (IOException e1) {
					logger.log(Level.WARNING,"could not read file "+ts.getFile().getName()+" while parsing PES again with general PESHandler",e1);
				}
				setCursor(Cursor.getDefaultCursor());
			}
		}
		KVP node = p.getPidHandler().getJTreeNode(mod);
		// https://www.java-tips.org/java-se-tips-100019/15-javax-swing/2393-have-a-popup-attached-to-a-jtree.html
		// thanks to Yong Zhang for the tip for refreshing the tree structure.
		kvp.add(node);
		((DefaultTreeModel)tree.getModel()).nodeStructureChanged(kvp);
		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * @param dmtn
	 * @param path
	 * @param kvp
	 */
	private void copyVisibleSubTreeToClipboard(KVP kvp, TreePath path) {

		String res = kvp.getPlainText() + System.lineSeparator() +
				getViewTree(kvp, "", path);
		StringSelection stringSelection = new StringSelection(res);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}

	/**
	 * @param dmtn
	 */
	private void copyEntireSubTreeToClipboard(KVP kvp) {
		String treeString = kvp.getPlainText() + System.lineSeparator() +
				getEntireTree(kvp, "");

		StringSelection stringSelection = new StringSelection( treeString );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}

	/**
	 * @param kvp
	 */
	private void copyItemToClipboard(KVP kvp) {
		StringSelection stringSelection = new StringSelection( kvp.getPlainText() );
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}

	/**
	 * @param kvp
	 * @return
	 */

	public static StringBuilder getEntireTree(KVP kvp, String preFix) {
		StringBuilder res = new StringBuilder();
		Enumeration<TreeNode> children = kvp.children();
		while (children.hasMoreElements()) {
			TreeNode next = children.nextElement();
			if (next instanceof KVP child) {
				res.append(preFix).append("+-").append(child.getPlainText()).append(System.lineSeparator());
				if (!child.isLeaf()) {
					if (child == kvp.getLastChild()) { // lastChild
						res.append(getEntireTree(child, preFix + "  ")); // last , so prefix with " "
					} else {
						res.append(getEntireTree(child, preFix + "| ")); // more children follow, so start with "| "
					}
				}
			}
		}
		return res;

	}

	/**
	 * @param kvp
	 * @return
	 */

	private StringBuilder getViewTree(KVP kvp, String preFix, TreePath path) {
		StringBuilder res = new StringBuilder();
		Enumeration<TreeNode> children = kvp.children();
		while(children.hasMoreElements()){

			TreeNode next = children.nextElement();
			if(next instanceof KVP child){
				TreePath childPath = path.pathByAddingChild(child);
				if(tree.isVisible(childPath)){
					res.append(preFix).append("+-").append(child.getPlainText()).append(System.lineSeparator());
					if(!child.isLeaf()){
						if (child == kvp.getLastChild()) { // lastChild
							res.append(getViewTree(child, preFix + "  ", childPath)); // last , so prefix with "  "
						} else {
							res.append(getViewTree(child, preFix + "| ", childPath)); // more children follow, so start with "| "
						}
					}
				}
			}
		}
		return res;
	}

	/**
	 * @param e
	 */
	private void showContextMenu(MouseEvent e) {
		TreePath path = tree.getSelectionPath();
		
		
		MenuElement[] subs = popup.getSubElements();
		for (MenuElement sub : subs) {
			JMenuItem menuElement = (JMenuItem) sub;
			menuElement.setEnabled(path != null);

		}
		
		// remove option menu from previous invocation
		if(popup.getSubElements().length > NO_DEFAULT_ELEMENTS_POP_UP_MENU){
			popup.remove(NO_DEFAULT_ELEMENTS_POP_UP_MENU);
		}

		//		add optional menu

		if(path!=null){
            if((path.getLastPathComponent() instanceof KVP kvp)){
				JMenuItem subMenu = kvp.getSubMenu();
				if(subMenu!=null){
					Object owner =  kvp.getOwner();
					if(owner instanceof PID p){ // if PID has a owner, it is a PES that maybe has not been parsed yet.
						GeneralPidHandler pesH = p.getPidHandler();
						if(!pesH.isInitialized()){
							subMenu.addActionListener(this);
							popup.add(subMenu);
						}
					}else{
						subMenu.removeActionListener(this);
						subMenu.addActionListener(this);
						popup.add(subMenu);
					}
				}else{
					if(kvp.isBytes()){
						JMenuItem bytesMenu = new JMenuItem("Save bytes as...");

						bytesMenu.setActionCommand(SAVE_BYTES);
						bytesMenu.addActionListener(this);
						popup.add(bytesMenu);
					}
				}
			}else{ //not a KVP, so it is a mutabletree node. Only used for TS packets lazy tree, so disable menu's
				 	subs = popup.getSubElements();
				for (MenuElement sub : subs) {
					JMenuItem menuElement = (JMenuItem) sub;
					menuElement.setEnabled(false);

				}

			}
		}

		popup.show((Component) e.getSource(), e.getX(), e.getY() );
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// empty block, we don't care

	}

	public boolean findAndShow(String s,KvpPreorderEnumaration enumeration) {
		
        KVP node = searchNode(s.toLowerCase(),enumeration);
        if (node != null) {
          showNode(node);
          return true;
        }
        return false;
	}
	
	public void showRoot() {
		 showNode((TreeNode)model.getRoot());
	}

	private void showNode(TreeNode node) {
		TreeNode[] nodes = model.getPathToRoot(node);
		TreePath path = new TreePath(nodes);
		
		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}

	public KvpPreorderEnumaration createNewDefaultMutableTreeNodePreorderEnumaration(){
		return new KvpPreorderEnumaration((KVP)model.getRoot());
	}

	private static KVP searchNode(String targetString, KvpPreorderEnumaration enumeration) {
		while (enumeration.hasMoreElements()) {
			KVP node = enumeration.nextElement();
			if (node != null){
				String nodeString = node.toString();
				if( nodeString.toLowerCase().contains(targetString)) {
					return node;
				}
			}
		}
		return null;
	}


	private void saveBytes(KVP kvp) {

		if(! kvp.isBytes()) {
			return;
		}
		SaveAble saveAble = file -> {
			try (FileOutputStream out = new FileOutputStream(file)) {
				out.write(kvp.getByteValue());
			} catch (IOException e) {
				logger.log(Level.WARNING, "could not write file", e);
			}
		};
		
		selectFileAndSave(kvp.getLabel(), saveAble);
	}

	/**
	 * @param fileName
	 * @param saveAble
	 */
	private void selectFileAndSave(String fileName, SaveAble saveAble) {
		JFileChooser chooser = GuiUtils.createFileChooser();

		chooser.setSelectedFile(new File(fileName));

		int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			PreferencesManager.setSaveDir(file.getParent());

			logger.info("Preparing to save as: " + file.getName() + ", path:" + file.getAbsolutePath() );
			confirmOverwriteIfExisting(file, saveAble);
		} else {
			logger.info("Save cancelled by user." );
		}
	}

	/**
	 * @param file
	 * @param saveAble
	 */
	private void confirmOverwriteIfExisting(File file, SaveAble saveAble) {
		boolean write=true;
		if(file.exists()){
			logger.log(Level.INFO, "file {} already exists.", file);
			int option = JOptionPane.showConfirmDialog(
					this, "File "+file+" already exists, want to overwrite?",
					"File already exists",
					JOptionPane.YES_NO_OPTION);
			if (JOptionPane.NO_OPTION == option) {
				write=false;
				logger.info("User canceled overwrite");
			}else{
				logger.info("User confirmed overwrite");
			}
		}
		if (write) {
			saveAble.save(file);
		}
	}

}

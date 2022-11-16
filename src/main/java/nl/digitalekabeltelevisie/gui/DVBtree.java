/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.toHexStringUnformatted;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.digitalekabeltelevisie.controller.KVP;
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
import nl.digitalekabeltelevisie.gui.xmleditorkit.XMLEditorKit;
import nl.digitalekabeltelevisie.main.DVBinspector;
import nl.digitalekabeltelevisie.util.DefaultMutableTreeNodePreorderEnumaration;
import nl.digitalekabeltelevisie.util.JTreeLazyList;
import nl.digitalekabeltelevisie.util.PreferencesManager;

/**
 * DVBTree is the container for the JTree (on the left side) and the image and text on the right side.
 * Also sets up pop-up menu items for the JTree, and handles events from it.
 *
 * @author Eric
 *
 */
public class DVBtree extends JPanel implements TransportStreamView , TreeSelectionListener, ActionListener, ClipboardOwner {

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			final TreePath path = tree.getSelectionPath();
			if (path != null 
					&& path.getLastPathComponent() instanceof DefaultMutableTreeNode dmtn
					&& dmtn.getUserObject() instanceof final KVP kvp) {
				copyItemToClipboard(kvp);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.
		 * Clipboard, java.awt.datatransfer.Transferable)
		 */
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
			final TreePath path = tree.getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode dmtn) {
				expandAllItems(dmtn);
			}
		}
	}
	
	/**
	 * key for cardlayout
	 */
	private static final String EMPTY_PANEL = "empty";
	/**
	 * key for cardlayout
	 */
	private static final String HTML_PANEL = "html";
	/**
	 * key for cardlayout
	 */
	private static final String IMAGE_PANEL = "image";

	/**
	 * key for cardlayout
	 */
	private static final String TABLE_PANEL = "table";

	/**
	 * key for cardlayout
	 */
	private static final String XML_PANEL = "xml";
	
	private static final long serialVersionUID = 9200238343077897328L;
	private static final Logger logger = Logger.getLogger(DVBtree.class.getName());

	private final JTree tree;
	private final JPanel detailPanel;
	private final JEditorPane editorPane;
	private final JEditorPane xmlPane;
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
	private final ImagePanel imagePanel = new ImagePanel();
	private final TablePanel tablePanel = new TablePanel(new JTable());

	/**
	 *
	 * Creates a new DVBTree
	 *
	 * @param transportStream stream to be displayed (can be {@code null})
	 * @param modus determines options of JTree, (like simple view, number list items, etc.)
	 */
	public DVBtree(final TransportStream transportStream, final int modus, DVBinspector controller) {
		super(new GridLayout(1,0));
		mod=modus;
		ts=transportStream;

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
					public void mouseReleased( final MouseEvent e ) {
						if ( e.isPopupTrigger()) {
							showContextMenu(e);
						}
					}
					@Override
					public void mousePressed(final MouseEvent e) {
						if ( e.isPopupTrigger()) {
							showContextMenu(e);
						}
					}

				});

		//Create the scroll pane and add the tree to it.
		final JScrollPane treeView = new JScrollPane(tree);

		InputMap inputMap = tree.getInputMap();
		KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK);
		inputMap.put(key, COPY);
		tree.getActionMap().put(COPY, new CopyAction());
		
		KeyStroke keyStar = KeyStroke.getKeyStroke('*');
		inputMap.put(keyStar, EXPAND_ALL);
		tree.getActionMap().put(EXPAND_ALL, new ExpandAllAction());

		editorPane = new JEditorPane();
		editorPane.getTransferHandler();
		editorPane.setContentType("text/html");
		editorPane.setText(null);
		editorPane.setEditable(false);
		editorPane.setBackground(Color.LIGHT_GRAY);
		editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		editorPane.setTransferHandler(new EditorTextHTMLTransferHandler(controller));


		editorPane.addHyperlinkListener(this::handleHyperLinkEvent);
		
		xmlPane = new JEditorPane();
		xmlPane.setEditorKit(new XMLEditorKit());
		xmlPane.setText(null);
		xmlPane.setEditable(false);
		xmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

		
		detailPanel = new JPanel(new CardLayout());
		JPanel empty = new JPanel();
		//each panel should handle own scrolling
		detailPanel.add(empty,EMPTY_PANEL);
		detailPanel.add(imagePanel,IMAGE_PANEL);
		detailPanel.add(new JScrollPane(editorPane),HTML_PANEL);
		detailPanel.add(tablePanel,TABLE_PANEL);
		detailPanel.add(new JScrollPane(xmlPane),XML_PANEL);


		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(detailPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(12);

		final Dimension minimumSize = new Dimension(300, 670);
		treeView.setMinimumSize(minimumSize);
		treeView.setPreferredSize(new Dimension(980, 670));
		splitPane.setDividerLocation(500);

		add(splitPane);
	}

	/**
	 * @param e
	 * @return
	 */
	private void handleHyperLinkEvent(HyperlinkEvent e) {
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
		if(trail ==null || 
				trail.isEmpty() || 
				!trail.startsWith("root") || 
				tree == null) {
			return null;
		}
		
		List<String> pathList = Arrays.asList(trail.split("/"));
		return searchNode(pathList,(DefaultMutableTreeNode)treeModel.getRoot());
		
		
	}

	/**
	 * @param crumbTrail
	 * @param node
	 * @return
	 */
	private MutableTreeNode searchNode(List<String> crumbTrail, DefaultMutableTreeNode node) {
		if (crumbTrail == null || crumbTrail.isEmpty()) {
			return null;
		}
		if (node.getUserObject() instanceof KVP kvp) {
			String crumbFound = kvp.getCrumb();
			if (crumbTrail.get(0).equalsIgnoreCase(crumbFound)) {
				if (crumbTrail.size() == 1) {
					return node;
				}
				List<String> subList = crumbTrail.subList(1, crumbTrail.size());
				for (Enumeration<TreeNode> e = node.children(); e.hasMoreElements();) {
					TreeNode nextChild = e.nextElement();
					MutableTreeNode res;
					if (nextChild instanceof DefaultMutableTreeNode dmtn) {
						res = searchNode(subList, dmtn);
						if (res != null) {
							return res;
						}
					} else if (nextChild instanceof JTreeLazyList.RangeNode rangeNode
							&& subList.get(0).equalsIgnoreCase(rangeNode.getLabel().trim())) {
						return rangeNode.findChildForActual(Integer.parseInt(subList.get(1)));
					}
				}
			}
			// name does not match
			return null;
		}
		// not a KVP, should not happen.
		logger.info("node.getUserObject() NOT instanceof KVP kvp");
		return null;
	}

	/**
	 * Update existing DVBTree to display a new {@link TransportStream}
	 *
	 * @param transportStream stream to be displayed (can be {@code null})
	 * @param viewContext ignored, required by {@link TransportStreamView}

	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	@Override
	public void setTransportStream(final TransportStream transportStream, final ViewContext viewContext){
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
	public boolean toggleMod(final int modus) {

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
	private static boolean isNotStructuralChange(final int modus) {
		return (modus != SIMPLE_MODUS) && (modus != PSI_ONLY_MODUS) && (modus != PACKET_MODUS);
	}

	private void rebuildTree(){
		if(ts!=null){
			model = new DefaultTreeModel(ts.getJTreeNode(this.mod));
			tree.setModel(model);
		} else {
			tree.setModel(null);
		}
	}
	
	
	
	/**
	 * 
	 * Retrieves the expansion state as a String, defined by a comma delimited list
	 * of each row node that is expanded.
	 * 
	 * Based on {@link http://www.algosome.com/articles/save-jtree-expand-state.html}
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
	 * 
	 * Based on {@link http://www.algosome.com/articles/save-jtree-expand-state.html}
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
	public void valueChanged(final TreeSelectionEvent e) {
		// node1 is either a DefaultMutableTreeNode (most normal items) or a MutableTreeNode (for LazyList)
		final MutableTreeNode node1 = (MutableTreeNode)tree.getLastSelectedPathComponent();
		imagePanel.setImage(null);
		editorPane.setText(null);
		xmlPane.setText(null);
		
		CardLayout cardLayout = (CardLayout)(detailPanel.getLayout());

		if (node1 == null){
			cardLayout.show(detailPanel, EMPTY_PANEL);
			return;
		}
		// not always a DefaultMutableTreeNode
		if(node1 instanceof DefaultMutableTreeNode node){

			if (node.getUserObject() instanceof final KVP kvp) {
				if(kvp.getImageSource()!=null){
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					BufferedImage img;
					try {
						img = kvp.getImageSource().getImage();
					} catch (RuntimeException e1) {
						logger.log(Level.WARNING, "could not create image from getImageSource():", e1);
						img = GuiUtils.getErrorImage("Ooops.\n\n" + "Something went wrong generating this image.\n\n"  + GuiUtils.getImproveMsg());
					}
					setCursor(Cursor.getDefaultCursor());
					if(img != null){
						imagePanel.setImage(img);
						cardLayout.show(detailPanel, IMAGE_PANEL);
						return;
					}
				} else if(kvp.getHTMLSource()!=null){
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					editorPane.setText("<html>" + kvp.getHTMLSource().getHTML() + "</html>");
					editorPane.setCaretPosition(0);
					setCursor(Cursor.getDefaultCursor());
					cardLayout.show(detailPanel, HTML_PANEL);
					return;
				} else if(kvp.getXmlSource()!=null){
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					// hack to reset
					xmlPane.setDocument(xmlPane.getEditorKit().createDefaultDocument());
					xmlPane.setText(kvp.getXmlSource().getXML());
					xmlPane.setCaretPosition(0);
					setCursor(Cursor.getDefaultCursor());
					cardLayout.show(detailPanel, XML_PANEL);
					return;
				} else {
					TableSource tableSource = kvp.getTableSource();
					if(tableSource!=null) {
						try {
							TableModel tableModel = tableSource.getTableModel();
							if(tableModel.getColumnCount()>0 && tableModel.getRowCount()>0) {
								tablePanel.setModel(tableModel);
								cardLayout.show(detailPanel, TABLE_PANEL);
							}else{
								cardLayout.show(detailPanel, EMPTY_PANEL);
							}
						}catch (RuntimeException e2) {
							cardLayout.show(detailPanel, EMPTY_PANEL);
							logger.log(Level.WARNING, "could not create table:", e2);
						}
						return;
					}
				}

			}
		}
		cardLayout.show(detailPanel, EMPTY_PANEL);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent ae) {

		final TreePath path = tree.getSelectionPath();
		if(path!=null){

			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
			final KVP kvp = (KVP)dmtn.getUserObject();
			if (ae.getActionCommand().equals(EXPAND)) {
				expandItem();
			}
			if (ae.getActionCommand().equals(EXPAND_ALL)) {
				expandAllItems(dmtn);
			}
			if (ae.getActionCommand().equals(COPY)) {
				copyItemToClipboard(kvp);
			}
			if (ae.getActionCommand().equals(COPY_TREE)){
				copyEntireSubTreeToClipboard(dmtn);
			}
			if (ae.getActionCommand().equals(VIEW)){
				copyVisibleSubTreeToClipboard(dmtn, path, kvp);
			}
			if (ae.getActionCommand().equals(PARSE)){
				parsePid(dmtn, kvp);
			}
			if (ae.getActionCommand().equals(SAVE)){
				saveDsmccFile(kvp);
			}
			if (ae.getActionCommand().equals(EXPORT)){
				saveDsmccTree(kvp);
			}
			if (ae.getActionCommand().equals(PLAY)){
				playAudio138183(kvp);
			}
			if (ae.getActionCommand().equals(STOP)){
				stopAudio138183(kvp);
			}
			if (ae.getActionCommand().equals(T2MI)){
				saveT2miTs(kvp);
			}
			if (ae.getActionCommand().equals(T42)){
				saveT42File(kvp);
			}
			if (ae.getActionCommand().equals(SAVE_BYTES)){
				saveBytes(kvp);
			}
		}
	}
	/**
	 * @param dmtn start node from where to expand
	 */
	private void expandAllItems(DefaultMutableTreeNode dmtn) {
		expandItem();
		long end = System.currentTimeMillis() + MAX_EXPAND_ALL_TIME_MILLISECS;
		expandAllItemsRecursive(dmtn,end);
	}
	
	/**
	 * @param dmtn start node from where to expand
	 * @param end time (System.currentTimeMillis()) when this should stop expanding, to prevent long responses in the AWT Event thread
	 */
	private void expandAllItemsRecursive(final DefaultMutableTreeNode dmtn, long end) {
		if (System.currentTimeMillis() > end) {
			return;
		}

		tree.expandPath(new TreePath(dmtn.getPath()));
		final Enumeration<?> children = dmtn.children();

		while (children.hasMoreElements()) {
			if (children.nextElement() instanceof DefaultMutableTreeNode child && !child.isLeaf()) {
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
	private static void stopAudio138183(final KVP kvp) {
		final Audio138183Handler audioHandler = (Audio138183Handler) kvp.getOwner();
		audioHandler.stop();
	}

	/**
	 * @param kvp
	 */
	private static void playAudio138183(final KVP kvp) {
		final Audio138183Handler audioHandler = (Audio138183Handler) kvp.getOwner();
		audioHandler.play();
	}

	/**
	 * @param kvp
	 */
	private void saveDsmccTree(final KVP kvp) {
		final DSMFile dsmFile = (DSMFile) kvp.getOwner();

		final JFileChooser chooser = createFileChooserDefaultSaveDir();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		final int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = chooser.getSelectedFile();
			PreferencesManager.setSaveDir(file.getAbsolutePath());

			logger.info("Preparing to save as: " + file.getName() + ", path:" + file.getAbsolutePath());
			// start at selected directory, first create new subFolder with name "label"
			final File newDir = new File(file, dsmFile.getLabel());
			confirmOverwriteIfExisting(newDir, dsmFile);
		} else {
			logger.info("Open command cancelled by user.");
		}
	}

	private static JFileChooser createFileChooserDefaultSaveDir() {
		final String defaultDir = PreferencesManager.getSaveDir();
		final JFileChooser chooser = new JFileChooser();
		if(defaultDir!=null){
			final File defDir = new File(defaultDir);
			chooser.setCurrentDirectory(defDir);
		}
		return chooser;
	}

	/**
	 * @param kvp
	 */
	private void saveDsmccFile(final KVP kvp) {
		final DSMFile dsmFile = (DSMFile) kvp.getOwner();
		String fileName = dsmFile.getLabel();

		selectFileAndSave(fileName, dsmFile);
	}

	
	private void saveT42File(final KVP kvp) {
		Object owner = kvp.getOwner();
		if (owner instanceof SaveAble savable) {
			if (savable instanceof SubPage subPage) {
				String fileName = "Page" + subPage.getMagazineNo() + toHexStringUnformatted(subPage.getPageNo(), 2) + "-"
						+ toHexStringUnformatted(subPage.getSubPageNo(), 4) + ".t42";
				selectFileAndSave(fileName, savable);

			} else if (savable instanceof EBUTeletextHandler txtHandler) {
				String fileName = "Txt Service.t42";
				selectFileAndSave(fileName, savable);

			}
		}
	}

	/**
	 * @param dmtn
	 * @param kvp
	 */
	private void parsePid(DefaultMutableTreeNode dmtn, final KVP kvp) {
		final PID p = (PID) kvp.getOwner();
		final int pid = p.getPid();
		GeneralPidHandler pesH = p.getPidHandler();
		if(!pesH.isInitialized()){ // prevent double click
			Map<Integer, GeneralPidHandler> handlerMap = new HashMap<>();
			handlerMap.put(pid, pesH);
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			try {
				ts.parsePidStreams(handlerMap);
			} catch (final IOException e) {
				logger.log(Level.WARNING,"could not read file "+ts.getFile().getName()+" while parsing PES",e);
				setCursor(Cursor.getDefaultCursor());
			}catch (RuntimeException e) {
				logger.log(Level.WARNING,"could not parse PID "+pid+" using handler "+pesH.getClass().getName(),e);
				if((pesH.getClass() != GeneralPesHandler.class) &&
					(pesH.getClass() != GeneralPsiTableHandler.class)){ // only if specialized subclass of GeneralPesHandler
					logger.log(Level.WARNING,"trying again with GeneralPesHandler");
					JOptionPane.showMessageDialog(this,
							"Error parsing PID PES Packets for "+p.getLabelMaker()+", falling back to general PES packets",
							"DVB Inspector",
							JOptionPane.WARNING_MESSAGE);
					p.setPidHandler(new GeneralPesHandler());
					pesH = p.getPidHandler();
					handlerMap = new HashMap<>();
					handlerMap.put(pid, pesH);
					try {
						ts.parsePidStreams(handlerMap);
					} catch (IOException e1) {
						logger.log(Level.WARNING,"could not read file "+ts.getFile().getName()+" while parsing PES again with general PESHandler",e1);
					}
					setCursor(Cursor.getDefaultCursor());
				}
			}
			final DefaultMutableTreeNode node = p.getPidHandler().getJTreeNode(mod);
			// https://www.java-tips.org/java-se-tips-100019/15-javax-swing/2393-have-a-popup-attached-to-a-jtree.html
			// thanks to Yong Zhang for the tip for refreshing the tree structure.
			dmtn.add(node);
			((DefaultTreeModel )tree.getModel()).nodeStructureChanged(dmtn);
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * @param dmtn
	 * @param path
	 * @param kvp
	 */
	private void copyVisibleSubTreeToClipboard(DefaultMutableTreeNode dmtn, final TreePath path, final KVP kvp) {

		String res = kvp.getPlainText() + System.lineSeparator() +
				getViewTree(dmtn, "", path);
		final StringSelection stringSelection = new StringSelection(res);
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}

	/**
	 * @param dmtn
	 */
	private void copyEntireSubTreeToClipboard(DefaultMutableTreeNode dmtn) {
		KVP kvp = (KVP)dmtn.getUserObject();
		String treeString = kvp.getPlainText() + System.lineSeparator() +
				getEntireTree(dmtn, "");

		final StringSelection stringSelection = new StringSelection( treeString );
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}

	/**
	 * @param kvp
	 */
	private void copyItemToClipboard(final KVP kvp) {
		final StringSelection stringSelection = new StringSelection( kvp.getPlainText() );
		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents( stringSelection, this );
	}

	/**
	 * @param dmtn
	 * @return
	 */

	public static StringBuilder getEntireTree(final DefaultMutableTreeNode dmtn,final String preFix) {
		final StringBuilder res = new StringBuilder();
		@SuppressWarnings("rawtypes")
		final Enumeration children = dmtn.children();
		while(children.hasMoreElements()){
			Object next = children.nextElement();
			if(next instanceof final DefaultMutableTreeNode child){
				Object userObject = child.getUserObject();
				if(userObject instanceof final KVP chKVP) {
					res.append(preFix).append("+-").append(chKVP.getPlainText()).append(System.lineSeparator());
					if(!child.isLeaf()){
						if (child == dmtn.getLastChild()) { // lastChild
							res.append(getEntireTree(child, preFix + "  ")); // last , so prefix with "  "
						} else {
							res.append(getEntireTree(child, preFix + "| ")); // more children follow, so start with "| "
						}
					}
				}else {
					logger.log(Level.SEVERE, "Not an KVP: {}",  userObject);
				}
			}
		}
		return res;
	}

	/**
	 * @param dmtn
	 * @return
	 */

	private StringBuilder getViewTree(final DefaultMutableTreeNode dmtn,final String preFix,final TreePath path) {
		final StringBuilder res = new StringBuilder();
		@SuppressWarnings("rawtypes")
		final Enumeration children = dmtn.children();
		while(children.hasMoreElements()){

			Object next = children.nextElement();
			if(next instanceof final DefaultMutableTreeNode child){
				final TreePath childPath = path.pathByAddingChild(child);
				if(tree.isVisible(childPath)){
					final KVP chKVP = (KVP)child.getUserObject();
					res.append(preFix).append("+-").append(chKVP.getPlainText()).append(System.lineSeparator());
					if(!child.isLeaf()){
						if (child == dmtn.getLastChild()) { // lastChild
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
	private void showContextMenu(final MouseEvent e) {
		final TreePath path = tree.getSelectionPath();
		
		
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
		DefaultMutableTreeNode dmtn;

		if(path!=null){
			MutableTreeNode node= (MutableTreeNode)path.getLastPathComponent();
			if((node instanceof DefaultMutableTreeNode)){
				dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
				final KVP kvp=(KVP)dmtn.getUserObject();
				JMenuItem subMenu = kvp.getSubMenu();
				if(subMenu!=null){
					final Object owner =  kvp.getOwner();
					if(owner instanceof final PID p){ // if PID has a owner, it is a PES that maybe has not been parsed yet.
						final GeneralPidHandler pesH = p.getPidHandler();
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
					if(kvp.getFieldType()== KVP.FIELD_TYPE_BYTES){
						final JMenuItem bytesMenu = new JMenuItem("Save bytes as...");
						
						bytesMenu.setActionCommand(SAVE_BYTES);
						bytesMenu.addActionListener(this);
						popup.add(bytesMenu);
					}
				}
			}else{ //not a defaultMutableTreeNode, so it is a mutabletree node. Only used for TS packets lazy tree, so disable menu's
				 	subs = popup.getSubElements();
				for (MenuElement sub : subs) {
					JMenuItem menuElement = (JMenuItem) sub;
					menuElement.setEnabled(false);

				}

			}
		}


		popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	@Override
	public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
		// empty block, we don't care

	}

	public boolean findAndShow(String s,DefaultMutableTreeNodePreorderEnumaration enumeration) {
		
        DefaultMutableTreeNode node = searchNode(s.toLowerCase(),enumeration);
        if (node != null) {
          showNode(node);
          return true;
        }
        return false;
	}

	private void showNode(MutableTreeNode node) {
		javax.swing.tree.TreeNode[] nodes = model.getPathToRoot(node);
		TreePath path = new TreePath(nodes);
		
		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}

	public DefaultMutableTreeNodePreorderEnumaration createNewDefaultMutableTreeNodePreorderEnumaration(){
		return new DefaultMutableTreeNodePreorderEnumaration((DefaultMutableTreeNode)model.getRoot());
	}

	private static DefaultMutableTreeNode searchNode(String targetString, DefaultMutableTreeNodePreorderEnumaration enumeration) {
		while (enumeration.hasMoreElements()) {
			DefaultMutableTreeNode node = enumeration.nextElement();
			if (node != null){
				String nodeString = node.getUserObject().toString();
				if( nodeString.toLowerCase().contains(targetString)) {
					return node;
				}
			}
		}
		return null;
	}


	private void saveBytes(KVP kvp) {

		if(kvp.getFieldType()!= KVP.FIELD_TYPE_BYTES) {
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
		final JFileChooser chooser = createFileChooserDefaultSaveDir();

		chooser.setSelectedFile(new File(fileName));

		final int returnVal = chooser.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			final File file = chooser.getSelectedFile();
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
	private void confirmOverwriteIfExisting(final File file, SaveAble saveAble) {
		boolean write=true;
		if(file.exists()){
			logger.log(Level.INFO, "file {} already exists.", file);
			final int n = JOptionPane.showConfirmDialog(
					this, "File "+file+" already exists, want to overwrite?",
					"File already exists",
					JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.NO_OPTION) {
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

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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.data.mpeg.dsmcc.ServiceDSMCC.DSMFile;
import nl.digitalekabeltelevisie.data.mpeg.pes.GeneralPesHandler;
import nl.digitalekabeltelevisie.main.DVBinspector;

/**
 * DVBTree is the container for the JTree (on the left side) and the image and text on the right side.
 * For now image and text are implemented as a label. Disadvantage is that both can not be copied by the user into the clipboard.
 * Also sets up pop-up menu items for the JTree, and handles events from it.
 *
 * @author Eric
 *
 */
public class DVBtree extends JPanel implements TransportStreamView , TreeSelectionListener, ActionListener, ClipboardOwner {
	/**
	 *
	 */
	private static final long serialVersionUID = 9200238343077897328L;
	private static final Logger logger = Logger.getLogger(DVBtree.class.getName());
	private static final String SAVE_DIR = "save_directory";

	private final JTree tree;
	private final JLabel label;
	private final JSplitPane splitPane;
	private final JPopupMenu popup;
	private final JMenuItem copyMenuItem;
	private final JMenuItem treeMenuItem;
	private final JMenuItem viewMenuItem;

	public static final int SIMPLE_MODUS=0x1;
	public static final int PSI_ONLY_MODUS=0x2;
	public static final int PACKET_MODUS=0x4;
	public static final int COUNT_LIST_ITEMS_MODUS=0x8;
	public static final int SHOW_PTS_MODUS=0x10;

	private int mod=0;
	private TransportStream ts;
	private DefaultTreeModel model;

	/**
	 *
	 * Creates a new DVBTree
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param modus determines options of JTree, (like simple view, number list items, etc.)
	 */
	public DVBtree(final TransportStream transportStream, final int modus) {
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
		copyMenuItem = new JMenuItem("Copy Item to clipboard");
		copyMenuItem.addActionListener(this);
		copyMenuItem.setActionCommand("copy");
		popup.add(copyMenuItem);

		treeMenuItem = new JMenuItem("Copy Entire Sub Tree to clipboard");
		treeMenuItem.addActionListener(this);
		treeMenuItem.setActionCommand("tree");
		popup.add(treeMenuItem);

		viewMenuItem = new JMenuItem("Copy Visible Sub Tree to clipboard");
		viewMenuItem.addActionListener(this);
		viewMenuItem.setActionCommand("view");
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


		//Create the image viewing pane.
		label = new JLabel();
		label.setHorizontalAlignment(SwingConstants.LEFT);
		label.setVerticalAlignment(SwingConstants.TOP);

		final JScrollPane imgView = new JScrollPane(label);

		//Add the scroll panes to a split pane.
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeView);
		splitPane.setRightComponent(imgView);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(12);

		final Dimension minimumSize = new Dimension(300, 670);
		treeView.setMinimumSize(minimumSize);
		treeView.setPreferredSize(new Dimension(980, 670));
		splitPane.setDividerLocation(500);

		add(splitPane);
	}

	/**
	 * Update existing DVBTree to display a new {@link TransportStream}
	 *
	 * @param transportStream stream to be displayed (can be <code>null</code>)
	 * @param viewContext ignored, required by {@link TransportStreamView}

	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	public void setTransportStream(final TransportStream transportStream, final ViewContext v){
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


	public int getMod() {
		return mod;
	}

	/**
	 * Toggle one or more bits in the current modus
	 * @param modus
	 */
	public void toggleMod(final int modus) {
		this.mod = this.mod ^ modus;

		final Preferences prefs = Preferences.userNodeForPackage(DVBinspector.class);
		prefs.putInt(DVBinspector.DEFAULT_VIEW_MODUS, mod);

		refreshView();
	}

	private void refreshView(){
		if(ts!=null){
			model = new DefaultTreeModel(ts.getJTreeNode(this.mod));
			tree.setModel(model);
		} else {
			tree.setModel(null);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(final TreeSelectionEvent e) {
		final DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();
		if (node == null){
			label.setText(null);
			label.setIcon(null);
			return;
		}

		final Object nodeInfo = node.getUserObject();

		if (nodeInfo instanceof KVP) {
			final KVP kvp = (KVP)nodeInfo;
			if(kvp.getImageSource()!=null){
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				final Image img = kvp.getImageSource().getImage();
				setCursor(Cursor.getDefaultCursor());
				if(img != null){
					label.setIcon(new ImageIcon(img));
					label.setText(null);
					return;
				}
			}else  if(kvp.getHTMLSource()!=null){
				final StringBuilder html = new StringBuilder("<html>").append(kvp.getHTMLSource().getHTML()).append("</html>");
				if(html != null){
					label.setIcon(null);
					label.setText(html.toString());
					return;
				}
			}

		}
		label.setIcon(null);
		label.setText(null);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent ae) {
		DefaultMutableTreeNode dmtn;

		final TreePath path = tree.getSelectionPath();
		if(path!=null){

			dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
			if (ae.getActionCommand().equals("copy")) {
				final KVP kvp = (KVP)dmtn.getUserObject();

				final StringSelection stringSelection = new StringSelection( kvp.getPlainText() );
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents( stringSelection, this );
			}
			if (ae.getActionCommand().equals("tree")){
				final KVP kvp = (KVP)dmtn.getUserObject();
				final String lineSep = System.getProperty("line.separator");
				final StringBuilder res = new StringBuilder(kvp.getPlainText());
				res.append(lineSep);

				res.append(getEntireTree(dmtn,""));
				final StringSelection stringSelection = new StringSelection( res.toString() );
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents( stringSelection, this );
			}
			if (ae.getActionCommand().equals("view")){
				final KVP kvp = (KVP)dmtn.getUserObject();
				final String lineSep = System.getProperty("line.separator");
				final StringBuilder res = new StringBuilder(kvp.getPlainText());
				res.append(lineSep);

				res.append(getViewTree(dmtn,"",path));
				final StringSelection stringSelection = new StringSelection( res.toString() );
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents( stringSelection, this );
			}
			if (ae.getActionCommand().equals("parse")){
				final KVP kvp = (KVP)dmtn.getUserObject();
				final PID p = (PID) kvp.getOwner();
				final int pid = p.getPid();
				final GeneralPesHandler pesH = p.getPesHandler();
				if(!pesH.isInitialized()){ // prevent double click
					final HashMap<Integer, GeneralPesHandler> h = new HashMap<Integer, GeneralPesHandler>();
					h.put(pid, pesH);
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					try {
						ts.parseStream(h);
					} catch (final IOException e) {
						logger.log(Level.WARNING,"could not read file"+ts.getFile().getName(),e);
						setCursor(Cursor.getDefaultCursor());
					}
					final DefaultMutableTreeNode node =((TreeNode)p.getPesHandler()).getJTreeNode(mod);
					// thanks to Yong Zhang for the tip for refreshing the tree structure.
					dmtn.add(node);
					((DefaultTreeModel )tree.getModel()).nodeStructureChanged(dmtn);
					setCursor(Cursor.getDefaultCursor());
				}
			}
			if (ae.getActionCommand().equals("save")){
				final KVP kvp = (KVP)dmtn.getUserObject();
				final nl.digitalekabeltelevisie.data.mpeg.dsmcc.ServiceDSMCC.DSMFile dsmFile = (DSMFile) kvp.getOwner();

				final Preferences prefs = Preferences.userNodeForPackage(DVBtree.class);

				final String defaultDir = prefs.get(SAVE_DIR, null);
				final JFileChooser chooser = new JFileChooser();
				if(defaultDir!=null){
					final File defDir = new File(defaultDir);
					chooser.setCurrentDirectory(defDir);
				}

				chooser.setSelectedFile(new File(dsmFile.getLabel()));

				final int returnVal = chooser.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					prefs.put(SAVE_DIR,file.getParent());

					logger.info("Preparing to save as: " + file.getName() + ", path:" + file.getAbsolutePath() );
					boolean write=true;
					if(file.exists()){
						logger.info("file "+file+" already exists.");
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
					if(write){
						dsmFile.saveFile(file,dsmFile.getBiopMessage());
					}

				} else {
					logger.info("Open command cancelled by user." );
				}

			}
			if (ae.getActionCommand().equals("export")){
				final KVP kvp = (KVP)dmtn.getUserObject();
				final DSMFile dsmFile = (DSMFile) kvp.getOwner();

				final Preferences prefs = Preferences.userNodeForPackage(DVBtree.class);

				final String defaultDir = prefs.get(SAVE_DIR, null);
				final JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(defaultDir!=null){
					final File defDir = new File(defaultDir);
					chooser.setCurrentDirectory(defDir);
				}

				final int returnVal = chooser.showSaveDialog(this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					prefs.put(SAVE_DIR,file.getAbsolutePath());

					logger.info("Preparing to save as: " + file.getName() + ", path:" + file.getAbsolutePath() );
					// start at selected directory, first create new subFolder with name "label"
					final File newDir = new File(file,dsmFile.getLabel());
					boolean write=true;
					if(newDir.exists()){
						logger.info("file "+newDir+" already exists.");
						final int n = JOptionPane.showConfirmDialog(
								this, "Directory "+newDir+" already exists, want to overwrite?",
								"Directory already exists",
								JOptionPane.YES_NO_OPTION);
						if (n == JOptionPane.NO_OPTION) {
							write=false;
							logger.info("User canceled overwrite");
						}else{
							logger.info("User confirmed overwrite");
						}
					}
					if(write){
						dsmFile.saveFile(newDir,dsmFile.getBiopMessage());
					}
				} else {
					logger.info("Open command cancelled by user." );
				}
			}

		}
	}

	/**
	 * @param dmtn
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private StringBuilder getEntireTree(final DefaultMutableTreeNode dmtn,final String preFix) {
		final String lineSep = System.getProperty("line.separator");
		final StringBuilder res = new StringBuilder();
		final Enumeration<DefaultMutableTreeNode> children = dmtn.children();
		while(children.hasMoreElements()){
			final DefaultMutableTreeNode child = children.nextElement();
			final KVP chKVP = (KVP)child.getUserObject();
			res.append(preFix).append("+-").append(chKVP.getPlainText()).append(lineSep);
			if(!child.isLeaf()){
				if(child!=dmtn.getLastChild()){
					res.append(getEntireTree(child,preFix+"| ")); // more children follow, so start with "| "
				}else{ // lastChild
					res.append(getEntireTree(child,preFix+"  ")); // last , so prefix with "  "
				}
			}
		}
		return res;
	}

	/**
	 * @param dmtn
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private StringBuilder getViewTree(final DefaultMutableTreeNode dmtn,final String preFix,final TreePath path) {
		final String lineSep = System.getProperty("line.separator");
		final StringBuilder res = new StringBuilder();
		final Enumeration<DefaultMutableTreeNode> children = dmtn.children();
		while(children.hasMoreElements()){
			final DefaultMutableTreeNode child = children.nextElement();
			final TreePath childPath = path.pathByAddingChild(child);
			if(tree.isVisible(childPath)){
				final KVP chKVP = (KVP)child.getUserObject();
				res.append(preFix).append("+-").append(chKVP.getPlainText()).append(lineSep);
				if(!child.isLeaf()){
					if(child!=dmtn.getLastChild()){
						res.append(getViewTree(child,preFix+"| ",childPath)); // more children follow, so start with "| "
					}else{ // lastChild
						res.append(getViewTree(child,preFix+"  ",childPath)); // last , so prefix with "  "
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
		treeMenuItem.setEnabled(path!=null);
		copyMenuItem.setEnabled(path!=null);
		viewMenuItem.setEnabled(path!=null);
		// remove option menu from previous invocation
		if(popup.getSubElements().length >= 4){
			popup.remove(3);
		}

		//		add optional menu
		DefaultMutableTreeNode dmtn;

		if(path!=null){
			dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
			final KVP kvp=(KVP)dmtn.getUserObject();
			JMenuItem subMenu = kvp.getSubMenu();
			if(subMenu!=null){
				final Object owner =  kvp.getOwner();
				if(owner instanceof PID){ // if PID has a owner, it is a PES that has not been parsed yet.
					final PID p = (PID) owner;
					final GeneralPesHandler pesH = p.getPesHandler();
					if(!pesH.isInitialized()){
						subMenu.addActionListener(this);
						popup.add(subMenu);
					}
				}else{
					subMenu.removeActionListener(this);
					subMenu.addActionListener(this);
					popup.add(subMenu);
				}
			}
		}


		popup.show( (JComponent)e.getSource(), e.getX(), e.getY() );
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
	 */
	public void lostOwnership(final Clipboard clipboard, final Transferable contents) {
		// empty block, we don't care

	}

}

package nl.digitalekabeltelevisie.test;

import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.Bouquet;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.DVBtree;

public class DVBBouqetInspector implements Runnable{

	private TransportStream transportStream;
	private DVBtree treeView;
	private Bouquet bouquet;


	public DVBBouqetInspector() {
		super();
	}

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final DVBBouqetInspector inspector = new DVBBouqetInspector();
		inspector.run();
	}

	public void run() {

		final File directory = new File("C:\\eric\\mpeg\\ts\\set20090319");

		bouquet = new Bouquet(directory);

		KVP.setNumberDisplay(KVP.NUMBER_DISPLAY_BOTH);
		//DefaultMutableTreeNode top = tStream.getJTreeNode(0);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI(bouquet.getJTreeNode(3));
			}
		});

	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from the
	 * event dispatch thread.
	 */
	private void createAndShowGUI(final DefaultMutableTreeNode treeNode) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			System.err.println("Couldn't use system look and feel.");
		}

		//Create and set up the window.
		final JFrame frame = new JFrame("DVB Bouquet Inspector");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//treeView = new DVBtree(treeNode);

		//Add content to the window.
		frame.add(treeView);

		JMenuBar menuBar;
		JMenu fileMenu;
		JMenu viewMenu;

		menuBar = new JMenuBar();
		fileMenu =new JMenu("File");
		menuBar.add(fileMenu);

		viewMenu =new JMenu("View");
		menuBar.add(viewMenu);
		final ButtonGroup hexDecGroup = new ButtonGroup();
		JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem("Hex [0xA0]");
		rbMenuItem.setSelected(true);
		hexDecGroup.add(rbMenuItem);
		viewMenu.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Dec [10]");
		hexDecGroup.add(rbMenuItem);
		viewMenu.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Hex/Dec [0xA0 (10)]");
		hexDecGroup.add(rbMenuItem);
		viewMenu.add(rbMenuItem);
		final File imageFile = new File("c:\\magnifying_glass.bmp");
		Image image;
		try {
			image = ImageIO.read(imageFile);
			frame.setIconImage(image);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public TransportStream getTransportStream() {
		return transportStream;
	}

	public void setTransportStream(final TransportStream transportStream) {
		this.transportStream = transportStream;
	}

	public DVBtree getTreeView() {
		return treeView;
	}

	public void setTreeView(final DVBtree treeView) {
		this.treeView = treeView;
	}


}

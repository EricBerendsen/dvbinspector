/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2013 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author  Eric
 */
public class ImagePanel extends JPanel implements FocusListener, MouseListener{

	private class ImageCanvas extends JPanel {


		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			Graphics2D gd = (Graphics2D) g;
			gd.drawImage(image, 0,0, (int) (scale*image.getWidth()), (int) (scale * image.getHeight()),null);
		}

		public Dimension getPreferredSize() {
			if(image!=null){
				return new Dimension((int) (scale*image.getWidth()), (int) (scale * image.getHeight()));
			}else{
				return new Dimension();
			}
		}

	}

	/**
	 * Put image on clipboard
	 *
	 * @author Eric
	 *
	 */
	public class CopyAction extends AbstractAction{

		/**
		 *
		 */
		public CopyAction(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			if(image!=null){
				ImageTransferable it = new ImageTransferable(image);
				final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents( it, it );
			}
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/**
	 * Start dialog to save image as PNG or JPG.
	 *
	 * @author Eric
	 *
	 */
	public class SaveAction extends AbstractAction{

		/**
		 *
		 */
		public SaveAction(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			File saveFile = new File("dvb_inspector_image_"+df.format(new Date()));
			JFileChooser chooser = new JFileChooser();
			FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPG", "jpg", "jpeg");
			chooser.addChoosableFileFilter(jpgFilter);
			FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG", "png");
			chooser.addChoosableFileFilter(pngFilter);
			chooser.setAcceptAllFileFilterUsed(false);
			final Preferences prefs = Preferences.userNodeForPackage(DVBtree.class);
			final String defaultDir = prefs.get(DVBtree.SAVE_DIR, null);
			if (defaultDir != null) {
				final File defDir = new File(defaultDir);
				chooser.setCurrentDirectory(defDir);
			}

			chooser.setSelectedFile(saveFile);
			int rval = chooser.showSaveDialog(imageCanvas);
			if (rval == JFileChooser.APPROVE_OPTION) {
				saveFile = chooser.getSelectedFile();
				prefs.put(DVBtree.SAVE_DIR, saveFile.getParent());

				FileNameExtensionFilter filter = (FileNameExtensionFilter) chooser.getFileFilter();
				String extension = filter.getExtensions()[0];
				if (!saveFile.getName().endsWith('.' + extension)) {
					saveFile = new File(saveFile.getPath() + "." + extension);
				}
				boolean write = true;
				if (saveFile.exists()) {
					final int n = JOptionPane.showConfirmDialog(imageCanvas,
							"File " + saveFile + " already exists, want to overwrite?",
							"File already exists",
							JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.NO_OPTION) {
						write = false;
					}
				}
				if (write) {

					try {
						ImageIO.write(image, extension, saveFile);
					} catch (IOException ex) {
					}
				}
			}
		}
	}


	/**
	 *
	 */
	private JToolBar buttonToolbar;
	/**
	 * Something that draws the actual image, this already exists.
	 */
	private ImageCanvas imageCanvas;


	private BufferedImage image;
	private double scale = 1;


	/**
	 *
	 */
	public ImagePanel() {
		super(new BorderLayout());

		setFocusable(true);
	    addMouseListener(this);
	    addFocusListener(this);

		imageCanvas = new ImageCanvas();

		buttonToolbar = new JToolBar();
		buttonToolbar.setFloatable(false);
		addZoomRadioButtons();

		buttonToolbar.add(Box.createHorizontalStrut(10)); // spacer
		CopyAction copyAction = new CopyAction("Copy");
		JButton copyButton = new JButton(copyAction);

		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,Event.CTRL_MASK);
		getInputMap().put(copyKey, "copy");
		getActionMap().put("copy", copyAction);

		buttonToolbar.add(copyButton);

		SaveAction saveAction = new SaveAction("Save As...");
		JButton saveButton = new JButton(saveAction);

		KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK);
		getInputMap().put(saveKey, "save");
		getActionMap().put("save", saveAction);

		buttonToolbar.add(saveButton);
		add(buttonToolbar,BorderLayout.PAGE_START);

		add(imageCanvas,BorderLayout.CENTER);
	}

	/**
	 * @return the buttonToolbar
	 */
	public JToolBar getButtonToolbar() {
		return buttonToolbar;
	}

	/**
	 * @param buttonToolbar the buttonToolbar to set
	 */
	public void setButtonToolbar(JToolBar buttonToolbar) {
		this.buttonToolbar = buttonToolbar;
	}

//	/**
//	 * @return the imageCanvas
//	 */
//	public JLabel getLabel() {
//		return imageCanvas;
//	}

//	/**
//	 * @param imageCanvas the imageCanvas to set
//	 */
//	public void setLabel(JLabel imageCanvas) {
//		this.label = imageCanvas;
//	}



	private void addZoomRadioButtons() {
		JLabel typeLabel = new JLabel("Zoom:");
		buttonToolbar.add(typeLabel);
		JRadioButton zoom1Button = new JRadioButton("½");
		zoom1Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scale = 0.5;
				showImage();

			}
		});
		JRadioButton zoom2Button = new JRadioButton("1");
		zoom2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scale=1;
				showImage();
			}
		});
		JRadioButton zoom3Button = new JRadioButton("2x");
		zoom3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scale=2;
				showImage();
			}
		});
		zoom2Button.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(zoom1Button);
		group.add(zoom2Button);
		group.add(zoom3Button);

		buttonToolbar.add(zoom1Button);
		buttonToolbar.add(zoom2Button);
		buttonToolbar.add(zoom3Button);
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {

		this.image = image;
		showImage();
	}

	/**
	 * Call when new image is set, or scale changed.
	 * @param image
	 */
	private void showImage()  {
		revalidate();
		repaint();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		 //Since the user clicked on us, let us get focus!
	    requestFocusInWindow();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// ignore
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// ignore
	}

}
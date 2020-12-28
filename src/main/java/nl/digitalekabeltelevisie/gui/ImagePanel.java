/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * @author  Eric
 */
public class ImagePanel extends JPanel implements ImageSource{

	private class ImageCanvas extends JPanel implements  MouseListener, FocusListener {
		
		public ImageCanvas() {
			setFocusable(true);
		    addMouseListener(this);
		}


		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		@Override
		public void paint(Graphics g) {
			Graphics2D gd2 = (Graphics2D) g;
			final AffineTransform originalTransform = gd2.getTransform();
			
			//create a new transform that paints pixel for pixel, not scaling for HiRes displays
			AffineTransform idTransForm = new AffineTransform(scale,0,0,scale,originalTransform.getTranslateX(),originalTransform.getTranslateY());
			gd2.setTransform(idTransForm);

			gd2.drawImage(image, 0,0, null);
		}

		@Override
		public Dimension getPreferredSize() {
			if(image!=null){
				final AffineTransform t = ((Graphics2D) this.getGraphics()).getTransform();
				return new Dimension((int) (scale*image.getWidth()/t.getScaleX()+1), (int) (scale * image.getHeight()/t.getScaleY()+1));
			}
			return new Dimension();
		}
		
		/* (non-Javadoc)
		 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			 //Since the user clicked on us, let us get focus!
		    requestFocusInWindow();
			imageCanvas.requestFocusInWindow();
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

		@Override
		public void focusGained(FocusEvent e) {
			// ignore
		}

		@Override
		public void focusLost(FocusEvent e) {
			// ignore
		}

	}

	/**
	 *
	 */
	private final JToolBar buttonToolbar;
	/**
	 * Something that draws the actual image, this already exists.
	 */
	private final ImageCanvas imageCanvas;


	private BufferedImage image;
	private double scale = 1;


	/**
	 *
	 */
	public ImagePanel() {
		super(new BorderLayout());

		setFocusable(true);

		imageCanvas = new ImageCanvas();
		JScrollPane imageScrollPane = new JScrollPane(imageCanvas);

		buttonToolbar = new JToolBar();
		buttonToolbar.setFloatable(false);
		addZoomRadioButtons();

		buttonToolbar.add(Box.createHorizontalStrut(10)); // spacer
		ImageCopyAction copyAction = new ImageCopyAction(this, "Copy", this);
		JButton copyButton = new JButton(copyAction);

		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK);
		imageCanvas.getInputMap().put(copyKey, "copy");
		imageCanvas.getActionMap().put("copy", copyAction);

		buttonToolbar.add(copyButton);

		ImageSaveAction saveAction = new ImageSaveAction(this, "Save As...",this);
		JButton saveButton = new JButton(saveAction);

		KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK);
		getInputMap().put(saveKey, "save");
		getActionMap().put("save", saveAction);

		buttonToolbar.add(saveButton);
		add(buttonToolbar,BorderLayout.PAGE_START);
		add(imageScrollPane,BorderLayout.CENTER);
	}


	private void addZoomRadioButtons() {
		JLabel typeLabel = new JLabel("Zoom:");
		buttonToolbar.add(typeLabel);
		JRadioButton zoom1Button = new JRadioButton("½");
		zoom1Button.addActionListener(e -> rescale(0.5));
		JRadioButton zoom2Button = new JRadioButton("1");
		zoom2Button.addActionListener(e -> rescale(1));
		JRadioButton zoom3Button = new JRadioButton("2x");
		zoom3Button.addActionListener(e -> rescale(2));
		zoom2Button.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(zoom1Button);
		group.add(zoom2Button);
		group.add(zoom3Button);

		buttonToolbar.add(zoom1Button);
		buttonToolbar.add(zoom2Button);
		buttonToolbar.add(zoom3Button);
	}

	private void rescale(double newScale) {
		scale = newScale;
		showImage();
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
	 */
	private void showImage()  {
		imageCanvas.revalidate();
		imageCanvas.scrollRectToVisible(new Rectangle(0,0,1,1));
		repaint();
	}


}
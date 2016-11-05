/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2016 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.*;

/**
 * @author  Eric
 */
public class ImagePanel extends JPanel implements FocusListener, MouseListener, ImageSource{

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
		ImageCopyAction copyAction = new ImageCopyAction(this, "Copy", this);
		JButton copyButton = new JButton(copyAction);

		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,Event.CTRL_MASK);
		getInputMap().put(copyKey, "copy");
		getActionMap().put("copy", copyAction);

		buttonToolbar.add(copyButton);

		ImageSaveAction saveAction = new ImageSaveAction(this, "Save As...",this);
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
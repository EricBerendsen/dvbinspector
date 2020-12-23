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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

/**
 * @author Eric
 */
public class TableExtPanel extends JPanel implements FocusListener, MouseListener, TableSource {
	/**
	 *
	 */
	private JToolBar buttonToolbar;
	private final TablePanel tablePanel = new TablePanel(new JTable());

	/**
	 *
	 */
	public TableExtPanel() {
		super(new BorderLayout());

		setFocusable(true);
		addMouseListener(this);
		addFocusListener(this);

		buttonToolbar = new JToolBar();
		buttonToolbar.setFloatable(false);
		buttonToolbar.setBorderPainted(true);

		buttonToolbar.add(Box.createHorizontalStrut(20)); // spacer
		TableCopyAction copyAction = new TableCopyAction(this, "Copy", this);
		JButton copyButton = new JButton(copyAction);

		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
		getInputMap().put(copyKey, "copy");
		getActionMap().put("copy", copyAction);

		buttonToolbar.add(copyButton);

		TableSaveAction saveAction = new TableSaveAction(this, "Save As...", this);
		JButton saveButton = new JButton(saveAction);

		KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
		getInputMap().put(saveKey, "save");
		getActionMap().put("save", saveAction);

		buttonToolbar.add(Box.createHorizontalStrut(50)); // spacer
		buttonToolbar.add(saveButton);

		add(buttonToolbar, BorderLayout.PAGE_START);
		add(tablePanel, BorderLayout.CENTER);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// Since the user clicked on us, let us get focus!
		requestFocusInWindow();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// ignore
	}

	public void setModel(TableModel tableModel) {
		tablePanel.setModel(tableModel);
	}

	@Override
	public TableModel getTableModel() {
		return tablePanel.getModel();
	}

}
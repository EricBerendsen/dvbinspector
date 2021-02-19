/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2019 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.event.*;

import javax.swing.*;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.utils.WrapLayout;


/**
 *
 */
public class EITView extends JPanel implements TransportStreamView{

	final EITableImage eitPanel;
	private final JPanel toolbar = new JPanel(new WrapLayout(FlowLayout.LEFT));

	/**
	 * @param transportStream
	 * @param viewContext
	 */
	public EITView(final TransportStream transportStream, final ViewContext viewContext) {
		super(new BorderLayout());
		eitPanel = new EITableImage(transportStream,viewContext);
		/**
		 *
		 */
		JScrollPane scrollGrid = new JScrollPane(eitPanel);
		scrollGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		createCopySaveButtonBar();
		
		toolbar.add(Box.createHorizontalStrut(50));
		addPfScheduleRadioButtons();
		toolbar.add(Box.createHorizontalStrut(50)); // spacer
		addZoomRadioButtons();
		
		add(toolbar,BorderLayout.PAGE_START);
		add(scrollGrid,BorderLayout.CENTER);
	}

	private void createCopySaveButtonBar() {

		ImageCopyAction copyAction = new ImageCopyAction(this, "Copy", eitPanel);
		JButton copyButton = new JButton(copyAction);
		KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C,Event.CTRL_MASK);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(copyKey, "copy");
		getActionMap().put("copy", copyAction);
		toolbar.add(copyButton);

		ImageSaveAction saveAction = new ImageSaveAction(this, "Save As...",eitPanel);
		JButton saveButton = new JButton(saveAction);
		KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK);
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(saveKey, "save");
		getActionMap().put("save", saveAction);
		toolbar.add(saveButton);
	}

	/**
	 *
	 */
	private void addPfScheduleRadioButtons() {
		JLabel typeLabel = new JLabel("Table:");
		toolbar.add(typeLabel);

		JRadioButton pfButton = new JRadioButton("Present/Following");
		pfButton.addActionListener(e -> eitPanel.selectPresentFollowing());
		JRadioButton scheduleButton = new JRadioButton("Schedule");
		scheduleButton.addActionListener(e -> eitPanel.selectSchedule());
		scheduleButton.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(pfButton);
		group.add(scheduleButton);

		toolbar.add(pfButton);
		toolbar.add(scheduleButton);
	}

	private void addZoomRadioButtons() {
		JLabel typeLabel = new JLabel("Zoom:");
		toolbar.add(typeLabel);
		JRadioButton zoom1Button = new JRadioButton("1");
		zoom1Button.addActionListener(e -> eitPanel.setZoom(30*1000L));
		JRadioButton zoom2Button = new JRadioButton("2");
		zoom2Button.addActionListener(e -> eitPanel.setZoom(15*1000L));
		JRadioButton zoom3Button = new JRadioButton("3");
		zoom3Button.addActionListener(e -> eitPanel.setZoom(7500L));
		zoom2Button.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(zoom1Button);
		group.add(zoom2Button);
		group.add(zoom3Button);

		toolbar.add(zoom1Button);
		toolbar.add(zoom2Button);
		toolbar.add(zoom3Button);
	}
	
	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	public void setTransportStream(final TransportStream transportStream, final ViewContext viewContext) {

		eitPanel.setTransportStream(transportStream,viewContext);
		validate();
		repaint();

	}




}

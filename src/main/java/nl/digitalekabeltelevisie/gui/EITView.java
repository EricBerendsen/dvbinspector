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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;


/**
 *
 */
public class EITView extends JPanel implements TransportStreamView{

	/**
	 *
	 */
	private JScrollPane scrollGrid;
	private EITPanel eitPanel;
	private JPanel buttonPanel;

	/**
	 * @param transportStream
	 * @param viewContext
	 */
	public EITView(final TransportStream transportStream, final ViewContext viewContext) {
		super(new BorderLayout());

		buttonPanel = new JPanel();
		addPfScheduleRadioButtons();
		addZoomRadioButtons();
		add(buttonPanel,BorderLayout.PAGE_START);

		eitPanel = new EITPanel(transportStream,viewContext);
		scrollGrid = new JScrollPane(eitPanel);
		scrollGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		add(scrollGrid,BorderLayout.CENTER);
	}

	/**
	 *
	 */
	private void addPfScheduleRadioButtons() {
		JLabel typeLabel = new JLabel("Table:");
		buttonPanel.add(typeLabel);
		JRadioButton pfButton = new JRadioButton("Present/Following");
		pfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eitPanel.selectPresentFollowing();
			}
		});
		JRadioButton scheduleButton = new JRadioButton("Schedule");
		scheduleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eitPanel.selectSchedule();
			}
		});
		scheduleButton.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(pfButton);
		group.add(scheduleButton);

		buttonPanel.add(pfButton);
		buttonPanel.add(scheduleButton);
	}

	private void addZoomRadioButtons() {
		JLabel typeLabel = new JLabel("Zoom:");
		buttonPanel.add(typeLabel);
		JRadioButton zoom1Button = new JRadioButton("1");
		zoom1Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eitPanel.setZoom(30*1000L);
			}
		});
		JRadioButton zoom2Button = new JRadioButton("2");
		zoom2Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eitPanel.setZoom(15*1000L);
			}
		});
		JRadioButton zoom3Button = new JRadioButton("3");
		zoom3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eitPanel.setZoom(7500L);
			}
		});
		zoom2Button.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		group.add(zoom1Button);
		group.add(zoom2Button);
		group.add(zoom3Button);

		buttonPanel.add(zoom1Button);
		buttonPanel.add(zoom2Button);
		buttonPanel.add(zoom3Button);
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

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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ItemEvent;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.data.mpeg.TransportStream;
import nl.digitalekabeltelevisie.gui.utils.WrapLayout;
import nl.digitalekabeltelevisie.util.Utils;


/**
 *
 */
public class GridView extends JPanel implements TransportStreamView{

	/**
	 *
	 */
	private final Grid grid;
	private final JPanel buttonPanel;

	/**
	 * @param transportStream
	 * @param viewContext
	 */
	public GridView(final TransportStream transportStream, final ViewContext viewContext) {
		//super(new GridLayout(1,0));
		super(new BorderLayout());
		buttonPanel = new JPanel(new WrapLayout());

		addPacketCheckBoxes();

		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer

		addZoomRadioButtons();

		buttonPanel.add(Box.createHorizontalStrut(20)); // spacer

		addGridLinesRadioButtons();

		add(buttonPanel,BorderLayout.PAGE_START);

		grid = new Grid(transportStream,viewContext);
		JScrollPane scrollGrid = new JScrollPane(grid);
		scrollGrid.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollGrid.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		add(scrollGrid,BorderLayout.CENTER);
	}

	/**
	 *
	 */
	private void addPacketCheckBoxes() {
		Image adImg = Utils.readIconImage("adaptation.bmp");
		Image payloadImg = Utils.readIconImage("payloadstart.bmp");

		Image errorImg = Utils.readIconImage("errorindicator.bmp");
		final ImageIcon adaptationIcon = new ImageIcon(adImg);
		final ImageIcon payloadIcon = new ImageIcon(payloadImg);
		final ImageIcon errorIcon = new ImageIcon(errorImg);

		JLabel adaptationLabel = new JLabel(adaptationIcon);
		buttonPanel.add(adaptationLabel);
		JCheckBox adaptationFieldButton = new JCheckBox("Show Adaptation Field");
		adaptationFieldButton.addItemListener(e -> grid.setShowAdaptationField(e.getStateChange()==ItemEvent.SELECTED));
		buttonPanel.add(adaptationFieldButton);

		buttonPanel.add(Box.createHorizontalStrut(10)); // spacer

		JLabel payLoadStartLabel = new JLabel(payloadIcon);
		buttonPanel.add(payLoadStartLabel);

		JCheckBox payLoadStartButton = new JCheckBox("Show Payload Start");
		payLoadStartButton.addItemListener(e -> grid.setShowPayloadStart(e.getStateChange()==ItemEvent.SELECTED));

		buttonPanel.add(payLoadStartButton);

		JLabel errorLabel = new JLabel(errorIcon);
		buttonPanel.add(errorLabel);

		JCheckBox errorButton = new JCheckBox("Show Error Indicator");
		errorButton.addItemListener(e -> grid.setShowErrorIndicator(e.getStateChange()==ItemEvent.SELECTED));

		buttonPanel.add(errorButton);
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.gui.TransportStreamView#setTransportStream(nl.digitalekabeltelevisie.data.mpeg.TransportStream, nl.digitalekabeltelevisie.controller.ViewContext)
	 */
	public void setTransportStream(final TransportStream transportStream, final ViewContext viewContext) {

		grid.setTransportStream(transportStream,viewContext);
		validate();
		repaint();
	}

	private void addZoomRadioButtons() {
		JLabel typeLabel = new JLabel("Zoom:");
		buttonPanel.add(typeLabel);
		int zoomLevels = 7;
		JRadioButton[] zoomButtons = new JRadioButton[zoomLevels];
		int size=1;
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < zoomButtons.length; i++) {
			final int s = size;
			zoomButtons[i] = new JRadioButton(""+(i+1));
			zoomButtons[i].addActionListener(e -> grid.setBlockSize(s));
			size *=2;
			group.add(zoomButtons[i]);
			buttonPanel.add(zoomButtons[i]);
		}

		zoomButtons[Math.min(zoomLevels-1, 4)].setSelected(true);
	}

	private void addGridLinesRadioButtons(){
		JLabel typeLabel = new JLabel("Grid Lines:");
		buttonPanel.add(typeLabel);
		ButtonGroup group = new ButtonGroup();

		addGridRadioButton(group,"Off",0,true);
		addGridRadioButton(group,"1",1,false);
		addGridRadioButton(group,"5",5,false);
		addGridRadioButton(group,"10",10,false);
		addGridRadioButton(group,"20",20,false);
	}

	/**
	 * @param group
	 */
	private void addGridRadioButton(ButtonGroup group, String label, final int value, boolean selected) {
		JRadioButton button = new JRadioButton(label);
		button.addActionListener(e -> grid.setGridLines(value));
		group.add(button);
		buttonPanel.add(button);
		button.setSelected(selected);
	}

}

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
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import nl.digitalekabeltelevisie.data.mpeg.TransportStream;


/**
 * @author Eric Berendsen
 *
 */
public class PacketSelectionPanel extends JPanel implements
ChangeListener,
PropertyChangeListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4816053364455328183L;
	private JFormattedTextField textField;
	private JSlider slider;
	private BoundedRangeModel boundedRangeModel;

	private final JLabel timeLabel = new JLabel();
	private NumberFormat numberFormat;
	private TransportStream transportStream;


	PacketSelectionPanel( final String myTitle, final int low, final int upper, final int value, final TransportStream tStream) {
		super();
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(myTitle),
				BorderFactory.createEmptyBorder(5,5,5,5)));

		transportStream = tStream;


		//Create the text field format, and then the text field.
		numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(0);
		final NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);//seems to be a no-op --
		//aha -- it changes the value property but doesn't cause the result to
		//be parsed (that happens on focus loss/return, I think).
		//
		textField = new JFormattedTextField(formatter);
		textField.setColumns(10);
		textField.setValue(value);
		textField.addPropertyChangeListener(this);
		boundedRangeModel = new DefaultBoundedRangeModel(value,0,low,upper);


		//Add the slider.
		slider = new JSlider(boundedRangeModel);
		boundedRangeModel.addChangeListener(this);

		//Make the text field/slider group a fixed size
		//to make stacked ConversionPanels nicely aligned.
		final JPanel unitGroup = new JPanel() {
			/**
			 *
			 */
			private static final long serialVersionUID = 5163968821838752192L;
			@Override
			public Dimension getMinimumSize() {
				return getPreferredSize();
			}

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}
		};
		unitGroup.setLayout(new BoxLayout(unitGroup,
				BoxLayout.PAGE_AXIS));
		unitGroup.setBorder(BorderFactory.createEmptyBorder(
				0,0,0,5));
		unitGroup.add(textField);
		unitGroup.add(slider);

		//Create a subpanel so the combo box isn't too tall
		//and is sufficiently wide.
		final JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel,
				BoxLayout.PAGE_AXIS));
		timePanel.add(timeLabel);

		//Put everything together.
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setLayout(new BorderLayout());
		add(unitGroup,BorderLayout.CENTER);
		add(timePanel,BorderLayout.EAST);
	}

	//Don't allow this panel to get taller than its preferred size.
	//BoxLayout pays attention to maximum size, though most layout
	//managers don't.
	@Override
	public Dimension getMaximumSize() {
		return new Dimension(Integer.MAX_VALUE,
				getPreferredSize().height);
	}


	/** Updates the text field when the main data model is updated. */
	public void stateChanged(final ChangeEvent e) {
		final NumberFormatter formatter = (NumberFormatter)textField.getFormatter();

		final int min = boundedRangeModel.getMinimum();
		final int max = boundedRangeModel.getMaximum();
		final int val = boundedRangeModel.getValue();

		formatter.setMinimum(min);
		formatter.setMaximum(max);
		textField.setValue(val);
		if(transportStream!=null){
			timeLabel.setText(transportStream.getShortPacketTime(val));
		}
	}


	/**
	 * Detects when the value of the text field (not necessarily the same
	 * number as you'd get from getText) changes.
	 */
	public void propertyChange(final PropertyChangeEvent e) {
		if ("value".equals(e.getPropertyName())) {
			final Number value = (Number)e.getNewValue();
			boundedRangeModel.setValue(value.intValue());
		}
	}

	public void setRangeValue(final int min, final int max, final int val,final TransportStream ts){
		slider.setMinimum(min);
		slider.setMaximum(max);
		slider.setValue(val);
		textField.setValue(val);
		transportStream = ts;
		if(transportStream!=null){
			timeLabel.setText(transportStream.getShortPacketTime(val));
		}
	}

	public int getValue(){
		return boundedRangeModel.getValue();
	}

}

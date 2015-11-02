/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2015 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import nl.digitalekabeltelevisie.main.DVBinspector;
import nl.digitalekabeltelevisie.util.DefaultMutableTreeNodePreorderEnumaration;

class FindDialog extends JDialog
                   implements ActionListener,
                              PropertyChangeListener {
    private String searchText = null;
    private JTextField textField;
    private JOptionPane optionPane;
    DVBinspector controller;

    private String buttonString1 = "Search";
    private String buttonString2 = "Cancel";
	private DefaultMutableTreeNodePreorderEnumaration enummeration;



    public FindDialog(DVBinspector controller) {
        super(controller.getFrame(), true);
        this.controller = controller;
        
        
		enummeration = controller.getTreeView().createNewDefaultMutableTreeNodePreorderEnumaration();
		controller.setSearchEnumeration(enummeration);
		controller.setSearchString(null);

        setTitle("Search");

        textField = new JTextField();

        String msgString1 = "Find";
         Object[] array = {msgString1,textField};

        Object[] options = {buttonString1, buttonString2};

        optionPane = new JOptionPane(array,
                                    JOptionPane.QUESTION_MESSAGE,
                                    JOptionPane.YES_NO_OPTION,
                                    null,
                                    options,
                                    options[0]);

        setContentPane(optionPane);


        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });

        textField.addActionListener(this);

        optionPane.addPropertyChangeListener(this);
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        optionPane.setValue(buttonString1);
    }

	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			if (buttonString1.equals(value)) {
				searchText = textField.getText();
				controller.setSearchString(searchText);

				boolean found = controller.getTreeView().findAndShow(searchText, enummeration);
				if (!found) {
					setTitle("No (more) instances of '" + searchText + "' found");
					controller.setSearchEnumeration(null);
					controller.setSearchString(null);
					textField.setEnabled(false);
					optionPane.getRootPane().getDefaultButton().setEnabled(false);

				} else {
					optionPane.getRootPane().getDefaultButton().setText("Next");
					textField.setEnabled(false);

				}

			} else {
				searchText = null;
				clearAndHide();
			}
		}
	}

    /** This method clears the dialog and hides it. */
    public void clearAndHide() {
        textField.setText(null);
        setVisible(false);
    }
}

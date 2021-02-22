/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import nl.digitalekabeltelevisie.controller.ChartLabel;
import nl.digitalekabeltelevisie.controller.ViewContext;
import nl.digitalekabeltelevisie.main.DVBinspector;

/*  */
public class PIDDialog extends JDialog
implements ActionListener,
PropertyChangeListener, ListSelectionListener {


	/**
	 *
	 */
	private static final long serialVersionUID = -3439016568120119881L;
	private final JList<ChartLabel> leftList;
	private final JList<ChartLabel> rightList;
	private DefaultListModel<ChartLabel> leftListModel;
	private DefaultListModel<ChartLabel> rightListModel;

	private final DVBinspector controller;

	private final AddAllAction addAllAction;
	private final AddAction addAction;
	private final SwitchAction switchAction;
	private final RemoveAction removeAction;
	private final RemoveAllAction removeAllAction;

	private final TopAction topAction;
	private final UpAction upAction;
	private final ReverseAction reverseAction;
	private final DownAction downAction;
	private final BottomAction bottomAction;

	private ViewContext orgView;

	private final PacketSelectionPanel packetSelectionStart;
	private final PacketSelectionPanel packetSelectionEnd;
	private final JComboBox<Integer> stepsChooser;
	public static final Integer[] STEP_OPTIONS = {1,2,5,10,20,50,100,200,500};


	public  void enableButtons(){
		final boolean leftEmpty = leftListModel.isEmpty();
		final boolean rightEmpty = rightListModel.isEmpty();
		final int rightNumbers= rightListModel.getSize();

		addAllAction.setEnabled(!leftEmpty);
		addAction.setEnabled((!leftEmpty)&&(leftList.getSelectedIndex()!=-1));
		switchAction.setEnabled(true);
		removeAction.setEnabled((!rightEmpty)&&(rightList.getSelectedIndex()!=-1));
		removeAllAction.setEnabled(!rightEmpty);
		topAction.setEnabled((!rightEmpty)&&(rightList.getSelectedIndex()>0));
		upAction.setEnabled((!rightEmpty)&&(rightList.getSelectedIndex()>0));
		reverseAction.setEnabled(!rightEmpty);
		downAction.setEnabled((!rightEmpty)&&(rightList.getSelectedIndex()<(rightNumbers-1))&&(rightList.getSelectedIndex()!=-1));
		bottomAction.setEnabled((!rightEmpty)&&(rightList.getSelectedIndex()<(rightNumbers-1))&&(rightList.getSelectedIndex()!=-1));

	}

	class AddAllAction extends AbstractAction {
		/**
		 *
		 */
		 private static final long serialVersionUID = 3689777563900027556L;
		 public AddAllAction(final String text) {
			 super(text);

		 }
		 public void actionPerformed(final ActionEvent e) {
			 final Enumeration<ChartLabel> el = leftListModel.elements();
			 while(el.hasMoreElements()){
				 rightListModel.addElement(el.nextElement());
			 }
			 leftListModel.clear();
			 enableButtons();
		 }
	}

	class AddAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 8979337275391648459L;
		public AddAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {

			final int index = leftList.getSelectedIndex();

			if(index!=-1){
				int rightIndex = rightList.getSelectedIndex(); //get selected index
				if (rightIndex == -1) { //no selection, so insert at end
					rightIndex = rightListModel.getSize();
				}
				rightListModel.insertElementAt(leftListModel.elementAt(index) , rightIndex);
				leftListModel.remove(index);
			}
			enableButtons();

		}
	}

	class SwitchAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 2303116336558669473L;
		public SwitchAction(final String text) {
			super(text);
		}
		public void actionPerformed(final ActionEvent e) {
			final DefaultListModel<ChartLabel> tmp = leftListModel;
			leftListModel = rightListModel;
			leftList.setModel(rightListModel);
			rightListModel = tmp;
			rightList.setModel(rightListModel);
			enableButtons();


		}
	}

	class RemoveAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 2805825694107261544L;
		public RemoveAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final int index = rightList.getSelectedIndex();

			if(index!=-1){
				int leftIndex = leftList.getSelectedIndex(); //get selected index
				if (leftIndex == -1) { //no selection, so insert at end
					leftIndex = leftListModel.getSize();
				}
				leftListModel.insertElementAt(rightListModel.elementAt(index) , leftIndex);


				rightListModel.remove(index);
			}
			enableButtons();

		}
	}


	class RemoveAllAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 4811839547583190284L;
		public RemoveAllAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final Enumeration<ChartLabel> el = rightListModel.elements();
			while(el.hasMoreElements()){
				leftListModel.addElement(el.nextElement());
			}
			rightListModel.clear();
			enableButtons();
		}
	}

	class UpAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 3534182235010446238L;
		public UpAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final int index = rightList.getSelectedIndex();

			if(index!=-1){

				rightListModel.insertElementAt(rightListModel.elementAt(index) , index-1);
				rightListModel.remove(index+1);
				rightList.setSelectedIndex(index-1);
				rightList.ensureIndexIsVisible(index-1);
			}
			enableButtons();
		}
	}

	class TopAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 3209628912633150373L;
		public TopAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final int index = rightList.getSelectedIndex();

			if(index!=-1){

				rightListModel.insertElementAt(rightListModel.elementAt(index) , 0);
				rightListModel.remove(index+1);
				rightList.setSelectedIndex(0);
				rightList.ensureIndexIsVisible(0);
			}
			enableButtons();
		}
	}

	class ReverseAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = -4739983729332909990L;
		public ReverseAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final int index = rightList.getSelectedIndex();

			final int l=rightListModel.size();

			for (int i = 0; i < (l/2); i++) {
				rightListModel.insertElementAt(rightListModel.elementAt(l-i-1) , i);
				rightListModel.remove(l-i);
				rightListModel.insertElementAt(rightListModel.elementAt(i+1) , l-i);
				rightListModel.remove(i+1);
			}
			if(index!=-1){
				rightList.setSelectedIndex(l-1-index);
				rightList.ensureIndexIsVisible(l-1-index);
			}
			enableButtons();
		}
	}

	class DownAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = -7274215756253610038L;
		public DownAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final int index = rightList.getSelectedIndex();

			if(index!=-1){

				rightListModel.insertElementAt(rightListModel.elementAt(index) , index+2);
				rightListModel.remove(index);
				rightList.setSelectedIndex(index+1);
				rightList.ensureIndexIsVisible(index+1);

			}
			enableButtons();
		}
	}

	class BottomAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 1894143066033119542L;
		public BottomAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final int index = rightList.getSelectedIndex();

			if(index!=-1){
				final int s=rightListModel.getSize();
				rightListModel.insertElementAt(rightListModel.elementAt(index) , s);
				rightListModel.remove(index);
				rightList.setSelectedIndex(s-1);
				rightList.ensureIndexIsVisible(s-1);

			}
			enableButtons();
		}
	}
	class ApplyAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = 8414410738504899008L;
		public ApplyAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			final List<ChartLabel> shown = new ArrayList<>();

			final Enumeration<?> el = rightListModel.elements();
			while(el.hasMoreElements()){
				shown.add((ChartLabel)el.nextElement());
			}

			final List<ChartLabel> notShown = new ArrayList<>();

			final Enumeration<ChartLabel> el2 = leftListModel.elements();
			while(el2.hasMoreElements()){
				notShown.add(el2.nextElement());
			}

			orgView.setShown(shown);
			orgView.setNotShown(notShown);

			orgView.setStartPacket(packetSelectionStart.getValue());
			orgView.setEndPacket(packetSelectionEnd.getValue());
			orgView.setGraphSteps((Integer)stepsChooser.getSelectedItem());

			controller.setPIDList(orgView);

		}
	}

	class OKAction extends ApplyAction {
		/**
		 *
		 */
		private static final long serialVersionUID = -4143479183600719898L;
		public OKAction(final String text) {
			super(text);

		}
		@Override
		public void actionPerformed(final ActionEvent e) {
			super.actionPerformed(e);
			setVisible(false);


		}
	}


	class CancelAction extends AbstractAction {
		/**
		 *
		 */
		private static final long serialVersionUID = -6217921517985338175L;
		public CancelAction(final String text) {
			super(text);

		}
		public void actionPerformed(final ActionEvent e) {
			clearAndHide();


		}
	}

	/** Creates the reusable dialog.
	 * @param binspector */
	public PIDDialog(final Frame aFrame, final ViewContext viewContext, final DVBinspector binspector) {
		super(aFrame, true);

		controller= binspector;
		setTitle("PID Selector");

		final JPanel panel = new JPanel(new BorderLayout());
		final JPanel pidPanel = new JPanel();
		pidPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("PID Filter"),
				BorderFactory.createEmptyBorder(5,5,5,5)));
		final JPanel packetPanel = new JPanel();
		pidPanel.setLayout(new BoxLayout(pidPanel,BoxLayout.X_AXIS));

		leftListModel = new DefaultListModel<>();
		for (final ChartLabel label : viewContext.getNotShown()) {
			leftListModel.addElement(label);

		}
		leftList = new JList<>(leftListModel);
		leftList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		rightListModel = new DefaultListModel<>();

		for (final ChartLabel label : viewContext.getShown()) {
			rightListModel.addElement(label);

		}
		rightList = new JList<>(rightListModel);


		final JPanel buttonPanel = new JPanel();
		final JPanel sortButtonPanel = new JPanel();
		final JPanel okCancelPanel = new JPanel();

		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		setLayout(new FlowLayout());

		addAllAction =new AddAllAction("add all >>");
		JButton addAllButton = new JButton(addAllAction);
		addAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(addAllButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		addAction = new AddAction("add >");
		JButton addButton = new JButton(addAction);
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(addButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		switchAction = new SwitchAction("< swap >");
		JButton switchButton = new JButton(switchAction);
		switchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(switchButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		removeAction = new RemoveAction("< remove");
		JButton removeButton = new JButton(removeAction);
		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(removeButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		removeAllAction =new RemoveAllAction("<< remove all");
		JButton removeAllButton = new JButton(removeAllAction);
		removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(removeAllButton);

		leftList.addListSelectionListener(this);
		rightList.addListSelectionListener(this);

		topAction =new TopAction("Top");
		JButton topButton = new JButton(topAction);
		topButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		upAction =new UpAction("Up");
		JButton upButton = new JButton(upAction);
		upButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		reverseAction =new ReverseAction("Reverse");
		JButton reverseButton = new JButton(reverseAction);
		reverseButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		downAction =new DownAction("Down");
		JButton downButton = new JButton(downAction);
		downButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		bottomAction =new BottomAction("Bottom");
		JButton bottomButton = new JButton(bottomAction);
		bottomButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		sortButtonPanel.setLayout(new BoxLayout(sortButtonPanel, BoxLayout.Y_AXIS));

		sortButtonPanel.add(topButton);
		sortButtonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		sortButtonPanel.add(upButton);
		sortButtonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		sortButtonPanel.add(reverseButton);
		sortButtonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		sortButtonPanel.add(downButton);
		sortButtonPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		sortButtonPanel.add(bottomButton);

		OKAction okAction = new OKAction("OK");
		JButton okButton = new JButton(okAction);
		okButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		ApplyAction applyAction = new ApplyAction("Apply");
		JButton applyButton = new JButton(applyAction);
		applyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		CancelAction cancelAction = new CancelAction("Cancel");
		JButton cancelButton = new JButton(cancelAction);
		cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

		okCancelPanel.add(okButton);
		okCancelPanel.add(applyButton);
		okCancelPanel.add(cancelButton);


		JScrollPane leftListScroller = new JScrollPane(leftList);
		final int h = (int)Math.max(leftList.getPreferredSize().getHeight(),rightList.getPreferredSize().getHeight());
		final int w = (int)Math.max(leftList.getPreferredSize().getWidth(),rightList.getPreferredSize().getWidth());
		leftListScroller.setSize(new Dimension(w, h));

		JScrollPane rightlistScroller = new JScrollPane(rightList);
		rightlistScroller.setSize(new Dimension(w, h));

		pidPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		pidPanel.add(leftListScroller);
		pidPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		pidPanel.add(buttonPanel);
		pidPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		pidPanel.add(rightlistScroller);
		pidPanel.add(Box.createRigidArea(new Dimension(10, 10)));
		pidPanel.add(sortButtonPanel);
		pidPanel.add(Box.createRigidArea(new Dimension(10, 10)));

		packetSelectionStart = new PacketSelectionPanel("Start", 0, viewContext.getMaxPacket(), 0,viewContext.getTransportStream());
		packetSelectionEnd = new PacketSelectionPanel("End",0, viewContext.getMaxPacket(), viewContext.getMaxPacket(),viewContext.getTransportStream());

		packetPanel.setLayout(new BoxLayout(packetPanel, BoxLayout.Y_AXIS));
		packetPanel.add(packetSelectionStart);
		packetPanel.add(packetSelectionEnd);

		final JPanel stepsChooserPanel = new JPanel();

		stepsChooserPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Steps"),
				BorderFactory.createEmptyBorder(5,5,5,5)));


		
		stepsChooser = new JComboBox<>(STEP_OPTIONS);
		stepsChooser.addActionListener(this);
		stepsChooserPanel.add(stepsChooser);



		final JPanel stepsPanel = new JPanel();
		stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.X_AXIS));
		stepsPanel.add(packetPanel);
		stepsPanel.add(stepsChooserPanel);

		panel.add(Box.createRigidArea(new Dimension(10, 10)),BorderLayout.NORTH);
		panel.add(stepsPanel,BorderLayout.NORTH);
		panel.add(pidPanel,BorderLayout.CENTER);
		panel.add(okCancelPanel,BorderLayout.SOUTH);

		//Make this dialog display it.
		setContentPane(panel);
		setResizable(true);

		//Handle window closing correctly.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent we) {
				resetLists();
			}
		});

		//Register an event handler that reacts to option pane state changes.
		panel.addPropertyChangeListener(this);

	}

	/** This method handles events for the steps choosr field. */
	public void actionPerformed(final ActionEvent e) {

		// EMPTY

	}



	public void propertyChange(final PropertyChangeEvent evt) {
		enableButtons();

	}

	public void valueChanged(final ListSelectionEvent e) {
		enableButtons();

	}

	/** Creates the reusable dialog. */
	public void setConfig(final ViewContext viewContext){

		orgView = viewContext;
		resetLists(viewContext);
	}

	public void resetLists(final ViewContext viewContex) {
		leftListModel.clear();

		for (final ChartLabel label : viewContex.getNotShown()) {
			leftListModel.addElement(label);

		}
		rightListModel.clear();
		for (final ChartLabel label : viewContex.getShown()) {
			rightListModel.addElement(label);
		}

		packetSelectionStart.setRangeValue(0, viewContex.getMaxPacket()-1, viewContex.getStartPacket(),viewContex.getTransportStream());
		packetSelectionEnd.setRangeValue(1, viewContex.getMaxPacket(), viewContex.getEndPacket(),viewContex.getTransportStream());

		for(int i=0; i<stepsChooser.getItemCount();i++ ){
			if(viewContex.getGraphSteps()==stepsChooser.getItemAt(i)){
				stepsChooser.setSelectedIndex(i);
			}
		}

		pack();

		rightList.revalidate();
		leftList.revalidate();

		pack();

	}
	public void resetLists() {
		resetLists(orgView);
	}
	public void clearAndHide() {
		resetLists();
		setVisible(false);
	}


}

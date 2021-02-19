package nl.digitalekabeltelevisie.data.mpeg.psi;
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


import static java.lang.Byte.toUnsignedInt;

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.*;
import nl.digitalekabeltelevisie.data.mpeg.*;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.*;


public class PMTsection extends TableSectionExtendedSyntax{


	private int pcrPid = 0;
	private int programInfoLength = 0;

	private List<Descriptor> descriptorList;
	private final List<Component> componentsList ;

	public static class Component implements TreeNode{

		private int streamtype;
		private int elementaryPID;
		private int esInfoLength;

		private List<Descriptor> componentDescriptorList;

		public String getStreamTypeString(){
			return Utils.getStreamTypeString(streamtype);
		}




		public int getElementaryPID() {
			return elementaryPID;
		}

		public void setElementaryPID(final int elementaryPID) {
			this.elementaryPID = elementaryPID;
		}

		public int getEsInfoLength() {
			return esInfoLength;
		}

		public void setEsInfoLength(final int infoLength) {
			esInfoLength = infoLength;
		}

		public int getStreamtype() {
			return streamtype;
		}

		public void setStreamtype(final int streamtype) {
			this.streamtype = streamtype;
		}

		public List<Descriptor> getComponentDescriptorList() {
			return componentDescriptorList;
		}

		public void setComponentDescriptorList(final List<Descriptor> descriptorList) {
			this.componentDescriptorList = descriptorList;
		}
		@Override
		public String toString(){
			final StringBuilder b = new StringBuilder("Component stream type=");
			b.append(getStreamtype()).append(" (").append(getStreamTypeString()).append("), ElementaryPID=").append(getElementaryPID()).append(", ");
			for (Descriptor d : componentDescriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){

			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("component ("+Utils.getStreamTypeString(streamtype)+")"));

			t.add(new DefaultMutableTreeNode(new KVP("stream_type",streamtype,Utils.getStreamTypeString(streamtype))));
			t.add(new DefaultMutableTreeNode(new KVP("elementary_PID",elementaryPID,null)));
			t.add(new DefaultMutableTreeNode(new KVP("ES_info_length",esInfoLength,null)));

			Utils.addListJTree(t,componentDescriptorList,modus,"component_descriptors");

			return t;
		}
	}


	private List<Component> buildComponentList(final byte[] data, final int i, final int length) {
		final ArrayList<Component> r = new ArrayList<>();
		int t =0;
		while(t<length){
			final Component c = new Component();
			c.setStreamtype(toUnsignedInt(data[i+t]));
			c.setElementaryPID((256 *(toUnsignedInt(data[i+t+1])& 0x1F)) + toUnsignedInt(data[i+t+2]));
			c.setEsInfoLength((256 *(toUnsignedInt(data[i+t+3])& 0x0F)) + toUnsignedInt(data[i+t+4]));
			c.setComponentDescriptorList(DescriptorFactory.buildDescriptorList(data,i+t+5,c.getEsInfoLength(), this));
			t+=5+c.getEsInfoLength();
			r.add(c);

		}

		return r;
	}


	public PMTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);

		pcrPid = Utils.getInt(raw_data.getData(),8,2, 0x1FFF);
		programInfoLength = Utils.getInt(raw_data.getData(),10,2, 0x0FFF);
		descriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(),12,programInfoLength,this);
		componentsList = buildComponentList(raw_data.getData(),12+programInfoLength, raw_data.getNoBytes()-16-programInfoLength);
	}


	public int getProgramNumber()
	{
		return getTableIdExtension();
	}

	public int noStreams(){
		return componentsList.size();
	}

	public int getElementaryPID(final int i){
		return ((toUnsignedInt(raw_data.getData()[10+programInfoLength+(i*4)])& 0x1F )*256) + toUnsignedInt(raw_data.getData()[11+programInfoLength+(i*4)]);
	}
	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("PMTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", program_number=").append(getProgramNumber()).append(", ").append(", PMT_PID:").append(getParentPID().getPid());
		return b.toString();
	}

	public int getPcrPid() {
		return pcrPid;
	}

	public void setPcrPid(final int pcrPid) {
		this.pcrPid = pcrPid;
	}

	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public void setDescriptorList(final List<Descriptor> descriptorList) {
		this.descriptorList = descriptorList;
	}

	public int getProgramInfoLength() {
		return programInfoLength;
	}

	public void setProgramInfoLength(final int programInfoLength) {
		this.programInfoLength = programInfoLength;
	}

	public List<Component> getComponentenList() {
		return componentsList;
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		KVP kvp = (KVP) t.getUserObject();
		kvp.setTableSource(this::getTableModel);

		t.add(new DefaultMutableTreeNode(new KVP("PMT_PID",getParentPID().getPid(),null)));
		t.add(new DefaultMutableTreeNode(new KVP("PCR_PID",pcrPid,null)));
		t.add(new DefaultMutableTreeNode(new KVP("program_info_length",programInfoLength,null)));
		Utils.addListJTree(t,descriptorList,modus,"program_info");
		Utils.addListJTree(t,componentsList,modus,"components");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "program_number";
	}


	public TableModel getTableModel() {
		FlexTableModel<PMTsection,Component> tableModel =  new FlexTableModel<>(PMTs.buildPmtTableHeader());

		tableModel.addData(this, getComponentenList());

		tableModel.process();
		return tableModel;
	}
}

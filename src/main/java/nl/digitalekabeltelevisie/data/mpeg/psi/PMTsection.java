package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2025 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.DescriptorFactory;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;


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

		public void setElementaryPID(int elementaryPID) {
			this.elementaryPID = elementaryPID;
		}

		public int getEsInfoLength() {
			return esInfoLength;
		}

		public void setEsInfoLength(int infoLength) {
			esInfoLength = infoLength;
		}

		public int getStreamtype() {
			return streamtype;
		}

		public void setStreamtype(int streamtype) {
			this.streamtype = streamtype;
		}

		public List<Descriptor> getComponentDescriptorList() {
			return componentDescriptorList;
		}

		public void setComponentDescriptorList(List<Descriptor> descriptorList) {
			this.componentDescriptorList = descriptorList;
		}
		@Override
		public String toString(){
			StringBuilder b = new StringBuilder("Component stream type=");
			b.append(getStreamtype()).append(" (").append(getStreamTypeString()).append("), ElementaryPID=").append(getElementaryPID()).append(", ");
			for (Descriptor d : componentDescriptorList) {
				b.append(d).append(", ");

			}
			return b.toString();

		}


		@Override
		public KVP getJTreeNode(int modus) {

			KVP t = new KVP("component (" + Utils.getStreamTypeString(streamtype) + ")");

			t.add(new KVP("stream_type", streamtype, Utils.getStreamTypeString(streamtype)));
			t.add(new KVP("elementary_PID", elementaryPID));
			t.add(new KVP("ES_info_length", esInfoLength));

			addListJTree(t, componentDescriptorList, modus, "component_descriptors");

			return t;
		}
	}

	private List<Component> buildComponentList(byte[] data, int i, int length) {
		ArrayList<Component> r = new ArrayList<>();
		int t =0;
		while(t<length){
			Component c = new Component();
			c.setStreamtype(toUnsignedInt(data[i+t]));
			c.setElementaryPID((256 *(toUnsignedInt(data[i+t+1])& 0x1F)) + toUnsignedInt(data[i+t+2]));
			c.setEsInfoLength((256 *(toUnsignedInt(data[i+t+3])& 0x0F)) + toUnsignedInt(data[i+t+4]));
			c.setComponentDescriptorList(DescriptorFactory.buildDescriptorList(data,i+t+5,c.getEsInfoLength(), this));
			t+=5+c.getEsInfoLength();
			r.add(c);

		}

		return r;
	}


	public PMTsection(PsiSectionData raw_data, PID parent){
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

	@Override
	public String toString(){
		StringBuilder b = new StringBuilder("PMTsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", program_number=").append(getProgramNumber()).append(", ").append(", PMT_PID:").append(getParentPID().getPid());
		return b.toString();
	}

	public int getPcrPid() {
		return pcrPid;
	}

	public void setPcrPid(int pcrPid) {
		this.pcrPid = pcrPid;
	}

	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public void setDescriptorList(List<Descriptor> descriptorList) {
		this.descriptorList = descriptorList;
	}

	public int getProgramInfoLength() {
		return programInfoLength;
	}

	public void setProgramInfoLength(int programInfoLength) {
		this.programInfoLength = programInfoLength;
	}

	public List<Component> getComponentenList() {
		return componentsList;
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.addTableSource(this::getTableModel, "Components");

		t.add(new KVP("PMT_PID", getParentPID().getPid()));
		t.add(new KVP("PCR_PID", pcrPid));
		t.add(new KVP("program_info_length", programInfoLength));
		addListJTree(t, descriptorList, modus, "program_info");
		addListJTree(t, componentsList, modus, "components");
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


	public boolean hasComponentWithPid(int pid) {
		for(Component component:componentsList) {
			if(component.getElementaryPID()==pid) {
				return true;
			}
		}
		return false;
	}
}

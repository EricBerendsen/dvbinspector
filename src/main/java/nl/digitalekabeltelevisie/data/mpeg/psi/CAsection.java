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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor.findGenericDescriptorsInList;

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.*;
import nl.digitalekabeltelevisie.gui.TableSource;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.*;

public class CAsection extends TableSectionExtendedSyntax implements TableSource{

	private List<Descriptor>	descriptorList;

	public CAsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data, parent);
		descriptorList = DescriptorFactory.buildDescriptorList(raw_data.getData(), 8, sectionLength - 9, this);
	}

	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder("CAsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber());
		return b.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		if(!descriptorList.isEmpty()) {
			KVP kvp = (KVP) t.getUserObject();
			kvp.setTableSource(this);
		}
		Utils.addListJTree(t, descriptorList, modus, "descriptors");
		return t;
	}
	

	@Override
	protected String getTableIdExtensionLabel() {
		return "reserved";
	}


	public List<Descriptor> getDescriptorList() {
		return descriptorList;
	}

	public void setDescriptorList(final List<Descriptor> descriptorList) {
		this.descriptorList = descriptorList;
	}

	@Override
	public TableModel getTableModel() {
		FlexTableModel<CAsection,CADescriptor> tableModel =  new FlexTableModel<>(CAT.buildCatTableHeader());
		tableModel.addData(this, findGenericDescriptorsInList(getDescriptorList(), CADescriptor.class));

		tableModel.process();
		return tableModel;
	}
		

	
//
//	@Override
//	public TableModel getTableModel() {
//		return TableUtils.getTableModel(CAT::buildCatTableHeader,()->getRowData()) ;
//	}
//
//
//	public List<Map<String, Object>> getRowData() {
//		List<Map<String, Object>> rowData = new ArrayList<Map<String,Object>>(); 
//		
//		for( Descriptor descriptor:descriptorList) {
//			if(descriptor instanceof TableRowSource)
//			rowData.add(((TableRowSource)descriptor).getTableRowData());
//		}
//		return rowData;
//	}

}

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

package nl.digitalekabeltelevisie.data.mpeg.psi;

import static java.util.Arrays.copyOfRange;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.FlexTableModel;


public class TDTsection extends TableSection {

	private final byte[] UTC_time;

	public TDTsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);
		UTC_time= copyOfRange(raw_data.getData(),3,8);
	}



	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("TDTsection UTC_Time=");
		b.append(Utils.toHexString(UTC_time)).append(", UTC_timeString=").append(getUTC_timeString()).append(", length=").append(getSectionLength());
		return b.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		KVP kvp = (KVP)t.getUserObject();
		kvp.setTableSource(this::getTableModel);
		t.add(new DefaultMutableTreeNode(new KVP("UTC_time",UTC_time,Utils.getUTCFormattedString(UTC_time))));
		return t;
	}


	public byte[] getUTC_time() {
		return UTC_time;
	}



	public String getUTC_timeString() {
		return Utils.getUTCFormattedString(UTC_time);
	}

	public TableModel getTableModel() {
		FlexTableModel<TDTsection,TDTsection> tableModel =  new FlexTableModel<>(TDT.buildTdtTableHeader());
		
		List<TDTsection> lst = new ArrayList<>();
		lst.add(this);
		tableModel.addData(this, lst);
		
		tableModel.process();
		return tableModel;
	}

}

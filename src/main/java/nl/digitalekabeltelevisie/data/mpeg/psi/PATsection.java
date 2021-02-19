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

import javax.swing.table.*;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;
import nl.digitalekabeltelevisie.gui.*;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.*;


public class PATsection extends TableSectionExtendedSyntax implements TableSource{

	private List<Program> programs;

	public class Program implements TreeNode{
		private int program_number;
		private int program_map_PID;

		public Program(final int program_number, final int program_map_PID) {
			super();
			this.program_number = program_number;
			this.program_map_PID = program_map_PID;
		}
		public int getProgram_map_PID() {
			return program_map_PID;
		}
		public void setProgram_map_PID(final int program_map_PID) {
			this.program_map_PID = program_map_PID;
		}
		public int getProgram_number() {
			return program_number;
		}
		public void setProgram_number(final int program_number) {
			this.program_number = program_number;
		}

		@Override
		public DefaultMutableTreeNode getJTreeNode(final int modus){
			String postFix = "";

			String serviceName = getServiceNameOrNit();
			if(serviceName!=null) {
				postFix = " ("+serviceName+")";
			}
			final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("program"+ postFix));

			t.add(new DefaultMutableTreeNode(new KVP("program_number",program_number,null)));
			t.add(new DefaultMutableTreeNode(new KVP("program_map_PID",program_map_PID,null)));

			return t;
		}

		public String getServiceNameOrNit() {
			String serviceName = getParentPID().getParentTransportStream().getPsi().getSdt().getServiceNameForActualTransportStream(program_number);
			if(serviceName==null && program_map_PID==16) {
				serviceName = "NIT";
			}
			return serviceName;
		}
	}

	public PATsection(final PsiSectionData raw_data, final PID parent){
		super(raw_data,parent);

		final int programsLength = sectionLength -9;
		programs = buildProgramList(raw_data.getData(),8,programsLength);
	}

	private List<Program> buildProgramList(final byte[] data, final int offset, final int programInfoLength) {
		final ArrayList<Program> r = new ArrayList<>();
		int t =0;
		while(t<programInfoLength){
			final Program c = new Program(Utils.getInt(data, offset+t, 2, 0xFFFF),Utils.getInt(data, offset+t+2, 2, 0x1FFF));
			t+=4;
			r.add(c);

		}

		return r;
	}
	public int getTransportStreamId()
	{
		return getTableIdExtension();
	}

	public int noPrograms(){
		return programs.size();
	}

	public int getProgramNumber(final int i){
		return (toUnsignedInt(raw_data.getData()[8+(i*4)]) *256) + toUnsignedInt(raw_data.getData()[9+(i*4)]);
	}

	public int getProgramMapPID(final int i){
		return ((toUnsignedInt(raw_data.getData()[10+(i*4)])& 0x1F )*256) + toUnsignedInt(raw_data.getData()[11+(i*4)]);
	}
	
	@Override
	public String toString(){
		final StringBuilder b = new StringBuilder("PATsection section=");
		b.append(getSectionNumber()).append(", lastSection=").append(getSectionLastNumber()).append(", transport_stream_id=").append(getTransportStreamId()).append(", noProgramms=").append(noPrograms());
		for (int i = 0; i < noPrograms(); i++) {
			b.append(", ").append(i).append(":").append(getProgramNumber(i)).append(":").append(getProgramMapPID(i));
		}
		return b.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		KVP kvp = (KVP) t.getUserObject();
		kvp.setTableSource(this);

		Utils.addListJTree(t,programs,modus,"programs");
		return t;
	}

	@Override
	protected String getTableIdExtensionLabel() {
		return "transport_stream_id";
	}

	public List<Program> getPrograms() {
		return programs;
	}

	public void setPrograms(final List<Program> programs) {
		this.programs = programs;
	}
	
	@Override
	public TableModel getTableModel() {
		FlexTableModel<PATsection,Program> tableModel =  new FlexTableModel<>(PAT.buildPatTableHeader());
		
		tableModel.addData(this, getPrograms());
		
		tableModel.process();
		return tableModel;

	}

}

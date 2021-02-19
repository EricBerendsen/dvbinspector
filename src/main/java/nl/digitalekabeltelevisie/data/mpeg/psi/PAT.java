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


import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.PATsection.Program;
import nl.digitalekabeltelevisie.util.Utils;
import nl.digitalekabeltelevisie.util.tablemodel.*;

public class PAT extends AbstractPSITabel{

	private PATsection []pat = null;
	private final List<PATsection []> patVersions = new ArrayList<>();
	private PATsection [] newPAT = null;
	private int actualVersionNo = -1;


	public PAT(final PSI parent){
		super(parent);
	}

	/**
	 * Add (or update) the section to the PAT
	 *
	 * TODO, like all PSI tables this will go wrong when we start just before a version update, when only the last sections of the old version are received.
	 * i.e. when we receive section 1, version 12, followed by section 0, version 13, section 1, version 13, etc, the PAT will consist of section 0, version 13 and section 1, version 12.
	 * chance of this happening is small, so for now leave it.
	 *
	 * TODO this will not work correctly when number of sections in PAT changes.
	 *
	 * OTOH, a PAT only has multiple sections if > 1000 bytes, i.e. 250 services. Not very likely.
	 *
	 * @param section
	 */
	public void update(final PATsection section){


		if(pat==null){
			pat = new PATsection[section.getSectionLastNumber()+1];
		}

		if(pat[section.getSectionNumber()]==null){
			pat[section.getSectionNumber()] = section;
		}else{
			final TableSection last = pat[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
		if(section.getVersion()!=actualVersionNo){
			if(newPAT==null){
				newPAT = new PATsection[section.getSectionLastNumber()+1];
			}
			newPAT[section.getSectionNumber()] = section;
			boolean allFilled = true;
			for (PATsection element : newPAT) {
				allFilled &= (element!=null);
			}
			if(allFilled){
				PATsection[] actualPAT = newPAT;
				patVersions.add(actualPAT);
				actualVersionNo = section.getVersion();
				newPAT = null;
			}
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		KVP kvp = new KVP("PAT");
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(kvp);

		if (pat != null) {
			kvp.setTableSource(this::getTableModel);
			for (PATsection element : pat) {
				if(element!= null){
					if(!Utils.simpleModus(modus)){ // show all versions
						addSectionVersionsToJTree(t, element, modus);
					}else{ // keep it simple
						t.add(element.getJTreeNode(modus));
					}
				}
			}
		}
		return t;
	}

	public int getTransportStreamId(){

		if(pat!=null){
			for (PATsection element : pat) {
				if(element!= null){
					return element.getTransportStreamId();
				}
			}
		}
		return -1;
	}

	/**
	 * Returns true if the pid is contained in the PAT
	 * TODO this checks if the pid is contained in any version of the PAT, (maybe older) so it may not be
	 * in the current PAT.
	 * @param pid
	 * @return
	 */
	public boolean inPAT(final int pid){

		if(pat!=null){
			for (PATsection element : pat) {
				PATsection patSectionVersion = element;
				while(patSectionVersion!= null){
					for (Program program : patSectionVersion.getPrograms()) {
						if(program.getProgram_map_PID()==pid){
							return true;
						}
					}
					patSectionVersion=(PATsection)patSectionVersion.getNextVersion();
				}
			}
		}
		return false;
	}

	public PATsection[] getPATsections() {
		return pat;
	}

	/**
	 * TODO work in progress
	 * 
	 * @param packetNo
	 * @return the PAT valid at moment of packetNO, i.e. for which all sections have been received,
	 * and which has not yet been replaced with a complete new PAT. If no complete PAT has been received yet at moment packetNo, the next complete PAT will be returned.
	 */
	public PAT getPat(long packetNo) {
		if((pat==null)||(pat.length==0)){
			return null;

		}else if((pat[0]!=null)&&(packetNo<pat[0].getFirst_packet_no())){ // no complete PAT yet
			return this;
		}else if((pat[pat.length-1]!=null)&&(packetNo<pat[pat.length].getFirst_packet_no())){ // no complete PAT yet
			return this;
		}else{
			PATsection l = pat[pat.length-1];

			while((l.getLast_packet_no()<packetNo)&&(l.getNextVersion()!=null)){
				l=(PATsection) l.getNextVersion();

			}
		return null;
		}
	}


	public TableModel getTableModel() {
		FlexTableModel<PATsection,Program> tableModel =  new FlexTableModel<>(buildPatTableHeader());
		
		if (pat != null) {
			for (PATsection element : pat) {
				if(element!= null){
					tableModel.addData(element, element.getPrograms());
				}
			}
		}
		
		tableModel.process();
		return tableModel;
		
	}

	static TableHeader<PATsection,Program>  buildPatTableHeader() {
		return new TableHeaderBuilder<PATsection,Program>().
				addOptionalRowColumn("program_number", Program::getProgram_number, Integer.class).
				addOptionalRowColumn("program_map_PID", Program::getProgram_map_PID, Integer.class).
				addOptionalRowColumn("name", Program::getServiceNameOrNit, String.class).
				build();
	}

}

package nl.digitalekabeltelevisie.data.mpeg.psi;
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


import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.PATsection.Program;
import nl.digitalekabeltelevisie.util.Utils;

public class PAT extends AbstractPSITabel{

	private PATsection []pat = null;

	public PAT(final PSI parent){
		super(parent);
	}

	public void update(final PATsection section){

		count++;

		if(pat==null){
			pat = new PATsection[section.getSectionLastNumber()+1];
		}

		if(pat[section.getSectionNumber()]==null){
			pat[section.getSectionNumber()] = section;
		}else{
			final TableSection last = pat[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PAT"));


		if (pat != null) {
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


}

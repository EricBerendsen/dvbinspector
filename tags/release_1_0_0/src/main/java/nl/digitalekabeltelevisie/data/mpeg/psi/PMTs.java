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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.util.Utils;

public class PMTs extends AbstractPSITabel implements Iterable<PMTsection []>{


	public PMTs(final PSI parentPSI) {
		super(parentPSI);

	}

	private Map<Integer, PMTsection []> pmts = new HashMap<Integer, PMTsection []>();

	public void update(final PMTsection section){
		count++;

		final int programNumber = section.getProgramNumber();
		PMTsection [] sections= pmts.get(programNumber);

		if(sections==null){
			sections = new PMTsection[section.getSectionLastNumber()+1];
			pmts.put(programNumber, sections);
		}
		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("PMTs"));
		final TreeSet<Integer> s = new TreeSet<Integer>(pmts.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer programNumber=i.next();
			final TableSection [] sections = pmts.get(programNumber);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("program",programNumber,getParentPSI().getSdt().getServiceName(programNumber)));
			for (final TableSection tableSection : sections) {
				if(tableSection!= null){
					if(!Utils.simpleModus(modus)){ // show all details
						addSectionVersionsToJTree(n, tableSection, modus);
					}else{ // keep it simple
						final PMTsection pmtSection = (PMTsection) tableSection;
						n.add(new DefaultMutableTreeNode(new KVP("PCR_PID",pmtSection.getPcrPid(),null)));
						addListJTree(n,pmtSection.getDescriptorList(),modus,"program_info");
						addListJTree(n,pmtSection.getComponentenList(),modus,"components");
					}

				}
			}
			t.add(n);

		}
		return t;
	}

	public int getPmtPID(final int programNumber){
		final TableSection [] sections = pmts.get(programNumber);
		for (TableSection section : sections) {
			if(section!= null){
				return section.getParentPID().getPid();
			}
		}
		return -1;
	}

	// PMT is always one section per program

	public PMTsection getPmt(final int programNumber){
		return pmts.get(programNumber)[0];
	}

	public Iterator<PMTsection[]> iterator(){
		return pmts.values().iterator();
	}

	public Map<Integer, PMTsection[]> getPmts() {
		return pmts;
	}

	public void setPmts(final Map<Integer, PMTsection[]> pmts) {
		this.pmts = pmts;
	}


}

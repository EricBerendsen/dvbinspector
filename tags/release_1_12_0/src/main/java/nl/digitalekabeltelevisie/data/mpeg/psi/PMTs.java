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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.psi.PMTsection.Component;
import nl.digitalekabeltelevisie.util.Utils;

public class PMTs extends AbstractPSITabel implements Iterable<PMTsection []>{


	public PMTs(final PSI parentPSI) {
		super(parentPSI);

	}

	private Map<Integer, PMTsection []> pmts = new HashMap<Integer, PMTsection []>();

	public void update(final PMTsection section){

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
			final PMTsection[] sections = pmts.get(programNumber);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("program",programNumber,getParentPSI().getSdt().getServiceNameForActualTransportStream(programNumber)));
			for (final PMTsection pmtSection : sections) {
				if(pmtSection!= null){
					if(Utils.simpleModus(modus)){
						// keep it simple
						n.add(new DefaultMutableTreeNode(new KVP("PCR_PID",pmtSection.getPcrPid(),null)));
						addListJTree(n,pmtSection.getDescriptorList(),modus,"program_info");
						addListJTree(n,pmtSection.getComponentenList(),modus,"components");
					}else{ // show all details
						addSectionVersionsToJTree(n, pmtSection, modus);
					}
				}
			}
			t.add(n);

		}
		return t;
	}

	public int getPmtPID(final int programNumber){
		final PMTsection [] sections = pmts.get(programNumber);
		for (PMTsection section : sections) {
			if(section!= null){
				return section.getParentPID().getPid();
			}
		}
		return -1;
	}
	
	public List<PMTsection>findPMTsFromComponentPID(int pid){
		ArrayList<PMTsection> result = new ArrayList<PMTsection>();
		for(PMTsection[] pmtArray: pmts.values()){
			PMTsection p = pmtArray[0];
			for(Component component:p.getComponentenList()){
				if(component.getElementaryPID()==pid){
					result.add(p);
					break; // every PMT is included once, even if more components wold point to same PID (which is illegal)
				}
			}
		}
		return result;
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

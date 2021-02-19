package nl.digitalekabeltelevisie.data.mpeg.psi;
/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2017 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

/**
 * @author Eric
 *
 */
public class SCTE35 extends AbstractPSITabel{


	public SCTE35(final PSI parentPSI) {
		super(parentPSI);

	}

	private final Map<Integer, SpliceInfoSections> spliceSections = new HashMap<>();

	public void update(final SpliceInfoSection section){

		final int pid = section.getParentPID().getPid();
		SpliceInfoSections  sections= spliceSections.get(pid);

		if(sections==null){
			sections = new SpliceInfoSections(parentPSI);
			spliceSections.put(pid, sections);
		}
		sections.update(section);
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("SCTE-35"));
		final SortedSet<Integer> s = new TreeSet<>(spliceSections.keySet());

		for (Integer pid : s) {
			final SpliceInfoSections sections = spliceSections.get(pid);
			t.add(sections.getJTreeNode(modus));

		}
		return t;
	}

	
	public SpliceInfoSections getSpliceInfoSections(int pid){
		return spliceSections.get(pid);
	}

}

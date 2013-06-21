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

import static nl.digitalekabeltelevisie.util.Utils.getAppTypeIDString;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;

public class AIT extends AbstractPSITabel{

	private final Map<Integer, AITsection []> aits = new HashMap<Integer, AITsection []>();
	private int pid = 0;

	public AIT(final PSI parent){
		super(parent);
	}

	public void update(final AITsection section){
		count++;
		pid=section.getParentPID().getPid();

		final int key = section.getApplication_type();
		AITsection [] sections= aits.get(key);

		if(sections==null){
			sections = new AITsection[section.getSectionLastNumber()+1];
			aits.put(key, sections);
		}
		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("AIT (Application Information Table) PID="+pid ));
		final TreeSet<Integer> s = new TreeSet<Integer>(aits.keySet());

		final Iterator<Integer> i = s.iterator();
		while(i.hasNext()){
			final Integer type=i.next();
			final AITsection [] sections = aits.get(type);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("AIT",type, getAppTypeIDString(type)));
			for (final AITsection tsection : sections) {
				if(tsection!= null){
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}
		return t;
	}


}

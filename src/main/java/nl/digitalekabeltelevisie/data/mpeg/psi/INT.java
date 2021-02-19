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
 *

 */
package nl.digitalekabeltelevisie.data.mpeg.psi;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.util.Utils;

/**
 *	based on EN 301 192 § 8.4.4
 *
 */
public class INT extends AbstractPSITabel{

	private final Map<Integer, INTsection []> networks = new HashMap<>();

	public INT(final PSI parent){
		super(parent);
	}

	public void update(final INTsection section){

		final int key = section.getPlatformID();
		INTsection[] sections = networks.computeIfAbsent(key, k -> new INTsection[section.getSectionLastNumber() + 1]);

		if(sections[section.getSectionNumber()]==null){
			sections[section.getSectionNumber()] = section;
		}else{
			final TableSection last = sections[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}
	}

	/* (non-Javadoc)
	 * @see nl.digitalekabeltelevisie.controller.TreeNode#getJTreeNode(int)
	 */
	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("INT (IP/MAC Notification Table)"));
		final TreeSet<Integer> s = new TreeSet<>(networks.keySet());

		for (Integer platformID : s) {
			final INTsection[] sections = networks.get(platformID);
			final DefaultMutableTreeNode n = new DefaultMutableTreeNode(new KVP("platform_id", platformID, Utils.getPlatformIDString(platformID)));
			for (final INTsection tsection : sections) {
				if (tsection != null) {
					addSectionVersionsToJTree(n, tsection, modus);
				}
			}
			t.add(n);

		}
		return t;
	}


}

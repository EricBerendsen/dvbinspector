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

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

public class CAT extends AbstractPSITabel {

	private CAsection[]	cat	= null;

	public List<Descriptor> getDescriptorList() {
		final ArrayList<Descriptor> l = new ArrayList<Descriptor>();
		for (CAsection element : cat) {
			if (element != null) {
				l.addAll(element.getDescriptorList());
			}
		}
		return l;
	}

	public CAT(final PSI parent) {
		super(parent);
	}

	/**
	 * @param section
	 */
	public void update(final CAsection section) {
		count++;
		if (cat == null) {
			cat = new CAsection[section.getSectionLastNumber() + 1];
		}
		if (cat[section.getSectionNumber()] == null) {
			cat[section.getSectionNumber()] = section;
		} else {
			final TableSection last = cat[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}

	}

	public DefaultMutableTreeNode getJTreeNode(final int modus) {

		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("CAT"));

		if (cat != null) {
			for (CAsection element : cat) {
				if (element != null) {
					t.add(element.getJTreeNode(modus));
				}
			}
		}
		return t;
	}

}

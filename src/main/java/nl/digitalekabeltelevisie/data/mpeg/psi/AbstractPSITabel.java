/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import javax.swing.tree.DefaultMutableTreeNode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
@JsonIgnoreProperties({"parentPSI"})

public abstract class AbstractPSITabel implements TreeNode{

	protected PSI parentPSI;

	protected AbstractPSITabel(final PSI parentPSI) {
		super();
		this.parentPSI = parentPSI;
	}

	public PSI getParentPSI() {
		return parentPSI;
	}

	public void setParentPSI(final PSI parentPSI) {
		this.parentPSI = parentPSI;
	}

	/**
	 *
	 * @param n
	 * @param tableSection
	 * @param modus
	 */
	public static void addSectionVersionsToJTree(final DefaultMutableTreeNode n, final TableSection tableSection, final int modus) {
		n.add(tableSection.getJTreeNode(modus));
		TableSection versions = tableSection.getNextVersion();
		while(versions!=null){ // even show new versions
			n.add(versions.getJTreeNode(modus));
			versions = versions.getNextVersion();
		}
	}

	/**
	 * add newSection to the end of the linked list if it is not equal to the current last version
	 * if it is equal to the current last version update the statistics
	 * 
	 * @param newSection 
	 * @param section
	 * @return the last updated TableSection 
	 */
	public static TableSection updateSectionVersion(final TableSection newSection, final TableSection section) {

		TableSection last = section;

		while(last.getNextVersion()!=null){
			last = last.getNextVersion();
		}
		if(last.equals(newSection)){ // already have an instance if this section, just update the stats on the existing section
			int previousPacketNo = last.getLast_packet_no();
			int distance = newSection.getPacket_no() - previousPacketNo;
			if(distance>last.getMaxPacketDistance()){
				last.setMaxPacketDistance(distance);
			}
			if(distance<last.getMinPacketDistance()){
				last.setMinPacketDistance(distance);
			}
			last.setLast_packet_no(newSection.getPacket_no());
			last.setOccurrence_count(last.getOccurrence_count()+1);
			return last;
		}
		last.setNextVersion(newSection);
		return newSection;
	}

}

/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.PSI;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric
 *
 */
public class TSDT extends AbstractPSITabel {
	
	private TSDTsection []tsdt = null;

	/**
	 * @param parentPSI
	 */
	public TSDT(PSI parentPSI) {
		super(parentPSI);
	}

	
	public void update(final TSDTsection section){

		if(tsdt==null){
			tsdt = new TSDTsection[section.getSectionLastNumber()+1];
		}

		if(tsdt[section.getSectionNumber()]==null){
			tsdt[section.getSectionNumber()] = section;
		}else{
			final TableSection last = tsdt[section.getSectionNumber()];
			updateSectionVersion(section, last);
		}

	}
	
	
	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		final DefaultMutableTreeNode t = new DefaultMutableTreeNode(new KVP("TSDT"));
		if (tsdt != null) {
			for (TSDTsection element : tsdt) {
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

}

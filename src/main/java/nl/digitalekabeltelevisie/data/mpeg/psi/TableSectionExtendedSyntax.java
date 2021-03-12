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

import nl.digitalekabeltelevisie.data.mpeg.PID;
import nl.digitalekabeltelevisie.data.mpeg.PsiSectionData;

/**
 * Used to create a TableSection, when we know sectionSyntaxIndicator==1 should be true (i.e. we know it is the long
 * form, i.e. because we know it is a NITSection)
 *
 * This forces it to have sectionSyntaxIndicator==1, else RuntimeException will be thrown
 *
 * @author Eric
 *
 */
public class TableSectionExtendedSyntax extends TableSection {


	/**
	 * @param raw_data
	 * @param parent
	 */
	public TableSectionExtendedSyntax(PsiSectionData raw_data, PID parent) {
		super(raw_data, parent);

		if(sectionSyntaxIndicator==0){ // not long format, but we are expecting it.. So error
			// if sectionSyntaxIndicator==1 CRC checking was already done in super()
			throw new RuntimeException("TableSectionExtendedSyntax(): sectionSyntaxIndicator==0 for pid:"+parent.getPid());

		}

	}



}

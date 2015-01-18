/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * Based on ETSI TS 102 323 V1.5.1 (2012-01) 10.3 Related content descriptor
 *
 * Just a marker, no extra impl needed
 * @author Eric
 *
 */
public class RelatedContentDescriptor extends Descriptor {

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public RelatedContentDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
	}

}

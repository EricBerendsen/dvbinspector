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

package nl.digitalekabeltelevisie.data.mpeg.dsmcc;

import nl.digitalekabeltelevisie.util.Utils;

public final class LiteComponentsFactory {

	/**
	 * 
	 */
	private LiteComponentsFactory() {
		// private to avoid instantion
	}

	public static LiteComponent createLiteComponent(final byte[] data, final int offset) {

		final int tag = Utils.getInt(data, offset, 4, Utils.MASK_31BITS); // is 32 bits, but never negative.
		switch (tag) {
		case 0x49534F50:
			return new BIOPObjectLocation(data, offset);
		case 0x49534F40:
			return new DSMConnBinder(data, offset);


		default:
			return new LiteComponent(data, offset);
		}


	}

}

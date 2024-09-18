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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.untable;

import static nl.digitalekabeltelevisie.util.Utils.*;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

/**
 * based on ETSI TS 102 006 V1.4.1 9.5.2.11 SSU_event_name_descriptor
 * @author Eric Berendsen
 *
 */
public class SSUEventNameDescriptor extends UNTDescriptor {

	private final String iso639LanguageCode;

	private final int nameLength;

	private final DVBString name;

	private final DVBString text;

	public SSUEventNameDescriptor(byte[] b, int offset, TableSection parent) {
		super(b, offset, parent);
		iso639LanguageCode = getISO8859_1String(b, 2, 3);
		nameLength = getInt(b, 5, 1, MASK_8BITS);
		// TS 102 006 V1.4.1 does not explicitly say this field uses coding as described
		// in ETSI EN 300 468 [4], annex A.
		name = new DVBString(b, 5);
		text = new DVBString(b, 6 + nameLength);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(int modus) {
		DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
		t.add(new KVP("name_length", nameLength));
		t.add(new KVP("name", name));
		t.add(new KVP("text", text));
		return t;
	}

}

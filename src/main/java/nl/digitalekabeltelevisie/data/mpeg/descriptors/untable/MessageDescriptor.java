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

import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

/**
 * @author Eric Berendsen
 *
 */
public class MessageDescriptor extends UNTDescriptor {

	private final int descriptorNumber;
	private final int lastDescriptorNumber;
	private final String iso639LanguageCode;
	private final DVBString text;

	/**
	 * @param b
	 * @param offset
	 * @param parent
	 */
	public MessageDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset, parent);

		descriptorNumber = Utils.getInt(b, offset + 2, 1, 0xF0)>>4;
		lastDescriptorNumber = Utils.getInt(b, offset + 2, 1, Utils.MASK_4BITS);
		iso639LanguageCode = getISO8859_1String(b, offset + 3, 3);
		text = new DVBString(b, offset + 6, descriptorLength - 4);

	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus) {
		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("descriptor_number", descriptorNumber, null)));
		t.add(new DefaultMutableTreeNode(new KVP("last_descriptor_number", lastDescriptorNumber, null)));
		t.add(new DefaultMutableTreeNode(new KVP("ISO_639_language_code", iso639LanguageCode, null)));
		t.add(new DefaultMutableTreeNode(new KVP("platform name", text, null)));
		return t;
	}

	public int getDescriptorNumber() {
		return descriptorNumber;
	}

	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	public int getLastDescriptorNumber() {
		return lastDescriptorNumber;
	}

	public DVBString getText() {
		return text;
	}


}

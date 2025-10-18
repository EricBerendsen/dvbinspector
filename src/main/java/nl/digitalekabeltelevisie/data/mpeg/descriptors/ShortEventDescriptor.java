/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2024 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class ShortEventDescriptor extends LanguageDependentEitDescriptor{


	private String  iso639LanguageCode;
	private final DVBString eventName;
	private DVBString text;

	public ShortEventDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		iso639LanguageCode = getISO8859_1String(b, 2, 3);
		int eventNameLength = getInt(b, 5, 1, MASK_8BITS);
		eventName = new DVBString(b, 5);
		text = new DVBString(b, 6 + eventNameLength);
	}

	@Override
	public String getIso639LanguageCode() {
		return iso639LanguageCode;
	}

	@Override
	public String toString() {
		return super.toString() + ",eventName="+eventName.toString()+",ISO_639_language_code="+getIso639LanguageCode();
	}

	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		if (Utils.simpleModus(modus)) {
			t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			if (eventName.getLength() > 0) {
				t.add(new KVP("event_name", eventName));
			}
			if (text.getLength() > 0) {
				t.add(new KVP("text", text));
			}
		} else {
			t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			t.add(new KVP("event_name", eventName));
			t.add(new KVP("text", text));
		}
		return t;
	}

	public DVBString getEventName() {
		return eventName;
	}

	public DVBString getText() {
		return text;
	}

}

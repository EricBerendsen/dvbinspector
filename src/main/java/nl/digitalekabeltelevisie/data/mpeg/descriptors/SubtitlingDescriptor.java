/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class SubtitlingDescriptor extends Descriptor{

	private final List<Subtitle> subtitleList = new ArrayList<>();


	public  record Subtitle(String iso639LanguageCode, int subtitlingType, int compositionPageId, int ancillaryPageId) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("subtitle");
			s.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			s.add(new KVP("subtitling_type", subtitlingType, getComponentType0x03String(subtitlingType)));
			s.add(new KVP("composition_page_id", compositionPageId));
			s.add(new KVP("ancillary_page_id", ancillaryPageId));
			return s;
		}

		@Override
		public String toString(){
			return "code:'"+iso639LanguageCode;
		}

	}

	public SubtitlingDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t=0;
		while (t<descriptorLength) {
			String languageCode=Utils.getISO8859_1String(b, 2+t, 3);
			int subtitling_type = Utils.getInt(b, 5+t, 1, Utils.MASK_8BITS);
			int composition_page_id = Utils.getInt(b, 6+t, 2, Utils.MASK_16BITS);
			int ancillary_page_id = Utils.getInt(b, 8+t, 2, Utils.MASK_16BITS);
			Subtitle s = new Subtitle(languageCode, subtitling_type,composition_page_id,ancillary_page_id);
			subtitleList.add(s);
			t+=8;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Subtitle subtitle : subtitleList) {
			buf.append(subtitle.toString());
		}
		return buf.toString();
	}


	@Override
	public KVP getJTreeNode(int modus) {
		KVP t = super.getJTreeNode(modus);
		Utils.addListJTree(t, subtitleList, modus, "subtitle_list");
		return t;
	}

	public List<Subtitle> getSubtitleList() {
		return subtitleList;
	}
}

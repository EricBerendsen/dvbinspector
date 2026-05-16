/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2026 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.atsc;

import static nl.digitalekabeltelevisie.util.Utils.MASK_13BITS;
import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class ServiceLocationDescriptor extends AtscDescriptor {

	private final int pcrPid;
	private final int numberElements;
	private final List<Element> elements = new ArrayList<>();

	public ServiceLocationDescriptor(final byte[] b, final TableSection parent) {
		super(b, parent);
		pcrPid = getInt(b, PRIVATE_DATA_OFFSET, 2, MASK_13BITS);
		numberElements = getInt(b, PRIVATE_DATA_OFFSET + 2, 1, MASK_8BITS);
		int offset = PRIVATE_DATA_OFFSET + 3;
		for (int i = 0; i < numberElements; i++) {
			Element element = new Element(b, offset);
			elements.add(element);
			offset += 6;
		}
	}

	@Override
	public KVP getJTreeNode(final int modus) {
		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("PCR_PID", pcrPid));
		t.add(new KVP("number_elements", numberElements));
		addListJTree(t, elements, modus, "elements");
		return t;
	}

	public int getPcrPid() {
		return pcrPid;
	}

	public int getNumberElements() {
		return numberElements;
	}

	public List<Element> getElements() {
		return elements;
	}

	public static class Element implements TreeNode {

		private final int streamType;
		private final int elementaryPid;
		private final String iso639LanguageCode;

		Element(final byte[] data, final int offset) {
			streamType = getInt(data, offset, 1, MASK_8BITS);
			elementaryPid = getInt(data, offset + 1, 2, MASK_13BITS);
			iso639LanguageCode = getISO8859_1String(data, offset + 3, 3);
		}

		@Override
		public KVP getJTreeNode(final int modus) {
			KVP t = new KVP("element");
			t.add(new KVP("stream_type", streamType, Utils.getStreamTypeString(streamType)));
			t.add(new KVP("elementary_PID", elementaryPid));
			t.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			return t;
		}

		public int getStreamType() {
			return streamType;
		}

		public int getElementaryPid() {
			return elementaryPid;
		}

		public String getIso639LanguageCode() {
			return iso639LanguageCode;
		}
	}
}

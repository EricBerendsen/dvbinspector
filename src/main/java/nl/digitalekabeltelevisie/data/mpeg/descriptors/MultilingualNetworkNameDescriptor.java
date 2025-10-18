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

package nl.digitalekabeltelevisie.data.mpeg.descriptors;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class MultilingualNetworkNameDescriptor extends Descriptor {

	private final List<NetworkName> networkNameList = new ArrayList<>();

	public static record NetworkName(String iso639LanguageCode, DVBString network_name) implements TreeNode{

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("network_name");
			s.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			s.add(new KVP("network_name", network_name));
			return s;
		}

	}

	public MultilingualNetworkNameDescriptor(byte[] b, final TableSection parent) {
		super(b, parent);
		int t = 2;
		while (t < (descriptorLength + 2)) {
			String languageCode = Utils.getISO8859_1String(b, t, 3);
			int network_name_length = Utils.getInt(b, t + 3, 1, Utils.MASK_8BITS);
			DVBString network_name = new DVBString(b, t + 3);
			NetworkName s = new NetworkName(languageCode, network_name);
			networkNameList.add(s);
			t += 4 + network_name_length;
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		for (NetworkName serviceName : networkNameList) {
			buf.append(serviceName.toString());
		}
		return buf.toString();
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		Utils.addListJTree(t, networkNameList, modus, "network_name_list");
		return t;
	}

	public List<NetworkName> getNetworkNameList() {
		return networkNameList;
	}
}

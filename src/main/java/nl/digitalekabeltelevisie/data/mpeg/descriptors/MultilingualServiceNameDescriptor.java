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

public class MultilingualServiceNameDescriptor extends Descriptor {

	private final List<ServiceName> serviceNameList = new ArrayList<>();


	public static record ServiceName(String iso639LanguageCode, DVBString service_provider_name, DVBString service_name) implements TreeNode{
		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("service_name");
			s.add(new KVP("ISO_639_language_code", iso639LanguageCode));
			s.add(new KVP("service_provider_name", service_provider_name));
			s.add(new KVP("service_name", service_name));
			return s;
		}

	}

	public MultilingualServiceNameDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 2;
		while (t < (descriptorLength + 2)) {
			String languageCode = Utils.getISO8859_1String(b, t, 3);
			int service_provider_name_length = Utils.getInt(b, t + 3, 1, Utils.MASK_8BITS);
			DVBString service_provider_name = new DVBString(b, t + 3);
			int service_name_length = Utils.getInt(b, t + 4 + service_provider_name_length, 1, Utils.MASK_8BITS);
			DVBString service_name = new DVBString(b, t + 4 + service_provider_name_length);
			ServiceName s = new ServiceName(languageCode, service_provider_name, service_name);
			serviceNameList.add(s);
			t += 5 + service_provider_name_length + service_name_length;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (ServiceName serviceName : serviceNameList) {
			buf.append(serviceName.toString());
		}


		return buf.toString();
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		Utils.addListJTree(t, serviceNameList, modus, "service_name_list");
		return t;
	}

	public List<ServiceName> getServiceNameList() {
		return serviceNameList;
	}
}

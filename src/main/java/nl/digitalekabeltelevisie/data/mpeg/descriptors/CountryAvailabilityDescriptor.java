/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2022 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CountryAvailabilityDescriptor extends Descriptor {

	private final int country_availability_flag;

	private final List<Country> countryList = new ArrayList<>();


	public static record Country(String countryCode) implements TreeNode {

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("country");
			s.add(new KVP("country_code", countryCode));
			return s;
		}
	}

	public CountryAvailabilityDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t=0;
		country_availability_flag = getInt(b, 2, 1, 0x80)>>7;

		while (t<(descriptorLength-1)) {
			String languageCode=getISO8859_1String(b, t+3, 3);
			Country s = new Country(languageCode);
			countryList.add(s);
			t+=3;
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		for (Country country : countryList) {
			buf.append(country.toString());
		}


		return buf.toString();
	}

	public static String getCountryAvailabilityFlagString(int flag) {
		return switch (flag) {
			case 0 -> "reception of the service is not intended";
			case 1 -> "reception of the service is intended";
			default -> "Illegal value";
		};
	}

	@Override
	public KVP getJTreeNode(int modus) {

		KVP t = super.getJTreeNode(modus);
		t.add(new KVP("country_availability_flag", country_availability_flag, getCountryAvailabilityFlagString(country_availability_flag)));
		addListJTree(t, countryList, modus, "country_list");
		return t;
	}
}

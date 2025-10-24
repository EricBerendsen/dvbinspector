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

package nl.digitalekabeltelevisie.data.mpeg.descriptors.privatedescriptors.canal_international;

import static nl.digitalekabeltelevisie.util.Utils.MASK_2BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.DVBString;
import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CosTimezoneDescriptor extends Descriptor {
	
	private List<TimezoneName> timezoneNames = new ArrayList<>();
	
	public record TimezoneName(String country_code, int country_region_id, int reserved, DVBString region_name) implements TreeNode {

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("TimezoneName");
			s.add(new KVP("country_code", country_code));
			s.add(new KVP("country_region_id", country_region_id));
			s.add(new KVP("reserved", reserved, null));
			s.add(new KVP("region_name", region_name));
			return s;
		}
	}

	public CosTimezoneDescriptor(byte[] b, TableSection parent) {
		super(b, parent);

		int t = 0;
		while (t < descriptorLength) {
			String countryCode = getISO8859_1String(b, 2 + t, 3);
			int countryRegionId = getInt(b, t + 5, 1, 0xFC) >> 2;
			int reserved = getInt(b, t + 5, 1, MASK_2BITS);
			DVBString regionName = new DVBString(b, t + 6);
			TimezoneName timezoneName = new TimezoneName(countryCode, countryRegionId, reserved, regionName);
			timezoneNames.add(timezoneName);
			t += 5 + regionName.getLength();
		}
	}
	
	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		addListJTree(t,timezoneNames,modus,"Timezone Names");
		return t;
	}


	@Override
	public String getDescriptorname(){
		return "cos_timezone_descriptor";
	}

}

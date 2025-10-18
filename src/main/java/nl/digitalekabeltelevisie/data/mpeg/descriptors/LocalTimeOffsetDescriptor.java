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

import static java.util.Arrays.copyOfRange;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;
import static nl.digitalekabeltelevisie.util.Utils.getUTCFormattedString;

import java.util.ArrayList;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class LocalTimeOffsetDescriptor extends Descriptor {

	private List<LocalTimeOffset> offsetList = new ArrayList<>();


	public static record LocalTimeOffset(String countryCode, int countryRegionId, int localTimeOffsetPolarity, byte[] localTimeOffset, byte[] timeOfChange, byte[] nextTimeOffset) implements TreeNode{

		public String getTimeOfChangeString() {
			return getUTCFormattedString(timeOfChange);
		}

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("time_offset");
			s.add(new KVP("country_code", countryCode));
			s.add(new KVP("country_region_id", countryRegionId));
			s.add(new KVP("local_time_offset_polarity", localTimeOffsetPolarity));
			s.add(new KVP("local_time_offset", localTimeOffset));
			s.add(new KVP("time_of_change", timeOfChange, getTimeOfChangeString()));
			s.add(new KVP("next_time_offset", nextTimeOffset));
			return s;
		}
		
		public String getLocalOffsetString() {
			StringBuilder sb = new StringBuilder();
			if(localTimeOffsetPolarity==0) {
				sb.append("+");
			}else {
				sb.append("-");
			}
			sb.append(Utils.getBCD(localTimeOffset, 0, 2)).
				append(":").
				append(Utils.getBCD(localTimeOffset, 2, 2));
			
			return sb.toString();
		}

		public String getNextTimeOffsetString() {
			StringBuilder sb = new StringBuilder();
			if(localTimeOffsetPolarity==0) {
				sb.append("+");
			}else {
				sb.append("-");
			}
			sb.append(Utils.getBCD(nextTimeOffset, 0, 2)).
				append(":").
				append(Utils.getBCD(nextTimeOffset, 2, 2));
			
			return sb.toString();
		}

	}

	public LocalTimeOffsetDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t = 0;
		while (t < descriptorLength) {
			String countryCode = getISO8859_1String(b, 2 + t, 3);
			int countryRegionId = getInt(b, t + 5, 1, 0xFC) >> 2;
			int localTimeOffsetPolarity = getInt(b, t + 5, 1, 0x01);
			byte[] localTimeOffset = copyOfRange(b, t + 6, t + 8);
			byte[] timeOfChange = copyOfRange(b, t + 8, t + 13);
			byte[] nextTimeOffset = copyOfRange(b, t + 13, t + 15);

			LocalTimeOffset s = new LocalTimeOffset(countryCode, countryRegionId, localTimeOffsetPolarity, localTimeOffset, timeOfChange,
					nextTimeOffset);
			offsetList.add(s);
			t += 13;
		}
	}

	public int getNoServices(){
		return offsetList.size();
	}


	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		for (int i = 0; i < getNoServices(); i++) {
			final LocalTimeOffset s = offsetList.get(i);
			buf.append("(").append(i).append(";").append(s.countryCode()).append(", time of next change").append(s.getTimeOfChangeString());
		}
		return buf.toString();
	}

	@Override
	public KVP getJTreeNode(int modus){
		KVP t = super.getJTreeNode(modus);
		addListJTree(t,offsetList,modus,"time_offset");
		return t;
	}

	public List<LocalTimeOffset> getOffsetList() {
		return offsetList;
	}


}

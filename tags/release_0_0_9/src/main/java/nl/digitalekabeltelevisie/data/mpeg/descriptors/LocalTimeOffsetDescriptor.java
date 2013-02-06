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

import static nl.digitalekabeltelevisie.util.Utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;
import nl.digitalekabeltelevisie.util.Utils;

public class LocalTimeOffsetDescriptor extends Descriptor {

	private List<LocalTimeOffset> offsetList = new ArrayList<LocalTimeOffset>();


	public static class LocalTimeOffset implements TreeNode{

		private final String countryCode ;
		private final int countryRegionId;
		private final int localTimeOffsetPolarity ;
		private final byte[] localTimeOffset ;
		private final byte[] timeOfChange ;
		private final String timeOfChangeString  ;
		private final byte[] nextTimeOffset;

		public LocalTimeOffset(final String c, final int id, final int localPolarity, final byte[] localOffset, final byte[] timeChange, final byte[] nextOffset) {
			countryCode = c;
			countryRegionId = id;
			localTimeOffsetPolarity =localPolarity;
			localTimeOffset =localOffset;
			timeOfChange =timeChange;
			timeOfChangeString = getUTCFormattedString(timeOfChange);
			nextTimeOffset = nextOffset;

		}

		public String getCountryCode() {
			return countryCode;
		}

		public int getCountryRegionId() {
			return countryRegionId;
		}


		public int getLocalTimeOffsetPolarity() {
			return localTimeOffsetPolarity;
		}



		public byte[] getTimeOfChange() {
			return timeOfChange;
		}

		public String getTimeOfChangeString() {
			return timeOfChangeString;
		}

		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("time_offset"));
			s.add(new DefaultMutableTreeNode(new KVP("country_code",countryCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("country_region_id",countryRegionId,null)));
			s.add(new DefaultMutableTreeNode(new KVP("local_time_offset_polarity",localTimeOffsetPolarity,null)));
			s.add(new DefaultMutableTreeNode(new KVP("local_time_offset",localTimeOffset,null)));
			s.add(new DefaultMutableTreeNode(new KVP("time_of_change",timeOfChange,timeOfChangeString)));
			s.add(new DefaultMutableTreeNode(new KVP("next_time_offset",nextTimeOffset,null)));
			return s;
		}

	}

	public LocalTimeOffsetDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String countryCode = getISO8859_1String(b,offset+2+t,3);
			final int countryRegionId = getInt(b, offset+t+5, 1, 0xFC) >>2;
		final int localTimeOffsetPolarity = getInt(b, offset+t+5, 1, 0x01);
		final byte[] localTimeOffset = Utils.copyOfRange(b, offset+t+6, offset+t+8);
		final byte[] timeOfChange = Utils.copyOfRange(b,offset+t+8,offset+t+13 );
		final byte[] nextTimeOffset = Utils.copyOfRange(b, offset+t+13, offset+t+15);

		final LocalTimeOffset s = new LocalTimeOffset(countryCode,countryRegionId,localTimeOffsetPolarity,localTimeOffset,timeOfChange,nextTimeOffset);
		offsetList.add(s);
		t+=13;
		}
	}

	public int getNoServices(){
		return offsetList.size();
	}


	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (int i = 0; i < getNoServices(); i++) {
			final LocalTimeOffset s = offsetList.get(i);
			buf.append("(").append(i).append(";").append(s.getCountryCode()).append(", time of next change").append(s.getTimeOfChangeString());
		}


		return buf.toString();
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,offsetList,modus,"time_offset");
		return t;
	}


}

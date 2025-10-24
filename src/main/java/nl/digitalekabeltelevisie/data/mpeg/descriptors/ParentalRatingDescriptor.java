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

import static nl.digitalekabeltelevisie.util.Utils.MASK_8BITS;
import static nl.digitalekabeltelevisie.util.Utils.addListJTree;
import static nl.digitalekabeltelevisie.util.Utils.getISO8859_1String;
import static nl.digitalekabeltelevisie.util.Utils.getInt;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class ParentalRatingDescriptor extends Descriptor {

	private List<Rating> ratingList = new ArrayList<>();


	public record Rating(String countryCode, int rating) implements TreeNode, Comparable<Rating>{

		@Override
		public KVP getJTreeNode(int modus) {
			KVP s = new KVP("rating");
			s.add(new KVP("country_code", countryCode, null));
			s.add(new KVP("rating", rating, getRatingTypeAge(rating)));
			return s;
		}

		@Override
		public String toString() {
			return getRatingTypeAge(rating) + ", countryCode:" + countryCode;
		}

		@Override
		public int compareTo(Rating o) {
			return  Comparator.comparing(Rating::rating)
					.thenComparing(Rating::countryCode)
					.compare(this, o);
		}

	}

	public ParentalRatingDescriptor(byte[] b, TableSection parent) {
		super(b, parent);
		int t=0;
		while (t<descriptorLength) {
			String countryCode=getISO8859_1String(b, t+2, 3);
			int rating = getInt(b, t+5, 1, MASK_8BITS);
			Rating s = new Rating(countryCode,rating);
			ratingList.add(s);
			t+=4;
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(super.toString());
		for (Rating rating : ratingList) {
			buf.append(rating.toString());
		}


		return buf.toString();
	}

	public static String getRatingTypeAge(int type) {

		if(type==0){
			return "undefined";
		}else if((1<=type)&&(type<=0x0F)){
			return "minimum age = "+ Integer.toString((type+3));
		}else{
			return "defined by the broadcaster";
		}
	}

	@Override
	public KVP getJTreeNode(int modus){

		KVP t = super.getJTreeNode(modus);
		addListJTree(t,ratingList,modus,"rating_list");
		return t;
	}

	public List<Rating> getRatingList() {
		return ratingList;
	}
}

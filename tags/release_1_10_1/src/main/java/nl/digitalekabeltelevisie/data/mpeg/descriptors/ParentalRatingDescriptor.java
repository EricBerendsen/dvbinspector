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

public class ParentalRatingDescriptor extends Descriptor {

	private List<Rating> ratingList = new ArrayList<Rating>();


	public static class Rating implements TreeNode{
		/**
		 *
		 */

		private final String countryCode;
		private final int rating;



		public Rating(final String countryCode, final int rating) {
			super();
			this.countryCode = countryCode;
			this.rating = rating;
		}



		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("rating"));
			s.add(new DefaultMutableTreeNode(new KVP("country_code",countryCode,null)));
			s.add(new DefaultMutableTreeNode(new KVP("rating",rating,getRatingTypeAge(rating))));
			return s;
		}



		@Override
		public String toString(){
			return "countryCode:'"+countryCode;
		}



		public int getRating() {
			return rating;
		}



		public String getCountryCode() {
			return countryCode;
		}


	}

	public ParentalRatingDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		while (t<descriptorLength) {
			final String countryCode=getISO8859_1String(b, offset+t+2, 3);
			final int rating = getInt(b, offset+t+5, 1, MASK_8BITS);
			final Rating s = new Rating(countryCode,rating);
			ratingList.add(s);
			t+=4;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (Rating rating : ratingList) {
			buf.append(rating.toString());
		}


		return buf.toString();
	}

	public static String getRatingTypeAge(final int type) {

		if(type==0){
			return "undefined";
		}else if((1<=type)&&(type<=0x0F)){
			return "minimum age = "+ Integer.toString((type+3));
		}else{
			return "defined by the broadcaster";
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		addListJTree(t,ratingList,modus,"rating_list");
		return t;
	}

	public List<Rating> getRatingList() {
		return ratingList;
	}
}

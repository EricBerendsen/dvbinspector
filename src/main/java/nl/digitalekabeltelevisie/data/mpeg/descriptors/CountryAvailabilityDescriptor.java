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
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.digitalekabeltelevisie.controller.KVP;
import nl.digitalekabeltelevisie.controller.TreeNode;
import nl.digitalekabeltelevisie.data.mpeg.psi.TableSection;

public class CountryAvailabilityDescriptor extends Descriptor {

	private int country_availability_flag = 0;

	private final List<Country> countryList = new ArrayList<Country>();


	public static class Country implements TreeNode{
		/**
		 * 
		 */
		private String countryCode;


		public Country(final String lCode){
			countryCode = lCode;
		}


		public DefaultMutableTreeNode getJTreeNode(final int modus){
			final DefaultMutableTreeNode s=new DefaultMutableTreeNode(new KVP("country"));
			s.add(new DefaultMutableTreeNode(new KVP("country_code",countryCode,null)));
			return s;
		}




		public String getCountryCode() {
			return countryCode;
		}


		public void setCountryCode(final String iso639LanguageCode) {
			this.countryCode = iso639LanguageCode;
		}

		@Override
		public String toString(){
			return "code:'"+countryCode;
		}


	}

	public CountryAvailabilityDescriptor(final byte[] b, final int offset, final TableSection parent) {
		super(b, offset,parent);
		int t=0;
		country_availability_flag = getInt(b, offset+2, 1, 0x80)>>7;

		while (t<(descriptorLength-1)) {
			final String languageCode=getISO8859_1String(b, offset+t+3, 3);
			final Country s = new Country(languageCode);
			countryList.add(s);
			t+=3;
		}
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder(super.toString());
		for (final Iterator<Country> iter = countryList.iterator(); iter.hasNext();) {
			buf.append(iter.next().toString());
		}


		return buf.toString();
	}

	public static String getCountryAvailabilityFlagString(final int flag) {
		switch (flag) {
		case 0: return "reception of the service is not intended";
		case 1: return "reception of the service is intended";
		default: return "Illegal value";
		}
	}

	@Override
	public DefaultMutableTreeNode getJTreeNode(final int modus){

		final DefaultMutableTreeNode t = super.getJTreeNode(modus);
		t.add(new DefaultMutableTreeNode(new KVP("country_availability_flag",country_availability_flag,getCountryAvailabilityFlagString(country_availability_flag))));
		addListJTree(t,countryList,modus,"country_list");
		return t;
	}
}

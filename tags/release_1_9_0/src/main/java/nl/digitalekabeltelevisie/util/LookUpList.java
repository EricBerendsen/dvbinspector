/**
 *
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 *
 *  This code is Copyright 2009-2014 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

package nl.digitalekabeltelevisie.util;

/**
 * @author Eric
 *
 */
public class LookUpList {

	RangeHashMap<Integer,String> table;

	public static class Builder{
		RangeHashMap<Integer,String> table;

		public Builder(){
			table = new RangeHashMap<Integer,String>();
		}

		public Builder add(int i, String s){
			table.put(i, i, s);
			return this;
		}

		public Builder add(int i, int j, String s){
			table.put(i, j, s);
			return this;
		}

		public LookUpList build(){
			return new LookUpList(this);
		}

		public RangeHashMap<Integer, String> getTable() {
			return table;
		}

	}

	private LookUpList(Builder builder){
		table = builder.getTable();
	}

	public String get(int i){
		return table.find(i);
	}
}

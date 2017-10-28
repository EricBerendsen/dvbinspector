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
package nl.digitalekabeltelevisie.util;

import java.util.TreeMap;

/**
 * @author Eric Berendsen
 *
 */
public class RangeHashMap<K extends Comparable<?>,V> {

	public class Entry{
		private K lower;
		private K upper;
		private V value;

		public Entry(final K lower, final K upper, final V value) {
			super();
			this.lower = lower;
			this.upper = upper;
			this.value = value;
		}

		public K getUpper() {
			return upper;
		}

		public V getValue() {
			return value;
		}

		public K getLower() {
			return lower;
		}

		public void setLower(K lower) {
			this.lower = lower;
		}

		public void setUpper(K upper) {
			this.upper = upper;
		}

		public void setValue(V value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "Entry [lower=" + lower + ", upper=" + upper + ", value=" + value + "]";
		}
	}

	private final TreeMap<K, Entry> table= new TreeMap<K, Entry>();

	public void put(final K start, final K end, final V value){
		final Entry e = new Entry(start,end,value);
		put(start, e);
	}

	@SuppressWarnings("unchecked")
	public V find(final K k){
		Entry e = findEntry(k);
		if((e!=null)&&(((Comparable<? super K>)e.getUpper()).compareTo(k)>=0)){
			return e.getValue();
		}
		return null;
	}

	public Entry findEntry(final K k) {
		Entry e = null;
		final K lowkey = table.floorKey(k);
		if(lowkey!=null){
			e = table.get(lowkey);
		}
		return e;
	}

	public void put(K start, Entry entry) {
		// TODO check on overlap with existing entries
		table.put(start, entry);
	}


}

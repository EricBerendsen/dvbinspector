package nl.digitalekabeltelevisie.util.tablemodel;

/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2021 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

import java.util.*;

public class TableHeader<E,R> {
	
	private final List<ColumnDetails<?>> header;
	private final Map<String, ColumnDetails<?>> map;
	

	public TableHeader(List<ColumnDetails<?>> header, Map<String, ColumnDetails<?>> map) {
		super();
		this.header = header;
		this.map = map;
	}


	public void flagUsed(String key) {
		ColumnDetails<?> cd = map.get(key);
		if(cd!=null) {
			cd.setUsed(true);
		}
		
	}

	public List<ColumnDetails<?>> getHeader() {
		return header;
	}

	public Map<String, ColumnDetails<?>> getMap() {
		return map;
	}

	public boolean isRepeatingColumn(String keyBase) {
		ColumnDetails<?> cd = map.get(keyBase);
		if (cd != null) {
			return cd.isList();
		}
		return false;
	}

	public void countOrdinal(String keyBase, int keyOrd) {
		ColumnDetails<?> cd = map.get(keyBase);
		if(cd!=null) {
			cd.setUsed(true);
			cd.setListMax(Integer.max(cd.getListMax(), keyOrd));
		}
		
		
	}

}

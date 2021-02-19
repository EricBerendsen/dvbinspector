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
import java.util.function.Function;



public class TableHeaderBuilder<E,R> {
	private final List<ColumnDetails<?>> header;
	private final Map<String, ColumnDetails<?>> map;
	
	private long keyBase = 0L;
	
	private String nextKey() {
		keyBase++;
		return Long.toHexString(keyBase);
	}

	public TableHeaderBuilder(){
		header = new ArrayList<>();
		map = new HashMap<>();
	}

	public TableHeaderBuilder<E,R> addRequiredBaseColumn(String name, Function<E, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<E> bcd = new ColumnDetails<E>(name, key, fun, null, type, true, false,null,true);
		header.add(bcd);
		map.put(key, bcd);
		return this;
	}

	public TableHeaderBuilder<E,R> addOptionalBaseColumn(String name, Function<E, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<E> bcd = new ColumnDetails<E>(name, key, fun, null, type, false, false,null,true);
		header.add(bcd);
		map.put(key, bcd);
		return this;
	}

	public TableHeaderBuilder<E,R> addRequiredRowColumn(String name, Function<R, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<R> bcd = new ColumnDetails<R>(name, key, fun, null, type, true, false,null,false);
		header.add(bcd);
		map.put(key, bcd);
		return this;
	}

	public TableHeaderBuilder<E,R> addOptionalRowColumn(String name, Function<R, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, key, fun, null, type, false, false,null,false);
		header.add(cd);
		map.put(key, cd);
		return this;
	}

	public TableHeaderBuilder<E,R> addOptionalRepeatingRowColumn(String name, Function<R, List<Object>> listFun, Class<?> type) {
		String keyBase = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, keyBase, null, listFun, type, false, true,null,false);
		header.add(cd);
		map.put(keyBase, cd);
		return this;
	}

	public TableHeaderBuilder<E,R> addOptionalRepeatingGroupedColumn(String name, Function<R, List<Object>> listFun, Class<?> type, String groupId) {
		String keyBase = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, keyBase, null, listFun, type, false, true,groupId,false);
		header.add(cd);
		map.put(keyBase, cd);
		return this;
	}

	public TableHeader<E,R> build() {
		return new TableHeader<E,R>(header, map);
	}
}
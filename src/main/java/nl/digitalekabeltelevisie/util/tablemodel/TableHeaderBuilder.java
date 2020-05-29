package nl.digitalekabeltelevisie.util.tablemodel;

import java.util.*;
import java.util.function.Function;



public class TableHeaderBuilder<E,R> {
	private List<ColumnDetails<?>> header;
	private Map<String, ColumnDetails<?>> map;
	
	private long keyBase = 0l;
	
	private String nextKey() {
		keyBase++;
		return Long.toHexString(keyBase);
	}

	public TableHeaderBuilder(){
		header = new ArrayList<>();
		map = new HashMap<>();
	}

	
	@Deprecated
	public TableHeaderBuilder<E,R> addRequiredBaseColumn(String name, String key1, Function<E, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<E> bcd = new ColumnDetails<E>(name, key, fun, null, type, true, false,null,true);
		header.add(bcd);
		map.put(key, bcd);
		return this;
	}

	public TableHeaderBuilder<E,R> addRequiredBaseColumn(String name, Function<E, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<E> bcd = new ColumnDetails<E>(name, key, fun, null, type, true, false,null,true);
		header.add(bcd);
		map.put(key, bcd);
		return this;
	}

	@Deprecated
	public TableHeaderBuilder<E,R> addOptionalBaseColumn(String name, String key1, Function<E, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<E> bcd = new ColumnDetails<E>(name, key, fun, null, type, false, false,null,true);
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

	@Deprecated
	public TableHeaderBuilder<E,R> addRequiredRowColumn(String name, String key1, Function<R, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<R> bcd = new ColumnDetails<R>(name, key, fun, null, type, true, false,null,false);
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
	
	@Deprecated
	public TableHeaderBuilder<E,R> addOptionalRowColumn(String name, String key1, Function<R, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, key, fun, null, type, false, false,null,false);
		header.add(cd);
		map.put(key, cd);
		return this;
	}

	public TableHeaderBuilder<E,R> addOptionalRowColumn(String name, Function<R, Object> fun, Class<?> type) {
		String key = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, key, fun, null, type, false, false,null,false);
		header.add(cd);
		map.put(key, cd);
		return this;
	}

	@Deprecated
	public TableHeaderBuilder<E,R> addOptionalRepeatingRowColumn(String name, String keyBase1, Function<R, List<Object>> listFun, Class<?> type) {
		String keyBase = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, keyBase, null, listFun, type, false, true,null,false);
		header.add(cd);
		map.put(keyBase, cd);
		return this;
	}

	public TableHeaderBuilder<E,R> addOptionalRepeatingRowColumn(String name, Function<R, List<Object>> listFun, Class<?> type) {
		String keyBase = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, keyBase, null, listFun, type, false, true,null,false);
		header.add(cd);
		map.put(keyBase, cd);
		return this;
	}
	
	@Deprecated
	public TableHeaderBuilder<E,R> addOptionalRepeatingGroupedColumn(String name, String keyBase1, Function<R, List<Object>> listFun, Class<?> type, String groupId) {
		String keyBase = nextKey();
		ColumnDetails<R> cd = new ColumnDetails<R>(name, keyBase, null, listFun, type, false, true,groupId,false);
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
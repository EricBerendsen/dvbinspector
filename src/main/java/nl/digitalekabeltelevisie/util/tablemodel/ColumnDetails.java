package nl.digitalekabeltelevisie.util.tablemodel;

import java.util.List;
import java.util.function.Function;

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

class ColumnDetails<E>{

	private String name;
	private String key;
	private final Function<E, Object> function;
	private final Function<E, List<Object>> listFunction;
	private Class<?> dataClass;
	boolean isList;
	boolean required;
	String groupId;

	boolean used;
	int listMax;
	
	private final boolean isBaseColumn;
	
	public ColumnDetails(String name, String key, Function<E, Object> function, Function<E, List<Object>> listFunction,Class<?> dataClass, boolean required,boolean isList, String groupId, boolean isBaseColumn) {
		super();
		this.name = name;
		this.key = key;
		this.function = function;
		this.listFunction = listFunction;
		this.dataClass = dataClass;
		this.required = required;
		this.isList = isList;
		this.groupId = groupId;
		this.used = false;
		this.listMax = 0;
		this.isBaseColumn = isBaseColumn;
	}
	

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Class<?> getDataClass() {
		return dataClass;
	}
	public void setDataClass(Class<?> dataClass) {
		this.dataClass = dataClass;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	public boolean isUsed() {
		return used;
	}


	public void setUsed(boolean used) {
		this.used = used;
	}


	public boolean isList() {
		return isList;
	}


	public void setList(boolean isList) {
		this.isList = isList;
	}


	public int getListMax() {
		return listMax;
	}


	public void setListMax(int listMax) {
		this.listMax = listMax;
	}


	public String getGroupId() {
		return groupId;
	}


	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


	public Function<E, Object> getFunction() {
		return function;
	}


	public boolean isBaseColumn() {
		return isBaseColumn;
	}


	public Function<E, List<Object>> getListFunction() {
		return listFunction;
	}
}
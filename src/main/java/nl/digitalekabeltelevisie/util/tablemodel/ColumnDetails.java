package nl.digitalekabeltelevisie.util.tablemodel;

/**
 * 
 *  http://www.digitalekabeltelevisie.nl/dvb_inspector
 * 
 *  This code is Copyright 2009-2020 by Eric Berendsen (e_berendsen@digitalekabeltelevisie.nl)
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

class ColumnDetails{

	private String name;
	private String key;
	private Class<?> dataClass;
	boolean isList;
	boolean required;
	boolean used;
	int listMax;
	
	public ColumnDetails(String name, String key, Class<?> dataClass, boolean required,boolean isList) {
		super();
		this.name = name;
		this.key = key;
		this.dataClass = dataClass;
		this.required = required;
		this.isList = isList;
		this.used = false;
		this.listMax = 0;
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
}
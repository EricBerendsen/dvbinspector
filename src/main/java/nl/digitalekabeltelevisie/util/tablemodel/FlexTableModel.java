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

import java.util.*;

import javax.swing.table.AbstractTableModel;

public class FlexTableModel extends AbstractTableModel {
	
	private static final String REPEATING_KEY_SEPARATOR = ":";
	private List<Map<String, Object>> model = new ArrayList<>();
	private TableHeader tableHeader;
	private List<String> displayableColumns = new ArrayList<String>();
	
	public FlexTableModel(TableHeader tableHeader) {
		this.tableHeader = tableHeader;
		
	}
	
	public void addRowData(Map<String, Object> rowData) {
		model.add(rowData);
	}
	
	public void addRowData(List<Map<String, Object>> rowData) {
		model.addAll(rowData);
	}

	public void process() {
		for (Map<String, Object> map : model) {
			for (String key : map.keySet()) {
				if(isRepeatingKey(key)) {
					String keyBase = getBase(key);
					int keyOrd = getOrdinal(key);
					if(tableHeader.isRepeatingColumn(keyBase)) {
						tableHeader.countOrdinal(keyBase,keyOrd);
					}
				}else if(map.get(key)!=null) {
					tableHeader.flagUsed(key);
				}
			}
		}
		
		for(ColumnDetails column:tableHeader.getHeader()) {
			if(column.isUsed()||column.isRequired()) {
				if(column.isList()) {
					String baseKey = column.getKey();
					for (int i = 0; i <= column.getListMax(); i++) {
						displayableColumns.add(baseKey + REPEATING_KEY_SEPARATOR + i);
					}
					
				}else {
					displayableColumns.add(column.getKey());
				}
			}
		}
	}
	

	private static int getOrdinal(String key) {
		int i = key.indexOf(REPEATING_KEY_SEPARATOR);
		return Integer.parseInt(key.substring(i+1));
	}

	private static String getBase(String key) {
		int i = key.indexOf(REPEATING_KEY_SEPARATOR);
		if(i==-1) {
			return key;
		}
		return key.substring(0,i);
	}

	private static boolean isRepeatingKey(String key) {
		return key.contains(REPEATING_KEY_SEPARATOR);
	}

	@Override
	public int getRowCount() {
		return model.size();
	}

	@Override
	public int getColumnCount() {
		return displayableColumns.size();
	}

    @Override
	public Class<?> getColumnClass(int columnIndex) {
        ColumnDetails columnDetails = tableHeader.getMap().get(getBaseKey(columnIndex));
		return columnDetails.getDataClass();
    }
    
	private Object getBaseKey(int columnIndex) {
		return getBase(displayableColumns.get(columnIndex));
	}

	@Override
	public String getColumnName(int columnIndex) {
		String key = displayableColumns.get(columnIndex);
		ColumnDetails columnDetails = tableHeader.getMap().get(getBase(key));
		if (columnDetails.isList()) {
			String baseName = columnDetails.getName();
			return baseName + " " + getOrdinal(key);
		}
		return columnDetails.getName();
	}
		
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return model.get(rowIndex).get(displayableColumns.get(columnIndex));
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		 return false;
	 }

}

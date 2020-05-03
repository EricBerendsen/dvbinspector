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
				if(map.get(key)!=null) {
					tableHeader.flagUsed(key);
				}
			}
		}
		
		for(ColumnDetails column:tableHeader.getHeader()) {
			if(column.isUsed()||column.isRequired()) {
				displayableColumns.add(column.getKey());
			}
		}
	}
	

	@Override
	public int getRowCount() {
		return model.size();
	}

	@Override
	public int getColumnCount() {
		return displayableColumns.size();
	}

    public Class<?> getColumnClass(int columnIndex) {
        return tableHeader.getMap().get(displayableColumns.get(columnIndex)).getDataClass();
    }
    
	@Override
	public String getColumnName(int columnIndex) {
		 return tableHeader.getMap().get(displayableColumns.get(columnIndex)).getName();
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

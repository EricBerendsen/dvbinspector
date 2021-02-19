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

import javax.swing.table.AbstractTableModel;

public class FlexTableModel<E,R> extends AbstractTableModel {
	
	private static final String REPEATING_KEY_SEPARATOR = ":";
	private final List<Map<String, Object>> model = new ArrayList<>();
	private final TableHeader<E,R> tableHeader;
	private final List<String> displayableColumns = new ArrayList<>();
	
	public FlexTableModel(TableHeader<E,R> tableHeader) {
		this.tableHeader = tableHeader;
		
	}
	
	public void addData(E entity, List<R> rows) {
		for (R row : rows) {
			if (row != null) {
				Map<String, Object> rowData = new HashMap<>();
				List<ColumnDetails<?>> columns = tableHeader.getHeader();
				for (ColumnDetails<?> column : columns) {
					if (column.isBaseColumn()) {
						@SuppressWarnings("unchecked")
						Function<E, Object> fun = (Function<E, Object>) column.getFunction();
						Object r = fun.apply(entity);
						rowData.put(column.getKey(), r);
					} else if (!column.isList()){
						@SuppressWarnings("unchecked")
						Function<R, Object> fun = (Function<R, Object>) column.getFunction();
						Object r = fun.apply(row);
						rowData.put(column.getKey(), r);
					} else { // repeating column , maybe grouped
						@SuppressWarnings("unchecked")
						Function<R, List<Object>> listFun = (Function<R, List<Object>>) column.getListFunction();
						List<Object> listValues = listFun.apply(row);
						if(listValues!=null) {
							int t=0;
							for(Object val:listValues) {
								rowData.put(column.getKey()+REPEATING_KEY_SEPARATOR+t, val);
								t++;
							}
						}
						
					}

				}
				model.add(rowData);
			}
		}
	}
	
	public void process() {
		determineUsedColumns();
		buildDisplayableColumnsList();
	}

	void buildDisplayableColumnsList() {
		List<ColumnDetails<?>> header = tableHeader.getHeader();
		int headerIndex = 0;
		while (headerIndex < header.size()) {
			ColumnDetails<?> column = header.get(headerIndex);
			if(column.isUsed()||column.isRequired()) {
				if(column.isList()) {
					if(column.getGroupId()==null) { //Repeating column
						addRepeatingColumn(column);
					}else { 
						headerIndex = addRepeatingColumnGroup(header, headerIndex, column);
					}
				}else { 
					addSimpleColumn(column);
				}
			}
			headerIndex++;
		}
	}

	int addRepeatingColumnGroup(List<ColumnDetails<?>> header, int headerIndexStart, ColumnDetails<?> column) {
		int headerIndex = headerIndexStart;
		List<ColumnDetails<?>> groupList = new ArrayList<>();
		int iterCount = column.getListMax();
		groupList.add(column);
		
		while(((headerIndex+1) < header.size()) 
			&& (header.get(headerIndex+1).isList() )
			&& (column.getGroupId().equals(header.get(headerIndex+1).getGroupId()))){
			ColumnDetails<?> nextCol = header.get(headerIndex+1);
			iterCount = Integer.max(iterCount, nextCol.getListMax());
			groupList.add(nextCol);
			headerIndex++;
		}
		// create columns
		for (int i = 0; i <= iterCount; i++) {
			for(ColumnDetails<?> groupedColumn:groupList) {
				String baseKey = groupedColumn.getKey();
				displayableColumns.add(baseKey + REPEATING_KEY_SEPARATOR + i);
			}
		}
		return headerIndex;
	}

	void addSimpleColumn(ColumnDetails<?> column) {
		displayableColumns.add(column.getKey());
	}

	void addRepeatingColumn(ColumnDetails<?> column) {
		String baseKey = column.getKey();
		for (int i = 0; i <= column.getListMax(); i++) {
			displayableColumns.add(baseKey + REPEATING_KEY_SEPARATOR + i);
		}
	}

	void determineUsedColumns() {
		for (Map<String, Object> row : model) {
			for (String key : row.keySet()) {
				if(isRepeatingKey(key)) {
					String keyBase = getBase(key);
					int keyOrd = getOrdinal(key);
					if(tableHeader.isRepeatingColumn(keyBase)) {
						tableHeader.countOrdinal(keyBase,keyOrd);
					}
				}else if(row.get(key)!=null) {
					tableHeader.flagUsed(key);
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
        ColumnDetails<?> columnDetails = tableHeader.getMap().get(getBaseKey(columnIndex));
		return columnDetails.getDataClass();
    }
    
	private String getBaseKey(int columnIndex) {
		return getBase(displayableColumns.get(columnIndex));
	}

	@Override
	public String getColumnName(int columnIndex) {
		String key = displayableColumns.get(columnIndex);
		ColumnDetails<?> columnDetails = tableHeader.getMap().get(getBase(key));
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

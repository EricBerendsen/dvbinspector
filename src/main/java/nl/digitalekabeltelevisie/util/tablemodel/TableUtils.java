package nl.digitalekabeltelevisie.util.tablemodel;

import java.util.*;

import javax.swing.table.TableModel;

import nl.digitalekabeltelevisie.data.mpeg.descriptors.Descriptor;

public class TableUtils {

	public static HashMap<String, Object> getDescriptorTableData(List<Descriptor> descriptorList2) {
		HashMap<String, Object> descriptorTableData = new HashMap<String, Object>();
		for(Descriptor descriptor:descriptorList2) {
			if(descriptor instanceof TableRowSource) {
				descriptorTableData.putAll(((TableRowSource)descriptor).getTableRowData());
			}
		}
		return descriptorTableData;
	}

	public static TableModel getTableModel(TableHeaderSource headerSource, TableDataSource dataSource) {
		FlexTableModel tableModel =  new FlexTableModel(headerSource.getTableHeader());
		
		List<Map<String, Object>> rowData = dataSource.getTableData();
		tableModel.addRowData(rowData);
		
		tableModel.process();
		return tableModel;
	
		
	}

}

package nl.digitalekabeltelevisie.util.tablemodel;

import java.util.*;

@FunctionalInterface
public interface TableDataSource {
	List<Map<String, Object>> getTableData();
}

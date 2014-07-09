package webit.generator.core.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.model.Table;
import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class TableFactory {

    private static TableFactory _instance;
    private static List<Table> _tables;
    private static Map<String, Table> _tableMap;

    public abstract List<Table> collectTables();

    public static TableFactory instance() {
        TableFactory instance = _instance;
        if (instance == null) {
            instance = _instance = (TableFactory) ResourceUtil.loadComponent("tableFactory");
        }
        return instance;
    }

    public static List<Table> getTables() {
        List<Table> tables = _tables;
        if (tables == null) {
            tables = _tables = instance().collectTables();
        }
        return tables;
    }

    public static Map<String, Table> getTableMap() {
        Map<String, Table> tableMap = _tableMap;
        if (tableMap == null) {
            List<Table> tables = getTables();
            tableMap = _tableMap = new HashMap<String, Table>();
            for (Table table : tables) {
                tableMap.put(table.entity, table);
            }
            _tableMap = tableMap;
        }
        return tableMap;
    }

    public static Table getTable(String entity) {
        return getTableMap().get(entity);
    }

}

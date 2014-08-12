package webit.generator.core.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.util.dbaccess.DatabaseAccesser;
import webit.generator.core.util.dbaccess.model.TableRaw;
import webit.generator.core.model.Table;
import webit.generator.core.util.Logger;
import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class TableFactory {

    private static TableFactory _instance;
    private static List<Table> _tables;
    private static Map<String, Table> _tableMap;

    protected abstract Table createTable(TableRaw tableRaw);

    public List<Table> collectTables() {

        final List<Table> tableList;
        {
            final Map<String, Table> tableMaps = _tableMap = new HashMap<String, Table>();
            for (Map.Entry<String, TableRaw> entry : DatabaseAccesser.getInstance().getAllTables().entrySet()) {
                TableRaw raw = entry.getValue();
                Table table = createTable(raw);
                if (table != null) {
                    tableMaps.put(table.entity, table);
                } else {
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Skip table (by TableFactory): " + raw);
                    }
                }
            }

            tableList = new ArrayList<Table>(tableMaps.values());
            //tables init
            for (Table table : tableList) {
                table.init();
            }
        }
        //table list sort
        Collections.sort(tableList);
        if (Logger.isInfoEnabled()) {
            for (Table table : tableList) {
                Logger.info("Loaded table: " + table.getSqlName() + "  " + table.getRemark());
            }
        }

        return tableList;
    }

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
            getTables();
            return getTableMap();
        }
        return tableMap;
    }

    public static Table getTable(String entity) {
        return getTableMap().get(entity);
    }

}

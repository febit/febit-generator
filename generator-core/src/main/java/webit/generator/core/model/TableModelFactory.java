package webit.generator.core.model;

import java.util.List;
import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class TableModelFactory {

    private static TableModelFactory _instance;
    private static List<TableModel> _tables;

    public abstract List<TableModel> collectTables();

    //XXX: 线程安全
    public static List<TableModel> collectTablesIfAbsent() {
        List<TableModel> tables = _tables;
        if (tables == null) {
            TableModelFactory instance = _instance;
            if (instance == null) {
                instance = _instance = (TableModelFactory) ResourceUtil.loadComponent("tableModelFactory");
            }
            tables = _tables = instance.collectTables();
        }

        return tables;
    }
}

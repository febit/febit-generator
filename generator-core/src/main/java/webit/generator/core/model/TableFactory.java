package webit.generator.core.model;

import java.util.List;
import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class TableFactory {

    private static TableFactory _instance;
    private static List<Table> _tables;

    public abstract List<Table> collectTables();

    //XXX: 线程安全
    public static List<Table> collectTablesIfAbsent() {
        List<Table> tables = _tables;
        if (tables == null) {
            TableFactory instance = _instance;
            if (instance == null) {
                instance = _instance = (TableFactory) ResourceUtil.loadComponent("tableFactory");
            }
            tables = _tables = instance.collectTables();
        }

        return tables;
    }
}

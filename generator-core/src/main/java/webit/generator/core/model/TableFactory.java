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
}

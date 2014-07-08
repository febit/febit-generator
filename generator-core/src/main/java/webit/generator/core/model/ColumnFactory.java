package webit.generator.core.model;

import java.util.Map;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.util.ResourceUtil;

/**
 *
 * @author Zqq
 */
public abstract class ColumnFactory {

    private static ColumnFactory _instance;

    public abstract Column createColumn(ColumnRaw raw, Table table, Map<String, Object> settings);

    public static Column create(ColumnRaw raw, Table table, Map<String, Object> settings) {
        ColumnFactory instance = _instance;
        if (instance == null) {
            instance = _instance = (ColumnFactory) ResourceUtil.loadComponent("columnFactory");
        }
        return instance.createColumn(raw, table, settings);
    }
}

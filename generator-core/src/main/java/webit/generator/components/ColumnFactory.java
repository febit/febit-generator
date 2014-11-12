package webit.generator.components;

import java.util.HashMap;
import java.util.Map;
import webit.generator.model.Column;
import webit.generator.model.Table;
import webit.generator.util.ResourceUtil;
import webit.generator.util.dbaccess.ColumnRaw;

/**
 *
 * @author Zqq
 */
public abstract class ColumnFactory {

    private static ColumnFactory _instance;
    private static final Map<ColumnRaw, Column> columnLinkRawMap = new HashMap<ColumnRaw, Column>();

    protected abstract Column createColumn(ColumnRaw raw, Table table);

    public static ColumnFactory instance() {
        ColumnFactory instance = _instance;
        if (instance == null) {
            instance = _instance = (ColumnFactory) ResourceUtil.loadComponent("columnFactory");
        }
        return instance;
    }

    public static Column getColumn(ColumnRaw raw) {
        return columnLinkRawMap.get(raw);
    }

    public static Column create(ColumnRaw raw, Table table) {
        Column column = instance().createColumn(raw, table);
        if (column != null) {
            columnLinkRawMap.put(raw, column);
        }
        return column;
    }
}

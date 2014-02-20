// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.dbaccess.DatabaseAccesser;
import webit.generator.core.dbaccess.model.Column;
import webit.generator.core.dbaccess.model.Table;
import webit.generator.core.util.Logger;
import webit.generator.core.util.ResourceUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class ConfigInit {

    protected void initColumnsConfig() {
        final Map<String, Table> tables = DatabaseAccesser.getInstance().getAllTables();
        Map<String, Map<String, Map<String, Object>>> tableColumn;
        Map<String, Map<String, Map<String, Object>>> tableColumn_new = new HashMap<String, Map<String, Map<String, Object>>>();

        tableColumn = ResourceUtil.loadTableColumns();
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            Table table = entry.getValue();

            Map<String, Map<String, Object>> columnMaps = tableColumn != null ? tableColumn.get(table.name) : null;
            Map<String, Map<String, Object>> columnMaps_new = new HashMap<String, Map<String, Object>>();
            tableColumn_new.put(table.name, columnMaps_new);

            List<Column> columns = table.getColumns();
            for (Column column : columns) {

                Map<String, Object> columnMap = columnMaps != null ? columnMaps.get(column.name) : null;

                if (columnMap == null) {
                    columnMap = new HashMap<String, Object>();
                    columnMap.put("query", false);
                }

                columnMaps_new.put(column.name, columnMap);
            }
        }
        ResourceUtil.saveTableColumns(tableColumn_new, tables);
    }

    public void process() throws IOException {

        initColumnsConfig();

        //ConfigInitProcesser
        List<String> processersClass = StringUtil.toUnBlankList(Config.getString("configInit"));
        if (processersClass != null && processersClass.size() != 0) {
            try {
                for (String item : processersClass) {
                    ((ConfigInitProcesser) ResourceUtil.loadClass(item).newInstance()).process(this);
                }
            } catch (Exception ex) {
                Logger.error("Exception: ", ex);
                throw new RuntimeException(ex);
            }
        }

    }
}

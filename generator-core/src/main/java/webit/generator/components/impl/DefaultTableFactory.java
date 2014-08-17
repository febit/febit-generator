package webit.generator.components.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import webit.generator.Config;
import webit.generator.components.ColumnFactory;
import webit.generator.components.TableFactory;
import webit.generator.components.TableNaming;
import webit.generator.model.Column;
import webit.generator.model.Table;
import webit.generator.util.Logger;
import webit.generator.util.dbaccess.model.ColumnRaw;
import webit.generator.util.dbaccess.model.TableRaw;

/**
 *
 * @author Zqq
 */
public class DefaultTableFactory extends TableFactory {

    private final Set<String> blackEntitys = new HashSet<String>();

    private boolean inited = false;

    public void init() {
        if (inited == true) {
            return;
        }
        inited = true;

        //加载表黑名单
        blackEntitys.addAll(Arrays.asList(Config.getArrayWithoutComment("blackEntitys")));
    }

    @Override
    protected Table createTable(final TableRaw tableRaw) {
        init();
        final String entity;
        final String sqlName;
        final String remark;
        final ArrayList<Column> columns;
        final ArrayList<Column> idColumns;
        final Map<String, Column> columnMap;
        final String modelType;
        final String modelSimpleType;
        final boolean blackEntity;
        final Map<String, Object> tableSettings;

        final TableNaming tableNaming = TableNaming.instance();

        remark = tableNaming.remark(tableRaw.remarks);
        columns = new ArrayList<Column>();
        idColumns = new ArrayList<Column>();
        columnMap = new HashMap<String, Column>();

        sqlName = tableNaming.sqlName(tableRaw.name);
        entity = tableNaming.entity(sqlName);

        blackEntity = blackEntitys.contains(entity);
        modelSimpleType = tableNaming.modelSimpleType(entity);
        modelType = tableNaming.modelType(modelSimpleType);
        tableSettings = Config.getTableSettings(entity);
        final Table table = new Table(entity, sqlName, remark, tableSettings, columnMap, columns, idColumns, modelType, modelSimpleType, blackEntity);
        {
            //XXX: tableSettings.get("id")
            for (ColumnRaw columnRaw : tableRaw.getColumns()) {
                Column cm = ColumnFactory.create(columnRaw, table);
                if (cm == null) {
                    if (Logger.isDebugEnabled()) {
                        Logger.debug("Skip column (by ColumnFactory): " + columnRaw);
                    }
                    continue;
                }
                if (cm.getIspk()) {
                    idColumns.add(cm);
                }
                columnMap.put(cm.getVarName(), cm);
            }
            columns.addAll(columnMap.values());
            idColumns.trimToSize();
            columns.trimToSize();
            Collections.sort(columns);
        }

        return table;
    }
}

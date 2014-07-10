package webit.generator.core.components.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import webit.generator.core.Config;
import webit.generator.core.components.ColumnFactory;
import webit.generator.core.components.TableFactory;
import webit.generator.core.components.TableNaming;
import webit.generator.core.dbaccess.DatabaseAccesser;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.dbaccess.model.TableRaw;
import webit.generator.core.model.Column;
import webit.generator.core.model.Table;
import webit.generator.core.util.Logger;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class DefaultTableFactory extends TableFactory {

    private final Set<String> blackEntitys = new HashSet<String>();

    private Pattern includes;
    private Pattern excludes;

    private boolean inited = false;

    public void init() {
        if (inited == true) {
            return;
        }
        inited = true;

        //加载表黑名单
        final List<String> tableBlacks = StringUtil.toUnBlankList(Config.getString("blackEntitys"));
        if (tableBlacks != null) {
            blackEntitys.addAll(tableBlacks);
        }

        {
            String includesString = Config.getString("includeTables");
            if (StringUtil.notEmpty(includesString)) {
                includes = Pattern.compile(includesString);
            }
            String excludesString = Config.getString("excludeTables");
            if (StringUtil.notEmpty(excludesString)) {
                excludes = Pattern.compile(excludesString);
            }
        }
    }

    /**
     * include this table or not.
     *
     * @param tableRaw
     * @return
     */
    protected boolean isInclude(final TableRaw tableRaw) {
        return (includes == null || includes.matcher(tableRaw.name).matches())
                && (excludes == null || !excludes.matcher(tableRaw.name).matches());
    }

    @Override
    protected Table createTable(final TableRaw tableRaw) {
        init();
        if (!isInclude(tableRaw)) {
            //XXX: DEBUG LOG
            return null;
        }
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
            //TODO: tableSettings.get("id")
            for (ColumnRaw column : tableRaw.getColumns()) {
                Column cm = ColumnFactory.create(column, table);
                if (cm == null) {
                    //XXX: DEBUG LOG
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

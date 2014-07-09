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
import webit.generator.core.util.ResourceUtil;
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

    public List<Table> collectTables() {
        init();
        //TODO: 规则转换
        final List<Table> tableList;
        {
            final Map<String, Table> tableMaps = new HashMap<String, Table>();
            final Map<String, Map<String, Map<String, Object>>> tableColumnMap = ResourceUtil.loadTableColumns();
            for (Map.Entry<String, TableRaw> entry : DatabaseAccesser.getInstance().getAllTables().entrySet()) {
                TableRaw raw = entry.getValue();
                if (!isInclude(raw)) {
                    //XXX: DEBUG LOG
                    continue;
                }
                Table table = createTable(raw, tableColumnMap.get(raw.name));
                if (table != null) {
                    //XXX: DEBUG LOG
                    tableMaps.put(raw.name, table);
                }
            }

            tableList = new ArrayList<Table>(tableMaps.values());
            //tables init
            for (Table table : tableList) {
                table.init(tableMaps);
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

    protected Table createTable(final TableRaw tableRaw, final Map<String, Map<String, Object>> tableColumnSetting) {

        final String entity;
        final String sqlName;
        final String remark;
        final ArrayList<Column> columns;
        final ArrayList<Column> idColumns;
        final Map<String, Column> columnMap;
        final String modelType;
        final String modelSimpleType;
        final boolean blackEntity;

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

        final Table table = new Table(entity, sqlName, remark, columnMap, columns, idColumns, modelType, modelSimpleType, blackEntity);

        {
            for (ColumnRaw column : tableRaw.getColumns()) {
                Column cm = ColumnFactory.create(column, table, tableColumnSetting != null ? tableColumnSetting.get(column.name) : null);
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

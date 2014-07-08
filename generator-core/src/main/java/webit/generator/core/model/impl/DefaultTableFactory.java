package webit.generator.core.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import webit.generator.core.Config;
import webit.generator.core.dbaccess.DatabaseAccesser;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.dbaccess.model.TableRaw;
import webit.generator.core.model.Column;
import webit.generator.core.model.ColumnFactory;
import webit.generator.core.model.Table;
import webit.generator.core.model.TableFactory;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.ResourceUtil;
import webit.generator.core.util.StringUtil;

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
        final List<String> tableBlacks = StringUtil.toUnBlankList(Config.getString("blackEntitys"));
        if (tableBlacks != null) {
            blackEntitys.addAll(tableBlacks);
        }

    }

    public List<Table> collectTables() {

        //TODO: 正则匹配
        //TODO: 规则转换
        final List<Table> tableList = new ArrayList<Table>();
        final Map<String, Table> tableMaps = new HashMap<String, Table>();
        final Map<String, Map<String, Map<String, Object>>> tableColumnMap = ResourceUtil.loadTableColumns();
        for (Map.Entry<String, TableRaw> entry : DatabaseAccesser.getInstance().getAllTables().entrySet()) {
            TableRaw raw = entry.getValue();
            Table table = createTable(raw, tableColumnMap.get(raw.name));
            if (table != null) {
                tableMaps.put(raw.name, table);
                if (!blackEntitys.contains(table.getEntity())) {
                    tableList.add(table);
                }
            }
        }

        //tables init
        for (Map.Entry<String, Table> entry : tableMaps.entrySet()) {
            entry.getValue().init(tableMaps);
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

    protected Table createTable(final TableRaw tableRaw, final Map<String, Map<String, Object>> tableColumnSetting) {

        final String entity;
        final String sqlName;
        final String remarks;
        final Map<String, Column> columnMap;
        final ArrayList<Column> columns;
        final ArrayList<Column> idColumns;
        final String modelFullName;
        final String modelSimpleName;
        final boolean blackEntity;

        remarks = tableRaw.remarks;
        columns = new ArrayList<Column>();
        idColumns = new ArrayList<Column>();
        columnMap = new HashMap<String, Column>();

        sqlName = StringUtil.cutPrefix(tableRaw.name, Config.getString("db.tablePrefix", ""));
        entity = ClassNameUtil.modelEntityNamingStrategy(sqlName);

        blackEntity = blackEntitys.contains(entity);
        modelSimpleName = Config.getString("modelPrefix", "") + ClassNameUtil.upperFirst(entity) + Config.getString("modelSuffix", "");
        modelFullName = Config.getString("modelPkg", Config.getRequiredString("basePkg")) + "." + modelSimpleName;

        final Table table = new Table(entity, sqlName, remarks, columnMap, columns, idColumns, modelFullName, modelSimpleName, blackEntity);
        for (ColumnRaw column : tableRaw.getColumns()) {
            Column cm = ColumnFactory.create(column, table, tableColumnSetting != null ? tableColumnSetting.get(column.name) : null);
            if (cm.isIspk()) {
                idColumns.add(cm);
            }
            columnMap.put(cm.getVarName(), cm);
        }
        columns.addAll(columnMap.values());
        Collections.sort(columns);
        idColumns.trimToSize();
        columns.trimToSize();

        return table;
    }
}

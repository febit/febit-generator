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
import webit.generator.core.model.ColumnModel;
import webit.generator.core.model.TableModel;
import webit.generator.core.model.TableModelFactory;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.ResourceUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class DefaultTableModelFactory extends TableModelFactory {

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

    public List<TableModel> collectTables() {

        //TODO: 正则匹配
        //TODO: 规则转换
        final Map<String, TableModel> tableMaps = new HashMap<String, TableModel>();
        final Map<String, Map<String, Map<String, Object>>> tableColumnMap = ResourceUtil.loadTableColumns();
        for (Map.Entry<String, TableRaw> entry : DatabaseAccesser.getInstance().getAllTables().entrySet()) {
            //String string = entry.getKey();
            TableRaw table = entry.getValue();
            tableMaps.put(table.name,
                    createTable(table, tableColumnMap.get(table.name)));
        }

        //tables init
        for (Map.Entry<String, TableModel> entry : tableMaps.entrySet()) {
            entry.getValue().init(tableMaps);
        }

        //MAP_TO_LIST and without black tables
        final List<TableModel> tableList = new ArrayList<TableModel>(tableMaps.size());
        TableModel tableModel;
        for (Map.Entry<String, TableModel> entry : tableMaps.entrySet()) {
            tableModel = entry.getValue();
            if (!blackEntitys.contains(tableModel.getEntity())) {
                tableList.add(tableModel);
            }
        }

        //table list sort
        Collections.sort(tableList);
        if (Logger.isInfoEnabled()) {
            for (TableModel tableModel1 : tableList) {
                Logger.info("Loaded table: " + tableModel1.getSqlName() + "  " + tableModel1.getRemark());
            }
        }

        return tableList;
    }

    protected TableModel createTable(final TableRaw table, final Map<String, Map<String, Object>> tableColumnSetting) {

        final String entity;
        final String sqlName;
        final String remarks;
        final Map<String, ColumnModel> columnMap;
        final ArrayList<ColumnModel> columns;
        final ArrayList<ColumnModel> idColumns;
        final String modelFullName;
        final String modelSimpleName;
        final boolean blackEntity;

        remarks = table.remarks;
        columns = new ArrayList<ColumnModel>();
        idColumns = new ArrayList<ColumnModel>();
        columnMap = new HashMap<String, ColumnModel>();

        sqlName = StringUtil.cutPrefix(table.name, Config.getString("db.tablePrefix", ""));
        entity = ClassNameUtil.modelEntityNamingStrategy(sqlName);
        
        blackEntity = blackEntitys.contains(entity);
        modelSimpleName = Config.getString("modelPrefix", "") + ClassNameUtil.upperFirst(entity) + Config.getString("modelSuffix", "");
        modelFullName = Config.getString("modelPkg", Config.getRequiredString("basePkg")) + "." + modelSimpleName;

        final TableModel tableModel = new TableModel(entity, sqlName, remarks, columnMap, columns, idColumns, modelFullName, modelSimpleName, blackEntity);
        for (ColumnRaw column : table.getColumns()) {
            //TODO: ColumnFactory
            ColumnModel cm = new ColumnModel(column, tableModel, tableColumnSetting != null ? tableColumnSetting.get(column.name) : null);
            if (cm.isIspk()) {
                idColumns.add(cm);
            }
            columnMap.put(cm.getVarName(), cm);
        }
        columns.addAll(columnMap.values());
        Collections.sort(columns);
        idColumns.trimToSize();
        columns.trimToSize();

        return tableModel;
    }
}

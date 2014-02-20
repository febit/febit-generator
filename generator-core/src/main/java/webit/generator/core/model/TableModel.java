// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.Config;
import webit.generator.core.dbaccess.model.Column;
import webit.generator.core.dbaccess.model.Table;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author ZQQ
 */
public class TableModel implements Comparable<TableModel> {

    private final String entity;
    private final String sqlName;
    private final String remarks;
    private final Map<String, ColumnModel> columnMap;
    private final List<ColumnModel> columns;
    private final List<ColumnModel> idColumns;
    //
    private final String modelFullName;
    private final String modelSimpleName;

    public TableModel(Table table, Map<String, Map<String, Object>> tableColumnSetting) {

        this.remarks = table.remarks;
        this.columnMap = new HashMap<String, ColumnModel>();
        this.sqlName = StringUtil.cutPrefix(table.name, Config.getString("db.tablePrefix", ""));
        this.entity = ClassNameUtil.modelEntityNamingStrategy(this.sqlName);
        this.modelSimpleName = Config.getString("modelPrefix", "") + ClassNameUtil.upperFirst(entity) + Config.getString("modelSuffix", "");
        this.modelFullName = Config.getString("modelPkg", Config.getRequiredString("basePkg")) + "." + modelSimpleName;
        this.idColumns = new ArrayList<ColumnModel>();
        
        for (Column column : table.getColumns()) {
            ColumnModel cm = new ColumnModel(column, this, tableColumnSetting != null ? tableColumnSetting.get(column.name) : null);
            if (cm.isIspk()) {
                this.idColumns.add(cm);
            }
            this.columnMap.put(cm.getVarName(), cm);
        }
        Collections.sort(this.columns = new ArrayList<ColumnModel>(this.columnMap.values()));
    }

    public void init(Map<String, TableModel> alltables) {
        for (Map.Entry<String, ColumnModel> entry : columnMap.entrySet()) {
            entry.getValue().resolveFK(alltables);
        }
    }

    public String getEntity() {
        return entity;
    }

    public String getSqlName() {
        return sqlName;
    }

    public String getRemarks() {
        return remarks;
    }

    /**
     * Only if has single one id column, returns this single id column, or
     * returns null.
     *
     * @return
     */
    public ColumnModel getIdColumn() {
        return idColumns.size() == 1 ? idColumns.get(0) : null;
    }

    public List<ColumnModel> getIdColumns() {
        return idColumns;
    }

    public Map<String, ColumnModel> getColumnMap() {
        return columnMap;
    }

    public ColumnModel getColumnByName(String name) {
        return columnMap.get(name);
    }

    public List<ColumnModel> getColumns() {
        return columns;
    }

    public String getModelFullName() {
        return modelFullName;
    }

    public String getModelSimpleName() {
        return modelSimpleName;
    }

    public List<ColumnModel> getFkColumns() {

        List<ColumnModel> columnModels = new ArrayList<ColumnModel>();
        for (ColumnModel columnModel : columns) {
            if (columnModel.isIsfk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<ColumnModel> getFkColumnsByType(String type) {

        List<ColumnModel> columnModels = new ArrayList<ColumnModel>();
        for (ColumnModel columnModel : columns) {
            if (columnModel.isIsfk() && columnModel.getFk_javaType().equals(type)) {
                columnModels.add(columnModel);
            }
        }
        return columnModels.isEmpty() ? null : columnModels;
    }

    /**
     * ext tables that use this single-id-column as foreign-key.
     *
     * @return
     */
    public List<TableModel> getExtTables() {
        final ColumnModel idColumn = getIdColumn();
        if (idColumn == null || idColumn.isIsLinkKey() == false) {
            return null;
        }

        List<TableModel> tableModels = new ArrayList<TableModel>();
        for (ColumnModel columnModel : idColumn.getLinkColumns()) {
            if (columnModel.isIspk()) {
                tableModels.add(columnModel.getParent());
            }
        }
        return tableModels.isEmpty() ? null : tableModels;
    }

    public List<ColumnModel> getEnumColumns() {
        List<ColumnModel> columnModels = new ArrayList<ColumnModel>();
        for (ColumnModel columnModel : columns) {
            if (columnModel.isIsenum()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<ColumnModel> getQueryColumns() {
        List<ColumnModel> columnModels = new ArrayList<ColumnModel>();
        for (ColumnModel columnModel : columns) {
            if (columnModel.isQuery() && !columnModel.isIspk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<ColumnModel> getUniqueColumns() {
        List<ColumnModel> columnModels = new ArrayList<ColumnModel>();
        for (ColumnModel columnModel : columns) {
            if (columnModel.isIsUnique() && !columnModel.isIspk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    @Override
    public int compareTo(TableModel o) {
        return entity.compareTo(o.entity);
    }
}

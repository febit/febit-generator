// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final boolean blackEntity;
    //
    private final String modelFullName;
    private final String modelSimpleName;

    public TableModel(String entity, String sqlName, String remarks, Map<String, ColumnModel> columnMap, List<ColumnModel> columns, List<ColumnModel> idColumns, String modelFullName, String modelSimpleName, boolean blackEntity) {
        this.entity = entity;
        this.sqlName = sqlName;
        this.remarks = remarks;
        this.columnMap = columnMap;
        this.columns = columns;
        this.idColumns = idColumns;
        this.modelFullName = modelFullName;
        this.modelSimpleName = modelSimpleName;
        this.blackEntity = blackEntity;
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

    public boolean isBlackEntity() {
        return blackEntity;
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

    public List<TableModel> getLinkedTables() {
        final ColumnModel idColumn = getIdColumn();
        if (idColumn == null || idColumn.isIsLinkKey() == false) {
            return null;
        }

        List<TableModel> tableModels = new ArrayList<TableModel>();
        for (ColumnModel columnModel : idColumn.getLinkColumns()) {
            tableModels.add(columnModel.getParent());
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

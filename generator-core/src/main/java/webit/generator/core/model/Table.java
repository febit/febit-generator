// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ZQQ
 */
public class Table implements Comparable<Table> {

    private final String entity;
    private final String sqlName;
    private final String remark;
    private final Map<String, Column> columnMap;
    private final List<Column> columns;
    private final List<Column> idColumns;
    private final boolean blackEntity;
    //
    private final String modelFullName;
    private final String modelSimpleName;

    public Table(String entity, String sqlName, String remark, Map<String, Column> columnMap, List<Column> columns, List<Column> idColumns, String modelFullName, String modelSimpleName, boolean blackEntity) {
        this.entity = entity;
        this.sqlName = sqlName;
        this.remark = remark;
        this.columnMap = columnMap;
        this.columns = columns;
        this.idColumns = idColumns;
        this.modelFullName = modelFullName;
        this.modelSimpleName = modelSimpleName;
        this.blackEntity = blackEntity;
    }

    public void init(Map<String, Table> alltables) {
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            entry.getValue().resolveFK(alltables);
        }
    }

    public String getEntity() {
        return entity;
    }

    public String getSqlName() {
        return sqlName;
    }

    public String getRemark() {
        return remark;
    }
    
    @Deprecated
    public String getRemarks() {
        return remark;
    }

    /**
     * Only if has single one id column, returns this single id column, or
     * returns null.
     *
     * @return
     */
    public Column getIdColumn() {
        return idColumns.size() == 1 ? idColumns.get(0) : null;
    }

    public List<Column> getIdColumns() {
        return idColumns;
    }

    public boolean isBlackEntity() {
        return blackEntity;
    }

    public Map<String, Column> getColumnMap() {
        return columnMap;
    }

    public Column getColumnByName(String name) {
        return columnMap.get(name);
    }

    public List<Column> getColumns() {
        return columns;
    }

    public String getModelFullName() {
        return modelFullName;
    }

    public String getModelSimpleName() {
        return modelSimpleName;
    }

    public List<Column> getFkColumns() {

        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.isIsfk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<Column> getFkColumnsByType(String type) {

        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
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
    public List<Table> getExtTables() {
        final Column idColumn = getIdColumn();
        if (idColumn == null || idColumn.isIsLinkKey() == false) {
            return null;
        }

        List<Table> tables = new ArrayList<Table>();
        for (Column columnModel : idColumn.getLinkColumns()) {
            if (columnModel.isIspk()) {
                tables.add(columnModel.getTable());
            }
        }
        return tables.isEmpty() ? null : tables;
    }

    public List<Table> getLinkedTables() {
        final Column idColumn = getIdColumn();
        if (idColumn == null || idColumn.isIsLinkKey() == false) {
            return null;
        }

        List<Table> tables = new ArrayList<Table>();
        for (Column columnModel : idColumn.getLinkColumns()) {
            tables.add(columnModel.getTable());
        }
        return tables.isEmpty() ? null : tables;
    }

    public List<Column> getEnumColumns() {
        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.isIsenum()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<Column> getQueryColumns() {
        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.isQuery() && !columnModel.isIspk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<Column> getUniqueColumns() {
        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.isIsUnique() && !columnModel.isIspk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    @Override
    public int compareTo(Table o) {
        return entity.compareTo(o.entity);
    }
}

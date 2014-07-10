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

    public final String entity;
    public final String sqlName;
    public final String remark;
    public final Map<String, Column> columnMap;
    public final List<Column> columns;
    public final List<Column> idColumns;
    public final boolean isBlackEntity;
    //
    public final String modelType;
    public final String modelSimpleType;
    public final Map<String, Object> attrs;

    public Table(String entity, String sqlName, String remark, Map<String, Object> attrs, Map<String, Column> columnMap, List<Column> columns, List<Column> idColumns, String modelType, String modelSimpleType, boolean blackEntity) {
        this.entity = entity;
        this.sqlName = sqlName;
        this.remark = remark;
        this.attrs = attrs;
        this.columnMap = columnMap;
        this.columns = columns;
        this.idColumns = idColumns;
        this.modelType = modelType;
        this.modelSimpleType = modelSimpleType;
        this.isBlackEntity = blackEntity;
    }

    public void init() {
        for (Map.Entry<String, Column> entry : columnMap.entrySet()) {
            entry.getValue().resolveFK();
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

    public Map<String, Object> getAttrs() {
        return attrs;
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

    public boolean getIsBlackEntity() {
        return isBlackEntity;
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

    public String getModelType() {
        return modelType;
    }

    public String getModelSimpleType() {
        return modelSimpleType;
    }

    public List<Column> getFkColumns() {

        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.getIsfk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<Column> getFkColumnsByType(String type) {

        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.getIsfk() && columnModel.getFkType().equals(type)) {
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
        if (idColumn == null || idColumn.getIsLinkKey() == false) {
            return null;
        }

        List<Table> tables = new ArrayList<Table>();
        for (Column columnModel : idColumn.getLinkColumns()) {
            if (columnModel.getIspk()) {
                tables.add(columnModel.getTable());
            }
        }
        return tables.isEmpty() ? null : tables;
    }

    public List<Table> getLinkedTables() {
        final Column idColumn = getIdColumn();
        if (idColumn == null || idColumn.getIsLinkKey() == false) {
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
            if (columnModel.getIsenum()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<Column> getQueryColumns() {
        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.getQuery() && !columnModel.getIspk()) {
                columnModels.add(columnModel);
            }
        }
        return columnModels;
    }

    public List<Column> getUniqueColumns() {
        List<Column> columnModels = new ArrayList<Column>();
        for (Column columnModel : columns) {
            if (columnModel.getIsUnique() && !columnModel.getIspk()) {
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

/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.generator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zqq90
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

    /**
     * Only if has single one id column, returns this single id column, or
     * returns null.
     *
     * @return
     */
    public Column getIdColumn() {
        return idColumns.size() == 1 ? idColumns.get(0) : null;
    }

    public Column getColumnByName(String name) {
        return columnMap.get(name);
    }

    public List<Column> getFkColumns() {
        final List<Column> results = new ArrayList<>(columns.size());
        for (Column column : columns) {
            if (column.getIsfk()) {
                results.add(column);
            }
        }
        return results;
    }

    public List<Column> getFkColumnsByType(String type) {
        final List<Column> results = new ArrayList<>(columns.size());
        for (Column column : columns) {
            if (column.getIsfk() && column.getFkType().equals(type)) {
                results.add(column);
            }
        }
        return results.isEmpty() ? null : results;
    }

    /**
     * ext tables that use this single-id-column as foreign-key.
     *
     * @return
     */
    public List<Table> getExtTables() {
        final Column idColumn = getIdColumn();
        if (idColumn == null || !idColumn.getIsLinkKey()) {
            return null;
        }
        final List<Column> linkColumns = idColumn.getLinkColumns();
        if (linkColumns.isEmpty()) {
            return null;
        }
        final List<Table> tables = new ArrayList<>(linkColumns.size());
        for (Column column : linkColumns) {
            if (column.getIspk()) {
                tables.add(column.getTable());
            }
        }
        return tables.isEmpty() ? null : tables;
    }

    public List<Table> getLinkedTables() {
        final Column idColumn = getIdColumn();
        if (idColumn == null || !idColumn.getIsLinkKey()) {
            return null;
        }
        final List<Column> linkColumns = idColumn.getLinkColumns();
        if (linkColumns.isEmpty()) {
            return null;
        }
        final List<Table> tables = new ArrayList<>(linkColumns.size());
        for (Column column : linkColumns) {
            tables.add(column.getTable());
        }
        return tables.isEmpty() ? null : tables;
    }

    public List<Column> getEnumColumns() {
        final List<Column> results = new ArrayList<>(columns.size());
        for (Column column : columns) {
            if (column.getIsenum()) {
                results.add(column);
            }
        }
        return results;
    }

    public List<Column> getQueryColumns() {
        final List<Column> results = new ArrayList<>(columns.size());
        for (Column column : columns) {
            if (column.getQuery() && !column.getIspk()) {
                results.add(column);
            }
        }
        return results;
    }

    public List<Column> getUniqueColumns() {
        final List<Column> results = new ArrayList<>(columns.size());
        for (Column column : columns) {
            if (column.getIsUnique() && !column.getIspk()) {
                results.add(column);
            }
        }
        return results;
    }

    @Override
    public int hashCode() {
        return this.entity.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Table)) {
            return false;
        }
        return this.entity.equals(((Table) obj).entity);
    }

    @Override
    public int compareTo(Table o) {
        return entity.compareTo(o.entity);
    }
}

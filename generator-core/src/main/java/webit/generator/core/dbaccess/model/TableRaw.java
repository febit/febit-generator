// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.dbaccess.model;

import java.util.ArrayList;
import java.util.List;

public class TableRaw implements Comparable<TableRaw>, java.io.Serializable, Cloneable {

    public final String name;
    public final String remarks;
    public final boolean isView;
    private final List<ColumnRaw> columns = new ArrayList<ColumnRaw>();
    private final List<ForeignKey> importedKeys = new ArrayList<ForeignKey>();
    private final List<ForeignKey> exportedKeys = new ArrayList<ForeignKey>();

    public TableRaw(String name, String remarks, boolean isView) {
        this.name = name;
        this.remarks = remarks;
        this.isView = isView;
    }

    public List<ColumnRaw> getColumns() {
        return columns;
    }

    public void addColumns(Iterable<ColumnRaw> columns) {
        for (ColumnRaw column : columns) {
            addColumn(column);
        }
    }

    public void addColumn(ColumnRaw column) {
        this.columns.add(column);
    }

    public List<ForeignKey> getImportedKeys() {
        return importedKeys;
    }

    public void addImportedKeys(ForeignKey key) {
        this.importedKeys.add(key);
    }

    public List<ForeignKey> getExportedKeys() {
        return exportedKeys;
    }

    public void addExportedKeys(ForeignKey key) {
        this.exportedKeys.add(key);
    }

    public int getPkCount() {
        int pkCount = 0;
        for (ColumnRaw c : columns) {
            if (c.isPk) {
                pkCount++;
            }
        }
        return pkCount;
    }

    public List<ColumnRaw> getPkColumns() {
        List<ColumnRaw> results = new ArrayList<ColumnRaw>();
        for (ColumnRaw c : getColumns()) {
            if (c.isPk) {
                results.add(c);
            }
        }
        return results;
    }

    public ColumnRaw getPkColumn() {
        if (getPkColumns().isEmpty()) {
            throw new IllegalStateException("not found primary key on table:" + name);
        }
        return getPkColumns().get(0);
    }

    @Override
    public int compareTo(TableRaw o) {
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TableRaw)) {
            return false;
        }
        return this.name.equals(((TableRaw) obj).name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}

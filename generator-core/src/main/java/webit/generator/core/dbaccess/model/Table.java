// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.dbaccess.model;

import java.util.ArrayList;
import java.util.List;

public class Table implements java.io.Serializable, Cloneable {

    public final String name;
    public final String remarks;
    private final List<Column> columns = new ArrayList<Column>();
    private final List<ForeignKey> importedKeys = new ArrayList<ForeignKey>();
    private final List<ForeignKey> exportedKeys = new ArrayList<ForeignKey>();

    public Table(String name, String remarks) {
        this.name = name;
        this.remarks = remarks;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void addColumns(Iterable<Column> columns) {
        for (Column column : columns) {
            addColumn(column);
        }
    }

    public void addColumn(Column column) {
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
        for (Column c : columns) {
            if (c.isPk) {
                pkCount++;
            }
        }
        return pkCount;
    }

    public List<Column> getPkColumns() {
        List<Column> results = new ArrayList<Column>();
        for (Column c : getColumns()) {
            if (c.isPk) {
                results.add(c);
            }
        }
        return results;
    }

    public Column getPkColumn() {
        if (getPkColumns().isEmpty()) {
            throw new IllegalStateException("not found primary key on table:" + name);
        }
        return getPkColumns().get(0);
    }
}

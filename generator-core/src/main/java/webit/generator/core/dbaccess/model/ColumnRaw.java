// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.dbaccess.model;

import webit.generator.core.Config;
import webit.generator.core.util.DBUtil;

public class ColumnRaw implements java.io.Serializable, Cloneable, Comparable<ColumnRaw> {

    public final TableRaw table;
    public final int type;
    public final String typeName;
    public final String name;
    public final int size;
    public final int decimalDigits;
    public final String defaultValue;
    public final String remarks;

    public final boolean isPk;
    public final boolean isNullable;
    public final boolean isIndexed;
    public final boolean isUnique;

    private boolean isFk;
    private ForeignKey hasOne = null;

    public ColumnRaw(TableRaw table, int type, String typeName,
            String name, int size, int decimalDigits, boolean isPk,
            boolean isNullable, boolean isIndexed, boolean isUnique,
            String defaultValue, String remarks) {

        this.table = table;
        this.type = type;
        this.name = name;
        this.typeName = typeName;
        this.size = size;
        this.decimalDigits = decimalDigits;
        this.isPk = isPk;
        this.isNullable = isNullable;
        this.isIndexed = isIndexed;
        this.isUnique = isUnique;
        this.defaultValue = defaultValue;
        this.remarks = remarks;
    }
    
    public String getJavaType() {
        final String normalJdbcJavaType = DBUtil.getJavaType(this.type, this.size, this.decimalDigits);
        return Config.getString("javaTypeMapping.".concat(normalJdbcJavaType), normalJdbcJavaType);
    }

    public boolean getIsFk() {
        return isFk;
    }
    
    public boolean isStringType() {
        return DBUtil.isStringType(type);
    }

    public void setIsFk(boolean isFk) {
        this.isFk = isFk;
    }

    public ForeignKey getHasOne() {
        return hasOne;
    }

    public void setHasOne(ForeignKey hasOne) {
        this.hasOne = hasOne;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.table != null ? this.table.hashCode() : 0);
        hash = 61 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ColumnRaw other = (ColumnRaw) obj;
        if (this.table != other.table && (this.table == null || !this.table.equals(other.table))) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ColumnRaw o) {

        if (this.isPk && !o.isPk) {
            return -1;
        }
        if (!this.isPk && o.isPk) {
            return 1;
        }

        return this.name.compareToIgnoreCase(o.name);
    }
}

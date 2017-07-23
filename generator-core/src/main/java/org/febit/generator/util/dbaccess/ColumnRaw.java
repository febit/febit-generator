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
package org.febit.generator.util.dbaccess;

import java.sql.Types;
import org.febit.generator.Config;

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
        final String javaType = DatabaseAccesser.getJavaType(this.type, this.size, this.decimalDigits);
        return Config.getString("javaTypeMapping.".concat(javaType), javaType);
    }

    public boolean getIsFk() {
        return isFk;
    }

    public boolean isTypeOfString() {
        int sqlType = this.type;
        return sqlType == Types.CHAR
                || sqlType == Types.VARCHAR
                || sqlType == Types.LONGVARCHAR
                || sqlType == Types.NCHAR
                || sqlType == Types.NVARCHAR;
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
    public int compareTo(ColumnRaw o) {
        if (this.isPk && !o.isPk) {
            return -1;
        }
        if (!this.isPk && o.isPk) {
            return 1;
        }
        return this.name.compareToIgnoreCase(o.name);
    }

    @Override
    public int hashCode() {
        return 61 * this.table.hashCode() + this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ColumnRaw)) {
            return false;
        }
        final ColumnRaw other = (ColumnRaw) obj;
        return this.table.equals(other.table) && this.name.equals(other.name);
    }

    @Override
    public String toString() {
        return table.toString() + '.' + name;
    }
}

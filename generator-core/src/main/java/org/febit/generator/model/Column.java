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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.febit.generator.Lazy;
import org.febit.generator.TableSettings;
import org.febit.generator.components.ColumnNaming;
import org.febit.generator.components.TableFactory;
import org.febit.generator.util.Logger;
import org.febit.generator.util.NamingUtil;
import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class Column implements Comparable<Column> {

    public final Table table;
    public final TableSettings.Attrs attrs;
    public final int size;
    public final boolean isUnique;
    public final boolean optional;
    public final boolean ispk;
    public final String remark;
    public final String name;
    public final String type;
    public final String sqlName;
    public final boolean query;
    //
    public final List<ColumnEnum> enums;
    public final Map<Object, ColumnEnum> enumMap;
    //
    public final List<Column> linkColumns; //被外键
    protected final String fkHint;
    protected Column fk;
    protected String fkVarName;
    //
    protected Object defaultValue;

    public Column(Table table, TableSettings.Attrs attrs, String sqlName, String name, String javaType, int size, boolean ispk, boolean isUnique, boolean optional, List<ColumnEnum> enums, String fkHint, Object defaultValue, String remark) {
        this.table = table;
        this.attrs = attrs;
        this.size = size;
        this.isUnique = isUnique;
        this.optional = optional;
        this.ispk = ispk;
        this.remark = remark;
        this.name = name;
        this.type = javaType;
        this.sqlName = sqlName;
        this.enums = enums;
        this.defaultValue = defaultValue;

        this.fkHint = fkHint != null ? fkHint : attrs.getValidString("fk");
        this.query = attrs.getBoolean("query");
        this.fkVarName = StringUtil.cutSuffix(name, "Id");
        this.linkColumns = new ArrayList<>();
        if (enums != null) {
            this.enumMap = new HashMap<>();
            enums.forEach((columnEnum) -> {
                this.enumMap.put(columnEnum.value, columnEnum);
            });
        } else {
            this.enumMap = null;
        }
    }

    protected void resolveFK() {
        if (fkHint == null) {
            return;
        }
        Table linkTable = Lazy.get(TableFactory.class).getTable(fkHint);
        if (linkTable != null) {
            fk = linkTable.getIdColumn();
            fk.addLinkColumns(this);
        } else {
            Logger.warn("Fk hint not found: {}.fk={}", this, fkHint);
        }
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueRaw() {
        if (defaultValue == null) {
            return null;
        }
        return defaultValue.toString();
    }

    public boolean isHasDefaultValue() {
        return defaultValue != null;
    }

    public String getDefaultValueShow() {
        if (defaultValue == null) {
            return "null";
        }
        if (defaultValue instanceof Boolean) {
            return defaultValue.toString();
        }
        if (defaultValue instanceof Number) {
            return defaultValue.toString();
        }
        return "\"" + defaultValue + "\"";
    }

    public String getSimpleType() {
        return NamingUtil.getClassSimpleName(type);
    }

    public String getGetterName() {
        return Lazy.get(ColumnNaming.class).getterName(name, type);
    }

    public String getSetterName() {
        return Lazy.get(ColumnNaming.class).setterName(name, type);
    }

    public String getFkSimpleType() {
        return fk.table.getModelSimpleType();
    }

    public String getFkVarName() {
        if (fk == null) {
            return null;
        }
        return fkVarName;
    }

    public String getFkType() {
        return fk.table.modelType;
    }

    public String getFkGetterName() {
        if (fk == null) {
            return null;
        }
        return Lazy.get(ColumnNaming.class).getterName(fkVarName, fk.table.modelType);
    }

    public String getFkSetterName() {
        if (fk == null) {
            return null;
        }
        return Lazy.get(ColumnNaming.class).setterName(fkVarName, fk.table.modelType);
    }

    public TableSettings.Attrs getAttrs() {
        return attrs;
    }

    public Object attr(String attr) {
        return attrs.get(attr);
    }

    public List<ColumnEnum> getEnums() {
        return enums;
    }

    public ColumnEnum getEnum(Object value) {
        return enumMap.get(value);
    }

    public String getName() {
        return name;
    }

    public String getVarName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSqlName() {
        return sqlName;
    }

    public boolean getIsUnique() {
        return isUnique;
    }

    public boolean getOptional() {
        return optional;
    }

    public boolean getIsenum() {
        return this.enums != null;
    }

    public boolean getIsfk() {
        return fk != null;
    }

    public String getRemark() {
        return remark;
    }

    public boolean getIspk() {
        return ispk;
    }

    public Column getFk() {
        return fk;
    }

    public boolean getQuery() {
        return query;
    }

    public int getSize() {
        return size;
    }

    public Table getTable() {
        return table;
    }

    @Deprecated
    public Table getParent() {
        return table;
    }

    public boolean getIsLinkKey() {
        return !linkColumns.isEmpty();
    }

    public List<Column> getLinkColumns() {
        return linkColumns;
    }

    public void addLinkColumns(Column cm) {
        linkColumns.add(cm);
    }

    public Map getEnumMap() {
        return enumMap;
    }

    @Override
    public int compareTo(Column o) {
        if (this.ispk && !o.ispk) {
            return -1;
        }
        if (!this.ispk && o.ispk) {
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
        if (!(obj instanceof Column)) {
            return false;
        }
        final Column other = (Column) obj;
        return this.table.equals(other.table)
                && this.name.equals(other.name);
    }

    @Override
    public String toString() {
        return this.table.toString() + '.' + this.name;
    }

}

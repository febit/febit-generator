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
import org.febit.generator.components.ColumnFactory;
import org.febit.generator.components.ColumnNaming;
import org.febit.generator.components.TableFactory;
import org.febit.generator.util.Logger;
import org.febit.generator.util.NamingUtil;
import org.febit.util.StringUtil;
import org.febit.generator.util.dbaccess.ColumnRaw;
import org.febit.generator.util.dbaccess.ForeignKey;

/**
 *
 * @author zqq90
 */
public class Column implements Comparable<Column> {

    public final Table table;
    public final Map<String, Object> attrs;
    public final ColumnRaw raw;
    public final int size;
    public final boolean isUnique;
    public final boolean optional;
    public final boolean ispk;
    public final boolean isGenerated; //GeneratedValue(strategy = GenerationType.IDENTITY)
    public final String remark;
    public final String name;
    public final String type;
    public final String sqlName;
    public final List<Column> linkColumns; //被外键
    public final boolean query;
    //
    public final boolean isenum;
    public final List<ColumnEnum> enums;
    public final Map enumMap;
    //
    protected boolean isLinkKey; //被外键
    protected final String fkHint;
    protected boolean isfk;
    protected Column fk;
    protected String fkVarName;
    protected String fkType;
    protected String fkGetterName;
    protected String fkSetterName;
    //
    protected Object defaultValue;

    public Column(Table table, Map<String, Object> attrs, ColumnRaw raw, int size,
            boolean isUnique, boolean optional, boolean ispk,
            boolean isfk, String fkHint, boolean isGenerated, String remark,
            String name, String javaType, String sqlName,
            boolean query,
            List<ColumnEnum> enums, Object defaultValue) {
        this.table = table;
        this.attrs = attrs;
        this.raw = raw;
        this.size = size;
        this.isUnique = isUnique;
        this.optional = optional;
        this.ispk = ispk;
        this.isfk = isfk;
        this.fkHint = fkHint;
        this.isGenerated = isGenerated;
        this.remark = remark;
        this.name = name;
        this.type = javaType;
        this.sqlName = sqlName;
        this.query = query;
        this.isenum = enums != null;
        this.enums = enums;
        this.defaultValue = defaultValue;

        this.linkColumns = new ArrayList<>();
        if (enums != null) {
            this.enumMap = new HashMap();
            enums.forEach((columnEnum) -> {
                this.enumMap.put(columnEnum.value, columnEnum);
            });
        } else {
            this.enumMap = null;
        }
    }

    protected void resolveFK() {
        if (!isfk) {
            return;
        }
        Column linkColumn = null;
        Table linkTable = null;

        ForeignKey foreignKey = raw.getHasOne();
        if (foreignKey != null) {
            linkColumn = Lazy.get(ColumnFactory.class).getColumn(foreignKey.pk);
            if (linkColumn != null) {
                linkTable = linkColumn.table;
            } else {
                Logger.warn("Loss foreignKey: " + foreignKey);
            }
        } else if (fkHint != null) {
            linkTable = Lazy.get(TableFactory.class).getTable(fkHint);
            if (linkTable != null) {
                linkColumn = linkTable.getIdColumn();
            } else {
                Logger.warn("Fk hint not found: " + this + ".fk=" + fkHint);
            }
        }

        if (linkColumn != null && linkTable != null) {
            fk = linkColumn;
            isfk = (fk != null);
            fkVarName = StringUtil.cutSuffix(name, "Id");
            fkType = linkTable.modelType;
            ColumnNaming columnNaming = Lazy.get(ColumnNaming.class);
            fkGetterName = columnNaming.getterName(fkVarName, fkType);
            fkSetterName = columnNaming.setterName(fkVarName, fkType);
            fk.addLinkColumns(this);
        } else {
            isfk = false;
            fk = null;
        }
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueRaw() {
        return defaultValue == null ? null : defaultValue.toString();
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
        return NamingUtil.getClassSimpleName(fkType);
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public ColumnRaw getRaw() {
        return raw;
    }

    public List<ColumnEnum> getEnums() {
        return enums;
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
        return isenum;
    }

    public boolean getIsfk() {
        return isfk;
    }

    public String getRemark() {
        return remark;
    }

    public boolean getIspk() {
        return ispk;
    }

    public boolean getIsGenerated() {
        return isGenerated;
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

    public String getFkVarName() {
        return fkVarName;
    }

    public String getFkType() {
        return fkType;
    }

    public String getFkGetterName() {
        return fkGetterName;
    }

    public String getFkSetterName() {
        return fkSetterName;
    }

    public boolean getIsLinkKey() {
        return isLinkKey;
    }

    public List<Column> getLinkColumns() {
        return linkColumns;
    }

    public void addLinkColumns(Column cm) {
        linkColumns.add(cm);
        isLinkKey = true;
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
        return this.table.entity + '.' + this.name;
    }

}

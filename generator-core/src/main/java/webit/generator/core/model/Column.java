// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.model;

import java.util.List;
import java.util.Map;
import webit.generator.core.components.ColumnFactory;
import webit.generator.core.components.ColumnNaming;
import webit.generator.core.components.TableFactory;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.dbaccess.model.ForeignKey;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.Logger;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author ZQQ
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
    public final String simpleType;
    public final String sqlName;
    public final String getterName;
    public final String setterName;
    public final List<Column> linkColumns; //被外键
    public final boolean query;
    //
    public final boolean isenum;
    public final List<ColumnEnumModel> enums;
    public final Map enumMap;
    //
    protected boolean isLinkKey; //被外键
    protected final String fkHint;
    protected boolean isfk;
    protected Column fk;
    protected String fkSimpleType;
    protected String fkVarName;
    protected String fkType;
    protected String fkGetterName;
    protected String fkSetterName;
    //
    protected String defaultValueRaw;
    protected Object defaultValue;
    protected boolean hasDefaultValue;
    protected String defaultValueShow;

    public Column(Table table, Map<String, Object> attrs, ColumnRaw raw, int size, boolean isUnique, boolean optional, boolean ispk, boolean isfk, String fkHint, boolean isGenerated, String remark, String name, String javaType, String javaSimpleType, String sqlName, String getterName, String setterName, List<Column> linkColumns, boolean query, boolean isenum, List<ColumnEnumModel> enums, Map enumMap, String defaultValueRaw, Object defaultValue, boolean hasDefaultValue, String defaultValueShow) {
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
        this.simpleType = javaSimpleType;
        this.sqlName = sqlName;
        this.getterName = getterName;
        this.setterName = setterName;
        this.linkColumns = linkColumns;
        this.query = query;
        this.isenum = isenum;
        this.enums = enums;
        this.enumMap = enumMap;
        this.defaultValueRaw = defaultValueRaw;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        this.defaultValueShow = defaultValueShow;
    }

    protected void resolveFK() {

        if (isfk) {
            Column linkColumn = null;
            Table linkTable = null;

            ForeignKey foreignKey = raw.getHasOne();
            if (foreignKey != null) {
                linkColumn = ColumnFactory.getColumn(foreignKey.pk);
                if (linkColumn != null) {
                    linkTable = linkColumn.table;
                } else {
                    Logger.warn("Loss foreignKey: " + foreignKey);
                }
            } else if (fkHint != null) {
                linkTable = TableFactory.getTable(fkHint);
                if (linkTable != null) {
                    Logger.warn("Fk hint not found: " + this + ".fk=" + fkHint);
                    linkColumn = linkTable.getIdColumn();
                }
            }

            if (linkColumn != null && linkTable != null) {
                fk = linkColumn;
                isfk = (fk != null);
                fkVarName = StringUtil.cutSuffix(name, "Id");
                fkType = linkTable.getModelType();
                fkSimpleType = ClassNameUtil.getClassSimpleName(fkType);
                fkGetterName = ColumnNaming.instance().getterName(fkVarName, fkType);
                fkSetterName = ColumnNaming.instance().getterName(fkVarName, fkType);
                fk.addLinkColumns(this);
            } else {
                isfk = false;
                fk = null;
            }
        }
    }

    public Map<String, Object> getAttrs() {
        return attrs;
    }

    public ColumnRaw getRaw() {
        return raw;
    }

    public List<ColumnEnumModel> getEnums() {
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

    public String getSimpleType() {
        return simpleType;
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

    public String getGetterName() {
        return getterName;
    }

    public String getSetterName() {
        return setterName;
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

    public String getFkSimpleType() {
        return fkSimpleType;
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

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueRaw() {
        return defaultValueRaw;
    }

    public boolean isHasDefaultValue() {
        return hasDefaultValue;
    }

    public String getDefaultValueShow() {
        return defaultValueShow;
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
    public String toString() {
        return this.table.entity + '.' + this.name;
    }

}

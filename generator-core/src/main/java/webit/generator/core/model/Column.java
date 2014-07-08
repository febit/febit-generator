// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.typeconverter.TypeConverterUtil;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author ZQQ
 */
public class Column implements Comparable<Column> {

    private final Table table;
    private final Map<String, Object> attrs;
    private final ColumnRaw raw;
    private final int size;
    private final boolean isUnique;
    private final boolean optional;
    private final boolean ispk;
    private final boolean isGenerated; //GeneratedValue(strategy = GenerationType.IDENTITY)
    private final String remark;
    private final String varName;
    private final String javaType;
    private final String javaSimpleType;
    private final String sqlName;
    private final String getterName;
    private final String setterName;
    private final List<Column> linkColumns; //被外键
    private final boolean query;
    //
    private boolean isLinkKey; //被外键
    //
    private final boolean isenum;
    private final List<ColumnEnumModel> enums;
    private final Map enumMap;
    //
    private boolean isfk;
    private Column fk;
    private String fk_javaSimpleType;
    private String fk_varName;
    private String fk_javaType;
    private String fk_getterName;
    private String fk_setterName;
    //
    private String defaultValueRaw;
    private Object defaultValue;
    private boolean hasDefaultValue;
    private String defaultValueShow;

    public Column(Table table, Map<String, Object> attrs, ColumnRaw raw, int size, boolean isUnique, boolean optional, boolean ispk, boolean isfk, boolean isGenerated, String remark, String varName, String javaType, String javaSimpleType, String sqlName, String getterName, String setterName, List<Column> linkColumns, boolean query, boolean isenum, List<ColumnEnumModel> enums, Map enumMap, String defaultValueRaw, Object defaultValue, boolean hasDefaultValue, String defaultValueShow) {
        this.table = table;
        this.attrs = attrs;
        this.raw = raw;
        this.size = size;
        this.isUnique = isUnique;
        this.optional = optional;
        this.ispk = ispk;
        this.isfk = isfk;
        this.isGenerated = isGenerated;
        this.remark = remark;
        this.varName = varName;
        this.javaType = javaType;
        this.javaSimpleType = javaSimpleType;
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

    void resolveFK(Map<String, Table> alltables) {

        if (isfk) {
            ColumnRaw pkColumn = raw.getHasOne().pk;
            Table pkTable = alltables.get(pkColumn.table.name);
            if (pkTable != null) {
                fk = pkTable.getColumnMap().get(pkColumn.name);
                isfk = (fk != null);
            }

            if (isfk) {
                fk_varName = StringUtil.cutSuffix(varName, "Id");
                fk_javaType = pkTable.getModelFullName();
                this.fk_javaSimpleType = ClassNameUtil.getClassSimpleName(fk_javaType);
                this.fk_getterName = ClassNameUtil.getGetterMethodName(fk_varName, fk_javaType);
                this.fk_setterName = ClassNameUtil.getSetterMethodName(fk_varName);
                fk.addLinkColumns(this);
            }
        }
    }

    public List<ColumnEnumModel> getEnums() {
        return enums;
    }

    public String getVarName() {
        return varName;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getSqlName() {
        return sqlName;
    }

    public boolean isIsUnique() {
        return isUnique;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isIsenum() {
        return isenum;
    }

    public boolean isIsfk() {
        return isfk;
    }

    public String getRemark() {
        return remark;
    }

    @Deprecated
    public String getRemarks() {
        return remark;
    }

    public String getJavaSimpleType() {
        return javaSimpleType;
    }

    public boolean isIspk() {
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

    public boolean isQuery() {
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

    public String getFk_varName() {
        return fk_varName;
    }

    public String getFk_javaType() {
        return fk_javaType;
    }

    public String getFk_javaSimpleType() {
        return fk_javaSimpleType;
    }

    public String getFk_getterName() {
        return fk_getterName;
    }

    public String getFk_setterName() {
        return fk_setterName;
    }

    public boolean isIsLinkKey() {
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

        return this.varName.compareToIgnoreCase(o.varName);
    }
}

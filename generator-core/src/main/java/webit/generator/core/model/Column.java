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
    private final String varName;
    private final String javaType;
    private final String javaSimpleType;
    private final String sqlName;
    private final String getterName;
    private final String setterName;
    private final boolean isUnique;
    private final boolean optional;
    private final boolean ispk;
    private final boolean isGenerated; //GeneratedValue(strategy = GenerationType.IDENTITY)
    private final List<Column> linkColumns; //被外键
    private final ColumnRaw raw;
    private final int size;
    private final boolean query;
    private boolean isLinkKey; //被外键
    private String remark;
    //
    private boolean isenum;
    private List<ColumnEnumModel> enums;
    private Map enumMap;
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
    private boolean hasDefaultValue = false;
    private String defaultValueShow = "null";

    //TODO: ColumnModelFactory
    public Column(ColumnRaw column, Table parent, Map<String, Object> settings) {

        this.table = parent;
        this.raw = column;
        this.sqlName = column.name;
        this.isUnique = column.isUnique;
        this.optional = column.isNullable;
        this.isfk = column.getIsFk();//&& !column.isPk();
        this.ispk = column.isPk; //主键？
        this.isGenerated = column.isPk && !column.getIsFk(); //XXX:是否主键自动生成
        this.size = column.size;
        this.remark = column.remarks;
        //
        this.varName = ClassNameUtil.modelColumnNamingStrategy(column.name);
        this.javaType = column.getJavaType();
        this.javaSimpleType = ClassNameUtil.getClassSimpleName(javaType);
        this.getterName = ClassNameUtil.getGetterMethodName(varName, javaType);
        this.setterName = ClassNameUtil.getSetterMethodName(varName);
        this.linkColumns = new ArrayList<Column>();
        //XXX:column settings 可丰富功能
        if (settings != null) {
            this.query = "true".equals(settings.get("query"));
        } else {
            this.query = false;
        }

        //parser default
        String defaultValueString = column.defaultValue;
        if (defaultValueString != null) {
            hasDefaultValue = true;
            defaultValueRaw = defaultValueString;
            defaultValueString = defaultValueString.trim();
            final Object defaultValueObject
                    = this.defaultValue
                    = TypeConverterUtil.convert(javaType, defaultValueString);
            if (defaultValueObject instanceof Boolean) {
                defaultValueShow = defaultValueObject.toString();
//            } else if (defaultValueObject instanceof BigDecimal) {
//                defaultValueShow = defaultValueObject.toString();
            } else if (defaultValueObject instanceof Number) {
                defaultValueShow = defaultValueObject.toString();
            } else {
                defaultValueShow = "\"" + defaultValueString + "\"";
            }
        }
        resolveColumnEnums();
    }

    void resolveColumnEnums() {
        final String myRemarks;
        final int start;
        final int end;
        if (this.javaType.equals("java.lang.Short")
                && remark != null
                && (myRemarks = this.remark.trim()).length() != 0
                && (end = myRemarks.lastIndexOf(')')) >= 0
                && (start = myRemarks.lastIndexOf("E(", end)) >= 0) {
            final String[] emumStr = StringUtil.splitc(myRemarks.substring(start + 2, end), ',');
            enums = new ArrayList<ColumnEnumModel>();
            enumMap = new HashMap();
            for (int i = 0; i < emumStr.length; i++) {
                ColumnEnumModel columnEnumModel = ColumnEnumModel.valueOf(emumStr[i]);
                enums.add(columnEnumModel);
                enumMap.put(columnEnumModel.value, columnEnumModel);
            }
            this.isenum = true;
            this.remark = this.remark.substring(0, start); //replaceAll(pattern_enum.pattern(), "");
        } else {
            this.isenum = false;
        }
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

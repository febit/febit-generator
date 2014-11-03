// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import webit.generator.Config;
import webit.generator.components.ColumnFactory;
import webit.generator.components.ColumnNaming;
import webit.generator.model.Column;
import webit.generator.model.ColumnEnum;
import webit.generator.model.Table;
import webit.generator.typeconverter.TypeConverterUtil;
import webit.generator.util.Logger;
import webit.generator.util.NamingUtil;
import webit.generator.util.ResourceUtil;
import webit.generator.util.StringUtil;
import webit.generator.util.dbaccess.model.ColumnRaw;

/**
 *
 * @author Zqq
 */
public class DefaultColumnFactory extends ColumnFactory {

    private Pattern includes;
    private Pattern excludes;

    private boolean inited = false;

    public void init() {
        if (inited == true) {
            return;
        }
        inited = true;

        {
            String includesString = Config.getString("includeColumns");
            if (StringUtil.notEmpty(includesString)) {
                includes = Pattern.compile(includesString);
            }
            String excludesString = Config.getString("excludeColumns");
            if (StringUtil.notEmpty(excludesString)) {
                excludes = Pattern.compile(excludesString);
            }
        }
    }

    /**
     * include this column or not.
     *
     * @param raw
     * @return
     */
    protected boolean isInclude(final ColumnRaw raw) {
        return (includes == null || includes.matcher(raw.name).matches())
                && (excludes == null || !excludes.matcher(raw.name).matches());
    }

    @Override
    protected Column createColumn(final ColumnRaw raw, final Table table) {
        init();
        if (!isInclude(raw)) {
            return null;
        }
        ColumnNaming columnNaming = ColumnNaming.instance();
        //
        final String varName = columnNaming.varName(raw.name);
        final String javaType = raw.getJavaType();
        final String javaSimpleType = NamingUtil.getClassSimpleName(javaType);
        final String getterName = columnNaming.getterName(varName, javaType);
        final String setterName = columnNaming.setterName(varName, javaType);
        final ArrayList linkColumns = new ArrayList<Column>();

        final Map<String, Object> attrs = Config.getColumnSettings(table, varName);

        final boolean query;
        final String fkHint;
        final boolean isfk;
        //XXX:column settings 可丰富功能
        query = "true".equals(attrs.get("query"));
        fkHint = (String) ResourceUtil.toValidValue(attrs.get("fk"));
        isfk = raw.getIsFk() || fkHint != null;

        //parser default
        String defaultValueRaw = null;
        Object defaultValue = null;
        boolean hasDefaultValue = false;
        String defaultValueShow = "null";
        {
            String defaultValueString = raw.defaultValue;
            if (defaultValueString != null) {
                hasDefaultValue = true;
                defaultValueRaw = defaultValueString;
                defaultValueString = defaultValueString.trim();
                final Object defaultValueObject
                        = defaultValue
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
        }

        //resolveColumnEnums
        String remark = columnNaming.remark(raw.remarks);
        final boolean isenum;
        final List<ColumnEnum> enums;
        final Map enumMap;
        if (remark != null) {
            remark = remark.trim();
        }
        {
            final int start;
            final int end;
            if (javaType.equals("java.lang.Short")
                    && remark != null
                    && remark.length() != 0
                    && (end = remark.lastIndexOf(')')) >= 0
                    && (start = remark.lastIndexOf("E(", end)) >= 0) {
                final String[] emumStr = StringUtil.toArray(remark.substring(start + 2, end), ',');
                enums = new ArrayList<ColumnEnum>();
                enumMap = new HashMap();
                for (String emumRaw : emumStr) {
                    ColumnEnum columnEnumModel;
                    try {
                        columnEnumModel = ColumnEnum.valueOf(emumRaw);
                    }catch (Exception e) {
                        Logger.error("Faild to parse column enum: "+ raw +" | "+ remark);
                        throw new RuntimeException(e);
                    }
                    enums.add(columnEnumModel);
                    enumMap.put(columnEnumModel.value, columnEnumModel);
                }
                isenum = true;
                remark = remark.substring(0, start).trim(); //replaceAll(pattern_enum.pattern(), "");
            } else {
                isenum = false;
                enums = null;
                enumMap = null;
            }
        }

        Column column = new Column(table, attrs, raw,
                raw.size, raw.isUnique, raw.isNullable, raw.isPk, isfk, fkHint,
                raw.isPk && !raw.getIsFk(), //Note:是否主键自动生成
                remark,
                varName, javaType, javaSimpleType,
                raw.name, getterName, setterName, linkColumns, query,
                isenum, enums, enumMap,
                defaultValueRaw, defaultValue, hasDefaultValue, defaultValueShow);

        return column;
    }
}

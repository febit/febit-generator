// Copyright (c) 2013, Webit Team. All Rights Reserved.
package webit.generator.core.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import webit.generator.core.dbaccess.model.ColumnRaw;
import webit.generator.core.model.Column;
import webit.generator.core.model.ColumnEnumModel;
import webit.generator.core.model.ColumnFactory;
import webit.generator.core.model.Table;
import webit.generator.core.typeconverter.TypeConverterUtil;
import webit.generator.core.util.ClassNameUtil;
import webit.generator.core.util.StringUtil;

/**
 *
 * @author Zqq
 */
public class DefaultColumnFactory extends ColumnFactory {


    @Override
    public Column createColumn(final ColumnRaw raw, final Table table, final Map<String, Object> attrs) {

        //
        final String varName = ClassNameUtil.modelColumnNamingStrategy(raw.name);
        final String javaType = raw.getJavaType();
        final String javaSimpleType = ClassNameUtil.getClassSimpleName(javaType);
        final String getterName = ClassNameUtil.getGetterMethodName(varName, javaType);
        final String setterName = ClassNameUtil.getSetterMethodName(varName);
        final ArrayList linkColumns = new ArrayList<Column>();
        final boolean query;

        {
            //XXX:column settings 可丰富功能
            if (attrs != null) {
                query = "true".equals(attrs.get("query"));
            } else {
                query = false;
            }
        }

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
        String remark = raw.remarks;
        final boolean isenum;
        final List<ColumnEnumModel> enums;
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
                final String[] emumStr = StringUtil.splitc(remark.substring(start + 2, end), ',');
                enums = new ArrayList<ColumnEnumModel>();
                enumMap = new HashMap();
                for (int i = 0; i < emumStr.length; i++) {
                    ColumnEnumModel columnEnumModel = ColumnEnumModel.valueOf(emumStr[i]);
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
                raw.size, raw.isUnique, raw.isNullable, raw.isPk, raw.getIsFk(),
                raw.isPk && !raw.getIsFk(), //Note:是否主键自动生成
                remark,
                varName, javaType, javaSimpleType,
                raw.name, getterName, setterName, linkColumns, query,
                isenum, enums, enumMap,
                defaultValueRaw, defaultValue, hasDefaultValue, defaultValueShow);
        
        return column;
    }
}

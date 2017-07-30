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
package org.febit.generator.components.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.febit.generator.TableSettings;
import org.febit.generator.components.ColumnFactory;
import org.febit.generator.components.ColumnNaming;
import org.febit.generator.model.Column;
import org.febit.generator.model.ColumnEnum;
import org.febit.generator.model.Table;
import org.febit.generator.typeconverter.TypeConverter;
import org.febit.generator.util.CommonUtil;
import org.febit.generator.util.Logger;
import org.febit.generator.util.NamingUtil;
import org.febit.util.StringUtil;
import org.febit.generator.util.dbaccess.ColumnRaw;

/**
 *
 * @author zqq90
 */
public class DefaultColumnFactory extends ColumnFactory {

    protected Pattern includes;
    protected Pattern excludes;

    protected TableSettings tableSettings;
    protected ColumnNaming columnNaming;
    protected TypeConverter typeConverter;

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
        if (!isInclude(raw)) {
            return null;
        }
        //
        final String varName = columnNaming.varName(raw.name);
        final String javaType = raw.getJavaType();
        final String javaSimpleType = NamingUtil.getClassSimpleName(javaType);
        final String getterName = columnNaming.getterName(varName, javaType);
        final String setterName = columnNaming.setterName(varName, javaType);
        final ArrayList linkColumns = new ArrayList<>();

        final Map<String, Object> attrs = tableSettings.getColumnAttrs(table, varName);

        final boolean query;
        final String fkHint;
        final boolean isfk;

        query = CommonUtil.toBoolean(attrs.get("query"));
        fkHint = (String) TableSettings.toValidValue(attrs.get("fk"));
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
                        = typeConverter.convert(javaType, defaultValueString);
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
                final String[] emumStr = StringUtil.splitc(remark.substring(start + 2, end), ',');
                enums = new ArrayList<>();
                enumMap = new HashMap();
                for (String emumRaw : emumStr) {
                    final ColumnEnum columnEnum;
                    try {
                        columnEnum = ColumnEnum.valueOf(emumRaw);
                    } catch (Exception e) {
                        Logger.error("Faild to parse column enum: " + raw + " | " + remark);
                        throw new RuntimeException(e);
                    }
                    enums.add(columnEnum);
                    enumMap.put(columnEnum.value, columnEnum);
                }
                isenum = true;
                remark = remark.substring(0, start).trim(); //replaceAll(pattern_enum.pattern(), "");
            } else {
                isenum = false;
                enums = null;
                enumMap = null;
            }
        }

        return new Column(table, attrs, raw,
                raw.size, raw.isUnique, raw.isNullable, raw.isPk, isfk, fkHint,
                raw.isPk && !raw.getIsFk(), //Note:是否主键自动生成
                remark,
                varName, javaType, javaSimpleType,
                raw.name, getterName, setterName, linkColumns, query,
                isenum, enums, enumMap,
                defaultValueRaw, defaultValue, hasDefaultValue, defaultValueShow);
    }
}

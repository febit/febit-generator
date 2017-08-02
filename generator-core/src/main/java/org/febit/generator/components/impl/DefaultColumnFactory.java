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
import java.util.List;
import java.util.regex.Pattern;
import org.febit.generator.TableSettings;
import org.febit.generator.components.ColumnFactory;
import org.febit.generator.components.ColumnNaming;
import org.febit.generator.model.Column;
import org.febit.generator.model.ColumnEnum;
import org.febit.generator.model.Table;
import org.febit.generator.typeconverter.TypeConverter;
import org.febit.generator.util.Logger;
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
        String varName = columnNaming.varName(raw.name);
        String javaType = raw.getJavaType();
        TableSettings.Attrs attrs = tableSettings.getColumnAttrs(table, varName);
        String fkHint = raw.getIsFk() ? raw.getHasOne().pk.table.name : null;
        Object defaultValue = raw.defaultValue != null
                ? typeConverter.convert(javaType, raw.defaultValue)
                : null;

        //resolveColumnEnums
        String remark = columnNaming.remark(raw.remarks);
        final List<ColumnEnum> enums;
        if (remark != null
                && javaType.equals("java.lang.Short")
                && remark.contains("E(")
                && remark.endsWith(")")) {
            enums = new ArrayList<>();
            for (String emumRaw : StringUtil.toArrayExcludeCommit(StringUtil.cutBetween(remark, "E(", ")"))) {
                try {
                    enums.add(ColumnEnum.valueOf(emumRaw));
                } catch (Exception e) {
                    Logger.error("Faild to parse column enum: " + raw + " | " + remark);
                    throw new RuntimeException(e);
                }
            }
            remark = StringUtil.cutTo(remark, "E(").trim();
        } else {
            enums = null;
        }

        return new Column(table, attrs, raw.name, varName, javaType,
                raw.size, raw.isPk, raw.isUnique, raw.isNullable,
                enums, fkHint, defaultValue, remark);
    }
}

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

import org.febit.util.StringUtil;

/**
 *
 * @author zqq90
 */
public class ColumnEnum {

    public final short value;
    public final String name;
    public final String remark;

    public ColumnEnum(short value, String name, String remark) {
        this.value = value;
        this.name = name;
        this.remark = remark;
    }

    public static ColumnEnum valueOf(String string) {
        final String[] arr = StringUtil.splitc(string, '|');
        StringUtil.trim(arr);
        Short id;
        String name = "UNKOWN";
        String remark = "unkown";
        String value = arr[0];
        id = Short.valueOf(value);
        if (arr.length >= 2) {
            name = arr[1];
            if (arr.length == 3) {
                remark = arr[2];
            }
        }
        return new ColumnEnum(id, name, remark);
    }
}

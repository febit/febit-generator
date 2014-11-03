// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.model;

import webit.generator.util.StringUtil;

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
        final String[] arr = StringUtil.toArray(string, '|');
        Short id;
        String name = "UNKOWN";
        String remark = "unkown";
        String value = arr[0].trim();
        id = Short.valueOf(value);
        if (arr.length >= 2) {
            name = arr[1].trim();
            if (arr.length == 3) {
                remark = arr[2].trim();
            }
        }
        return new ColumnEnum(id, name, remark);
    }
}

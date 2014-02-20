// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core.model;

/**
 *
 * @author zqq90
 */
public class ColumnEnumModel {

    public final short value;
    public final String name;
    public final String remark;

    public ColumnEnumModel(short value, String name, String remark) {
        this.value = value;
        this.name = name;
        this.remark = remark;
    }

    public static ColumnEnumModel valueOf(String string) {
        final String[] arr = webit.generator.core.util.StringUtil.splitc(string, '|');
        Short id;
        String name = "UNKOWN";
        String remark = "unkown";
        id = Short.valueOf(arr[0].trim());
        if (arr.length >= 2) {
            name = arr[1].trim();
            if (arr.length == 3) {
                remark = arr[2].trim();
            }
        }
        return new ColumnEnumModel(id, name, remark);
    }
}

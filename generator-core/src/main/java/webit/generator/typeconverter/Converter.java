// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.typeconverter;

/**
 *
 * @author zqq90
 */
public interface Converter<T> {

    T convert(String stringValue);
}

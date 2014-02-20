// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.util.Map;
import webit.generator.core.model.TableModel;

/**
 *
 * @author zqq90
 */
public interface GeneratorProcesser {

    void init(Generator generator);

    void afterInitRoot();

    void beforeMargeTemplates();

    void beforeMargeCommonTemplate(String templateName, Map<String, Object> params);

    void beforeMargeTableTemplate(String templateName, Map<String, Object> params, TableModel table);
}

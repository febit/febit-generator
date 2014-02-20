// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.util.Map;
import webit.generator.core.model.TableModel;

/**
 *
 * @author zqq90
 */
public class GeneratorProcesserAdapter implements GeneratorProcesser {

    protected Generator generator;

    public void init(Generator generator) {
        this.generator = generator;
    }

    public void afterInitRoot() {
    }

    public void beforeMargeTemplates() {
    }

    public void beforeMargeCommonTemplate(String templateName, Map<String, Object> params) {
    }

    public void beforeMargeTableTemplate(String templateName, Map<String, Object> params, TableModel table) {
    }

}

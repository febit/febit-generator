// Copyright (c) 2013-2014, Webit Team. All Rights Reserved.
package webit.generator.core;

import java.util.Map;
import webit.generator.core.model.Table;

/**
 *
 * @author zqq90
 */
public class GeneratorProcesserAdapter implements GeneratorProcesser {

    protected Generator generator;

    @Override
    public void init(Generator generator) {
        this.generator = generator;
    }

    @Override
    public void afterInitRoot() {
    }

    @Override
    public void beforeMargeTemplates() {
    }

    @Override
    public void beforeMargeCommonTemplate(String templateName, Map<String, Object> params) {
    }

    @Override
    public void beforeMargeTableTemplate(String templateName, Map<String, Object> params, Table table) {
    }

}

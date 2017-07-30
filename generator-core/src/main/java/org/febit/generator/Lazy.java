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
package org.febit.generator;

import org.febit.util.Petite;
import org.febit.util.Props;

/**
 *
 * @author zqq90
 */
public class Lazy {

    private static Config _config;
    private static Petite _petite;

    public static Config config() {
        if (_config == null) {
            throw new IllegalStateException("Config hadn't yet been initialized!");
        }
        return _config;
    }

    public static Petite petite() {
        if (_petite == null) {
            throw new IllegalStateException("Petite hadn't yet been initialized!");
        }
        return _petite;
    }

    public static void init(Props props) {
        if (_config != null) {
            return;
        }
        _init(new Config().putAll(props));
    }

    public static <T> T get(Class<T> type) {
        return petite().get(type);
    }

    private static synchronized void _init(Config config) {
        if (_config != null) {
            return;
        }
        _config = config;
        _petite = config.buildPetite();
    }

}

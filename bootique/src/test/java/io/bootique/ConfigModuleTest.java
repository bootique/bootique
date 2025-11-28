/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.type.TypeRef;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Deprecated
public class ConfigModuleTest {

    @Test
    public void defaultConfigPrefix() {
        assertEquals("testxyz", new TestXyzModule().defaultConfigPrefix());
        assertEquals("xyz", new Xyz().defaultConfigPrefix());
    }

    @Test
    public void configPrefix() {
        assertEquals("testxyz", new TestXyzModule().configPrefix);
        assertEquals("custom-prefix", new TestXyzModule("custom-prefix").configPrefix);
    }

    @Test
    public void configConfig() {

        TestConfigFactory f = new TestConfigFactory();

        TestXyzModule m = new TestXyzModule();
        m.config(String.class, f);
        assertEquals("testxyz", f.passedPrefix);
    }

    class TestXyzModule extends ConfigModule {

        public TestXyzModule() {

        }

        public TestXyzModule(String configPrefix) {
            super(configPrefix);
        }
    }

    class Xyz extends ConfigModule {

    }

    static class TestConfigFactory implements ConfigurationFactory {

        String passedPrefix;

        @Override
        public <T> T config(Class<T> type, String prefix) {
            this.passedPrefix = prefix;
            return null;
        }

        @Override
        public <T> T config(TypeRef<? extends T> type, String prefix) {
            return null;
        }
    }
}

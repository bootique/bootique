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
import io.bootique.meta.application.OptionMetadata;
import io.bootique.type.TypeRef;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigurationFactoryIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void emptyConfig() {
        BQRuntime runtime = appManager.runtime(
                Bootique.app("--config=src/test/resources/io/bootique/empty.yml"));

        Map<String, String> config = runtime
                .getInstance(ConfigurationFactory.class)
                .config(new TypeRef<>() {
                }, "");
        assertTrue(config.isEmpty());
    }

    @Test
    public void configBoundToString() {
        BQRuntime runtime = appManager.runtime(
                Bootique.app().module(b -> BQCoreModule.extend(b).setProperty("bq.x", "val")));

        String val = runtime.getInstance(ConfigurationFactory.class).config(String.class, "x");
        assertEquals("val", val);
    }

    @Test
    public void combineConfigAndEmptyConfig() {
        BQRuntime runtime = appManager.runtime(
                Bootique.app("--config=classpath:io/bootique/test1.yml", "--config=classpath:io/bootique/empty.yml"));

        Map<String, String> config = runtime.
                getInstance(ConfigurationFactory.class)
                .config(new TypeRef<>() {
                }, "");
        assertEquals(1, config.size());
        assertEquals("b", config.get("a"));
    }

    @Test
    public void combineConfigs() {
        BQRuntime runtime = appManager.runtime(
                Bootique.app("--config=classpath:io/bootique/test1.yml", "--config=classpath:io/bootique/test2.yml"));

        Map<String, String> config = runtime
                .getInstance(ConfigurationFactory.class)
                .config(new TypeRef<>() {
                }, "");
        assertEquals(2, config.size());
        assertEquals("e", config.get("a"));
        assertEquals("d", config.get("c"));
    }

    @Test
    public void combineConfigs_ReverseOrder() {
        BQRuntime runtime = appManager.runtime(
                Bootique.app("--config=classpath:io/bootique/test2.yml", "--config=classpath:io/bootique/test1.yml"));

        Map<String, String> config = runtime
                .getInstance(ConfigurationFactory.class)
                .config(new TypeRef<>() {
                }, "");
        assertEquals(2, config.size());
        assertEquals("b", config.get("a"));
        assertEquals("d", config.get("c"));
    }

    @Test
    public void dIConfig() {

        BQRuntime runtime = appManager.runtime(
                Bootique.app().module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/diconfig1.yml")
                        .addConfig("classpath:io/bootique/diconfig2.yml")));

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<>() {
                }, "");
        assertEquals(3, config.size());
        assertEquals("1", config.get("a"));
        assertEquals("2", config.get("b"));
        assertEquals("3", config.get("c"));
    }

    @Test
    public void dIConfig_VsCliOrder() {

        BQRuntime runtime = appManager.runtime(
                Bootique.app("-c", "classpath:io/bootique/cliconfig.yml").module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/diconfig1.yml")
                        .addConfig("classpath:io/bootique/diconfig2.yml")));

        Map<String, Integer> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<>() {
                }, "");
        assertEquals(3, config.size());
        assertEquals(Integer.valueOf(5), config.get("a"));
        assertEquals(Integer.valueOf(2), config.get("b"));
        assertEquals(Integer.valueOf(6), config.get("c"));
    }

    @Test
    public void dIOnOptionConfig() {

        Function<String, Map<String, Integer>> configReader =
                arg -> {
                    BQRuntime runtime = appManager.runtime(Bootique.app(arg)
                            .module(b -> BQCoreModule.extend(b)
                                    .mapConfigResource("opt", "classpath:io/bootique/diconfig1.yml")
                                    .mapConfigResource("opt", "classpath:io/bootique/diconfig2.yml")
                                    .addOption(OptionMetadata.builder("opt").build())));

                    return runtime.getInstance(ConfigurationFactory.class)
                            .config(new TypeRef<Map<String, Integer>>() {
                            }, "");
                };

        assertTrue(configReader.apply("").isEmpty());

        Map<String, Integer> config = configReader.apply("--opt");

        assertEquals(3, config.size());
        assertEquals(Integer.valueOf(1), config.get("a"));
        assertEquals(Integer.valueOf(2), config.get("b"));
        assertEquals(Integer.valueOf(3), config.get("c"));
    }

    @Test
    public void dIOnOptionConfig_OverrideWithOption() {

        Function<String, Map<String, Integer>> configReader =
                arg -> {
                    BQRuntime runtime = appManager.runtime(Bootique.app(arg)
                            .module(b -> BQCoreModule.extend(b)
                                    .mapConfigResource("opt", "classpath:io/bootique/diconfig1.yml")
                                    .mapConfigResource("opt", "classpath:io/bootique/diconfig2.yml")
                                    .addOption(OptionMetadata.builder("opt").valueOptional().build())
                                    .mapConfigPath("opt", "a")));

                    return runtime.getInstance(ConfigurationFactory.class)
                            .config(new TypeRef<Map<String, Integer>>() {
                            }, "");
                };

        assertTrue(configReader.apply("").isEmpty());

        Map<String, Integer> config = configReader.apply("--opt=8");
        assertEquals(3, config.size());
        assertEquals(Integer.valueOf(8), config.get("a"));
        assertEquals(Integer.valueOf(2), config.get("b"));
        assertEquals(Integer.valueOf(3), config.get("c"));
    }

    @Test
    public void configEnvOverrides_Alias() {
        BQRuntime runtime = appManager.runtime(
                Bootique.app("--config=src/test/resources/io/bootique/test3.yml")
                        .module(b -> BQCoreModule.extend(b).declareVar("a", "V1")
                                .declareVar("c.m.f", "V2")
                                .declareVar("c.m.k", "V3")
                                .setVar("V1", "K")
                                .setVar("V2", "K1")
                                .setVar("V3", "4")));

        O1 b1 = runtime.getInstance(ConfigurationFactory.class).config(O1.class, "");

        assertEquals("K", b1.a);
        assertEquals(4, b1.c.m.k);
        assertEquals("n", b1.c.m.l);
        assertEquals("K1", b1.c.m.f);
    }

    static class O1 {
        private String a;
        private O2 c;

        public void setA(String a) {
            this.a = a;
        }

        public void setC(O2 c) {
            this.c = c;
        }
    }

    static class O2 {

        private O3 m;

        public void setM(O3 m) {
            this.m = m;
        }
    }

    static class O3 {
        private int k;
        private String f;
        private String l;

        public void setK(int k) {
            this.k = k;
        }

        public void setF(String f) {
            this.f = f;
        }

        public void setL(String l) {
            this.l = l;
        }
    }
}

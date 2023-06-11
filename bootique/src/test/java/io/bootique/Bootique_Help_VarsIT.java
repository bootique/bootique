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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.command.CommandOutcome;
import io.bootique.di.BQModule;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class Bootique_Help_VarsIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void test() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O2.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .moduleProvider(configurableProvider)
                .bootLogger(logger)
                .module(b -> BQCoreModule.extend(b)
                        .declareVar("x.m", "X_VALID_VAR")
                        .declareVar("x.y.prop", "X_INVALID_VAR")
                ));

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    @Test
    public void test_SameVarTwoPaths() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configMetadataProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O4.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .bootLogger(logger)
                .moduleProvider(configMetadataProvider)
                .module(b -> BQCoreModule.extend(b)
                        .declareVar("x.p1", "VAR1")
                        .declareVar("x.p2", "VAR1")
                ));

        assertTrue(run.isSuccess());

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertEquals(1, countSubstring(help, "VAR1"), () -> "Unexpected number of VAR1 mentions in help:\n" + help);
    }

    @Test
    public void test_SameVarTwoPaths_Descriptions() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configMetadataProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O4.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .bootLogger(logger)
                .moduleProvider(configMetadataProvider)
                .module(b -> BQCoreModule.extend(b)
                        .declareVar("x.p1", "VAR1", "DP1")
                        .declareVar("x.p2", "VAR1", "DP2")
                ));

        assertTrue(run.isSuccess());

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);

        assertEquals(1, countSubstring(help, "DP1"), () -> "No 'DP1' description:\n" + help);
        assertEquals(1, countSubstring(help, "DP2"), () -> "No 'DP2' description:\n" + help);
    }

    @Test
    public void test_SameVarTwoIdenticalPaths_Descriptions() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configMetadataProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O4.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .bootLogger(logger)
                .moduleProvider(configMetadataProvider)
                .module(b -> BQCoreModule.extend(b)
                        .declareVar("x.p1", "VAR1", "DP1")
                        .declareVar("x.p1", "VAR1", "DP1")
                ));

        assertTrue(run.isSuccess());

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);

        assertEquals(1, countSubstring(help, "VAR1"), () -> "Unexpected number of VAR1 mentions in help:\n" + help);
        assertEquals(1, countSubstring(help, "DP1"), () -> "Only one 'DP1' description is expected:\n" + help);
    }

    @Test
    public void testDescription() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O2.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .moduleProvider(configurableProvider)
                .bootLogger(logger)
                .module(b -> BQCoreModule.extend(b).declareVar("x.m", "s", "New description")));

        assertTrue(run.isSuccess());

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("New description"));
        assertFalse(help.contains("Origin description"));
    }

    @Test
    public void testWithMap() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O1.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .bootLogger(logger)
                .moduleProvider(configurableProvider)
                .module(b -> BQCoreModule.extend(b)
                        .declareVar("x.m.prop", "X_VALID_VAR")
                        .declareVar("x.m.prop.x", "X_INVALID_VAR")));

        assertTrue(run.isSuccess());

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    @Test
    public void testWithList() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(System.err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", O3.class);
            }
        };

        CommandOutcome run = appManager.run(Bootique.app("--help")
                .moduleProvider(configurableProvider)
                .bootLogger(logger)
                .module(b -> BQCoreModule.extend(b)
                        .declareVar("x.a[0]", "X_VALID_VAR")
                        .declareVar("x.a[0].x", "X_INVALID_VAR")));

        assertTrue(run.isSuccess());

        String help = out.toString();
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    private static int countSubstring(String s, String ss) {
        int count = 0, pos = 0;
        while (pos >= 0) {
            pos = s.indexOf(ss, pos);
            if (pos >= 0) {
                count++;
                pos = pos + 4;
            }
        }

        return count;
    }

    @BQConfig
    static class O1 {
        private Map<String, String> m;

        @BQConfigProperty
        public void setM(Map<String, String> m) {
            this.m = m;
        }
    }

    @BQConfig
    static class O2 {
        private String m;

        @BQConfigProperty("Origin description")
        public void setM(String m) {
            this.m = m;
        }
    }

    @BQConfig
    static class O3 {
        private List<String> a;

        @BQConfigProperty
        public void setA(List<String> a) {
            this.a = a;
        }
    }

    @BQConfig
    static class O4 {
        private String p1;
        private String p2;
        private String p3;

        @BQConfigProperty
        public O4 setP1(String p1) {
            this.p1 = p1;
            return this;
        }

        @BQConfigProperty
        public O4 setP2(String p2) {
            this.p2 = p2;
            return this;
        }

        @BQConfigProperty
        public O4 setP3(String p3) {
            this.p3 = p3;
            return this;
        }
    }
}

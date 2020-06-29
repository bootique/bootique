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
import io.bootique.cli.Cli;
import io.bootique.di.BQModule;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Bootique_DeclareVarsIT {

    @RegisterExtension
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testInHelp() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", Bean5.class);
            }
        };

        BQRuntime runtime = testFactory.app()
                .moduleProvider(configurableProvider)
                .declareVar("x.m", "X_VALID_VAR")
                .declareVar("x.y.prop", "X_INVALID_VAR")
                .bootLogger(logger)
                .createRuntime();

        Cli cli = runtime.getInstance(Cli.class);
        runtime.getInstance(HelpCommand.class).run(cli);

        String help = new String(out.toByteArray());
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    @Test
    public void testInHelpDescription() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", Bean5.class);
            }
        };

        BQRuntime runtime = testFactory.app()
                .moduleProvider(configurableProvider)
                .declareVar("x.m", "s", "New description")
                .bootLogger(logger)
                .createRuntime();

        Cli cli = runtime.getInstance(Cli.class);
        runtime.getInstance(HelpCommand.class).run(cli);

        String help = new String(out.toByteArray());
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("New description"));
        assertFalse(help.contains("Origin description"));
    }

    @Test
    public void testInHelpWithMap() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public BQModule module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", Bean4.class);
            }
        };

        BQRuntime runtime = testFactory.app()
                .moduleProvider(configurableProvider)
                .declareVar("x.m.prop", "X_VALID_VAR")
                .declareVar("x.m.prop.x", "X_INVALID_VAR")
                .bootLogger(logger)
                .createRuntime();

        Cli cli = runtime.getInstance(Cli.class);
        runtime.getInstance(HelpCommand.class).run(cli);

        String help = new String(out.toByteArray());
        assertTrue(help.contains("ENVIRONMENT"), "No ENVIRONMENT section:\n" + help);
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    @BQConfig
    static class Bean4 {
        private Map<String, String> m;

        @BQConfigProperty
        public void setM(Map<String, String> m) {
            this.m = m;
        }
    }

    @BQConfig
    static class Bean5 {
        private String m;

        @BQConfigProperty("Origin description")
        public void setM(String m) {
            this.m = m;
        }
    }
}

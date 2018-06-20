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

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.config.ConfigurationFactory;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class BootiqueExceptionsHandlerIT {

    @Test
    public void testCli_BadOption() {
        CommandOutcome out = Bootique.app("-x").exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("x is not a recognized option", out.getMessage());
    }

    @Test
    public void testCli_TwoCommands() {
        CommandOutcome out = Bootique.app("-x", "-y")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(new Command() {

                            @Override
                            public CommandMetadata getMetadata() {
                                return CommandMetadata.builder("xcommand").build();
                            }

                            @Override
                            public CommandOutcome run(Cli cli) {
                                return CommandOutcome.succeeded();
                            }
                        })
                        .addCommand(new Command() {

                            @Override
                            public CommandMetadata getMetadata() {
                                return CommandMetadata.builder("ycommand").build();
                            }

                            @Override
                            public CommandOutcome run(Cli cli) {
                                return CommandOutcome.succeeded();
                            }
                        })
                )
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("CLI options match multiple commands: xcommand, ycommand.", out.getMessage());
    }

    @Test
    public void testConfig_FileNotFound() {
        CommandOutcome out = Bootique.app("-c", "com/foo/no_such_config.yml")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertTrue(out.getMessage(), out.getMessage().startsWith("Config resource is not found or is inaccessible: file:"));
        assertTrue(out.getMessage(), out.getMessage().endsWith("no_such_config.yml"));
    }

    @Test
    public void testConfig_BadUrl() {
        CommandOutcome out = Bootique.app("-c", "nosuchprotocol://myconfig")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("Invalid config resource url: nosuchprotocol://myconfig", out.getMessage());
    }

    @Test
    public void testConfig_BadUrlProtocol() {
        // underscores in protocol name cause IllegalArgumentException in URI
        CommandOutcome out = Bootique.app("-c", "no_such_protocol://myconfig")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("Invalid config resource url: no_such_protocol://myconfig", out.getMessage());
    }

    @Test
    public void testDI_ProviderMethodException() {
        CommandOutcome out = Bootique.app("-m")
                .module(new ModuleWithProviderMethodException())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("test provider exception", out.getMessage());
    }

    @Test
    public void testModules_CircularOverrides() {
        CommandOutcome out = Bootique.app()
                .module(new ModuleProviderWithOverride1())
                .module(new ModuleProviderWithOverride2())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());

        final String outMessage = out.getMessage();

        assertTrue(
            "Circular override dependency between DI modules: ModuleWithOverride2 -> ModuleWithOverride1 -> ModuleWithOverride2".equals(outMessage) ||
                "Circular override dependency between DI modules: ModuleWithOverride1 -> ModuleWithOverride2 -> ModuleWithOverride1".equals(outMessage)
        );
    }

    @Test
    public void testModules_MultipleOverrides() {
        CommandOutcome out = Bootique.app()
                .module(new CoreOverrideProvider1())
                .module(new CoreOverrideProvider2())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertTrue(out.getMessage(), out.getMessage()
                .startsWith("Module BQCoreModule provided by Bootique is overridden twice by " +
                        "BootiqueExceptionsHandlerIT"));
    }

    @Test
    public void testDI_TwoCommandsSameName() {
        CommandOutcome out = Bootique.app("-x")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(new Command() {

                            @Override
                            public CommandMetadata getMetadata() {
                                return CommandMetadata.builder("xcommand").build();
                            }

                            @Override
                            public CommandOutcome run(Cli cli) {
                                return CommandOutcome.succeeded();
                            }
                        })
                        .addCommand(new Command() {

                            @Override
                            public CommandMetadata getMetadata() {
                                return CommandMetadata.builder("xcommand").build();
                            }

                            @Override
                            public CommandOutcome run(Cli cli) {
                                return CommandOutcome.succeeded();
                            }
                        })
                )
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        String message = out.getMessage();
        assertTrue(message.startsWith("More than one DI command named 'xcommand'. Conflicting types: "));
        assertTrue(message.contains("io.bootique.BootiqueExceptionsHandlerIT$3"));
        assertTrue(message.contains("io.bootique.BootiqueExceptionsHandlerIT$4"));
    }

    public static class ConfigDependent {

        @Inject
        public ConfigDependent(ConfigurationFactory factory) {
        }
    }

    public static class ModuleWithProviderMethodException implements Module {

        public static class MyCommand implements Command {

            @Override
            public CommandMetadata getMetadata() {
                return CommandMetadata.builder(MyCommand.class).build();
            }

            @Override
            public CommandOutcome run(Cli cli) {
                return null;
            }
        }

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(MyCommand.class);
        }

        @Provides
        @Singleton
        public MyCommand provideCommand() {
            throw new BootiqueException(1, "test provider exception");
        }
    }

    public static class ModuleProviderWithOverride1 implements BQModuleProvider {

        public static class ModuleWithOverride1 implements Module {
            @Override
            public void configure(Binder binder) {

            }
        }

        @Override
        public Module module() {
            return new ModuleWithOverride1();
        }

        @Override
        public Collection<Class<? extends Module>> overrides() {
            return Collections.singleton(ModuleProviderWithOverride2.ModuleWithOverride2.class);
        }
    }

    public static class ModuleProviderWithOverride2 implements BQModuleProvider {

        public static class ModuleWithOverride2 implements Module {
            @Override
            public void configure(Binder binder) {

            }
        }

        @Override
        public Module module() {
            return new ModuleWithOverride2();
        }

        @Override
        public Collection<Class<? extends Module>> overrides() {
            return Collections.singleton(ModuleProviderWithOverride1.ModuleWithOverride1.class);
        }
    }

    public static class CoreOverrideProvider1 implements BQModuleProvider {

        @Override
        public Module module() {
            return b -> {
            };
        }

        @Override
        public Collection<Class<? extends Module>> overrides() {
            return Collections.singleton(BQCoreModule.class);
        }
    }

    public static class CoreOverrideProvider2 implements BQModuleProvider {

        @Override
        public Module module() {
            return b -> {
            };
        }

        @Override
        public Collection<Class<? extends Module>> overrides() {
            return Collections.singleton(BQCoreModule.class);
        }
    }
}

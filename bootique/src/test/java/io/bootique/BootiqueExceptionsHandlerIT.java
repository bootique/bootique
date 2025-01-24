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

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.meta.application.CommandMetadata;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BootiqueExceptionsHandlerIT {

    @Test
    public void cli_BadOption() {
        CommandOutcome out = Bootique.app("-x").exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("x is not a recognized option", out.getMessage());
    }

    @Test
    public void cli_TwoCommands() {
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
    public void config_FileNotFound() {
        CommandOutcome out = Bootique.app("-c", "com/foo/no_such_config.yml", "-t")
                .module(b -> b.bind(ConfigDependent.class))
                .module(b -> BQCoreModule.extend(b).addCommand(TestCommand.class))
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertTrue(out.getMessage().startsWith("Config resource is not found or is inaccessible: file:"), out.getMessage());
        assertTrue(out.getMessage().endsWith("no_such_config.yml"), out.getMessage());
    }

    @Test
    public void config_BadUrl() {
        CommandOutcome out = Bootique.app("-c", "nosuchprotocol://myconfig", "-t")
                .module(b -> b.bind(ConfigDependent.class))
                .module(b -> BQCoreModule.extend(b).addCommand(TestCommand.class))
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("Invalid config resource url: nosuchprotocol://myconfig", out.getMessage());
    }

    @Test
    public void config_BadUrlProtocol() {
        // underscores in protocol name cause IllegalArgumentException in URI
        CommandOutcome out = Bootique.app("-c", "no_such_protocol://myconfig", "-t")
                .module(b -> b.bind(ConfigDependent.class))
                .module(b -> BQCoreModule.extend(b).addCommand(TestCommand.class))
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("Invalid config resource url: no_such_protocol://myconfig", out.getMessage());
    }

    @Test
    public void dI_ProviderMethodBqException() {
        CommandOutcome out = Bootique.app("-m")
                .module(new ModuleWithProviderMethodBqException())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertEquals("test provider exception", out.getMessage());
    }

    @Test
    public void dI_ProviderMethodNPException() {
        CommandOutcome out = Bootique.app("-m")
                .module(new ModuleWithProviderMethodNPException())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNotNull(out.getException());
        assertEquals("Command exception: 'test NPE'.", out.getMessage());
    }

    @Test
    public void modules_CircularOverrides() {
        CommandOutcome out = Bootique.app()
                .module(new ModuleWithOverride1())
                .module(new ModuleWithOverride2())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());

        final String outMessage = out.getMessage();

        assertTrue(
                "Circular override dependency between DI modules: ModuleWithOverride2 -> ModuleWithOverride1".equals(outMessage) ||
                        "Circular override dependency between DI modules: ModuleWithOverride1 -> ModuleWithOverride2".equals(outMessage)
        );
    }

    @Test
    public void modules_MultipleOverrides() {
        CommandOutcome out = Bootique.app()
                .module(new CoreOverrideModule1())
                .module(new CoreOverrideModule2())
                .exec();

        assertEquals(1, out.getExitCode());
        assertNull(out.getException());
        assertTrue(out.getMessage().startsWith("Module BQCoreModule is overridden more than once. Last overriding module:"),
                out.getMessage());
    }

    @Test
    public void dI_TwoCommandsSameName() {
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

    public static class TestCommand extends CommandWithMetadata {

        @Inject
        Provider<ConfigDependent> configDependent;

        public TestCommand() {
            super(CommandMetadata.of("test"));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            configDependent.get();
            return CommandOutcome.succeeded();
        }
    }

    public static class ModuleWithProviderMethodBqException implements BQModule {

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(MyCommand.class);
        }

        @Provides
        @Singleton
        public MyCommand provideCommand() {
            throw new BootiqueException(1, "test provider exception");
        }

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
    }

    public static class ModuleWithProviderMethodNPException implements BQModule {

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(MyCommand.class);
        }

        @Provides
        @Singleton
        public MyCommand provideCommand() {
            throw new NullPointerException("test NPE");
        }

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
    }

    public static class ModuleWithOverride1 implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).overrides(ModuleWithOverride2.class).build();
        }
    }

    public static class ModuleWithOverride2 implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).overrides(ModuleWithOverride1.class).build();
        }
    }

    public static class CoreOverrideModule1 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).overrides(BQCoreModule.class).build();
        }
    }

    public static class CoreOverrideModule2 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).overrides(BQCoreModule.class).build();
        }
    }
}

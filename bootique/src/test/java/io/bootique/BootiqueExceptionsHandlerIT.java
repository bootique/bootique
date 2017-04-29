package io.bootique;

import com.google.inject.Binder;
import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.config.ConfigurationFactory;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BootiqueExceptionsHandlerIT {

    @Test
    public void testCli_BadOption() {
        CommandOutcome out = Bootique.app("-x").exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof ProvisionException);
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
        assertTrue(out.getException() instanceof ProvisionException);
        assertEquals("CLI options match multiple commands: xcommand, ycommand.", out.getMessage());
    }

    @Test
    public void testConfig_FileNotFound() {
        CommandOutcome out = Bootique.app("-c", "com/foo/no_such_config.yml")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof CreationException);
        assertTrue(out.getMessage(), out.getMessage().startsWith("Config resource is not found or is inaccessible: file:"));
        assertTrue(out.getMessage(), out.getMessage().endsWith("no_such_config.yml"));
    }

    @Test
    public void testConfig_BadUrl() {
        CommandOutcome out = Bootique.app("-c", "nosuchprotocol://myconfig")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof CreationException);
        assertEquals("Invalid config resource url: nosuchprotocol://myconfig", out.getMessage());
    }

    @Test
    public void testConfig_BadUrlProtocol() {
        // underscores in protocol name cause IllegalArgumentException in URI
        CommandOutcome out = Bootique.app("-c", "no_such_protocol://myconfig")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof CreationException);
        assertEquals("Invalid config resource url: no_such_protocol://myconfig", out.getMessage());
    }

    @Test
    public void testDI_ProviderMethodException() {
        CommandOutcome out = Bootique.app("-m")
                .module(new ModuleWithProviderMethodException())
                .exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof ProvisionException);
        assertEquals("test provider exception", out.getMessage());
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
        assertTrue(out.getException() instanceof ProvisionException);
        assertEquals("More than one DI command named 'xcommand'. Conflicting types: " +
                        "io.bootique.BootiqueExceptionsHandlerIT$4, io.bootique.BootiqueExceptionsHandlerIT$3.",
                out.getMessage());
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
            throw new BootiqueException(CommandOutcome.failed(1, "test provider exception"));
        }
    }

}

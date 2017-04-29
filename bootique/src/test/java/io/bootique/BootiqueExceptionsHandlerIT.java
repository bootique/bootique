package io.bootique;

import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.ProvisionException;
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
        assertTrue(out.getMessage().startsWith("Config resource 'file:"));
        assertTrue(out.getMessage().endsWith("no_such_config.yml' is not found."));
    }

    @Test
    public void testConfig_BadUrl() {
        CommandOutcome out = Bootique.app("-c", "nosuchprotocol://myconfig")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof CreationException);
        assertEquals("Invalid config resource 'nosuchprotocol://myconfig'.", out.getMessage());
    }

    @Test
    public void testConfig_BadUrlProtocol() {
        // underscores in protocol name cause IllegalArgumentException in URI
        CommandOutcome out = Bootique.app("-c", "no_such_protocol://myconfig")
                .module(b -> b.bind(ConfigDependent.class).asEagerSingleton())
                .exec();

        assertEquals(1, out.getExitCode());
        assertTrue(out.getException() instanceof CreationException);
        assertEquals("Invalid config resource 'no_such_protocol://myconfig'.", out.getMessage());
    }

    public static class ConfigDependent {

        @Inject
        public ConfigDependent(ConfigurationFactory factory) {
        }
    }
}

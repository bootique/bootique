package io.bootique;

import io.bootique.application.CommandMetadata;
import io.bootique.application.OptionMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.CliConfigurationSource;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Bootique_CliOptionsIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testConfigOption() {
        BQRuntime runtime = runtimeFactory.app("--config=abc.yml").createRuntime();
        assertEquals(runtime.getInstance(Cli.class).optionStrings(CliConfigurationSource.CONFIG_OPTION), "abc.yml");
    }

    @Test
    public void testConfigOptions() {
        BQRuntime runtime = runtimeFactory.app("--config=abc.yml", "--config=xyz.yml").createRuntime();
        assertEquals(runtime.getInstance(Cli.class).optionStrings(CliConfigurationSource.CONFIG_OPTION), "abc.yml",
                "xyz.yml");
    }

    @Test
    public void testHelpOption() {
        BQRuntime runtime = runtimeFactory.app("--help").createRuntime();
        assertTrue(runtime.getInstance(Cli.class).hasOption("help"));
    }

    @Test
    public void testNoHelpOption() {
        BQRuntime runtime = runtimeFactory.app("a", "b").createRuntime();
        assertFalse(runtime.getInstance(Cli.class).hasOption("help"));
    }

    private void assertEquals(Collection<String> result, String... expected) {
        assertArrayEquals(expected, result.toArray());
    }

    @Test
    public void testDefaultCommandOptions() {
        BQRuntime runtime = runtimeFactory.app("-l", "x", "--long=y", "-s")
                .module(binder -> BQCoreModule.setDefaultCommand(binder, TestCommand.class))
                .createRuntime();


        Cli cli = runtime.getInstance(Cli.class);

        assertTrue(cli.hasOption("s"));
        Assert.assertEquals("x_y", String.join("_", cli.optionStrings("long")));
    }

    static final class TestCommand extends CommandWithMetadata {

        public TestCommand() {
            super(CommandMetadata.builder(TestCommand.class)
                    .addOption(OptionMetadata.builder("long").valueRequired())
                    .addOption(OptionMetadata.builder("s")));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }
}

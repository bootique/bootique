package io.bootique;

import com.google.inject.ProvisionException;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.CliConfigurationSource;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Assert;
import org.junit.Ignore;
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
    public void testHelpOption_Short() {
        BQRuntime runtime = runtimeFactory.app("-h").createRuntime();
        assertTrue(runtime.getInstance(Cli.class).hasOption("help"));
    }

    @Test
    public void testNoHelpOption() {
        BQRuntime runtime = runtimeFactory.app("a", "b").createRuntime();
        assertFalse(runtime.getInstance(Cli.class).hasOption("help"));
    }

    @Test
    public void testOverlappingOptions() {
        BQRuntime runtime = runtimeFactory.app("--o1")
                .module(b -> {
                    BQCoreModule.contributeOptions(b).addBinding().toInstance(OptionMetadata.builder("o1").build());
                    BQCoreModule.contributeOptions(b).addBinding().toInstance(OptionMetadata.builder("o2").build());
                })
                .createRuntime();
        assertTrue(runtime.getInstance(Cli.class).hasOption("o1"));
        assertFalse(runtime.getInstance(Cli.class).hasOption("o2"));
    }

    // TODO: ignoring this test for now. There is a bug in JOpt 5.0.3...
    // JOpt should detect conflicting options and throw an exception. Instead JOpts triggers second option.
    @Test(expected = ProvisionException.class)
    @Ignore
    public void testOverlappingOptions_Short() {
        BQRuntime runtime = runtimeFactory.app("-o")
                .module(b -> {
                    BQCoreModule.contributeOptions(b).addBinding().toInstance(OptionMetadata.builder("o1").build());
                    BQCoreModule.contributeOptions(b).addBinding().toInstance(OptionMetadata.builder("o2").build());
                })
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test(expected = ProvisionException.class)
    public void testCommand_IllegalShort() {
        BQRuntime runtime = runtimeFactory.app("-x")
                .module(b -> BQCoreModule.contributeCommands(b).addBinding().to(XaCommand.class))
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test
    public void testCommand_ExplicitShort() {
        BQRuntime runtime = runtimeFactory.app("-A")
                .module(b -> BQCoreModule.contributeCommands(b).addBinding().to(XaCommand.class))
                .createRuntime();
        assertTrue(runtime.getInstance(Cli.class).hasOption("xa"));
    }

    @Test(expected = ProvisionException.class)
    public void testOverlappingCommands_IllegalShort() {
        BQRuntime runtime = runtimeFactory.app("-x")
                .module(b -> {
                    BQCoreModule.contributeCommands(b).addBinding().to(XaCommand.class);
                    BQCoreModule.contributeCommands(b).addBinding().to(XbCommand.class);
                })
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test(expected = ProvisionException.class)
    public void testIllegalAbbreviation() {
        BQRuntime runtime = runtimeFactory.app("--xc")
                .module(b -> BQCoreModule.contributeCommands(b).addBinding().to(XccCommand.class))
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test
    public void testOverlappingCommands_Short() {
        BQRuntime runtime = runtimeFactory.app("-A")
                .module(b -> {
                    BQCoreModule.contributeCommands(b).addBinding().to(XaCommand.class);
                    BQCoreModule.contributeCommands(b).addBinding().to(XbCommand.class);
                })
                .createRuntime();

        assertTrue(runtime.getInstance(Cli.class).hasOption("xa"));
        assertFalse(runtime.getInstance(Cli.class).hasOption("xb"));
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

    private void assertEquals(Collection<String> result, String... expected) {
        assertArrayEquals(expected, result.toArray());
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

    static final class XaCommand extends CommandWithMetadata {

        public XaCommand() {
            super(CommandMetadata.builder(XaCommand.class).shortName('A'));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XbCommand extends CommandWithMetadata {

        public XbCommand() {
            super(CommandMetadata.builder(XbCommand.class).shortName('B'));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XccCommand extends CommandWithMetadata {

        public XccCommand() {
            super(CommandMetadata.builder(XccCommand.class).shortName('B'));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }
}

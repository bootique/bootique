package io.bootique;

import com.google.inject.ProvisionException;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.CliConfigurationSource;
import io.bootique.config.ConfigurationFactory;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
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
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("o1").build(),
                        OptionMetadata.builder("o2").build()
                ))
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
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("o1").build(),
                        OptionMetadata.builder("o2").build()
                ))
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test(expected = ProvisionException.class)
    public void testCommand_IllegalShort() {
        BQRuntime runtime = runtimeFactory.app("-x")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class))
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test
    public void testCommand_ExplicitShort() {
        BQRuntime runtime = runtimeFactory.app("-A")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class))
                .createRuntime();
        assertTrue(runtime.getInstance(Cli.class).hasOption("xa"));
    }

    @Test(expected = ProvisionException.class)
    public void testOverlappingCommands_IllegalShort() {
        BQRuntime runtime = runtimeFactory.app("-x")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class).addCommand(XbCommand.class))
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test(expected = ProvisionException.class)
    public void testIllegalAbbreviation() {
        BQRuntime runtime = runtimeFactory.app("--xc")
                .module(b -> BQCoreModule.extend(b).addCommand(XccCommand.class))
                .createRuntime();
        runtime.getInstance(Cli.class);
    }

    @Test
    public void testOverlappingCommands_Short() {
        BQRuntime runtime = runtimeFactory.app("-A")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class).addCommand(XbCommand.class))
                .createRuntime();

        assertTrue(runtime.getInstance(Cli.class).hasOption("xa"));
        assertFalse(runtime.getInstance(Cli.class).hasOption("xb"));
    }

    @Test
    public void testDefaultCommandOptions() {
        BQRuntime runtime = runtimeFactory.app("-l", "x", "--long=y", "-s")
                .module(binder -> BQCoreModule.extend(binder).setDefaultCommand(TestCommand.class))
                .createRuntime();


        Cli cli = runtime.getInstance(Cli.class);

        assertTrue(cli.hasOption("s"));
        Assert.assertEquals("x_y", String.join("_", cli.optionStrings("long")));
    }

    @Test
    public void testOption_OverrideConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.l", "opt-1")
                        .addOption("c.m.k", "opt-2"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        Assert.assertEquals("x", bean1.c.m.l);
    }

    @Test
    public void testOptionAbsentInYAML() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(binder -> BQCoreModule.extend(binder)
                        .addOption("c.m.f", "opt-1"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        Assert.assertEquals("x", bean1.c.m.f);
    }

    @Test
    public void testConfigOverrideOrder_PropsVarsOptions() {
        System.setProperty("bq.c.m.l", "prop_c_m_l");

        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=Option")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.l", "opt-1")
                        .setVar("BQ_C_M_L", "var_c_m_l"))
                .createRuntime();

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
        Assert.assertEquals("Option", bean1.c.m.l);

        System.clearProperty("bq.c.m.l");
    }

    @Test(expected = ProvisionException.class)
    public void testOptionsWithOverlappingPathOnCLI_NotAllowed() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-2", "--opt-3")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k", "opt-1")
                        .addOption("c.m.k", "2", "opt-2")
                        .addOption("c.m.k", "3", "opt-3"))
                .createRuntime();

        runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
    }

    @Test
    public void testOptionsWithOverlappingPathOneOnCLI_OverrideConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-2")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k", "opt-1")
                        .addOption("c.m.k", "2", "opt-2")
                        .addOption("c.m.k", "3", "opt-3"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        Assert.assertEquals(2, bean1.c.m.k);
    }

    @Test(expected = ProvisionException.class)
    public void testOptionsNamesDuplicate() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k", "0", "opt-1")
                        .addOption("c.m.l", "test", "opt-1"))
                .createRuntime();

        runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
    }

    @Test(expected = ProvisionException.class)
    public void testOptionsNamesDuplicatePathTheSame_NotAllowed() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k", "0", "opt-1")
                        .addOption("c.m.k", "0", "opt-1"))
                .createRuntime();

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
        Assert.assertEquals(0, bean1.c.m.k);
    }

    @Test(expected = ProvisionException.class)
    public void testOptionWithNotMappedConfigPath() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k.x", "opt-1"))
                .createRuntime();

        runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
    }

    @Test
    public void testConfigFileOption_OverrideConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt")
                .module(binder -> BQCoreModule.extend(binder)
                        .addConfigFileOption("classpath:io/bootique/config/configTest4.yml", "file-opt"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        Assert.assertEquals("x", bean1.c.m.l);
    }

    @Test
    public void testMultipleConfigFileOption_OverrideInCLIOrder() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt-2", "--file-opt-1")
                .module(binder -> BQCoreModule.extend(binder)
                        .addConfigFileOption("classpath:io/bootique/config/configTest4Opt1.yml", "file-opt-1")
                        .addConfigFileOption("classpath:io/bootique/config/configTest4Opt2.yml", "file-opt-2"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        Assert.assertEquals(3, bean1.c.m.k);
        Assert.assertEquals("f", bean1.c.m.f);
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

    static class Bean1 {
        private String a;
        private Bean2 c;

        public void setA(String a) {
            this.a = a;
        }

        public void setC(Bean2 c) {
            this.c = c;
        }
    }

    static class Bean2 {

        private Bean3 m;

        public void setM(Bean3 m) {
            this.m = m;
        }
    }

    static class Bean3 {
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

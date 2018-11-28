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

import com.google.inject.ProvisionException;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.CliConfigurationSource;
import io.bootique.config.ConfigurationFactory;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.run.Runner;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class Bootique_CliOptionsIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testConfigOption() {
        BQRuntime runtime = runtimeFactory.app("--config=abc.yml").createRuntime();
        assertCollectionsEquals(runtime.getInstance(Cli.class).optionStrings(CliConfigurationSource.CONFIG_OPTION), "abc.yml");
    }

    @Test
    public void testConfigOptions() {
        BQRuntime runtime = runtimeFactory.app("--config=abc.yml", "--config=xyz.yml").createRuntime();
        assertCollectionsEquals(runtime.getInstance(Cli.class).optionStrings(CliConfigurationSource.CONFIG_OPTION), "abc.yml",
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

    @Test(expected = ProvisionException.class)
    public void testNameConflict_TwoOptions() {
        runtimeFactory.app()
                .module(b -> BQCoreModule.extend(b)
                        .addOption(OptionMetadata.builder("opt1").build())
                        .addOption(OptionMetadata.builder("opt1").build()))
                .createRuntime()
                .run();
    }

    @Test(expected = ProvisionException.class)
    public void testNameConflict_TwoCommands() {
        runtimeFactory.app()
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(Xd1Command.class)
                        .addCommand(Xd2Command.class))
                .createRuntime()
                .run();
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
        assertEquals("x_y", String.join("_", cli.optionStrings("long")));
    }

    @Test
    public void testOption_OverrideConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(binder -> BQCoreModule
                        .extend(binder)
                        .addOption("c.m.l", "opt-1")
                        .addOption("c.m.k", "2", "opt-2"))
                .createRuntime();

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");


        assertEquals("e", bean1.a);
        assertEquals("x", bean1.c.m.l);
        assertEquals(1, bean1.c.m.k);
    }

    @Test
    public void testOptionPathAbsentInYAML() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(binder -> BQCoreModule
                        .extend(binder)
                        .addOption("c.m.f", "opt-1"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("x", bean1.c.m.f);
    }

    @Test
    public void testOptionsCommandAndModuleOverlapping() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--cmd-1", "--opt-1")
                .module(binder -> BQCoreModule.extend(binder)
                        .addOption("c.m.k", "2", "opt-1")
                        .addCommand(new TestOptionCommand1()))
                .createRuntime();

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
        Runner runner = runtime.getInstance(Runner.class);

        runner.run();

        assertEquals(2, bean1.c.m.k);
    }

    @Test
    public void testOptionsOrder_OnCLI() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt-1",
                "--opt-2=y", "--opt-1=x")
                .module(binder -> BQCoreModule.extend(binder)
                        .addConfig("classpath:io/bootique/config/test4Copy.yml")
                        .addOption("c.m.f", "opt-1")
                        .addOption("c.m.f", "opt-2")
                        .addOption(OptionMetadata.builder("file-opt-1").build())
                        .addConfigOnOption("file-opt-1", "classpath:io/bootique/config/configTest4Opt1.yml")
                        .addConfigOnOption("file-opt-1", "classpath:io/bootique/config/configTest4Decorate.yml"))
                .createRuntime();

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
        assertEquals(4, bean1.c.m.k);
        assertEquals("x", bean1.c.m.f);
        assertEquals("copy", bean1.c.m.l);
        assertEquals("e", bean1.a);
    }

    @Test
    public void testOptionsWithOverlappingPath_OverrideConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-2", "--opt-3")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k", "opt-1")
                        .addOption("c.m.k", "2", "opt-2")
                        .addOption("c.m.k", "3", "opt-3"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals(3, bean1.c.m.k);
    }

    @Test(expected = ProvisionException.class)
    public void testOptionWithNotMappedConfigPath() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(binder -> BQCoreModule.extend(binder).addOption("c.m.k.x", "opt-1"))
                .createRuntime();

        runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
    }

    @Test
    public void testOptionConfigFile_OverrideConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt")
                .module(binder -> BQCoreModule.extend(binder)
                        .addOption(OptionMetadata.builder("file-opt").build())
                        .addConfigOnOption("file-opt", "classpath:io/bootique/config/configTest4.yml"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("x", bean1.c.m.l);
    }

    @Test
    public void testMultipleOptionsConfigFiles_OverrideInCLIOrder() {
        BQRuntime runtime = runtimeFactory.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt-2", "--file-opt-1")
                .module(binder -> BQCoreModule.extend(binder)
                        .addOption(OptionMetadata.builder("file-opt-1").build())
                        .addOption(OptionMetadata.builder("file-opt-2").build())
                        .addOption("c.m.f", "opt-1")
                        .addConfigOnOption("file-opt-1", "classpath:io/bootique/config/configTest4Opt1.yml")
                        .addConfigOnOption("file-opt-2", "classpath:io/bootique/config/configTest4Opt2.yml"))
                .createRuntime();
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals(2, bean1.c.m.k);
        assertEquals("f", bean1.c.m.f);
    }

    @Test
    public void testOptionDefaultValue() {
        BQRuntime runtime = runtimeFactory.app("--option")
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("option").defaultValue("val").build()
                ))
                .createRuntime();
        Cli cli = runtime.getInstance(Cli.class);
        assertTrue(cli.hasOption("option"));
        assertEquals("val", cli.optionString("option"));
    }

    @Test
    public void testMissingOptionDefaultValue() {
        BQRuntime runtime = runtimeFactory.app()
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("option").defaultValue("val").build()
                ))
                .createRuntime();
        Cli cli = runtime.getInstance(Cli.class);
        // Check that no value is set if option is missing in args
        assertFalse(cli.hasOption("option"));
        assertNull(cli.optionString("option"));
    }

    @Test
    public void testCommandWithOptionWithDefaultValue() {
        BQRuntime runtime = runtimeFactory.app("-cmd", "--option")
                .module(b -> BQCoreModule.extend(b).addCommand(CommandWithDefaultOptionValue.class))
                .createRuntime();

        Cli cli = runtime.getInstance(Cli.class);
        assertTrue(cli.hasOption("o"));
        assertEquals("val", cli.optionString("o"));
    }

    private void assertCollectionsEquals(Collection<String> result, String... expected) {
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

    static final class Xd1Command extends CommandWithMetadata {

        public Xd1Command() {
            super(CommandMetadata.builder("xd"));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class Xd2Command extends CommandWithMetadata {

        public Xd2Command() {
            super(CommandMetadata.builder("xd"));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XeCommand extends CommandWithMetadata {

        public XeCommand() {
            super(CommandMetadata.builder("xe").addOption(OptionMetadata.builder("opt1").build()));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class TestOptionCommand1 extends CommandWithMetadata {

        public TestOptionCommand1() {
            super(CommandMetadata.builder(TestOptionCommand1.class)
                    .name("cmd-1")
                    .addOption(OptionMetadata.builder("opt-1")
                            .configPath("c.m.f").defaultValue("3")));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class CommandWithDefaultOptionValue extends CommandWithMetadata {

        public CommandWithDefaultOptionValue() {
            super(CommandMetadata.builder("cmd")
                    .addOption(OptionMetadata.builder("option").defaultValue("val").build()));
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

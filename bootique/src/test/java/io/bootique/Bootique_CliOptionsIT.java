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
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.jackson.CliConfigurationLoader;
import io.bootique.di.DIRuntimeException;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.run.Runner;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class Bootique_CliOptionsIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void configOption() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=abc.yml"));
        assertCollectionsEquals(runtime.getInstance(Cli.class).optionStrings(CliConfigurationLoader.CONFIG_OPTION), "abc.yml");
    }

    @Test
    public void configOptions() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=abc.yml", "--config=xyz.yml"));
        assertCollectionsEquals(runtime.getInstance(Cli.class).optionStrings(CliConfigurationLoader.CONFIG_OPTION), "abc.yml",
                "xyz.yml");
    }

    @Test
    public void helpOption() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--help"));
        assertTrue(runtime.getInstance(Cli.class).hasOption("help"));
    }

    @Test
    public void helpOption_Short() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-h"));
        assertTrue(runtime.getInstance(Cli.class).hasOption("help"));
    }

    @Test
    public void noHelpOption() {
        BQRuntime runtime = appManager.runtime(Bootique.app("a", "b"));
        assertFalse(runtime.getInstance(Cli.class).hasOption("help"));
    }

    @Test
    public void overlappingOptions() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--o1")
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("o1").build(),
                        OptionMetadata.builder("o2").build()
                )));
        assertTrue(runtime.getInstance(Cli.class).hasOption("o1"));
        assertFalse(runtime.getInstance(Cli.class).hasOption("o2"));
    }

    @Test
    public void nameConflict_TwoOptions() {
        assertThrows(DIRuntimeException.class, () -> appManager.runtime(Bootique.app()
                        .module(b -> BQCoreModule.extend(b)
                                .addOptions(
                                        OptionMetadata.builder("opt1").build(),
                                        OptionMetadata.builder("opt1").build())))
                .run());
    }

    @Test
    public void nameConflict_TwoCommands() {
        assertThrows(DIRuntimeException.class, () -> appManager.runtime(Bootique.app()
                        .module(b -> BQCoreModule.extend(b)
                                .addCommand(Xd1Command.class)
                                .addCommand(Xd2Command.class)))
                .run());
    }

    // TODO: ignoring this test for now. There is a bug in JOpt 5.0.3...
    //       JOpt should detect conflicting options and throw an exception. Instead JOpts triggers second option.
    @Test
    @Disabled
    public void testOverlappingOptions_Short() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-o")
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("o1").build(),
                        OptionMetadata.builder("o2").build()
                )));
        assertThrows(DIRuntimeException.class, () -> runtime.getInstance(Cli.class));
    }

    // TODO: Same name of option and command should be disallowed.
    //  This test is broken, it is here just to document current behaviour.
    @Test
    public void commandWithOptionNameOverlap() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-x")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(Xd1Command.class)
                        .addOption(OptionMetadata.builder("xd").build())
                ));
        runtime.run();
        assertTrue(runtime.getInstance(Cli.class).hasOption("xd"));
    }

    @Test
    public void command_IllegalShort() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-x")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class)));
        assertThrows(DIRuntimeException.class, () -> runtime.getInstance(Cli.class));
    }

    @Test
    public void command_ExplicitShort() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-A")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class)));
        assertTrue(runtime.getInstance(Cli.class).hasOption("xa"));
    }

    @Test
    public void overlappingCommands_IllegalShort() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-x")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class).addCommand(XbCommand.class)));
        assertThrows(DIRuntimeException.class, () -> runtime.getInstance(Cli.class));
    }

    @Test
    public void illegalAbbreviation() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--xc")
                .module(b -> BQCoreModule.extend(b).addCommand(XccCommand.class)));
        assertThrows(DIRuntimeException.class, () -> runtime.getInstance(Cli.class));
    }

    @Test
    public void overlappingCommands_Short() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-A")
                .module(b -> BQCoreModule.extend(b).addCommand(XaCommand.class).addCommand(XbCommand.class)));

        assertTrue(runtime.getInstance(Cli.class).hasOption("xa"));
        assertFalse(runtime.getInstance(Cli.class).hasOption("xb"));
    }

    @Test
    public void defaultCommandOptions() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-l", "x", "--long=y", "-s")
                .module(b -> BQCoreModule.extend(b).setDefaultCommand(TestCommand.class)));


        Cli cli = runtime.getInstance(Cli.class);

        assertTrue(cli.hasOption("s"));
        assertEquals("x_y", String.join("_", cli.optionStrings("long")));
    }

    @Test
    public void option_OverrideConfig() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(b -> BQCoreModule
                        .extend(b)
                        .addOptions(OptionMetadata.builder("opt-1").valueOptional().build(),
                                OptionMetadata.builder("opt-2").valueOptionalWithDefault("2").build())
                        .mapConfigPath("opt-1", "c.m.l")
                        .mapConfigPath("opt-2", "c.m.k")));

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("e", bean1.a);
        assertEquals("x", bean1.c.m.l);
        assertEquals(1, bean1.c.m.k);
    }

    @Test
    public void optionPathAbsentInYAML() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(b -> BQCoreModule
                        .extend(b)
                        .addOption(OptionMetadata.builder("opt-1").valueOptional().build())
                        .mapConfigPath("opt-1", "c.m.f")));
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("x", bean1.c.m.f);
    }

    @Test
    public void optionsCommandAndModuleOverlapping() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--cmd-1", "--opt-1")
                .module(binder -> BQCoreModule.extend(binder)
                        .addOption(OptionMetadata.builder("opt-1").valueOptionalWithDefault("2").build())
                        .mapConfigPath("opt-1", "c.m.k")
                        .addCommand(new TestOptionCommand1())));

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
        Runner runner = runtime.getInstance(Runner.class);

        runner.run();

        assertEquals(2, bean1.c.m.k);
    }

    @Test
    public void optionsOrder_OnCLI() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt-1",
                        "--opt-2=y", "--opt-1=x")
                .module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/config/test4Copy.yml")
                        .addOptions(OptionMetadata.builder("opt-1").valueOptional().build(),
                                OptionMetadata.builder("opt-2").valueOptional().build(),
                                OptionMetadata.builder("file-opt-1").build())
                        .mapConfigPath("opt-1", "c.m.f")
                        .mapConfigPath("opt-2", "c.m.f")
                        .mapConfigResource("file-opt-1", "classpath:io/bootique/config/configTest4Opt1.yml")
                        .mapConfigResource("file-opt-1", "classpath:io/bootique/config/configTest4Decorate.yml")));

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
        assertEquals(4, bean1.c.m.k);
        assertEquals("x", bean1.c.m.f);
        assertEquals("copy", bean1.c.m.l);
        assertEquals("e", bean1.a);
    }

    @Test
    public void optionsWithOverlappingPath_OverrideConfig() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--opt-2", "--opt-3")
                .module(b -> BQCoreModule.extend(b)
                        .addOptions(OptionMetadata.builder("opt-1").valueOptional().build(),
                                OptionMetadata.builder("opt-2").valueOptionalWithDefault("2").build(),
                                OptionMetadata.builder("opt-3").valueOptionalWithDefault("3").build())
                        .mapConfigPath("opt-1", "c.m.k")
                        .mapConfigPath("opt-2", "c.m.k")
                        .mapConfigPath("opt-3", "c.m.k")));
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals(3, bean1.c.m.k);
    }

    @Test
    public void optionWithNotMappedConfigPath() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(b -> BQCoreModule.extend(b)
                        .mapConfigPath("opt-1", "c.m.k.x")));

        assertThrows(DIRuntimeException.class, () -> runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, ""));
    }

    @Test
    public void optionConfigFile_OverrideConfig() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt")
                .module(b -> BQCoreModule.extend(b)
                        .addOption(OptionMetadata.builder("file-opt").build())
                        .mapConfigResource("file-opt", "classpath:io/bootique/config/configTest4.yml")));
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("x", bean1.c.m.l);
    }

    @Test
    public void multipleOptionsConfigFiles_OverrideInCLIOrder() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt-2", "--file-opt-1")
                .module(b -> BQCoreModule.extend(b)
                        .addOptions(OptionMetadata.builder("file-opt-1").build(),
                                OptionMetadata.builder("file-opt-2").build())
                        .mapConfigPath("opt-1", "c.m.f")
                        .mapConfigResource("file-opt-1", "classpath:io/bootique/config/configTest4Opt1.yml")
                        .mapConfigResource("file-opt-2", "classpath:io/bootique/config/configTest4Opt2.yml")));
        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals(2, bean1.c.m.k);
        assertEquals("f", bean1.c.m.f);
    }

    @Test
    public void optionDefaultValue() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--option")
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("option").valueOptionalWithDefault("val").build()
                )));
        Cli cli = runtime.getInstance(Cli.class);
        assertTrue(cli.hasOption("option"));
        assertEquals("val", cli.optionString("option"));
    }

    @Test
    public void missingOptionDefaultValue() {
        BQRuntime runtime = appManager.runtime(Bootique.app()
                .module(b -> BQCoreModule.extend(b).addOptions(
                        OptionMetadata.builder("option").valueOptionalWithDefault("val").build()
                )));
        Cli cli = runtime.getInstance(Cli.class);
        // Check that no value is set if option is missing in args
        assertFalse(cli.hasOption("option"));
        assertNull(cli.optionString("option"));
    }

    @Test
    public void commandWithOptionWithDefaultValue() {
        BQRuntime runtime = appManager.runtime(Bootique.app("-cmd", "--option")
                .module(b -> BQCoreModule.extend(b).addCommand(CommandWithDefaultOptionValue.class)));

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
                    .addOption(OptionMetadata.builder("s")).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XaCommand extends CommandWithMetadata {

        public XaCommand() {
            super(CommandMetadata.builder(XaCommand.class).shortName('A').build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XbCommand extends CommandWithMetadata {

        public XbCommand() {
            super(CommandMetadata.builder(XbCommand.class).shortName('B').build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XccCommand extends CommandWithMetadata {

        public XccCommand() {
            super(CommandMetadata.builder(XccCommand.class).shortName('B').build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class Xd1Command extends CommandWithMetadata {

        public Xd1Command() {
            super(CommandMetadata.of("xd"));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class Xd2Command extends CommandWithMetadata {

        public Xd2Command() {
            super(CommandMetadata.of("xd"));
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class XeCommand extends CommandWithMetadata {

        public XeCommand() {
            super(CommandMetadata.builder("xe")
                    .addOption(OptionMetadata.builder("opt1").build()).build());
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
                    .addOption(OptionMetadata.builder("opt-1").build()).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static final class CommandWithDefaultOptionValue extends CommandWithMetadata {

        public CommandWithDefaultOptionValue() {
            super(CommandMetadata.builder("cmd")
                    .addOption(OptionMetadata.builder("option").valueOptionalWithDefault("val").build()).build());
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

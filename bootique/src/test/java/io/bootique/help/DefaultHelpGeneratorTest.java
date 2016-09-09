package io.bootique.help;

import io.bootique.cli.meta.CliApplication;
import io.bootique.cli.meta.CliOption;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultHelpGeneratorTest {

    private static void assertLines(DefaultHelpGenerator generator, String... expectedLines) {

        StringBuilder expected = new StringBuilder();
        for (String s : expectedLines) {
            expected.append(s).append(FormattedAppender.NEWLINE);
        }

        String help = generator.generate();
        assertNotNull(help);
        assertEquals(expected.toString(), help);
    }

    @Test
    public void testGenerate_Name() {

        CliApplication app = CliApplication.builder("myapp").build();

        assertLines(new DefaultHelpGenerator(app),
                "NAME",
                "      myapp"
        );
    }

    @Test
    public void testGenerate_Name_Description() {

        CliApplication app = CliApplication.builder("myapp", "this is my app").build();

        assertLines(new DefaultHelpGenerator(app),
                "NAME",
                "      myapp: this is my app"
        );
    }

    @Test
    public void testGenerate_Name_Options() {

        CliOption listOpt = CliOption.builder("list", "Lists everything").build();
        CliOption runOpt = CliOption.builder("run", "Runs specified command").valueRequired().build();
        CliOption debugOpt = CliOption.builder("debug", "Switches to debug mode").valueOptional("level").build();

        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(listOpt)
                .addOption(runOpt)
                .addOption(debugOpt)
                .build();

        assertLines(new DefaultHelpGenerator(app),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -d [level], --debug[=level]",
                "           Switches to debug mode",
                "      -l, --list",
                "           Lists everything",
                "      -r val, --run=val",
                "           Runs specified command"
        );
    }

    @Test
    public void testGenerate_Name_Options_ShortNameConflict() {

        CliOption resetOpt = CliOption.builder("reset", "Resets everything").build();
        CliOption listOpt = CliOption.builder("list", "Lists everything").build();
        CliOption runOpt = CliOption.builder("run", "Runs everything").build();
        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(resetOpt)
                .addOption(listOpt)
                .addOption(runOpt)
                .build();

        assertLines(new DefaultHelpGenerator(app),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -l, --list",
                "           Lists everything",
                "      --reset",
                "           Resets everything",
                "      --run",
                "           Runs everything"
        );
    }
}

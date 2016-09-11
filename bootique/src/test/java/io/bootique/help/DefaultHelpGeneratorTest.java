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

        assertLines(new DefaultHelpGenerator(app, 80),
                "NAME",
                "      myapp"
        );
    }

    @Test
    public void testGenerate_Name_Description() {

        CliApplication app = CliApplication.builder("myapp", "this is my app").build();

        assertLines(new DefaultHelpGenerator(app, 80),
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

        assertLines(new DefaultHelpGenerator(app, 80),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -d [level], --debug[=level]",
                "           Switches to debug mode",
                "",
                "      -l, --list",
                "           Lists everything",
                "",
                "      -r val, --run=val",
                "           Runs specified command"
        );
    }

    @Test
    public void testGenerate_Name_ShortOptions() {

        CliOption listOpt = CliOption.builder("l", "Lists everything").build();
        CliOption runOpt = CliOption.builder("r", "Runs specified command").valueRequired().build();
        CliOption debugOpt = CliOption.builder("debug", "Switches to debug mode").valueOptional("level").build();

        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(listOpt)
                .addOption(runOpt)
                .addOption(debugOpt)
                .build();

        assertLines(new DefaultHelpGenerator(app, 80),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -d [level], --debug[=level]",
                "           Switches to debug mode",
                "",
                "      -l",
                "           Lists everything",
                "",
                "      -r val",
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

        assertLines(new DefaultHelpGenerator(app, 80),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -l, --list",
                "           Lists everything",
                "",
                "      --reset",
                "           Resets everything",
                "",
                "      --run",
                "           Runs everything"
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_LineFoldingTooShort() {
        CliApplication app = CliApplication.builder("myapp").build();
        new DefaultHelpGenerator(app, 30).generate();
    }

    @Test
    public void testGenerate_TrimSpace() {

        CliOption opt1 = CliOption.builder("a1", "  Word1 word2 \t\n").build();

        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(opt1)
                .build();

        assertLines(new DefaultHelpGenerator(app, 80),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -a, --a1",
                "           Word1 word2"
        );
    }

    @Test
    public void testGenerate_LineFolding() {

        CliOption opt1 = CliOption.builder("a1", "Word1 word2 word3 word4 longword5 longerword6 morewords7").build();
        CliOption opt2 = CliOption.builder("b1", "Word8 word9 word10 word11 longword12 longerword13 morewords14").build();
        CliOption opt3 = CliOption.builder("c1", "Word15, word16").build();

        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(opt1)
                .addOption(opt2)
                .addOption(opt3)
                .build();

        assertLines(new DefaultHelpGenerator(app, 41),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -a, --a1",
                "           Word1 word2 word3 word4",
                "           longword5 longerword6",
                "           morewords7",
                "",
                "      -b, --b1",
                "           Word8 word9 word10 word11",
                "           longword12 longerword13",
                "           morewords14",
                "",
                "      -c, --c1",
                "           Word15, word16"
        );
    }

    @Test
    public void testGenerate_LineFolding_VeryLongWord() {

        CliOption opt1 = CliOption.builder("a1", "Word1_word2_word3_word4_longword5_longerword6_morewords7").build();
        CliOption opt2 = CliOption.builder("b1", "Word1 word2_word3_word4_longword5_longerword6_morewords7").build();
        CliOption opt3 = CliOption.builder("c1", "Word1_word2_word3_word4_longword5_longerword6 morewords7").build();

        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(opt1)
                .addOption(opt2)
                .addOption(opt3)
                .build();

        // must insert a forced break...
        assertLines(new DefaultHelpGenerator(app, 41),
                "NAME",
                "      myapp",
                "",
                "OPTIONS",
                "      -a, --a1",
                "           Word1_word2_word3_word4_longwo",
                "           rd5_longerword6_morewords7",
                "",
                "      -b, --b1",
                "           Word1 word2_word3_word4_longwo",
                "           rd5_longerword6_morewords7",
                "",
                "      -c, --c1",
                "           Word1_word2_word3_word4_longwo",
                "           rd5_longerword6 morewords7"
        );
    }
}

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
                "   myapp"
        );
    }

    @Test
    public void testGenerate_Name_Description() {

        CliApplication app = CliApplication.builder("myapp", "this is my app").build();

        assertLines(new DefaultHelpGenerator(app),
                "NAME",
                "   myapp: this is my app"
        );
    }

    @Test
    public void testGenerate_Name_Options() {

        CliOption listOpt = CliOption.builder("list", "Lists everything").build();
        CliOption runOpt = CliOption.builder("run", "Runs everything").build();
        CliApplication app = CliApplication
                .builder("myapp")
                .addOption(listOpt)
                .addOption(runOpt)
                .build();

        assertLines(new DefaultHelpGenerator(app),
                "NAME",
                "   myapp",
                "",
                "OPTIONS",
                "   -l, --list",
                "        Lists everything",
                "   -r, --run",
                "        Runs everything"
        );
    }


}

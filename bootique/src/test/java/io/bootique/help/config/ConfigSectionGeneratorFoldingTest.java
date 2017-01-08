package io.bootique.help.config;

import io.bootique.Bootique;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import org.junit.Test;

public class ConfigSectionGeneratorFoldingTest {


    @Test
    public void testVisitObjectConfig() {

        ConfigValueMetadata px = ConfigValueMetadata.builder("px")
                .type(Bootique.class)
                .description("Description line 1. Description line 2. Description line 3.").build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigSectionGeneratorTest.ConfigRoot1.class)
                .addProperty(px)
                .build();

        ConfigSectionGeneratorTest.assertLines(m1Config, 30,
                "m1root:",
                "      #",
                "      # Root config of M1",
                "      # Resolved as 'io.bootiq",
                "      # ue.help.config.ConfigS",
                "      # ectionGeneratorTest$Co",
                "      # nfigRoot1'.",
                "      #",
                "",
                "      # Description line 1.",
                "      # Description line 2.",
                "      # Description line 3.",
                "      # Resolved as 'io.bootiq",
                "      # ue.Bootique'.",
                "      px: <value>"
        );
    }
}

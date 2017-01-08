package io.bootique.help.config;

import io.bootique.Bootique;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultConfigHelpGeneratorTest {

    static final String NEWLINE = System.getProperty("line.separator");


    private static void assertLines(DefaultConfigHelpGenerator generator, String... expectedLines) {

        StringBuilder expected = new StringBuilder();
        for (String s : expectedLines) {
            expected.append(s).append(NEWLINE);
        }

        String help = generator.generate();
        assertNotNull(help);
        assertEquals(expected.toString(), help);
    }

    @Test
    public void testGenerate_Empty() {
        ModulesMetadata modules = ModulesMetadata.builder().build();
        assertLines(new DefaultConfigHelpGenerator(modules, 80));
    }

    @Test
    public void testGenerate_Name() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").build();
        ModulesMetadata modules = ModulesMetadata.builder(module1).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1"
        );
    }

    @Test
    public void testGenerate_Name_MultiModule() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").build();
        ModulesMetadata modules = ModulesMetadata.builder(module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1",
                "",
                "      M2"
        );
    }

    @Test
    public void testGenerate_Name_MultiModule_Sorting() {

        ModuleMetadata module0 = ModuleMetadata.builder("MB").build();
        ModuleMetadata module1 = ModuleMetadata.builder("MA").build();
        ModuleMetadata module2 = ModuleMetadata.builder("MC").build();
        ModulesMetadata modules = ModulesMetadata.builder(module0, module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      MA",
                "",
                "      MB",
                "",
                "      MC"
        );
    }

    @Test
    public void testGenerate_Name_Description() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").description("Module called M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").build();

        ModulesMetadata modules = ModulesMetadata.builder(module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1: Module called M1",
                "",
                "      M2"
        );
    }

    @Test
    public void testGenerate_Configs() {

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigValueMetadata.builder("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigValueMetadata.builder("p1").type(String.class).build())
                .build();

        ConfigObjectMetadata m2Config = ConfigObjectMetadata
                .builder("m2root")
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p0").type(Boolean.class).build())
                .addProperty(ConfigValueMetadata.builder("p4").type(Bootique.class).build())
                .build();

        ModuleMetadata module1 = ModuleMetadata.builder("M1").addConfig(m1Config).build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").addConfig(m2Config).build();

        ModulesMetadata modules = ModulesMetadata.builder(module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1",
                "",
                "      M2",
                "",
                "CONFIGURATION",
                "      m1root:",
                "            #",
                "            # Root config of M1",
                "            # Resolved as 'io.bootique.help.config.DefaultConfigHelpGeneratorTest$ConfigRoot1'.",
                "            #",
                "",
                "            p1: <string>",
                "            # Designates an integer value",
                "            p2: <int>",
                "",
                "      m2root:",
                "            #",
                "            # Resolved as 'io.bootique.help.config.DefaultConfigHelpGeneratorTest$ConfigRoot2'.",
                "            #",
                "",
                "            p0: <true|false>",
                "            # Resolved as 'io.bootique.Bootique'.",
                "            p4: <value>"
        );
    }

    public static class ConfigRoot1 {

    }

    public static class ConfigRoot2 {

    }
}

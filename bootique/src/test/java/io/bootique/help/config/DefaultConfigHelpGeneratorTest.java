package io.bootique.help.config;

import io.bootique.Bootique;
import io.bootique.module.ConfigObjectMetadata;
import io.bootique.module.ConfigPropertyMetadata;
import io.bootique.module.ModuleMetadata;
import io.bootique.module.ModulesMetadata;
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

        ModuleMetadata module1 = ModuleMetadata.builder().name("M1").build();
        ModulesMetadata modules = ModulesMetadata.builder().addModule(module1).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1"
        );
    }

    @Test
    public void testGenerate_Name_MultiModule() {

        ModuleMetadata module1 = ModuleMetadata.builder().name("M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder().name("M2").build();
        ModulesMetadata modules = ModulesMetadata.builder().addModule(module1).addModule(module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1",
                "",
                "      M2"
        );
    }

    @Test
    public void testGenerate_Name_MultiModule_Sorting() {

        ModuleMetadata module0 = ModuleMetadata.builder().name("MB").build();
        ModuleMetadata module1 = ModuleMetadata.builder().name("MA").build();
        ModuleMetadata module2 = ModuleMetadata.builder().name("MC").build();
        ModulesMetadata modules = ModulesMetadata.builder()
                .addModule(module0)
                .addModule(module1)
                .addModule(module2)
                .build();

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

        ModuleMetadata module1 = ModuleMetadata.builder().name("M1").description("Module called M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder().name("M2").build();

        ModulesMetadata modules = ModulesMetadata.builder().addModule(module1).addModule(module2).build();

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
                .builder()
                .name("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigPropertyMetadata.builder().name("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigPropertyMetadata.builder().name("p1").type(String.class).build())
                .build();

        ConfigObjectMetadata m2Config = ConfigObjectMetadata
                .builder()
                .name("m2root")
                .type(ConfigRoot2.class)
                .addProperty(ConfigPropertyMetadata.builder().name("p0").type(Boolean.class).build())
                .addProperty(ConfigPropertyMetadata.builder().name("p4").type(Bootique.class).build())
                .build();

        ModuleMetadata module1 = ModuleMetadata.builder().name("M1").addConfig(m1Config).build();
        ModuleMetadata module2 = ModuleMetadata.builder().name("M2").addConfig(m2Config).build();

        ModulesMetadata modules = ModulesMetadata.builder().addModule(module1).addModule(module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1",
                "",
                "      M2",
                "",
                "CONFIGURATION",
                "      # Type: io.bootique.help.config.DefaultConfigHelpGeneratorTest$ConfigRoot1",
                "      # Root config of M1",
                "      m1root:",
                "            # Type: String",
                "            p1: 'string'",
                "",
                "            # Type: int",
                "            # Designates an integer value",
                "            p2: 100",
                "",
                "      # Type: io.bootique.help.config.DefaultConfigHelpGeneratorTest$ConfigRoot2",
                "      m2root:",
                "            # Type: boolean",
                "            p0: false",
                "",
                "            # Type: io.bootique.Bootique",
                "            p4: value"
        );
    }

    public static class ConfigRoot1 {

    }

    public static class ConfigRoot2 {

    }
}

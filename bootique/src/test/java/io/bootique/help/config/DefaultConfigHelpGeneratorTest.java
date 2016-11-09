package io.bootique.help.config;

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

        ModuleMetadata module1 = ModuleMetadata.builder("M1").build();
        ModulesMetadata modules = ModulesMetadata.builder().addModule(module1).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1"
        );
    }

    @Test
    public void testGenerate_Name_MultiModule() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").build();
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

        ModuleMetadata module0 = ModuleMetadata.builder("MB").build();
        ModuleMetadata module1 = ModuleMetadata.builder("MA").build();
        ModuleMetadata module2 = ModuleMetadata.builder("MC").build();
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

        ModuleMetadata module1 = ModuleMetadata.builder("M1").description("Module called M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").build();

        ModulesMetadata modules = ModulesMetadata.builder().addModule(module1).addModule(module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 80),
                "MODULES",
                "      M1: Module called M1",
                "",
                "      M2"
        );
    }
}

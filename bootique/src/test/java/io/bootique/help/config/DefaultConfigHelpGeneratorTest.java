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

package io.bootique.help.config;

import io.bootique.Bootique;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void generate_Empty() {
        ModulesMetadata modules = ModulesMetadata.builder().build();
        assertLines(new DefaultConfigHelpGenerator(modules, 300));
    }

    @Test
    public void generate_Name() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").build();
        ModulesMetadata modules = ModulesMetadata.builder(module1).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 300),
                "MODULES",
                "      M1"
        );
    }

    @Test
    public void generate_Name_MultiModule() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").build();
        ModulesMetadata modules = ModulesMetadata.builder(module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 300),
                "MODULES",
                "      M1",
                "",
                "      M2"
        );
    }

    @Test
    public void generate_Name_MultiModule_Sorting() {

        ModuleMetadata module0 = ModuleMetadata.builder("MB").build();
        ModuleMetadata module1 = ModuleMetadata.builder("MA").build();
        ModuleMetadata module2 = ModuleMetadata.builder("MC").build();
        ModulesMetadata modules = ModulesMetadata.builder(module0, module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 300),
                "MODULES",
                "      MA",
                "",
                "      MB",
                "",
                "      MC"
        );
    }

    @Test
    public void generate_Name_Description() {

        ModuleMetadata module1 = ModuleMetadata.builder("M1").description("Module called M1").build();
        ModuleMetadata module2 = ModuleMetadata.builder("M2").build();

        ModulesMetadata modules = ModulesMetadata.builder(module1, module2).build();

        assertLines(new DefaultConfigHelpGenerator(modules, 300),
                "MODULES",
                "      M1: Module called M1",
                "",
                "      M2"
        );
    }

    @Test
    public void generate_Configs() {

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

        assertLines(new DefaultConfigHelpGenerator(modules, 300),
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

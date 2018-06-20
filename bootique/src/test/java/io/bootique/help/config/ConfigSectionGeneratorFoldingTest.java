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

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

package io.bootique.option;

import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.meta.application.OptionValueCardinality;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionApiTest {

    @Test
    public void flagAndValueOptions_ExposeMetadata() {
        FlagOption flag = Option.flag("switch")
                .description("A boolean switch")
                .shortName('s')
                .build();

        ValueOption value = Option.valueOption("value")
                .description("A required value")
                .shortName('v')
                .build();

        assertEquals("switch", flag.getMetadata().getName());
        assertEquals("value", value.getMetadata().getName());
        assertEquals(OptionValueCardinality.NONE, flag.getMetadata().getValueCardinality());
        assertEquals(OptionValueCardinality.REQUIRED, value.getMetadata().getValueCardinality());
    }

    @Test
    public void configOptions_ExposeConfigBindings() {
        ConfigValueOption configValue = Option.configValue("opt-1", "c.m.f").build();
        ConfigResourceOption configResource = Option.configResource("file-opt-1", "classpath:io/bootique/config/configTest4Opt1.yml")
                .build();

        assertEquals("c.m.f", configValue.getConfigPath());
        assertEquals("classpath:io/bootique/config/configTest4Opt1.yml", configResource.getConfigResourceId());
    }

    @Test
    public void configValueOption_RejectsNullConfigPath() {
        assertThrows(NullPointerException.class, () -> Option.configValue("opt-1", null));
    }

    @Test
    public void configResourceOption_RejectsNullConfigResourceId() {
        assertThrows(NullPointerException.class, () -> Option.configResource("file-opt-1", null));
    }

    @Test
    public void commandMetadata_AcceptsOptionObjects() {
        CommandMetadata metadata = CommandMetadata.builder("sample")
                .addOption(Option.flag("switch").build())
                .addOption(Option.valueOption("value").build())
                .build();

        assertEquals("sample", metadata.getName());
        assertEquals(2, metadata.getOptions().size());
        assertTrue(metadata.getOptions().stream().map(OptionMetadata::getName).anyMatch("switch"::equals));
        assertTrue(metadata.getOptions().stream().map(OptionMetadata::getName).anyMatch("value"::equals));
    }
}

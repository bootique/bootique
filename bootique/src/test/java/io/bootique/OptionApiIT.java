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

package io.bootique;

import io.bootique.cli.Cli;
import io.bootique.config.ConfigurationFactory;
import io.bootique.option.ConfigResourceOption;
import io.bootique.option.ConfigValueOption;
import io.bootique.option.FlagOption;
import io.bootique.option.Option;
import io.bootique.option.ValueOption;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionApiIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void flagAndValueOptions_ExposeMetadataAndCliHelpers() {
        FlagOption flag = Option.flag("switch")
                .description("A boolean switch")
                .shortName('s')
                .build();

        ValueOption value = Option.valueOption("value")
                .description("A required value")
                .shortName('v')
                .build();

        BQRuntime runtime = appManager.runtime(Bootique.app("-s", "--value=abc")
                .module(b -> BQCoreModule.extend(b).addOptions(flag, value)));

        Cli cli = runtime.getInstance(Cli.class);

        assertTrue(flag.isSet(cli));
        assertTrue(value.isSet(cli));
        assertEquals("abc", value.getValue(cli));
    }

    @Test
    public void configValueOption_BindsConfigPath() {
        ConfigValueOption option = Option.configValue("opt-1", "c.m.f")
                .description("Maps an option to config")
                .build();

        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--opt-1=x")
                .module(b -> BQCoreModule.extend(b).addOption(option)));

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("x", bean1.c.m.f);
    }

    @Test
    public void configResourceOption_BindsConfigResource() {
        ConfigResourceOption option = Option.configResource("file-opt-1", "classpath:io/bootique/config/configTest4Opt1.yml")
                .build();

        BQRuntime runtime = appManager.runtime(Bootique.app("--config=classpath:io/bootique/config/test4.yml", "--file-opt-1")
                .module(b -> BQCoreModule.extend(b).addOption(option)));

        Bean1 bean1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("f", bean1.c.m.f);
        assertEquals(1, bean1.c.m.k);
    }

    private record Bean1(String a, Bean2 c) {}

    private record Bean2(Bean3 m) {}

    private record Bean3(String f, int k) {}
}

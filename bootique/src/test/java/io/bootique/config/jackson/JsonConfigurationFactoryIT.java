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

package io.bootique.config.jackson;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.config.ConfigurationFactory;
import io.bootique.unit.TestAppManager;
import io.bootique.unit.TestWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonConfigurationFactoryIT {

    @RegisterExtension
    static final TestWebServer webServer = new TestWebServer("classpath:io/bootique");

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void noConfig() {
        BQRuntime runtime = appManager.runtime(Bootique.app());
        JsonConfigurationFactory configFactory = (JsonConfigurationFactory) runtime.getInstance(ConfigurationFactory.class);
        assertEquals("{}", configFactory.rootNode.toString());
    }

    @Test
    public void yaml() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=http://127.0.0.1:12025/test1.yml"));
        JsonConfigurationFactory configFactory = (JsonConfigurationFactory) runtime.getInstance(ConfigurationFactory.class);
        assertEquals("{\"a\":\"b\"}", configFactory.rootNode.toString());
    }

    @Test
    public void json() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=http://127.0.0.1:12025/test1.json"));
        JsonConfigurationFactory configFactory = (JsonConfigurationFactory) runtime.getInstance(ConfigurationFactory.class);
        assertEquals("{\"x\":1}", configFactory.rootNode.toString());
    }

    @Test
    public void jsonYaml() {

        BQRuntime runtime = appManager.runtime(Bootique.app(
                "--config=http://127.0.0.1:12025/test1.json",
                "--config=http://127.0.0.1:12025/test1.yml"));

        JsonConfigurationFactory configFactory = (JsonConfigurationFactory) runtime.getInstance(ConfigurationFactory.class);
        assertEquals("{\"x\":1,\"a\":\"b\"}", configFactory.rootNode.toString());
    }

    @Test
    public void yamlProps() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=http://127.0.0.1:12025/test1.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.a", "B")));
        JsonConfigurationFactory configFactory = (JsonConfigurationFactory) runtime.getInstance(ConfigurationFactory.class);
        assertEquals("{\"a\":\"B\"}", configFactory.rootNode.toString());
    }

    @Test
    public void yamlNestedProps() {
        BQRuntime runtime = appManager.runtime(Bootique.app("--config=http://127.0.0.1:12025/test3.yml")
                .module(b -> BQCoreModule.extend(b).setProperty("bq.c.m.k", "67")));
        JsonConfigurationFactory configFactory = (JsonConfigurationFactory) runtime.getInstance(ConfigurationFactory.class);
        assertEquals("{\"a\":\"e\",\"c\":{\"m\":{\"k\":\"67\",\"l\":\"n\"}}}", configFactory.rootNode.toString());
    }

}

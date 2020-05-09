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

package io.bootique.config;

import io.bootique.cli.Cli;
import io.bootique.log.BootLogger;
import io.bootique.resource.ResourceFactory;
import io.bootique.unit.BQInternalWebServerTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.URL;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class CliConfigurationSource_WebConfigSourceIT {

    @RegisterExtension
    public BQInternalWebServerTestFactory testFactory = new BQInternalWebServerTestFactory();

    private BootLogger mockBootLogger;
    private Function<URL, String> configReader;

    @BeforeEach
    public void before() {
        this.mockBootLogger = mock(BootLogger.class);
        this.configReader = CliConfigurationSourceTest.createConfigReader();
    }

    @Test
    public void testGet_HttpUrl() {

        testFactory.app("--server").resourceUrl(new ResourceFactory("classpath:io/bootique/config"))
                .createRuntime();

        String url = "http://localhost:12025/CliConfigurationSource_WebConfigSourceIT1.yml";
        Cli cli = CliConfigurationSourceTest.createCli(url);
        String config = CliConfigurationSource.builder(mockBootLogger)
                .cliConfigs(cli)
                .build()
                .get()
                .map(configReader)
                .collect(joining(";"));
        assertEquals("g: h", config);
    }
}

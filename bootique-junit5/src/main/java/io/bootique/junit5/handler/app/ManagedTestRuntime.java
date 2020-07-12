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
package io.bootique.junit5.handler.app;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import io.bootique.junit5.BQApp;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * @since 2.0
 */
public class ManagedTestRuntime {

    // using JUnit logger
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedTestRuntime.class);

    private BQRuntime runtime;
    private String name;
    private BQApp config;

    public ManagedTestRuntime(BQRuntime runtime, String name, BQApp config) {
        this.runtime = runtime;
        this.name = name;
        this.config = config;
    }

    public boolean immediateShutdown() {
        return config.immediateShutdown();
    }

    public boolean skipRun() {
        return config.skipRun();
    }

    public String getName() {
        return name;
    }

    public CommandOutcome run() {
        LOGGER.debug(() -> "Starting Bootique runtime '" + name + "'...");
        return runtime.run();
    }

    public void shutdown() {
        LOGGER.debug(() -> "Stopping Bootique runtime '" + name + "'...");
        try {
            runtime.shutdown();
        } catch (Exception e) {
            // ignore...
        }
    }
}

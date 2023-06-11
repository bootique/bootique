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

package io.bootique.unit;

import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.command.CommandOutcome;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Bootique runtime lifecycle manager for tests. Needed as we can't use "bootique-junit5" facilities in the core tests.
 */
public class TestAppManager implements BeforeEachCallback, AfterEachCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestAppManager.class);

    protected Collection<BQRuntime> runtimes;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        this.runtimes = new ArrayList<>();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {

        LOGGER.info("Stopping runtime...");
        Collection<BQRuntime> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.forEach(runtime -> {
                try {
                    runtime.shutdown();
                } catch (Exception e) {
                    // ignore...
                }
            });
        }
    }

    public BQRuntime runtime(Bootique app) {
        BQRuntime runtime = app.createRuntime();
        this.runtimes.add(runtime);
        return runtime;
    }

    public CommandOutcome run(Bootique app) {
        return runtime(app).run();
    }
}

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
package io.bootique.junit5;

import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.shutdown.ShutdownManager;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class BQTestFactory_PerClass_LifecycleIT {

    // "BQTestTool" default ordering should align with field declaration ordering
    // (suppose depends on compiler and runtime?)
    @BQTestTool
    static final Tester tester = new Tester();

    @BQTestTool
    static final BQTestFactory testFactory = new BQTestFactory();

    @RepeatedTest(value = 3, name = "Check shutdowns ... {currentRepetition}")
    public void shutdowns() {
        tester.run();
    }

    public static class ShutdownTrackerModule implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @Singleton
        Tester provideTester(ShutdownManager shutdownManager) {
            shutdownManager.onShutdown(tester::onShutdown);
            return tester;
        }
    }

    public static class Tester implements BQAfterScopeCallback {

        private int calls;
        private int shutdowns;

        public void onShutdown() {
            shutdowns++;
        }

        public void run() {

            assertEquals(0, shutdowns);
            assertEquals(calls, testFactory.getRuntimes().size());

            testFactory.app()
                    .module(new ShutdownTrackerModule())
                    .createRuntime()
                    // this will bootstrap shutdown hook
                    .getInstance(Tester.class);

            assertEquals(0, shutdowns);

            calls++;
            assertEquals(calls, testFactory.getRuntimes().size());
        }

        @Override
        public void afterScope(BQTestScope scope, ExtensionContext context) {
            assertEquals(3, calls);
            assertEquals(3, shutdowns);
            assertEquals(3, testFactory.getRuntimes().size(), "All runtimes must stay in the end, in a shutdown state");
        }
    }
}

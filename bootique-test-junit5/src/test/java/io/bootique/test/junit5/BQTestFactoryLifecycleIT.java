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
package io.bootique.test.junit5;

import io.bootique.BQModuleProvider;
import io.bootique.BaseModule;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BQTestFactoryLifecycleIT {

    @RegisterExtension
    @Order(1)
    public static Tester tester = new Tester();

    @RegisterExtension
    public static BQTestFactory testFactory = new BQTestFactory();

    @RepeatedTest(value = 3, name = "Check shutdowns ... {currentRepetition}")
    public void testShutdowns() {
        tester.run();
    }

    public static class ShutdownTrackerModule extends BaseModule {

        @Provides
        @Singleton
        Tester provideTester(ShutdownManager shutdownManager) {
            shutdownManager.addShutdownHook(BQTestFactoryLifecycleIT.tester::onShutdown);
            return BQTestFactoryLifecycleIT.tester;
        }
    }

    public static class Tester implements AfterAllCallback {

        private int calls;
        private int shutdowns;

        public void onShutdown() {
            shutdowns++;
        }

        public void run() {

            assertEquals(calls, shutdowns);
            assertEquals(0, testFactory.getRuntimes().size());

            testFactory.app()
                    .module((BQModuleProvider) new ShutdownTrackerModule())
                    .createRuntime()
                    // this will bootstrap shutdown hook
                    .getInstance(Tester.class);

            assertEquals(calls, shutdowns);
            assertEquals(1, testFactory.getRuntimes().size());

            calls++;
        }

        @Override
        public void afterAll(ExtensionContext context) {
            assertEquals(3, calls);
            assertEquals(3, shutdowns);
            assertEquals(1, testFactory.getRuntimes().size(), "Only one runtime must stay in the end, in a shutdown state");
        }
    }
}

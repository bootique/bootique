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

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BQTestFactoryLifecycleIT {

    private static final AtomicInteger shutdowns = new AtomicInteger(0);

    @RegisterExtension
    public static BQTestFactory testFactory = new BQTestFactory();

    @AfterAll
    public static void afterClass() {
        assertEquals(1, testFactory.getRuntimes().size(), "Only one runtime must stay in the end, in a shutdown state");
        assertEquals(2, shutdowns.get());
    }

    @Test
    @DisplayName("Check shutdowns 1")
    @Order(1)
    public void testShutdowns1() {
        assertEquals(0, testFactory.getRuntimes().size());
        assertEquals(0, shutdowns.get());

        makeRuntimeWithShutdownTracker();

        assertEquals(1, testFactory.getRuntimes().size());
        assertEquals(0, shutdowns.get());
    }

    @Test
    @DisplayName("Check shutdowns 2")
    @Order(2)
    public void testShutdowns2() {
        assertEquals(0, testFactory.getRuntimes().size());
        assertEquals(1, shutdowns.get());

        makeRuntimeWithShutdownTracker();

        assertEquals(1, testFactory.getRuntimes().size());
        assertEquals(1, shutdowns.get());
    }

    private void makeRuntimeWithShutdownTracker() {
        testFactory.app()
                .module(new ShutdownTrackerModule())
                .createRuntime()
                .getInstance(ShutdownTracker.class);
    }

    public static class ShutdownTrackerModule implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @Singleton
        ShutdownTracker provideShutdownTracker(ShutdownManager shutdownManager) {
            ShutdownTracker tracker = new ShutdownTracker();
            shutdownManager.addShutdownHook(tracker::shutdown);
            return tracker;
        }
    }

    public static class ShutdownTracker {

        public void shutdown() {
            shutdowns.getAndIncrement();
        }
    }
}

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

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class BQApp_MethodIT implements AfterEachCallback {

    @BQApp
    protected final BQRuntime methodApp = Bootique
            .app("--daemon")
            .autoLoadModules()
            .module(new TestModule())
            .createRuntime();

    @Override
    public void afterEach(ExtensionContext context) {
        methodApp.getInstance(DaemonCommand.class).assertStarted();
        methodApp.getInstance(DaemonCommand.class).assertStopped();
    }

    @Test
    public void test1() {
        methodApp.getInstance(DaemonCommand.class).assertStarted();
        methodApp.getInstance(DaemonCommand.class).assertNotStopped();
    }

    @Test
    public void test2() {
        methodApp.getInstance(DaemonCommand.class).assertStarted();
        methodApp.getInstance(DaemonCommand.class).assertNotStopped();
    }

    @RepeatedTest(3)
    public void repeated() {
        methodApp.getInstance(DaemonCommand.class).assertStarted();
        methodApp.getInstance(DaemonCommand.class).assertNotStopped();
    }

    static class TestModule implements BQModule {

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(DaemonCommand.class);
        }

        @Provides
        @Singleton
        DaemonCommand provideDaemonCommand(ShutdownManager shutdownManager) {
            DaemonCommand command = new DaemonCommand();
            return shutdownManager.onShutdown(command);
        }
    }

    static class DaemonCommand implements Command, AutoCloseable {

        private boolean started;
        private boolean stopped;
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public void close() {
            stopped = true;
        }

        public void assertStarted() {
            assertTrue(started, "Runtime is not started");
            assertEquals(1, counter.get(), "Runtime started more than once: " + counter.get());
        }

        public void assertNotStopped() {
            assertFalse(stopped, "Runtime should not have been shut down");
        }

        public void assertStopped() {
            assertTrue(stopped, "Runtime should have been shut down");
        }

        @Override
        public CommandOutcome run(Cli cli) {
            started = true;
            counter.getAndIncrement();
            return CommandOutcome.succeededAndForkedToBackground();
        }
    }
}

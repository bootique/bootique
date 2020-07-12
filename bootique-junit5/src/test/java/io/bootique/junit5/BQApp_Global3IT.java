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
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;
import org.junit.jupiter.api.Test;

import javax.inject.Singleton;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// not inheriting from BQApp_Global_Base to see if unrelated classes can coexist in a single test runner
@BQTest
public class BQApp_Global3IT {

    @BQApp(BQTestScope.GLOBAL)
    protected static final BQRuntime globalApp = Bootique
            .app("--daemon")
            .autoLoadModules()
            .module(new TestModule())
            .createRuntime();

    @Test
    public void test1() {
        globalApp.getInstance(DaemonCommand.class).assertStarted();
        globalApp.getInstance(DaemonCommand.class).assertNotStopped();
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
            shutdownManager.addShutdownHook(command);
            return command;
        }
    }

    static class DaemonCommand implements Command, AutoCloseable {

        private boolean started;
        private boolean stopped;

        @Override
        public void close() {
            stopped = true;
        }

        public void assertStarted() {
            assertTrue(started, "Runtime is not started");
        }

        public void assertNotStopped() {
            assertFalse(stopped, "Runtime should not have been shut down");
        }

        @Override
        public CommandOutcome run(Cli cli) {
            started = true;
            return CommandOutcome.succeededAndForkedToBackground();
        }
    }
}

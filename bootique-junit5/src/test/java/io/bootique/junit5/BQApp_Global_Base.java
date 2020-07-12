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

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class BQApp_Global_Base {

    @BQApp(BQTestScope.GLOBAL)
    protected static final BQRuntime globalApp = Bootique
            .app("--daemon")
            .autoLoadModules()
            .module(new TestModule())
            .createRuntime();

    protected void assertState() {
        globalApp.getInstance(ShutdownCounter.class).assertNoShutdowns();
    }

    static class TestModule implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bind(ShutdownCounter.class).to(ShutdownCounter.class).initOnStartup();
            BQCoreModule.extend(binder).addCommand(DaemonCommand.class);
        }

        @Provides
        @Singleton
        ShutdownCounter provideShutdownCounter(ShutdownManager shutdownManager) {
            ShutdownCounter counter = new ShutdownCounter();
            shutdownManager.addShutdownHook(counter);
            return counter;
        }
    }

    static class ShutdownCounter implements AutoCloseable {
        AtomicInteger shutdownCounter = new AtomicInteger();

        @Override
        public void close() {
            shutdownCounter.incrementAndGet();
        }

        public void assertNoShutdowns() {
            assertEquals(0, shutdownCounter.get(), "Runtime should not have been shut down");
        }
    }

    static class DaemonCommand implements Command {

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeededAndForkedToBackground();
        }
    }
}

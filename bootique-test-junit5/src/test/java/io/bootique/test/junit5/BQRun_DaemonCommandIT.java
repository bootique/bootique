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

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.shutdown.ShutdownManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BQRun_DaemonCommandIT.ShutdownTester.class)
@BQTest
@DisplayName("@BQRun daemon command")
public class BQRun_DaemonCommandIT {

    @BQRun
    public static final BQRuntime app = Bootique.app("-x")
            .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
            .createRuntime();

    @Test
    public void testStillRunning() {
        assertEquals(1, XCommand.runCount);
        assertFalse(app.getInstance(XCommand.class).isStopped());
    }

    public static class ShutdownTester implements AfterAllCallback {

        @Override
        public void afterAll(ExtensionContext context) {
            assertTrue(app.getInstance(XCommand.class).isStopped());
        }
    }

    @Singleton
    public static class XCommand implements Command {

        static int runCount;

        private boolean stopped;
        private ExecutorService executorService;

        @Inject
        public XCommand(ShutdownManager shutdownManager) {
            shutdownManager.addShutdownHook(this::shutdown);
        }

        public boolean isStopped() {
            return stopped;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            runCount++;

            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(this::doUntilStopped);

            return CommandOutcome.succeededAndForkedToBackground();
        }

        public void doUntilStopped() {

            while (!stopped) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore...
                }
            }
        }

        public void shutdown() {
            stopped = true;
            executorService.shutdown();
        }
    }
}

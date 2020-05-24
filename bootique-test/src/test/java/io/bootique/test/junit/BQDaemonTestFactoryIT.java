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

package io.bootique.test.junit;

import javax.inject.Inject;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.junit5.TestIO;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class BQDaemonTestFactoryIT {

    @Rule
    public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();

    @Test
    public void test_GetOutcome() throws InterruptedException {

        BQRuntime runtime = testFactory.app().createRuntime();

        assertFalse(testFactory.getOutcome(runtime).isPresent());
        testFactory.start(runtime);
        Thread.sleep(300);
        assertTrue(testFactory.getOutcome(runtime).isPresent());
    }

    @Test
    public void testStart_StartupFailure() {

        CommandOutcome failed = CommandOutcome.failed(-1, "Intended failure");

        try {
            testFactory.app("")
                    .module(b ->
                            BQCoreModule.extend(b).setDefaultCommand(cli -> failed))
                    .startupCheck(r -> false)
                    .start();
            fail("Should throw exception");
        } catch (BootiqueException e) {
            assertEquals(-1, e.getOutcome().getExitCode());
            assertEquals("Daemon failed to start: " + failed, e.getOutcome().getMessage());
        }
    }

    @Test
    public void testStart_RuntimeFailure() {
        CommandOutcome failed = CommandOutcome.failed(-1, "Intended failure");
        try {
            testFactory.app("")
                    .module(b ->
                            BQCoreModule.extend(b).addCommand(cli -> failed))
                    .startupCheck(r -> false)
                    .start();
            fail("Should throw exception");
        } catch (BootiqueException e) {
            assertEquals(-1, e.getOutcome().getExitCode());
            assertTrue(e.getOutcome().getMessage()
                    .startsWith("Daemon failed to start: [-1: Error invoking provider method 'provideCli()'" ));
        }
    }

    @Test
    public void test_StartupAndWait() {
        BQRuntime r1 = testFactory.app("a1", "a2").startupAndWaitCheck().start();
        assertArrayEquals(new String[]{"a1", "a2"}, r1.getArgs());

        BQRuntime r2 = testFactory.app("b1", "b2").startupAndWaitCheck().start();
        assertNotSame(r1, r2);
        assertArrayEquals(new String[]{"b1", "b2"}, r2.getArgs());
    }

    @Test
    public void testStart_Streams_NoTrace() {

        TestIO io = TestIO.noTrace();

        testFactory.app("-x")
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .bootLogger(io.getBootLogger())
                .startupAndWaitCheck()
                .start();

        assertEquals("--out--", io.getStdout().trim());
        assertEquals("--err--", io.getStderr().trim());
    }

    @Test
    public void testStart_Streams_Trace() {

        TestIO io = TestIO.trace();

        testFactory.app("-x")
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .bootLogger(io.getBootLogger())
                .startupAndWaitCheck()
                .start();

        assertEquals("--out--", io.getStdout().trim());
        assertTrue(io.getStderr().trim().endsWith("--err--"));
    }

    public static class XCommand extends CommandWithMetadata {

        private BootLogger logger;

        @Inject
        public XCommand(BootLogger logger) {
            super(CommandMetadata.builder(BQTestFactoryIT.XCommand.class));
            this.logger = logger;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            logger.stderr("--err--");
            logger.stdout("--out--");
            return CommandOutcome.succeeded();
        }
    }
}

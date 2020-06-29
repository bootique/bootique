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

package io.bootique.command;

import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.cli.Cli;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommandDecoratorIT {

    private final ThreadTester threadTester = new ThreadTester();

    @RegisterExtension
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();
    
    private MainCommand mainCommand;
    private SuccessfulCommand successfulCommand;
    private FailingCommand failingCommand;

    @BeforeEach
    public void before() {

        // test for previous tests side effects - the previous test must be cleanly shutdown...
        this.threadTester.assertPoolSize(0);

        this.mainCommand = new MainCommand();
        this.successfulCommand = new SuccessfulCommand();
        this.failingCommand = new FailingCommand();
    }

    @Test
    public void testAlsoRun_Instance() {

        Command cmd = mock(Command.class);
        when(cmd.run(any())).thenReturn(CommandOutcome.succeeded());

        new AppRunner(CommandDecorator.alsoRun(cmd)).runAndWaitExpectingSuccess();

        assertTrue(mainCommand.isExecuted());
        verify(cmd).run(any(Cli.class));
    }

    @Test
    public void testAlsoRun_NameRef() {

        CommandDecorator decorator = CommandDecorator.alsoRun("s");

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_NameRef_WithArgs() {

        CommandDecorator decorator = CommandDecorator.alsoRun("s", "--sflag");

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_TypeRef() {

        CommandDecorator decorator = CommandDecorator.alsoRun(SuccessfulCommand.class);

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_TypeRef_WithArgs() {

        CommandDecorator decorator = CommandDecorator.alsoRun(SuccessfulCommand.class, "--sflag");

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testBeforeRun_Instance() {

        Command cmd = mock(Command.class);
        when(cmd.run(any())).thenReturn(CommandOutcome.succeeded());

        CommandDecorator decorator = CommandDecorator.beforeRun(cmd);

        new AppRunner(decorator).runExpectingSuccess();
        verify(cmd).run(any(Cli.class));
    }

    @Test
    public void testBeforeRun_Failure_NameRef() {

        CommandDecorator decorator = CommandDecorator.beforeRun("f");

        new AppRunner(decorator).runExpectingFailure();
    }

    @Test
    public void testBeforeRun_Failure_NameRef_WithArgs() {

        CommandDecorator decorator = CommandDecorator.beforeRun("f", "--fflag");

        new AppRunner(decorator).runExpectingFailure();
        assertTrue(failingCommand.hasFlagOption());
    }

    @Test
    public void testBeforeRun_Failure_TypeRef() {
        CommandDecorator decorator = CommandDecorator.beforeRun(FailingCommand.class);

        new AppRunner(decorator).runExpectingFailure();
    }

    @Test
    public void testBeforeRun_Failure_TypeRef_WithArgs() {
        CommandDecorator decorator = CommandDecorator.beforeRun(FailingCommand.class, "--fflag");
        new AppRunner(decorator).runExpectingFailure();

        assertTrue(failingCommand.hasFlagOption());
    }

    @Test
    public void testBeforeAndAlsoRun() {

        Command c1 = mock(Command.class);
        when(c1.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c2 = mock(Command.class);
        when(c2.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c3 = mock(Command.class);
        when(c3.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c4 = mock(Command.class);
        when(c4.run(any())).thenReturn(CommandOutcome.succeeded());

        CommandDecorator decorator = CommandDecorator
                .builder()
                .beforeRun(c1).beforeRun(c2)
                .alsoRun(c3).alsoRun(c4)
                .build();

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(mainCommand.isExecuted());
        verify(c1).run(any(Cli.class));
        verify(c2).run(any(Cli.class));
        verify(c3).run(any(Cli.class));
        verify(c4).run(any(Cli.class));
    }

    @Test
    public void testMultipleDecoratorsForTheSameCommand() {

        Command c1 = mock(Command.class);
        when(c1.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c2 = mock(Command.class);
        when(c2.run(any())).thenReturn(CommandOutcome.succeeded());

        testFactory.app("--a")
                .module(b -> BQCoreModule.extend(b)
                        .addCommand(mainCommand)
                        .decorateCommand(mainCommand.getClass(), CommandDecorator.beforeRun(c1))
                        .decorateCommand(mainCommand.getClass(), CommandDecorator.beforeRun(c2)))
                .createRuntime()
                .run();

        verify(c1).run(any(Cli.class));
        verify(c2).run(any(Cli.class));
        assertTrue(mainCommand.isExecuted());
    }

    private static class MainCommand extends ExecutableOnceCommand {

        private static final String NAME = "a";

        MainCommand() {
            super(NAME);
        }
    }

    private static class SuccessfulCommand extends ExecutableOnceCommand {

        private static final String NAME = "s";
        private static final String FLAG_OPT = "sflag";

        SuccessfulCommand() {
            super(NAME, FLAG_OPT);
        }

        public boolean hasFlagOption() {
            return cliRef.get().hasOption(FLAG_OPT);
        }
    }

    private static class FailingCommand extends ExecutableOnceCommand {

        private static final String NAME = "f";
        private static final String FLAG_OPT = "fflag";

        FailingCommand() {
            super(NAME, FLAG_OPT);
        }

        public boolean hasFlagOption() {
            return cliRef.get().hasOption(FLAG_OPT);
        }

        @Override
        public CommandOutcome run(Cli cli) {
            super.run(cli);
            return CommandOutcome.failed(1, NAME);
        }
    }

    static abstract class ExecutableOnceCommand extends CommandWithMetadata {

        protected final AtomicReference<Cli> cliRef;

        public ExecutableOnceCommand(String commandName) {
            this(commandName, Optional.empty());
        }

        public ExecutableOnceCommand(String commandName, String flagOption) {
            this(commandName, Optional.of(flagOption));
        }

        private ExecutableOnceCommand(String commandName, Optional<String> flagOption) {
            super(buildMetadata(commandName, flagOption));
            this.cliRef = new AtomicReference<>();
        }

        private static CommandMetadata buildMetadata(String commandName, Optional<String> flagOption) {
            CommandMetadata.Builder builder = CommandMetadata.builder(commandName);
            flagOption.ifPresent(o -> builder.addOption(OptionMetadata.builder(o)));
            return builder.build();
        }

        @Override
        public CommandOutcome run(Cli cli) {
            if (!cliRef.compareAndSet(null, cli)) {
                throw new IllegalStateException("Already executed");
            }

            return CommandOutcome.succeeded();
        }

        public boolean isExecuted() {
            return cliRef.get() != null;
        }
    }

    private static class ThreadTester {

        public void assertPoolSize(int expected) {
            long matched = allThreads().filter(this::isPoolThread).count();

            if (expected < matched) {
                // let shutdown finish...
                try {
                    Thread.sleep(80);
                } catch (InterruptedException e) {
                    // ignore....
                }
            }

            matched = allThreads().filter(this::isPoolThread).count();

            assertEquals(expected, matched);
        }

        private boolean isPoolThread(Thread t) {
            // the name comes from HeartbeatFactory
            return t.getName().startsWith("bootique-command-");
        }

        private Stream<Thread> allThreads() {
            ThreadGroup tg = Thread.currentThread().getThreadGroup();
            while (tg.getParent() != null) {
                tg = tg.getParent();
            }

            Thread[] active = new Thread[tg.activeCount()];
            tg.enumerate(active);
            return Arrays.stream(active);
        }
    }

    private class AppRunner {
        private CommandDecorator decorator;
        private BQModuleProvider moduleProvider;

        public AppRunner(CommandDecorator decorator) {
            this.decorator = decorator;
        }

        public AppRunner module(BQModuleProvider moduleProvider) {
            this.moduleProvider = moduleProvider;
            return this;
        }

        public void runExpectingSuccess() {
            CommandOutcome outcome = run();
            assertTrue(outcome.isSuccess(), outcome.getMessage());
            assertTrue(mainCommand.isExecuted());
        }

        public void runAndWaitExpectingSuccess() {
            assertTrue(runAndWait().isSuccess());
            assertTrue(mainCommand.isExecuted());
        }

        public void runExpectingFailure() {
            assertFalse(run().isSuccess());
            assertFalse(mainCommand.isExecuted());
        }

        private CommandOutcome run() {
            BQInternalTestFactory.Builder builder = testFactory
                    .app("--a")
                    .module(b -> BQCoreModule.extend(b)
                            .addCommand(mainCommand)
                            .addCommand(successfulCommand)
                            .addCommand(failingCommand)
                            .decorateCommand(mainCommand.getClass(), decorator));

            if (moduleProvider != null) {
                builder.moduleProvider(moduleProvider);
            }

            return builder.createRuntime().run();
        }

        private CommandOutcome runAndWait() {

            CommandOutcome outcome = run();

            // wait for the parallel commands to finish
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return outcome;
        }
    }

}

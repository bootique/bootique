package io.bootique.test.junit5;

import com.google.inject.Inject;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.BootiqueException;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.test.TestIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BQDaemonTestExtensionIT {

    @RegisterExtension
    public static BQDaemonTestExtension testExtension = new BQDaemonTestExtension();

    @Test
    public void test_GetOutcome() throws InterruptedException {

        BQRuntime runtime = testExtension.app().createRuntime();

        assertFalse(testExtension.getOutcome(runtime).isPresent());
        testExtension.start(runtime);
        Thread.sleep(300);
        assertTrue(testExtension.getOutcome(runtime).isPresent());
    }

    @Test
    public void testStart_StartupFailure() {

        CommandOutcome failed = CommandOutcome.failed(-1, "Intended failure");

        try {
            testExtension.app("")
                    .module(b ->
                            BQCoreModule.extend(b).setDefaultCommand(cli -> failed))
                    .startupCheck(r -> false)
                    .start();
        } catch (BootiqueException e) {
            assertEquals(-1, e.getOutcome().getExitCode());
            assertEquals("Daemon failed to start: " + failed, e.getOutcome().getMessage());
        }
    }

    @Test
    public void test_StartupAndWait() {
        BQRuntime r1 = testExtension.app("a1", "a2").startupAndWaitCheck().start();
        assertArrayEquals(new String[]{"a1", "a2"}, r1.getArgs());

        BQRuntime r2 = testExtension.app("b1", "b2").startupAndWaitCheck().start();
        assertNotSame(r1, r2);
        assertArrayEquals(new String[]{"b1", "b2"}, r2.getArgs());
    }

    @Test
    public void testStart_Streams_NoTrace() {

        TestIO io = TestIO.noTrace();

        testExtension.app("-x")
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

        testExtension.app("-x")
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
            super(CommandMetadata.builder(BQTestExtensionIT.XCommand.class));
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

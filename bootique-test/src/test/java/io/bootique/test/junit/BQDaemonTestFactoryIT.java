package io.bootique.test.junit;

import com.google.inject.Inject;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.test.TestIO;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class BQDaemonTestFactoryIT {

    @Rule
    public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();

    @Test
    public void test_StartupAndWait() {
        BQRuntime r1 = testFactory.app("a1", "a2").startupAndWaitCheck().start();
        assertArrayEquals(new String[]{"a1", "a2"}, r1.getArgs());

        BQRuntime r2 = testFactory.app("b1", "b2").startupAndWaitCheck().start();
        assertNotSame(r1, r2);
        assertArrayEquals(new String[]{"b1", "b2"}, r2.getArgs());
    }

    @Test
    public void testCreateRuntime_Streams_NoTrace() {

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
    public void testCreateRuntime_Streams_Trace() {

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

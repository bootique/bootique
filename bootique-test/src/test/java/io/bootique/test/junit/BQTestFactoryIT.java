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

public class BQTestFactoryIT {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testCreateRuntime_Injection() {
        BQRuntime runtime = testFactory.app("-x").autoLoadModules().createRuntime();
        assertArrayEquals(new String[]{"-x"}, runtime.getArgs());
    }

    @Test
    public void testCreateRuntime_Streams_NoTrace() {

        TestIO io = TestIO.noTrace();

        CommandOutcome result = testFactory.app("-x")
                .autoLoadModules()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .bootLogger(io.getBootLogger())
                .createRuntime()
                .run();

        assertTrue(result.isSuccess());
        assertEquals("--out--", io.getStdout().trim());
        assertEquals("--err--", io.getStderr().trim());
    }

    public static class XCommand extends CommandWithMetadata {

        private BootLogger logger;

        @Inject
        public XCommand(BootLogger logger) {
            super(CommandMetadata.builder(XCommand.class));
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

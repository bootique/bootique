package io.bootique.junit5;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(BQApp_SkipRunIT.ShutdownTester.class)
@BQTest
@DisplayName("@BQApp.skipRun = true")
public class BQApp_SkipRunIT {

    @BQApp(skipRun = true)
    public static final BQRuntime app = Bootique.app("-x")
            .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
            .createRuntime();

    @Test
    public void didNotRun() {
        assertDoesNotThrow(() -> app.getInstance(XCommand.class));
        assertEquals(0, XCommand.runCount);
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

        @Inject
        public XCommand(ShutdownManager shutdownManager) {
            shutdownManager.onShutdown(this::shutdown);
        }

        @Override
        public CommandOutcome run(Cli cli) {
            runCount++;
            return CommandOutcome.succeeded();
        }

        public boolean isStopped() {
            return stopped;
        }

        public void shutdown() {
            stopped = true;
        }
    }
}

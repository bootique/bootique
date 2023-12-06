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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
@DisplayName("@BQApp.immediateShutdown = true")
public class BQApp_ImmediateShutdownIT {

    @BQApp(immediateShutdown = true)
    public static final BQRuntime app = Bootique.app("-x")
            .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
            .createRuntime();

    @Test
    public void didRun() {
        assertEquals(1, XCommand.runCount);
        assertTrue(app.getInstance(XCommand.class).isStopped());
    }

    @Singleton
    public static class XCommand implements Command {

        static int runCount;
        private boolean stopped;
        private ExecutorService executorService;

        @Inject
        public XCommand(ShutdownManager shutdownManager) {
            shutdownManager.onShutdown(this::shutdown);
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

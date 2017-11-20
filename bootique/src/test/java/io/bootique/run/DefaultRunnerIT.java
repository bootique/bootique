package io.bootique.run;

import com.google.inject.Inject;
import io.bootique.BQCoreModule;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.command.Commands;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.unit.BQInternalInMemoryPrintStream;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultRunnerIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private BQInternalInMemoryPrintStream out;
    private BootLogger logger;

    @Before
    public void before() {
        this.out = new BQInternalInMemoryPrintStream(System.out);
        this.logger = new DefaultBootLogger(true, out, System.err);
    }

    @Test
    public void testRun_Explicit() {

        testFactory.app("-x")
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .bootLogger(logger)
                .createRuntime()
                .run();

        assertTrue(out.toString().contains("x_was_run"));
    }

    @Test
    public void testRun_Implicit_Default() {

        BQInternalInMemoryPrintStream out = new BQInternalInMemoryPrintStream(System.out);
        BootLogger logger = new DefaultBootLogger(false, out, System.err);

        testFactory.app()
                .module(b -> BQCoreModule.extend(b).setDefaultCommand(XCommand.class))
                .bootLogger(logger)
                .createRuntime()
                .run();

        assertTrue(out.toString().contains("x_was_run"));
    }

    @Test
    public void testRun_Implicit_Help() {

        testFactory.app()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .bootLogger(logger)
                .createRuntime()
                .run();

        String help = out.toString();

        assertTrue(help.contains("-h, --help"));
        assertTrue(help.contains("-x"));
        assertFalse(help.contains("x_was_run"));
    }

    @Test
    public void testRun_Implicit_NoModuleCommands_NoHelp() {

        testFactory.app()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .module(Commands.builder(YCommand.class).noModuleCommands().build())
                .bootLogger(logger)
                .createRuntime()
                .run();

        assertFalse(out.toString().contains("-h, --help"));
    }

    @Test
    public void testRun_Implicit_NoModuleCommands_HelpAllowed() {

        testFactory.app()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .module(Commands.builder(YCommand.class, HelpCommand.class).noModuleCommands().build())
                .bootLogger(logger)
                .createRuntime()
                .run();

        String help = out.toString();

        assertTrue(help.contains("-h, --help"));
        assertFalse(help.contains("-x"));
        assertTrue(help.contains("-y"));

        assertFalse(help.contains("x_was_run"));
        assertFalse(help.contains("y_was_run"));
    }


    @Test
    public void testRun_Implicit_HelpRedefined() {

        testFactory.app()
                .module(b -> BQCoreModule.extend(b).addCommand(XCommand.class))
                .module(Commands.builder(XHelpCommand.class).noModuleCommands().build())
                .bootLogger(logger)
                .createRuntime()
                .run();

        String help = out.toString();

        assertFalse(help.contains("-h, --help"));
        assertTrue(help.contains("xhelp_was_run"));
    }

    @Test
    public void testRun_Implicit_Default_NoModuleCommands() {

        testFactory.app()
                .module(b -> BQCoreModule.extend(b).setDefaultCommand(XCommand.class))
                .module(Commands.builder(X1Command.class).noModuleCommands().build())
                .bootLogger(logger)
                .createRuntime()
                .run();

        String help = out.toString();

        assertFalse(help.contains("x_was_run"));
        assertTrue(help.contains("x1_was_run"));
    }

    public static class XHelpCommand extends CommandWithMetadata {

        private BootLogger logger;

        @Inject
        public XHelpCommand(BootLogger logger) {
            // use meta from X
            super(CommandMetadata.builder(HelpCommand.class));
            this.logger = logger;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            logger.stdout("xhelp_was_run");
            return CommandOutcome.succeeded();
        }
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
            logger.stdout("x_was_run");
            return CommandOutcome.succeeded();
        }
    }

    public static class X1Command extends CommandWithMetadata {

        private BootLogger logger;

        @Inject
        public X1Command(BootLogger logger) {
            // use meta from X
            super(CommandMetadata.builder(XCommand.class));
            this.logger = logger;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            logger.stdout("x1_was_run");
            return CommandOutcome.succeeded();
        }
    }

    public static class YCommand extends CommandWithMetadata {

        private BootLogger logger;

        @Inject
        public YCommand(BootLogger logger) {
            super(CommandMetadata.builder(YCommand.class));
            this.logger = logger;
        }

        @Override
        public CommandOutcome run(Cli cli) {
            logger.stdout("y_was_run");
            return CommandOutcome.succeeded();
        }
    }
}

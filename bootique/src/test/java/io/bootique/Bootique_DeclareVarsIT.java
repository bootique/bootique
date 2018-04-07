package io.bootique;

import com.google.inject.Module;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.cli.Cli;
import io.bootique.help.HelpCommand;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Bootique_DeclareVarsIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testInHelp() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public Module module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", Bean5.class);
            }
        };

        BQRuntime runtime = testFactory.app()
                .module(configurableProvider)
                .declareVar("x.m", "X_VALID_VAR")
                .declareVar("x.y.prop", "X_INVALID_VAR")
                .bootLogger(logger)
                .createRuntime();

        Cli cli = runtime.getInstance(Cli.class);
        runtime.getInstance(HelpCommand.class).run(cli);

        String help = new String(out.toByteArray());
        assertTrue("No ENVIRONMENT section:\n" + help, help.contains("ENVIRONMENT"));
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    @Test
    @Ignore
    public void testInHelpWithMap() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            @Override
            public Module module() {
                return b -> {
                };
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", Bean4.class);
            }
        };

        BQRuntime runtime = testFactory.app()
                .module(configurableProvider)
                .declareVar("x.m.prop", "X_VALID_VAR")
                .declareVar("x.m.prop.x", "X_INVALID_VAR")
                .bootLogger(logger)
                .createRuntime();

        Cli cli = runtime.getInstance(Cli.class);
        runtime.getInstance(HelpCommand.class).run(cli);

        String help = new String(out.toByteArray());
        assertTrue("No ENVIRONMENT section:\n" + help, help.contains("ENVIRONMENT"));
        assertTrue(help.contains("X_VALID_VAR"));
        assertFalse(help.contains("X_INVALID_VAR"));
    }

    @BQConfig
    static class Bean4 {
        private Map<String, String> m;

        @BQConfigProperty
        public void setM(Map<String, String> m) {
            this.m = m;
        }
    }

    @BQConfig
    static class Bean5 {
        private String m;

        @BQConfigProperty
        public void setM(String m) {
            this.m = m;
        }
    }
}

package io.bootique;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.inject.Module;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.cli.Cli;
import io.bootique.config.ConfigurationFactory;
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

import static org.junit.Assert.*;

public class Bootique_VarsIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testDeclaredVarSetValue() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("MY_VAR", "myValue")
                .declareVar("c.m.l", "MY_VAR")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("myValue", b1.c.m.l);
    }

    @Test
    public void testDeclaredVar_ConfigPathCaseSensitivity() {
        BQRuntime runtime = testFactory.app()
                .declareVar("m.propCamelCase", "MY_VAR")
                .var("MY_VAR", "myValue")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");
        assertNotNull("Map did not resolve", b4.m);
        assertEquals("Unexpected map contents: " + b4.m, "myValue", b4.m.get("propCamelCase"));
    }

    @Test
    public void testDeclaredVar_NameCaseSensitivity() {
        BQRuntime runtime = testFactory.app()
                .declareVar("m.propCamelCase", "MY_VAR")
                .var("my_var", "myValue")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");
        assertNull(b4.m);
    }

    @Test
    public void testDeclaredVar_InHelp() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            private final Module module = b -> {
            };

            @Override
            public Module module() {
                return module;
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("x", Bean5.class);
            }

            @Override
            public BQModuleId id() {
                return BQModuleId.of(module);
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
    public void testDeclaredVar_InHelpWithMap() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        BootLogger logger = new DefaultBootLogger(false, new PrintStream(out), new PrintStream(err));

        BQModuleProvider configurableProvider = new BQModuleProvider() {
            private final Module module = b -> {
            };

            @Override
            public Module module() {
                return module;
            }

            @Override
            public BQModuleId id() {
                return BQModuleId.of(module);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Bean1 {
        private String a;
        private Bean2 c;

        public void setA(String a) {
            this.a = a;
        }

        public void setC(Bean2 c) {
            this.c = c;
        }
    }

    static class Bean2 {
        private Bean3 m;

        public void setM(Bean3 m) {
            this.m = m;
        }
    }

    static class Bean3 {
        private String k;
        private String f;
        private String l;

        public void setK(String k) {
            this.k = k;
        }

        public void setF(String f) {
            this.f = f;
        }

        public void setL(String l) {
            this.l = l;
        }
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

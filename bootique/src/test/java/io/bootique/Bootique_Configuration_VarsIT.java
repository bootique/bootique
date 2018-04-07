package io.bootique;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.ConfigurationFactory;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class Bootique_Configuration_VarsIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testDeclareVar_SetValue() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("MY_VAR", "myValue")
                .declareVar("c.m.l", "MY_VAR")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("myValue", b1.c.m.l);
    }

    @Test
    public void testDeclareVar_ConfigPathCaseSensitivity() {
        BQRuntime runtime = testFactory.app()
                .declareVar("m.propCamelCase", "MY_VAR")
                .var("MY_VAR", "myValue")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");
        assertNotNull("Map did not resolve", b4.m);
        assertEquals("Unexpected map contents: " + b4.m, "myValue", b4.m.get("propCamelCase"));
    }

    @Test
    public void testDeclareVar_NameCaseSensitivity() {
        BQRuntime runtime = testFactory.app()
                .declareVar("m.propCamelCase", "MY_VAR")
                .var("my_var", "myValue")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");
        assertNull(b4.m);
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



}

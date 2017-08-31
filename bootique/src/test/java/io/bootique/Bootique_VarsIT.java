package io.bootique;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.bootique.config.ConfigurationFactory;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Bootique_VarsIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testVarSetValue() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("BQ_C_M_F", "f")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("f", b1.c.m.f);
    }

    @Test
    public void testDeclaredVarSetValue() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("BQ_C_M_F", "f")
                .var("MY_VAR", "myValue")
                .declareVar("c.m.l", "MY_VAR")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("myValue", b1.c.m.l);
        assertEquals("f", b1.c.m.f);
    }

    @Test
    @Ignore
    public void testVarCamelCase_AppliedInRandomTheOrder() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("BQ_C_m_F", "camel")
                .var("BQ_C_M_F", "myValue")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("camel", b1.c.m.f);
    }

    @Test
    public void testDeclaredVar_CaseSensitivity() {
        BQRuntime runtime = testFactory.app()
                .declareVar("m.propCamelCase", "MY_VAR")
                .var("MY_VAR", "myValue")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");
        assertNotNull("Map did not resolve", b4.m);
        assertEquals("Unexpected map contents: " + b4.m, "myValue", b4.m.get("propCamelCase"));
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

    static class Bean4 {
        private Map<String, String> m;

        public void setM(Map<String, String> m) {
            this.m = m;
        }
    }

}

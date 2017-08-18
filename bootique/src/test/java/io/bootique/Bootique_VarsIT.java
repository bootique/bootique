package io.bootique;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.inject.ProvisionException;
import io.bootique.config.ConfigurationFactory;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
                .varAlias("c.m.l", "MY_VAR")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("myValue", b1.c.m.l);
        assertEquals("f", b1.c.m.f);
    }

    @Test
    public void testVarCamelCase_AppliedInRandomTheOrder() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("BQ_C_m_F", "camel")
                .var("BQ_C_M_F", "myValue")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("camel", b1.c.m.f);
    }

    @Test(expected = ProvisionException.class)
    public void testDeclaredVar_CanonicalizeVarNameConflict() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/config/configEnvironment.yml")
                .var("BQ_C_M_F", "var1")
                .var("MY_VAR", "myVar")
                //canonical name is BQ_C_M_F (c.m.f -> BQ_C_M_F)
                .varAlias("c.m.f", "MY_VAR")
                .createRuntime();

        runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");
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

}

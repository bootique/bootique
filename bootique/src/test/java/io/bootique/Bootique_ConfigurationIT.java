package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.type.TypeRef;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class Bootique_ConfigurationIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testEmptyConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/empty.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{}", config.toString());
    }

    @Test
    public void testConfigEmptyConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test1.yml",
                "--config=src/test/resources/io/bootique/empty.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=b}", config.toString());
    }

    @Test
    public void testConfigConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test1.yml",
                "--config=src/test/resources/io/bootique/test2.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=e, c=d}", config.toString());
    }

    @Test
    public void testConfigConfig_Reverse() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test2.yml",
                "--config=src/test/resources/io/bootique/test1.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=b, c=d}", config.toString());
    }

    @Test
    public void testConfigEnvOverrides() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test2.yml").var("BQ_A", "F")
                .createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");

        assertEquals("{a=F, c=d}", config.toString());
    }

    @Test
    public void testConfigEnvOverrides_Nested() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test3.yml")
                .var("BQ_A", "F")
                .var("BQ_C_M_F", "F1")
                .var("BQ_C_M_K", "3")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("F", b1.a);
        assertEquals(3, b1.c.m.k);
        assertEquals("n", b1.c.m.l);
        assertEquals("F1", b1.c.m.f);
    }

    @Test
    public void testConfigEnvVars_NoYaml() {
        BQRuntime runtime = runtimeFactory.app()
                .var("BQ_A", "F")
                .var("BQ_C_M_F", "F1")
                .var("BQ_C_M_K", "3")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("F", b1.a);
        assertEquals(3, b1.c.m.k);
        assertNull(b1.c.m.l);
        assertEquals("F1", b1.c.m.f);
    }

    @Test
    public void testConfigEnvVars_NoYaml_Prefix() {
        BQRuntime runtime = runtimeFactory.app()
                .var("BQ_P_A", "F")
                .var("BQ_P_C_M_F", "F1")
                .var("BQ_P_C_M_K", "3")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "p");

        assertEquals("F", b1.a);
        assertEquals(3, b1.c.m.k);
        assertNull(b1.c.m.l);
        assertEquals("F1", b1.c.m.f);
    }

    @Test
    public void testConfigEnvVars_Map() {
        BQRuntime runtime = runtimeFactory.app()
                .var("BQ_M_X", "XXX")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");

        // this assertion highlights a limitation of the shell var CI approach - we end up stuck with an uppercase
        // key that may not be what the end users expect
        assertEquals("XXX", b4.m.get("X"));
    }

    @Test
    public void testConfigEnvVars_MapOverride() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test4.yml")
                .var("BQ_M_X", "XXX")
                .createRuntime();

        Bean4 b4 = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");

        assertEquals("XXX", b4.m.get("x"));
        assertEquals("b", b4.m.get("y"));
    }

    @Test
    public void testConfigEnvOverrides_Alias() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test3.yml")
                .varAlias("a", "V1")
                .varAlias("c.m.f", "V2")
                .varAlias("c.m.k", "V3")
                .var("V1", "K")
                .var("V2", "K1")
                .var("V3", "4")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("K", b1.a);
        assertEquals(4, b1.c.m.k);
        assertEquals("n", b1.c.m.l);
        assertEquals("K1", b1.c.m.f);
    }

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
        private int k;
        private String f;
        private String l;

        public void setK(int k) {
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

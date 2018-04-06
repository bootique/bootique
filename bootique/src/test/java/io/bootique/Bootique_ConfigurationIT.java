package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.type.TypeRef;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class Bootique_ConfigurationIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    @Test
    public void testEmptyConfig() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/empty.yml").createRuntime();

        Map<String, String> config = runtime
                .getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{}", config.toString());
    }

    @Test
    public void testCombineConfigAndEmptyConfig() {
        BQRuntime runtime = testFactory
                .app("--config=classpath:io/bootique/test1.yml", "--config=classpath:io/bootique/empty.yml")
                .createRuntime();

        Map<String, String> config = runtime.
                getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=b}", config.toString());
    }

    @Test
    public void testCombineConfigs() {
        BQRuntime runtime = testFactory
                .app("--config=classpath:io/bootique/test1.yml", "--config=classpath:io/bootique/test2.yml")
                .createRuntime();

        Map<String, String> config = runtime
                .getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=e, c=d}", config.toString());
    }

    @Test
    public void testCombineConfigs_ReverseOrder() {
        BQRuntime runtime = testFactory
                .app("--config=classpath:io/bootique/test2.yml", "--config=classpath:io/bootique/test1.yml")
                .createRuntime();

        Map<String, String> config = runtime
                .getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=b, c=d}", config.toString());
    }

    @Test
    public void testDIConfig() {

        BQRuntime runtime = testFactory.app()
                .module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/diconfig1.yml")
                        .addConfig("classpath:io/bootique/diconfig2.yml"))
                .createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=1, b=2, c=3}", config.toString());
    }

    @Test
    public void testDIConfig_VsCliOrder() {

        BQRuntime runtime = testFactory.app("-c", "classpath:io/bootique/cliconfig.yml")
                .module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/diconfig1.yml")
                        .addConfig("classpath:io/bootique/diconfig2.yml"))
                .createRuntime();

        Map<String, Integer> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, Integer>>() {
                }, "");
        assertEquals("{a=5, b=2, c=6}", config.toString());
    }

    @Test
    public void testDIOnOptionConfig() {

        Function<String, String> configReader =
                arg -> {
                    BQRuntime runtime = testFactory.app(arg)
                            .module(b -> BQCoreModule.extend(b)
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig1.yml")
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig2.yml")
                                    .addOption(OptionMetadata.builder("opt").build()))
                            .createRuntime();

                    Map<String, Integer> config =
                            runtime.getInstance(ConfigurationFactory.class)
                                    .config(new TypeRef<Map<String, Integer>>() {
                                    }, "");

                    return config.toString();
                };

        assertEquals("{}", configReader.apply(""));
        assertEquals("{a=1, b=2, c=3}", configReader.apply("--opt"));
    }

    @Test
    public void testDIOnOptionConfig_OverrideWithOption() {

        Function<String, String> configReader =
                arg -> {
                    BQRuntime runtime = testFactory.app(arg)
                            .module(b -> BQCoreModule.extend(b)
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig1.yml")
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig2.yml")
                                    .addOption("a", "opt"))
                            .createRuntime();

                    return runtime.getInstance(ConfigurationFactory.class)
                            .config(new TypeRef<Map<String, Integer>>() {
                            }, "").toString();
                };

        assertEquals("{}", configReader.apply(""));
        assertEquals("{a=8, b=2, c=3}", configReader.apply("--opt=8"));
    }

    @Test
    public void testConfigEnvOverrides_Alias() {
        BQRuntime runtime = testFactory.app("--config=src/test/resources/io/bootique/test3.yml")
                .declareVar("a", "V1")
                .declareVar("c.m.f", "V2")
                .declareVar("c.m.k", "V3")
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

    @Test
    public void testConfig_OverrideWithProperties() {
        BQRuntime runtime = testFactory.app("--config=classpath:io/bootique/Bootique_ConfigurationIT_props.yml")
                .property("bq.c", "D")
                .property("bq.m.z", "2")
                .createRuntime();

        Bean4 b = runtime.getInstance(ConfigurationFactory.class).config(Bean4.class, "");

        assertEquals("b", b.a);
        assertEquals("D", b.c);
        assertEquals("y", b.m.x);
        assertEquals(2, b.m.z);
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
        private String a;
        private String c;
        private Bean4M m;

        public void setA(String a) {
            this.a = a;
        }

        public void setC(String c) {
            this.c = c;
        }

        public void setM(Bean4M m) {
            this.m = m;
        }
    }

    static class Bean4M {
        private String x;
        private int z;

        public void setX(String x) {
            this.x = x;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }
}

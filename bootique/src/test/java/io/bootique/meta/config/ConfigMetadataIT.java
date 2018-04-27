package io.bootique.meta.config;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.help.ConsoleAppender;
import io.bootique.help.config.ConfigSectionMapGenerator;
import io.bootique.meta.module.ModulesMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConfigMetadataIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testSingleConfig() {
        BQRuntime runtime = runtimeFactory.app().module(new BQModuleProvider() {
            @Override
            public Module module() {
                return Mockito.mock(Module.class);
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("pf", TestConfig.class);
            }

            @Override
            public BQModule.Builder moduleBuilder() {
                return BQModuleProvider.super
                        .moduleBuilder()
                        .name("my");
            }
        }).createRuntime();

        Collection<ConfigMetadataNode> configs = runtime
                .getInstance(ModulesMetadata.class)
                .getModules()
                .stream()
                .filter(mmd -> "my".equals(mmd.getName()))
                .findFirst()
                .get()
                .getConfigs();

        assertEquals(1, configs.size());

        ConfigValueMetadata cm = (ConfigValueMetadata) configs.iterator().next();
        assertTrue("pf".equals(cm.getName()));

        String walkThrough = cm.accept(new ConfigMetadataVisitor<String>() {

            @Override
            public String visitObjectMetadata(ConfigObjectMetadata metadata) {
                StringBuilder buffer = new StringBuilder(visitValueMetadata(metadata));
                metadata.getProperties().forEach(p -> buffer.append(":[").append(p.accept(this)).append("]"));
                return buffer.toString();
            }

            @Override
            public String visitValueMetadata(ConfigValueMetadata metadata) {
                return metadata.getName() + ":" + metadata.getType().getTypeName() + ":" + metadata.getDescription();
            }
        });

        assertEquals("pf:io.bootique.meta.config.ConfigMetadataIT$TestConfig:null:[p1:java.lang.String:(p1 desc)]",
                walkThrough);
    }

    @Test
    public void testRecursiveConfig() {
        BQRuntime runtime = runtimeFactory.app().module(new BQModuleProvider() {
            @Override
            public Module module() {
                return Mockito.mock(Module.class);
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("pf", TestRecursiveConfig.class);
            }

            @Override
            public BQModule.Builder moduleBuilder() {
                return BQModuleProvider.super
                        .moduleBuilder()
                        .name("my");
            }
        }).createRuntime();

        Collection<ConfigMetadataNode> configs = runtime
                .getInstance(ModulesMetadata.class)
                .getModules()
                .stream()
                .filter(mmd -> "my".equals(mmd.getName()))
                .findFirst()
                .get()
                .getConfigs();

        assertEquals(1, configs.size());

        ConfigValueMetadata cm = (ConfigValueMetadata) configs.iterator().next();
        assertTrue("pf".equals(cm.getName()));

        StringBuilder buffer = new StringBuilder();
        ConsoleAppender out = new ConsoleAppender(buffer, 300);

        cm.accept(new ConfigSectionMapGenerator(TestRecursiveConfig.class, out));
        String help = buffer.toString();
        assertNotNull(help);

        assertEquals(help, "<value>:\n" +
                "      #\n" +
                "      # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestRecursiveConfig'.\n" +
                "      #\n" +
                "\n" +
                "      p3:\n" +
                "            #\n" +
                "            # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestRecursiveConfig'.\n" +
                "            #\n" +
                "      p4:\n" +
                "            #\n" +
                "            # Resolved as 'List<io.bootique.meta.config.ConfigMetadataIT$TestRecursiveConfig>'.\n" +
                "            #\n" +
                "            -\n" +
                "                  #\n" +
                "                  # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestRecursiveConfig'.\n" +
                "                  #\n");
    }

    @BQConfig
    public static class TestConfig {

        private String p1;
        private String p2;

        @BQConfigProperty("(p1 desc)")
        public void setP1(String p1) {
            this.p1 = p1;
        }

        public void setP2(String p2) {
            this.p2 = p2;
        }
    }

    @BQConfig
    public static class TestRecursiveConfig {

        @BQConfigProperty
        public void setP3(TestRecursiveConfig v) {
        }

        @BQConfigProperty
        public void setP4(List<TestRecursiveConfig> v) {
        }
    }
}

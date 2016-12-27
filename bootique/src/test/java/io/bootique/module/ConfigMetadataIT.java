package io.bootique.module;

import com.google.inject.Module;
import io.bootique.BQModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
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
            public Map<String, Class<?>> configs() {
                return Collections.singletonMap("pf", TestConfig.class);
            }

            @Override
            public BQModule.Builder moduleBuilder() {
                return BQModuleProvider.super
                        .moduleBuilder()
                        .name("my");
            }
        }).createRuntime();

       Collection<ConfigObjectMetadata> configs = runtime
                .getInstance(ModulesMetadata.class)
                .getModules()
                .stream()
                .filter(mmd -> "my".equals(mmd.getName()))
                .findFirst()
                .get()
                .getConfigs();

        assertEquals(1, configs.size());

        ConfigObjectMetadata cm = configs.iterator().next();
        assertTrue("pf".equals(cm.getName()));

        String walkThrough = cm.accept(new ConfigMetadataVisitor<String>() {

            @Override
            public String visitConfigMetadata(ConfigObjectMetadata metadata) {
                StringBuilder buffer = new StringBuilder(visitConfigPropertyMetadata(metadata));
                metadata.getProperties().forEach(p -> buffer.append(":[").append(p.accept(this)).append("]"));
                return buffer.toString();
            }

            @Override
            public String visitConfigPropertyMetadata(ConfigPropertyMetadata metadata) {
                return metadata.getName() + ":" + metadata.getType().getName() + ":" + metadata.getDescription();
            }
        });

        assertEquals("pf:io.bootique.module.ConfigMetadataIT$TestConfig:null:[p1:java.lang.String:(p1 desc)]" ,
                walkThrough);
    }

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
}

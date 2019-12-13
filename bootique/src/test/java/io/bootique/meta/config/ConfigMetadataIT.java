/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.meta.config;

import io.bootique.BQModuleMetadata;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.di.BQModule;
import io.bootique.help.ConsoleAppender;
import io.bootique.help.ValueObjectDescriptor;
import io.bootique.help.config.ConfigSectionMapGenerator;
import io.bootique.meta.module.ModulesMetadata;
import io.bootique.resource.FolderResourceFactory;
import io.bootique.resource.ResourceFactory;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
            public BQModule module() {
                return Mockito.mock(BQModule.class);
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("pf", TestConfig.class);
            }

            @Override
            public BQModuleMetadata.Builder moduleBuilder() {
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
            public BQModule module() {
                return Mockito.mock(BQModule.class);
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("pf", TestRecursiveConfig.class);
            }

            @Override
            public BQModuleMetadata.Builder moduleBuilder() {
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


    @Test
    public void testValueObjectConfig() {
        BQRuntime runtime = runtimeFactory.app()
                .addValueObjectsDescriptor(TestVO.class, new ValueObjectDescriptor("Test Value Object"))
                .module(new BQModuleProvider() {
            @Override
            public BQModule module() {
                return Mockito.mock(BQModule.class);
            }

            @Override
            public Map<String, Type> configs() {
                return Collections.singletonMap("pf", TestValueObjectConfig.class);
            }

            @Override
            public BQModuleMetadata.Builder moduleBuilder() {
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
        assertEquals("pf", cm.getName());

        StringBuilder buffer = new StringBuilder();
        ConsoleAppender out = new ConsoleAppender(buffer, 300);

        cm.accept(new ConfigSectionMapGenerator(TestValueObjectConfig.class, out));
        String help = buffer.toString();
        assertNotNull(help);

        assertEquals(help, "<value>:\n" +
                "      #\n" +
                "      # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestValueObjectConfig'.\n" +
                "      #\n" +
                "\n" +
                "      # (p1 desc)\n" +
                "      # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestVO'.\n" +
                "      p1: <Test Value Object>\n");
    }

    @Test
    public void testSampleValue() {
        ConfigValueMetadata valueMetadata = new ConfigValueMetadata();
        assertEquals("<int>", valueMetadata.getSampleValue(Integer.class));
        assertEquals("<int>", valueMetadata.getSampleValue(Integer.TYPE));
        assertEquals("<true|false>", valueMetadata.getSampleValue(Boolean.class));
        assertEquals("<true|false>", valueMetadata.getSampleValue(Boolean.TYPE));
        assertEquals("<string>", valueMetadata.getSampleValue(String.class));
        assertEquals("<value>", valueMetadata.getSampleValue(Bootique.class));
        assertEquals("<value>", valueMetadata.getSampleValue(HashMap.class));
        assertEquals("<value>", valueMetadata.getSampleValue(ArrayList.class));
        assertEquals("<a|B|Cd>", valueMetadata.getSampleValue(E.class));
        assertEquals("<resource-uri>", valueMetadata.getSampleValue(ResourceFactory.class));
        assertEquals("<folder-resource-uri>", valueMetadata.getSampleValue(FolderResourceFactory.class));
    }

    @Test
    public void testGetValueLabel() throws NoSuchFieldException {
        ConfigValueMetadata valueMetadata = ConfigValueMetadata.builder().valueLabel("Test Label").build();
        assertEquals("<Test Label>", valueMetadata.getValueLabel());
    }

    @Test
    public void testNoTypeValueLabel() {
        ConfigValueMetadata valueMetadata = new ConfigValueMetadata();
        assertEquals("?", valueMetadata.getValueLabel());
    }

    @Test
    public void testTypeValueLabel() throws NoSuchFieldException {
        ConfigValueMetadata valueMetadata = ConfigValueMetadata.builder().type(E.class).build();
        assertEquals("<a|B|Cd>", valueMetadata.getValueLabel());
    }

    public enum E {
        a, B, Cd
    }

    public static class TestVO {
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

    @BQConfig
    public static class TestValueObjectConfig {

        private TestVO p1;

        @BQConfigProperty("(p1 desc)")
        public void setP1(TestVO p1) {
            this.p1 = p1;
        }

    }

}

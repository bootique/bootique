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

import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;
import io.bootique.help.ConsoleAppender;
import io.bootique.help.ValueObjectDescriptor;
import io.bootique.help.config.ConfigSectionMapGenerator;
import io.bootique.meta.module.ModulesMetadata;
import io.bootique.resource.FolderResourceFactory;
import io.bootique.resource.ResourceFactory;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigMetadataIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void singleConfig() {
        BQModuleProvider provider = () -> BuiltModule
                .of(Mockito.mock(BQModule.class))
                .moduleName("my")
                .config("pf", TestConfig.class).build();

        BQRuntime runtime = appManager.runtime(Bootique.app().moduleProvider(provider));

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

        String walkThrough = cm.accept(new ConfigMetadataVisitor<>() {

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
    public void recursiveConfig() {
        BQModuleProvider provider = () -> BuiltModule
                .of(Mockito.mock(BQModule.class))
                .moduleName("my")
                .config("pf", TestRecursiveConfig.class).build();

        BQRuntime runtime = appManager.runtime(Bootique.app().moduleProvider(provider));

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

        cm.accept(new ConfigSectionMapGenerator(TestRecursiveConfig.class, out, new HashSet<>()));
        String help = buffer.toString();
        assertNotNull(help);

        assertEquals("<value>:\n" +
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
                "                  #\n", help.replace("\r", ""));
    }


    @Test
    public void valueObjectConfig() {
        BQModuleProvider provider = () -> BuiltModule
                .of(Mockito.mock(BQModule.class))
                .moduleName("my")
                .config("pf", TestValueObjectConfig.class).build();

        BQRuntime runtime = appManager.runtime(Bootique.app()
                .module(b -> BQCoreModule.extend(b).addValueObjectDescriptor(TestVO.class, new ValueObjectDescriptor("Test Value Object")))
                .moduleProvider(provider));

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

        cm.accept(new ConfigSectionMapGenerator(TestValueObjectConfig.class, out, new HashSet<>()));
        String help = buffer.toString();
        assertNotNull(help);

        assertEquals("<value>:\n" +
                "      #\n" +
                "      # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestValueObjectConfig'.\n" +
                "      #\n" +
                "\n" +
                "      # (p1 desc)\n" +
                "      # Resolved as 'io.bootique.meta.config.ConfigMetadataIT$TestVO'.\n" +
                "      p1: <Test Value Object>\n", help.replace("\r", ""));
    }

    @Test
    public void sampleValue() {
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
    public void getValueLabel() {
        ConfigValueMetadata valueMetadata = ConfigValueMetadata.builder().valueLabel("Test Label").build();
        assertEquals("<Test Label>", valueMetadata.getValueLabel());
    }

    @Test
    public void noTypeValueLabel() {
        ConfigValueMetadata valueMetadata = new ConfigValueMetadata();
        assertEquals("?", valueMetadata.getValueLabel());
    }

    @Test
    public void typeValueLabel() {
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

        @BQConfigProperty("(p1 desc)")
        public void setP1(String p1) {
        }

        public void setP2(String p2) {
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

        @BQConfigProperty("(p1 desc)")
        public void setP1(TestVO p1) {
        }
    }

}

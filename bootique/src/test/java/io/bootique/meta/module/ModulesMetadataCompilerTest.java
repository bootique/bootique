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

package io.bootique.meta.module;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.log.DefaultBootLogger;
import io.bootique.meta.config.ConfigMetadataCompiler;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModulesMetadataCompilerTest {

    private ModulesMetadataCompiler createCompiler() {
        ConfigMetadataCompiler configCompiler = new ConfigMetadataCompiler(
                new DefaultBootLogger(true),
                c -> Stream.empty(),
                Collections.emptyMap());
        return new ModulesMetadataCompiler(configCompiler);
    }

    // https://github.com/bootique/bootique/issues/372
    @Test
    public void overlappingConfigPrefixes() {
        BQModule module = b -> {
        };
        ModuleCrate crate = ModuleCrate.of(module)
                .moduleName("M1")
                .config("parent", ParentConfig.class)
                .config("parent.child", ChildConfig.class)
                .build();

        ModulesMetadata modulesMetadata = createCompiler().compile(List.of(crate));
        ModuleMetadata moduleMetadata = modulesMetadata.getModules().iterator().next();
        Collection<ConfigMetadataNode> configs = moduleMetadata.getConfigs();

        assertEquals(1, configs.size(), "Overlapping prefix 'parent.child' should be merged into 'parent'");

        ConfigObjectMetadata parentMeta = (ConfigObjectMetadata) configs.iterator().next();
        assertEquals("parent", parentMeta.getName());

        assertTrue(
                parentMeta.getProperties().stream().anyMatch(p -> "child".equals(p.getName())),
                "Expected 'child' to be a property of 'parent'");
    }

    // https://github.com/bootique/bootique/issues/372
    @Test
    public void dottedConfigPath_NoExplicitParent() {
        BQModule module = b -> {
        };
        ModuleCrate crate = ModuleCrate.of(module)
                .moduleName("M1")
                .config("parent.child", ChildConfig.class)
                .build();

        ModulesMetadata modulesMetadata = createCompiler().compile(List.of(crate));
        ModuleMetadata moduleMetadata = modulesMetadata.getModules().iterator().next();
        Collection<ConfigMetadataNode> configs = moduleMetadata.getConfigs();

        assertEquals(1, configs.size(), "Dotted path 'parent.child' should produce a single root config 'parent'");

        ConfigObjectMetadata parentMeta = (ConfigObjectMetadata) configs.iterator().next();
        assertEquals("parent", parentMeta.getName());

        assertTrue(
                parentMeta.getProperties().stream().anyMatch(p -> "child".equals(p.getName())),
                "Expected 'child' to be a property of 'parent'");
    }

    // https://github.com/bootique/bootique/issues/372
    @Test
    public void deepDottedConfigPath_NoExplicitAncestors() {
        BQModule module = b -> {
        };
        ModuleCrate crate = ModuleCrate.of(module)
                .moduleName("M1")
                .config("parent.child.grandchild", GrandchildConfig.class)
                .build();

        ModulesMetadata modulesMetadata = createCompiler().compile(List.of(crate));
        ModuleMetadata moduleMetadata = modulesMetadata.getModules().iterator().next();
        Collection<ConfigMetadataNode> configs = moduleMetadata.getConfigs();

        assertEquals(1, configs.size());

        ConfigObjectMetadata parentMeta = (ConfigObjectMetadata) configs.iterator().next();
        assertEquals("parent", parentMeta.getName());

        ConfigObjectMetadata childMeta = (ConfigObjectMetadata) parentMeta.getProperties().stream()
                .filter(p -> "child".equals(p.getName()))
                .findFirst()
                .orElseThrow();
        assertTrue(childMeta.getProperties().stream().anyMatch(p -> "grandchild".equals(p.getName())));
    }

    @Test
    public void multipleChildrenUnderSameParent() {
        BQModule module = b -> {
        };
        ModuleCrate crate = ModuleCrate.of(module)
                .moduleName("M1")
                .config("parent.child1", ChildConfig.class)
                .config("parent.child2", GrandchildConfig.class)
                .build();

        ModulesMetadata modulesMetadata = createCompiler().compile(List.of(crate));
        ModuleMetadata moduleMetadata = modulesMetadata.getModules().iterator().next();
        Collection<ConfigMetadataNode> configs = moduleMetadata.getConfigs();

        assertEquals(1, configs.size());

        ConfigObjectMetadata parentMeta = (ConfigObjectMetadata) configs.iterator().next();
        assertEquals("parent", parentMeta.getName());
        assertTrue(parentMeta.getProperties().stream().anyMatch(p -> "child1".equals(p.getName())));
        assertTrue(parentMeta.getProperties().stream().anyMatch(p -> "child2".equals(p.getName())));
    }

    @Test
    public void pathConflict_ContainerAlreadyAValue() {
        BQModule module = b -> {
        };
        // "parent" compiled as a value (String), then "parent.child" tries to use it as a container
        ModuleCrate crate = ModuleCrate.of(module)
                .moduleName("M1")
                .config("parent", String.class)
                .config("parent.child", ChildConfig.class)
                .build();

        assertThrows(IllegalArgumentException.class, () -> createCompiler().compile(List.of(crate)));
    }

    @Test
    public void invalidPath_StartsWithDot() {
        ModuleCrate crate = ModuleCrate.of((BQModule) b -> {}).config(".parent", ParentConfig.class).build();
        assertThrows(IllegalArgumentException.class, () -> createCompiler().compile(List.of(crate)));
    }

    @Test
    public void invalidPath_EndsWithDot() {
        ModuleCrate crate = ModuleCrate.of((BQModule) b -> {}).config("parent.", ParentConfig.class).build();
        assertThrows(IllegalArgumentException.class, () -> createCompiler().compile(List.of(crate)));
    }

    @Test
    public void invalidPath_RepeatingDots() {
        ModuleCrate crate = ModuleCrate.of((BQModule) b -> {}).config("parent..child", ParentConfig.class).build();
        assertThrows(IllegalArgumentException.class, () -> createCompiler().compile(List.of(crate)));
    }

    @BQConfig
    static class ParentConfig {

        @BQConfigProperty
        public void setP1(String p1) {
        }
    }

    @BQConfig
    static class ChildConfig {

        @BQConfigProperty
        public void setP2(int p2) {
        }
    }

    @BQConfig
    static class GrandchildConfig {

        @BQConfigProperty
        public void setP3(int p3) {
        }
    }
}

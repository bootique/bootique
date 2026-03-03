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

import io.bootique.ModuleCrate;
import io.bootique.meta.config.ConfigMetadataCompiler;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ModulesMetadataCompiler {

    private final ConfigMetadataCompiler configCompiler;

    public ModulesMetadataCompiler(ConfigMetadataCompiler configCompiler) {
        this.configCompiler = configCompiler;
    }

    public ModulesMetadata compile(Collection<ModuleCrate> modules) {
        ModulesMetadata.Builder builder = ModulesMetadata.builder();
        modules.forEach(m -> builder.addModule(toModuleMetadata(m)));
        return builder.build();
    }

    private ModuleMetadata toModuleMetadata(ModuleCrate crate) {
        return ModuleMetadata
                .builder(crate.getModuleName())
                .type(crate.getModule().getClass())
                .description(crate.getDescription())
                .deprecated(crate.isDeprecated())
                .addConfigs(toConfigs(crate))
                .build();
    }

    private Collection<ConfigMetadataNode> toConfigs(ModuleCrate module) {

        Map<String, Type> configTypes = module.getConfigs();
        if (configTypes.isEmpty()) {
            return Collections.emptyList();
        }

        ConfigObjectMetadata commonRoot = ConfigObjectMetadata.builder("temp_root").build();

        // Sort alphabetically so parent paths are always processed before their children
        // (e.g. "parent" before "parent.child")
        configTypes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> process(commonRoot, e.getKey(), e.getValue()));

        // TODO: suppose we can have a single configuration root instead of a collection? In this case we will not
        //   need to unpack "commonRoot"
        return commonRoot.getProperties();
    }

    private void process(
            ConfigObjectMetadata parent,
            String path,
            Type type) {

        if (path.charAt(0) == '.') {
            throw new IllegalArgumentException("Invalid path. Can not start with a '.': " + path);
        }

        if (path.charAt(path.length() - 1) == '.') {
            throw new IllegalArgumentException("Invalid path. Can not end with a '.': " + path);
        }

        String[] chunks = path.split("\\.");
        int parentLen = chunks.length - 1;
        for (int i = 0; i < parentLen; i++) {
            if (chunks[i].isEmpty()) {
                throw new IllegalArgumentException("Invalid path. Can not have repeating '.': " + path);
            }

            parent = getOrCreateParent(parent, chunks[i]);
        }

        parent.getProperties().add(configCompiler.compile(chunks[parentLen], type));
    }

    private ConfigObjectMetadata getOrCreateParent(ConfigObjectMetadata grandParent, String name) {
        ConfigMetadataNode p = grandParent
                .getProperties()
                .stream()
                .filter(n -> name.equals(n.getName()))
                .findFirst()
                .orElse(null);

        if (p == null) {

            ConfigObjectMetadata np = ConfigObjectMetadata.builder(name).build();
            grandParent.getProperties().add(np);

            return np;
        } else if (p instanceof ConfigObjectMetadata parentContainer) {
            return parentContainer;
        } else {
            throw new IllegalArgumentException("Path container component '" + name + "' conflicts with an existing value path");
        }
    }

}

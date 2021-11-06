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
package io.bootique.junit5;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @since 2.0
 */
public class BQRuntimeChecker {

    /**
     * Verifies that runtime contains expected modules.
     *
     * @param runtime         a Bootique runtime whose contents we are testing.
     * @param expectedModules a vararg array of expected module types.
     */
    @SafeVarargs
    public static void testModulesLoaded(BQRuntime runtime, Class<? extends BQModule>... expectedModules) {

        ModulesMetadata modulesMetadata = runtime.getInstance(ModulesMetadata.class);

        Set<String> actualModules = modulesMetadata
                .getModules()
                .stream()
                .map(ModuleMetadata::getName)
                .collect(Collectors.toSet());

        List<String> missingModules = Stream.of(expectedModules)
                .map(Class::getSimpleName)
                // Using class names for checking whether modules exist - weak.
                .filter(n -> !actualModules.contains(n))
                .collect(Collectors.toList());

        if(!missingModules.isEmpty()) {
            fail("The following expected modules are missing: " + missingModules);
        }
    }
}

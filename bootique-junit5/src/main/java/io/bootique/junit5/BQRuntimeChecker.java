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

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
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

        HashSet<String> actualModules = new HashSet<>(modulesMetadata
                .getModules()
                .stream()
                .map(ModuleMetadata::getName)
                .collect(toList()));

        List<String> missingModules = new ArrayList<>();
        Stream.of(expectedModules)
                .map(Class::getSimpleName)
                .forEach(n -> {

                    // Using class names for checking module existing - weak.
                    if (!actualModules.contains(n)) {
                        missingModules.add(n);
                    }
                });


        if(!missingModules.isEmpty()) {
            fail("The following expected modules are missing: " + missingModules);
        }
    }
}

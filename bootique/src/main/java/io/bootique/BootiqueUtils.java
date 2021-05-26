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

package io.bootique;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Non-public utils methods serving {@link Bootique} class.
 */
final class BootiqueUtils {

    private BootiqueUtils() {
        throw new AssertionError("Should not be called.");
    }

    static Collection<BQModuleMetadata> moduleProviderDependencies(Collection<BQModuleProvider> rootSet) {
        return moduleProviderDependencies(rootSet, new HashSet<>());
    }

    private static Set<BQModuleMetadata> moduleProviderDependencies(
            Collection<BQModuleProvider> rootSet,
            Set<BQModuleMetadata> metadata) {

        for (BQModuleProvider moduleProvider : rootSet) {
            BQModuleMetadata next = moduleProvider.moduleBuilder().build();
            if (metadata.add(next)) {
                Collection<BQModuleProvider> dependencies = moduleProvider.dependencies();
                if (!dependencies.isEmpty()) {
                    metadata.addAll(moduleProviderDependencies(dependencies, metadata));
                }
            }
        }

        return metadata;
    }

    static String[] mergeArrays(String[] a1, String[] a2) {
        if (a1.length == 0) {
            return a2;
        }

        if (a2.length == 0) {
            return a1;
        }

        String[] merged = new String[a1.length + a2.length];
        System.arraycopy(a1, 0, merged, 0, a1.length);
        System.arraycopy(a2, 0, merged, a1.length, a2.length);

        return merged;
    }

    static String[] toArray(Collection<String> collection) {
        return collection.toArray(new String[0]);
    }
}


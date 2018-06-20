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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Provides API to look up subclasses of a given class within a predefined set of types.
 *
 * @since 0.21
 */
public class ConfigHierarchyResolver {

    private Map<Class<?>, Type> hierarchies;

    protected ConfigHierarchyResolver(Map<Class<?>, Type> hierarchies) {
        this.hierarchies = hierarchies;
    }

    public static <T> ConfigHierarchyResolver create(Collection<Class<? extends T>> typeSet) {

        Map<Class<?>, Type> hierarchies = new HashMap<>();

        // Types are graph nodes
        typeSet.forEach(c -> hierarchies.put(c, new Type(c)));

        // subclass and interface implementation relationship are graph edges
        hierarchies.values().forEach(t -> {

            Class<?> superClass = t.getRootClass().getSuperclass();
            while (superClass != null) {
                Type superType = hierarchies.get(superClass);
                if (superType != null) {
                    superType.getDirectSubclasses().add(t);
                    break;
                }

                superClass = superClass.getSuperclass();
            }

            for(Class<?> iface : t.getRootClass().getInterfaces()) {
                Type iType = hierarchies.get(iface);
                if(iType != null) {
                    iType.getDirectSubclasses().add(t);
                }
            }
        });

        return new ConfigHierarchyResolver(hierarchies);
    }

    public Stream<Class<?>> directSubclasses(Class<?> superclass) {
        Type type = hierarchies.get(superclass);
        return (type == null) ? Stream.empty() : type.getDirectSubclasses().stream().map(Type::getRootClass);
    }

    static class Type {
        private Class<?> rootClass;
        private Collection<Type> directSubclasses;

        public Type(Class<?> rootClass) {
            this.rootClass = rootClass;
            this.directSubclasses = new HashSet<>();
        }

        public Collection<Type> getDirectSubclasses() {
            return directSubclasses;
        }

        public Class<?> getRootClass() {
            return rootClass;
        }
    }
}

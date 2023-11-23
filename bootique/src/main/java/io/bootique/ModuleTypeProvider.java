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

import io.bootique.di.BQModule;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

class ModuleTypeProvider implements BQModuleProvider {

    private final Class<? extends BQModule> moduleType;
    private final Collection<Class<? extends BQModule>> overrides;

    public ModuleTypeProvider(Class<? extends BQModule> moduleType) {
        this(moduleType, Collections.emptyList());
    }

    public ModuleTypeProvider(Class<? extends BQModule> moduleType, Collection<Class<? extends BQModule>> overrides) {
        this.moduleType = moduleType;
        this.overrides = overrides != null ? overrides : Collections.emptyList();
    }

    @Override
    public BQModule module() {
        try {
            return moduleType.getDeclaredConstructor().newInstance();
        } catch (
                InstantiationException |
                IllegalAccessException |
                NoSuchMethodException |
                InvocationTargetException e) {
            throw new RuntimeException("Error instantiating Module of type: " + moduleType.getName(), e);
        }
    }

    @Override
    public Collection<Class<? extends BQModule>> overrides() {
        return overrides;
    }
}

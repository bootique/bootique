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

import java.util.Objects;

/**
 * An abstract {@link BQModule} identifier, generally is a thin wrapper around module {@link Class}.
 *
 * @deprecated ModuleCrate inlines this logic in its own hashCode/equals.
 */
@Deprecated(since = "3.0", forRemoval = true)
class BQModuleId {

    private final Class<? extends BQModule> moduleClass;

    static BQModuleId of(BQModule module) {
        return new BQModuleId(module.getClass());
    }

    BQModuleId(Class<? extends BQModule> moduleClass) {
        this.moduleClass = Objects.requireNonNull(moduleClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (moduleClass.isSynthetic()) {
            return false;
        }
        BQModuleId that = (BQModuleId) o;
        return moduleClass.equals(that.moduleClass);
    }

    @Override
    public int hashCode() {
        return moduleClass.hashCode();
    }
}

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

import com.google.inject.Module;

/**
 * A superclass of modules that combines functionality of a DI module with that of a module provider. For most modules
 * creating a separate class that implements {@link BQModuleProvider} is an overkill, and using this class as a superclass
 * cuts down on boilerplate code. Note that this class still requires an entry in
 * `META-INF/services/io.bootique.BQModuleProvider` file for the module to be auto-loadable.
 *
 * @since 1.1
 */
public abstract class BaseModule extends ConfigModule implements BQModuleProvider {

    protected BaseModule() {
    }

    protected BaseModule(String configPrefix) {
        super(configPrefix);
    }

    @Override
    public Module module() {
        return this;
    }
}

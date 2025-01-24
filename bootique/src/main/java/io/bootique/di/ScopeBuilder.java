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

package io.bootique.di;

public interface ScopeBuilder {

    /**
     * Sets the scope of a bound instance. This method is used to change the default scope
     * which is a singleton by default to a custom scope.
     */
    void in(Scope scope);

    /**
     * Sets the scope of a bound instance to singleton. Singleton is normally the default.
     */
    void inSingletonScope();

    /**
     * Sets the scope of a bound instance to "no scope". This means that a new instance of
     * an object will be created on every call to {@link Injector#getInstance(Class)} or
     * to {@link jakarta.inject.Provider} of this instance.
     */
    void withoutScope();

    /**
     * Marks this injection point to be triggered early without explicit call to {@link Injector#getInstance(Class)}.
     * Such service will be created right after {@link Injector} is fully setup.
     *
     * NOTE: use with caution, as this will effectively create all graph of services that this service depends on.
     */
    void initOnStartup();

}

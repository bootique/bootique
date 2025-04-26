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

import jakarta.inject.Provider;

/**
 * Defines the scope of the instances created by the DI container. I.e. whether instances
 * are shared between the callers, and for how longs or whether they are created anew.
 * Scope object is also used to tie DI-produced instances to the Injector events, such as
 * shutdown. Default scope in Bootique DI is "singleton".
 */
public interface Scope {

    /**
     * @since 3.0
     */
    <T> Provider<T> scope(Provider<T> unscoped);
}

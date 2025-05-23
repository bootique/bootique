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

package io.bootique.shutdown;

import java.util.Map;

/**
 * A service that can shut down other services when the app is exiting.
 */
public interface ShutdownManager {

    /**
     * Registers an AutoCloseable object to be invoked during Bootique runtime shutdown.
     *
     * @since 3.0
     */
    default <T extends AutoCloseable> T onShutdown(T object) {
        return onShutdown(object, AutoCloseable::close);
    }

    /**
     * Registers an object and its shutdown callback method to be invoked during Bootique runtime shutdown.
     *
     * @since 3.0
     */
    <T> T onShutdown(T object, ShutdownCallback<T> shutdownCallback);

    /**
     * Executes shutdown, calling shutdown methods of each registered object.
     *
     * @return a map of registered shutdown objects to Exceptions for the objects that threw  during shutdown
     */
    Map<?, ? extends Throwable> shutdown();
}

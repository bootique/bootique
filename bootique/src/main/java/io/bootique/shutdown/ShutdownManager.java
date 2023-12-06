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
     * Registers an object whose "close" method needs to be invoked during shutdown.
     *
     * @param shutdownListener an object that needs to be notified on shutdown.
     * @deprecated in favor of one of the "onShutdown" methods.
     */
    @Deprecated(since = "3.0", forRemoval = true)
    default void addShutdownHook(AutoCloseable shutdownListener) {
        onShutdown(shutdownListener, AutoCloseable::close);
    }

    /**
     * Registers an object and its shutdown method to be invoked during shutdown.
     *
     * @since 3.0
     */
    default <T extends AutoCloseable> T onShutdown(T object) {
        return onShutdown(object, AutoCloseable::close);
    }

    /**
     * Registers an object and its shutdown method to be invoked during shutdown.
     *
     * @since 3.0
     */
    <T> T onShutdown(T object, ShutdownCallback<T> shutdownCallback);

    /**
     * Executes shutdown, calling "close" method of all registered listeners.
     *
     * @return a map of shutdown listeners to exceptions they generated, if any.
     */
    Map<?, ? extends Throwable> shutdown();
}

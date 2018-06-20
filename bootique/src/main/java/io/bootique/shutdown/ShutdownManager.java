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
 * A service that can shutdown other services when the app is exiting.
 *
 * @since 0.11
 */
public interface ShutdownManager {

    /**
     * Registers an object whose "close" method needs to be invoked during shutdown.
     *
     * @param shutdownListener an object that needs to be notified on shutdown.
     */
    void addShutdownHook(AutoCloseable shutdownListener);

    /**
     * Executes shutdown, calling "close" method of all registered listeners.
     * @return a map of shutdown listeners to shutdown exceptions they generated, if any.
     */
    Map<?, ? extends Throwable> shutdown();
}

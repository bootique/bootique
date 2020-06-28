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
package io.bootique.junit5.handler;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;

/**
 * @since 2.0
 */
public class Callback {

    protected BeforeAllCallback beforeAll;
    protected BeforeEachCallback beforeEach;
    protected AfterEachCallback afterEach;
    protected AfterAllCallback afterAll;

    public void addToRegistry(CallbackRegistry registry) {
        if (beforeAll != null) {
            registry.beforeAll = registry.addToCallbacks(registry.beforeAll, beforeAll);
        }

        if (beforeEach != null) {
            registry.beforeEach = registry.addToCallbacks(registry.beforeEach, beforeEach);
        }

        if (afterEach != null) {
            registry.afterEach = registry.addToCallbacks(registry.afterEach, afterEach);
        }

        if (afterAll != null) {
            registry.afterAll = registry.addToCallbacks(registry.afterAll, afterAll);
        }
    }
}

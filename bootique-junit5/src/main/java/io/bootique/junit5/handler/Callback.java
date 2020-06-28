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

import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.scope.BQAfterMethodCallback;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
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

    public static Callback create(Object callback) {

        Callback callbackWrapper = new Callback();

        if (callback instanceof BQBeforeScopeCallback) {
            callbackWrapper.beforeAll = new OnlyOnceBeforeScopeCallback((BQBeforeScopeCallback) callback, BQTestScope.GLOBAL);
        }

        if (callback instanceof BQBeforeMethodCallback) {
            callbackWrapper.beforeEach = c -> ((BQBeforeMethodCallback) callback).beforeMethod(BQTestScope.GLOBAL, c);
        }

        if (callback instanceof BQAfterMethodCallback) {
            callbackWrapper.afterEach = c -> ((BQAfterMethodCallback) callback).afterMethod(BQTestScope.GLOBAL, c);
        }

        if (callback instanceof BQAfterScopeCallback) {
            callbackWrapper.afterAll = c -> ((BQAfterScopeCallback) callback).afterScope(BQTestScope.GLOBAL, c);
        }

        return callbackWrapper;
    }

    public BeforeAllCallback getBeforeAll() {
        return beforeAll;
    }

    public BeforeEachCallback getBeforeEach() {
        return beforeEach;
    }

    public AfterAllCallback getAfterAll() {
        return afterAll;
    }

    public AfterEachCallback getAfterEach() {
        return afterEach;
    }
}

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
import org.junit.jupiter.api.extension.*;

import java.util.LinkedHashSet;

/**
 * @since 2.0
 */
public class CallbackRegistry {

    private LinkedHashSet<BeforeAllCallback> beforeAll;
    private LinkedHashSet<BeforeEachCallback> beforeEach;
    private LinkedHashSet<AfterEachCallback> afterEach;
    private LinkedHashSet<AfterAllCallback> afterAll;

    public void add(BQTestScope scope, Object tool) {

        if (tool instanceof BQBeforeScopeCallback) {

            switch (scope) {
                // TODO: global scope instances must be managed separately via static wrappers that ensure "only-once"
                //  invocation of "beforeAll" across multiple test classes
                case GLOBAL:
                    this.beforeAll = addToCallbacks(beforeAll, c -> ((BQBeforeScopeCallback) tool).beforeScope(BQTestScope.GLOBAL, c));
                    break;
                case TEST_CLASS:
                    this.beforeAll = addToCallbacks(beforeAll, c -> ((BQBeforeScopeCallback) tool).beforeScope(BQTestScope.TEST_CLASS, c));
                    break;
                case TEST_METHOD:
                    this.beforeEach = addToCallbacks(beforeEach, c -> ((BQBeforeScopeCallback) tool).beforeScope(BQTestScope.TEST_METHOD, c));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported/unresolved test scope: " + scope);
            }
        }

        if (tool instanceof BQBeforeMethodCallback) {

            switch (scope) {
                case GLOBAL:
                    this.beforeEach = addToCallbacks(beforeEach, c -> ((BQBeforeMethodCallback) tool).beforeMethod(BQTestScope.GLOBAL, c));
                    break;
                case TEST_CLASS:
                    this.beforeEach = addToCallbacks(beforeEach, c -> ((BQBeforeMethodCallback) tool).beforeMethod(BQTestScope.TEST_CLASS, c));
                    break;
                case TEST_METHOD:
                    this.beforeEach = addToCallbacks(beforeEach, c -> ((BQBeforeMethodCallback) tool).beforeMethod(BQTestScope.TEST_METHOD, c));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported/unresolved test scope: " + scope);
            }
        }

        if (tool instanceof BQAfterMethodCallback) {

            switch (scope) {
                case GLOBAL:
                    this.afterEach = addToCallbacks(afterEach, c -> ((BQAfterMethodCallback) tool).afterMethod(BQTestScope.GLOBAL, c));
                    break;
                case TEST_CLASS:
                    this.afterEach = addToCallbacks(afterEach, c -> ((BQAfterMethodCallback) tool).afterMethod(BQTestScope.TEST_CLASS, c));
                    break;
                case TEST_METHOD:
                    this.afterEach = addToCallbacks(afterEach, c -> ((BQAfterMethodCallback) tool).afterMethod(BQTestScope.TEST_METHOD, c));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported/unresolved test scope: " + scope);
            }
        }

        if (tool instanceof BQAfterScopeCallback) {

            switch (scope) {
                case GLOBAL:
                    // don't bother with "afterAll" for the GLOBAL scope. We won't be able to tell whether class end
                    // signifies the end of the scope.
                    break;
                case TEST_CLASS:
                    this.afterAll = addToCallbacks(afterAll, c -> ((BQAfterScopeCallback) tool).afterScope(BQTestScope.TEST_CLASS, c));
                    break;
                case TEST_METHOD:
                    this.afterEach = addToCallbacks(afterEach, c -> ((BQAfterScopeCallback) tool).afterScope(BQTestScope.TEST_METHOD, c));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported/unresolved test scope: " + scope);
            }
        }
    }

    protected <T> LinkedHashSet<T> addToCallbacks(LinkedHashSet<T> set, T instance) {
        if (set == null) {
            set = new LinkedHashSet<>();
        }

        set.add(instance);
        return set;
    }

    public void beforeAll(ExtensionContext context) throws Exception {
        if (beforeAll != null) {
            for (BeforeAllCallback c : beforeAll) {
                c.beforeAll(context);
            }
        }
    }

    public void beforeEach(ExtensionContext context) throws Exception {
        if (beforeEach != null) {
            for (BeforeEachCallback c : beforeEach) {
                c.beforeEach(context);
            }
        }
    }

    public void afterEach(ExtensionContext context) throws Exception {
        if (afterEach != null) {
            for (AfterEachCallback c : afterEach) {
                c.afterEach(context);
            }
        }
    }


    public void afterAll(ExtensionContext context) throws Exception {
        if (afterAll != null) {
            for (AfterAllCallback c : afterAll) {
                c.afterAll(context);
            }
        }
    }
}

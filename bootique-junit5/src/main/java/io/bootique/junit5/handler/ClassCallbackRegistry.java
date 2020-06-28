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
import io.bootique.junit5.BQTestTool;
import io.bootique.junit5.scope.BQAfterMethodCallback;
import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeMethodCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @since 2.0
 */
public class ClassCallbackRegistry extends CallbackRegistry {

    public static ClassCallbackRegistry create(ExtensionContext context) {

        ClassCallbackRegistry registry = new ClassCallbackRegistry();

        Class<?> testType = context.getRequiredTestClass();
        Predicate<Field> predicate = f -> registry.supportedCallback(f);

        ReflectionUtils
                .findFields(testType, predicate, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .forEach(f -> registry.add(resolveInstance(null, f)));

        return registry;
    }

    @Override
    public boolean supportedCallback(Field callbackField) {
        BQTestTool a = callbackField.getAnnotation(BQTestTool.class);
        if (a == null) {
            return false;
        }

        boolean isStatic = ReflectionUtils.isStatic(callbackField);

        switch (a.value()) {
            case TEST_CLASS:
                if (!isStatic) {
                    throw new JUnitException("@BQTestTool field '"
                            + callbackField.getDeclaringClass().getName() + "." + callbackField.getName()
                            + "' must be static to be used in TEST_CLASS scope");
                }
                return true;
            case IMPLICIT:
                // static fields with no explicit scope should be treated as TEST_CLASS
                return isStatic;
            default:
                return false;
        }
    }

    protected void add(Object callback) {

        if (callback instanceof BQBeforeScopeCallback) {
            this.beforeAll = addToCallbacks(beforeAll, c -> ((BQBeforeScopeCallback) callback).beforeScope(BQTestScope.TEST_CLASS, c));
        }

        if (callback instanceof BQBeforeMethodCallback) {
            this.beforeEach = addToCallbacks(beforeEach, c -> ((BQBeforeMethodCallback) callback).beforeMethod(BQTestScope.TEST_CLASS, c));
        }

        if (callback instanceof BQAfterMethodCallback) {
            this.afterEach = addToCallbacks(afterEach, c -> ((BQAfterMethodCallback) callback).afterMethod(BQTestScope.TEST_CLASS, c));
        }

        if (callback instanceof BQAfterScopeCallback) {
            this.afterAll = addToCallbacks(afterAll, c -> ((BQAfterScopeCallback) callback).afterScope(BQTestScope.TEST_CLASS, c));
        }
    }
}

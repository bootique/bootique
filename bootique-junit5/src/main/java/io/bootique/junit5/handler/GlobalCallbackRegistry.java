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
public class GlobalCallbackRegistry extends CallbackRegistry {

    public static GlobalCallbackRegistry create(ExtensionContext context, GlobalCallbacks globalCallbacks) {

        GlobalCallbackRegistry registry = new GlobalCallbackRegistry();

        Class<?> testType = context.getRequiredTestClass();
        Predicate<Field> predicate = f -> registry.supportedCallback(f);

        ReflectionUtils
                .findFields(testType, predicate, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .forEach(f -> registry.add(f, globalCallbacks));

        return registry;
    }

    @Override
    public boolean supportedCallback(Field callbackField) {
        BQTestTool a = callbackField.getAnnotation(BQTestTool.class);
        if (a == null) {
            return false;
        }

        if (a.value() != BQTestScope.GLOBAL) {
            return false;
        }

        if (!ReflectionUtils.isStatic(callbackField)) {
            throw new JUnitException("@BQTestTool field '"
                    + callbackField.getDeclaringClass().getName() + "." + callbackField.getName()
                    + "' must be static to be used in GLOBAL scope");
        }

        return true;
    }

    protected void add(Field f, GlobalCallbacks globalCallbacks) {
        Callback callback = globalCallbacks.computeIfAbsent(f, this::callback);
        callback.addToRegistry(this);
    }

    protected Callback callback(Field field) {

        Object callback = resolveInstance(null, field);
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

        // don't bother with "afterAll" for the GLOBAL scope. We won't be able to tell whether class end
        // signifies the end of the scope.

        return callbackWrapper;
    }
}

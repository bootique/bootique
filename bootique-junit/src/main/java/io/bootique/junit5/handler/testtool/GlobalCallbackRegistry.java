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
package io.bootique.junit5.handler.testtool;

import io.bootique.junit5.BQTestScope;
import io.bootique.junit5.BQTestTool;
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
                .stream()
                .map(globalCallbacks::computeIfAbsent)
                .forEach(registry::add);

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

    protected void add(Callback callback) {

        if (callback.getBeforeAll() != null) {
            beforeAll = addToCallbacks(beforeAll, callback.getBeforeAll());
        }

        if (callback.getBeforeEach() != null) {
            beforeEach = addToCallbacks(beforeEach, callback.getBeforeEach());
        }

        if (callback.getAfterEach() != null) {
            afterEach = addToCallbacks(afterEach, callback.getAfterEach());
        }

        // don't bother with "afterAll". When it is called on a class, we don't know whether this is the last class
        // in the test scope or not...
    }
}

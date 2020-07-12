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
package io.bootique.junit5.handler.app;

import io.bootique.BQRuntime;
import io.bootique.junit5.BQApp;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * @since 2.0
 */
public class MethodAppRegistry extends BQAppRegistry {

    public static MethodAppRegistry create(ExtensionContext context) {

        MethodAppRegistry registry = new MethodAppRegistry();

        Class<?> testType = context.getRequiredTestClass();
        Predicate<Field> predicate = f -> registry.supportsApp(f);
        Object testInstance = context.getRequiredTestInstance();

        ReflectionUtils
                .findFields(testType, predicate, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .forEach(f -> registry.addRuntime(testInstance, f));

        return registry;
    }

    @Override
    public boolean supportsApp(Field appField) {
        BQApp a = appField.getAnnotation(BQApp.class);
        if (a == null) {
            return false;
        }

        if (!BQRuntime.class.isAssignableFrom(appField.getType())) {
            throw new JUnitException("Field '" + appField.getDeclaringClass().getName() + "." + appField.getName() +
                    "' is annotated with @BQApp but is not a BQRuntime.");
        }

        boolean isStatic = ReflectionUtils.isStatic(appField);
        switch (a.value()) {
            case TEST_METHOD:
                if (isStatic) {
                    throw new JUnitException("@BQApp field '"
                            + appField.getDeclaringClass().getName() + "." + appField.getName()
                            + "' must not be static to be used in TEST_METHOD scope");
                }
                return true;
            case IMPLIED:
                // instance fields with no explicit scope should be treated as TEST_METHOD
                return !isStatic;
            default:
                return false;
        }
    }
}

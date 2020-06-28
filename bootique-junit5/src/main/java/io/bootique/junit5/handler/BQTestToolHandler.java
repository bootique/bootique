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
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.function.Predicate;

/**
 * Manages custom lifecycles of objects annotated with @{@link BQTestTool}.
 *
 * @since 2.0
 */
public class BQTestToolHandler implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BQTestToolHandler.class);
    private static final String STATIC_CALLBACK_REGISTRY = "staticCallbackRegistry";
    private static final String INSTANCE_CALLBACK_REGISTRY = "instanceCallbackRegistry";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getOrCreateStaticCallbackRegistry(context).beforeAll(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        getOrCreateStaticCallbackRegistry(context).afterEach(context);
        getOrCreateInstanceCallbackRegistry(context).afterEach(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        getOrCreateStaticCallbackRegistry(context).beforeEach(context);
        getOrCreateInstanceCallbackRegistry(context).beforeEach(context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getOrCreateStaticCallbackRegistry(context).afterAll(context);
    }

    protected CallbackRegistry getOrCreateStaticCallbackRegistry(ExtensionContext context) {
        // using "root" context for the registry store
        return (CallbackRegistry) context
                .getRoot()
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(STATIC_CALLBACK_REGISTRY, s -> createCallbackRegistry(context, true));
    }

    protected CallbackRegistry getOrCreateInstanceCallbackRegistry(ExtensionContext context) {
        // using leaf context for the registry store
        return (CallbackRegistry) context
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(INSTANCE_CALLBACK_REGISTRY, s -> createCallbackRegistry(context, false));
    }

    protected CallbackRegistry createCallbackRegistry(ExtensionContext context, boolean staticTools) {

        CallbackRegistry registry = new CallbackRegistry();

        // TODO: global scope support via a static registry
        Class<?> testType = context.getRequiredTestClass();
        Object testInstance = staticTools ? null : context.getRequiredTestInstance();
        Predicate<Field> predicate = staticTools
                ? isIncluded().and(f -> ReflectionUtils.isStatic(f))
                : isIncluded().and(f -> !ReflectionUtils.isStatic(f));

        ReflectionUtils
                .findFields(testType, predicate, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .forEach(f -> addToRegistry(registry, testInstance, f));

        return registry;
    }

    protected Predicate<Field> isIncluded() {
        return f -> AnnotationSupport.isAnnotated(f, BQTestTool.class);
    }

    protected void addToRegistry(CallbackRegistry registry, Object testInstance, Field f) {
        Object instance = resolveInstance(testInstance, f);
        BQTestScope scope = resolveScope(f);
        registry.add(scope, instance);
    }

    protected Object resolveInstance(Object testInstance, Field f) {
        f.setAccessible(true);
        Object instance;
        try {
            instance = f.get(testInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error reading runtime field", e);
        }
        Preconditions.notNull(instance, () -> "Test tool instance '" + f.getName() + "' must be initialized explicitly");
        return instance;
    }

    protected BQTestScope resolveScope(Field f) {
        BQTestTool config = f.getAnnotation(BQTestTool.class);
        switch (config.value()) {
            case GLOBAL:
            case TEST_METHOD:
            case TEST_CLASS:
                return config.value();
            default:
                return ReflectionUtils.isStatic(f) ? BQTestScope.TEST_CLASS : BQTestScope.TEST_METHOD;
        }
    }
}

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

import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.extension.*;

/**
 * Manages custom lifecycles of objects annotated with @{@link BQTestTool}.
 *
 * @since 2.0
 */
public class BQTestToolHandler implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BQTestToolHandler.class);
    private static final String GLOBAL_CALLBACKS = "globalCallbacks";
    private static final String GLOBAL_CALLBACK_REGISTRY = "globalCallbackRegistry";
    private static final String CLASS_CALLBACK_REGISTRY = "classCallbackRegistry";
    private static final String METHOD_CALLBACK_REGISTRY = "methodCallbackRegistry";

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getOrCreateGlobalCallbackRegistry(context).beforeAll(context);
        getOrCreateClassCallbackRegistry(context).beforeAll(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        getOrCreateGlobalCallbackRegistry(context).beforeEach(context);
        getOrCreateClassCallbackRegistry(context).beforeEach(context);
        getOrCreateMethodCallbackRegistry(context).beforeEach(context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        getOrCreateGlobalCallbackRegistry(context).afterEach(context);
        getOrCreateClassCallbackRegistry(context).afterEach(context);
        getOrCreateMethodCallbackRegistry(context).afterEach(context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // don't bother invoking GLOBAL callbacks
        getOrCreateClassCallbackRegistry(context).afterAll(context);
    }

    protected CallbackRegistry getOrCreateGlobalCallbackRegistry(ExtensionContext context) {
        // storing GlobalCallbacks (callbacks by Field) in the root context to be shared between test classes
        // storing CallbackRegistry in the class context to make sure the right fields are invoked for a given test

        ExtensionContext classContext = getClassContext(context);
        ExtensionContext rootContext = classContext.getRoot();
        GlobalCallbacks globalCallbacks = (GlobalCallbacks) rootContext
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(GLOBAL_CALLBACKS, s -> new GlobalCallbacks(rootContext));

        return (CallbackRegistry) classContext
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(GLOBAL_CALLBACK_REGISTRY, s -> GlobalCallbackRegistry.create(context, globalCallbacks));
    }

    protected CallbackRegistry getOrCreateClassCallbackRegistry(ExtensionContext context) {
        // storing in ClassExtensionContext
        return (CallbackRegistry) getClassContext(context)
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(CLASS_CALLBACK_REGISTRY, s -> ClassCallbackRegistry.create(context));
    }

    protected CallbackRegistry getOrCreateMethodCallbackRegistry(ExtensionContext context) {
        // storing in leaf context whatever it is
        return (CallbackRegistry) context
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(METHOD_CALLBACK_REGISTRY, s -> MethodCallbackRegistry.create(context));
    }

    protected ExtensionContext getClassContext(ExtensionContext context) {
        if (context == null) {
            throw new RuntimeException("Can't find org.junit.jupiter.engine.descriptor.ClassExtensionContext in the context hierarchy");
        }

        // access non-public class
        if (context.getClass().getName().equals("org.junit.jupiter.engine.descriptor.ClassExtensionContext")) {
            return context;
        }

        return getClassContext(context.getParent().orElse(null));
    }
}

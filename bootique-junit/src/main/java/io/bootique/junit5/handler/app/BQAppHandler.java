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

import io.bootique.junit5.BQApp;
import io.bootique.junit5.handler.HandlerUtil;
import org.junit.jupiter.api.extension.*;

/**
 * Starts runtimes annotated with {@link BQApp}, shuts them down at the end of their declared or implied scope.
 *
 * @since 2.0
 */
public class BQAppHandler implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BQAppHandler.class);
    private static final String GLOBAL_REGISTRY = "globalRegistry";
    private static final String CLASS_REGISTRY = "classRegistry";
    private static final String METHOD_REGISTRY = "methodRegistry";

    @Override
    public void beforeAll(ExtensionContext context) {
        getOrCreateGlobalAppRegistry(context).beforeContext(context);
        getOrCreateClassAppRegistry(context).beforeContext(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        getOrCreateMethodAppRegistry(context).beforeContext(context);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        getOrCreateMethodAppRegistry(context).afterContext();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Only invoking per-class callbacks. Don't bother with GLOBAL callbacks. GLOBALs are shutdown elsewhere by
        // implementing ExtensionContext.Store.CloseableResource
        getOrCreateClassAppRegistry(context).afterContext();
    }

    protected BQAppRegistry getOrCreateGlobalAppRegistry(ExtensionContext context) {
        return (BQAppRegistry) context.getRoot()
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(GLOBAL_REGISTRY, s -> new GlobalAppRegistry());
    }

    protected BQAppRegistry getOrCreateClassAppRegistry(ExtensionContext context) {
        return (BQAppRegistry) HandlerUtil.getClassContext(context)
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(CLASS_REGISTRY, s -> new ClassAppRegistry());
    }

    protected BQAppRegistry getOrCreateMethodAppRegistry(ExtensionContext context) {
        return (BQAppRegistry) context
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(METHOD_REGISTRY, s -> new MethodAppRegistry());
    }
}

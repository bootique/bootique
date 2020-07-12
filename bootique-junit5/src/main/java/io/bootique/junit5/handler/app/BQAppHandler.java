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
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Starts runtimes annotated with {@link BQApp}, shuts them down at the end of their declared or implied scope.
 *
 * @since 2.0
 */
public class BQAppHandler implements BeforeAllCallback, BeforeEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BQAppHandler.class);
    private static final String GLOBAL_REGISTRY = "globalRegistry";
    private static final String CLASS_REGISTRY = "classRegistry";
    private static final String METHOD_REGISTRY = "methodRegistry";

    // "before" methods would init the registries, start the apps and store the registries in a configured scope.
    // JUnit stores will shut down the registries at the end of the scope, so no need for "after" methods

    @Override
    public void beforeAll(ExtensionContext context) {
        initGlobalAppRegistryIfNeeded(context);
        initClassAppRegistryIfNeeded(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        initMethodAppRegistryIfNeeded(context);
    }

    protected void initGlobalAppRegistryIfNeeded(ExtensionContext context) {
        context.getRoot()
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(GLOBAL_REGISTRY, s -> GlobalAppRegistry.create(context).start());
    }

    protected void initClassAppRegistryIfNeeded(ExtensionContext context) {
        HandlerUtil.getClassContext(context)
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(CLASS_REGISTRY, s -> ClassAppRegistry.create(context).start());
    }

    protected void initMethodAppRegistryIfNeeded(ExtensionContext context) {
        context
                .getStore(NAMESPACE)
                .getOrComputeIfAbsent(METHOD_REGISTRY, s -> MethodAppRegistry.create(context).start());
    }
}

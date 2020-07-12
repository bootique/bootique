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
import io.bootique.junit5.handler.HandlerUtil;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;

/**
 * @since 2.0
 */
public abstract class BQAppRegistry {

    private LinkedHashMap<Field, ManagedTestRuntime> runtimes;

    public BQAppRegistry() {
        this.runtimes = new LinkedHashMap<>();
    }

    protected ManagedTestRuntime createManagedRuntime(Object testInstance, Field field) {
        BQRuntime runtime = (BQRuntime) HandlerUtil.resolveInstance(testInstance, field);
        Preconditions.notNull(runtime, () -> "Runtime instance '" + field.getName() + "' must be initialized explicitly");
        return new ManagedTestRuntime(runtime, field.getName(), field.getAnnotation(BQApp.class));
    }

    public abstract void beforeContext(ExtensionContext context);

    public void afterContext() {
        runtimes.values().stream()
                // skip runtimes that were already shutdown
                .filter(r -> !r.immediateShutdown())
                .forEach(ManagedTestRuntime::shutdown);
    }

    protected void onAppFieldFound(Object testInstance, Field field) {
        runtimes.computeIfAbsent(field, f -> createManagedRuntime(testInstance, f).startIfNeeded());
    }
}

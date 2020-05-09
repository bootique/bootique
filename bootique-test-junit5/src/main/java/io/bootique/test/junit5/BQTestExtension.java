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
package io.bootique.test.junit5;

import io.bootique.BQRuntime;
import io.bootique.command.CommandOutcome;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Starts runtimes annotated with @{@link BQApp}. Runtimes must be static variables and are started once per test class
 * and are shut down when all tests in this class finish.
 *
 * @since 2.0
 */
public class BQTestExtension implements BeforeAllCallback, AfterAllCallback {

    // usijg JUnit logger
    private static final Logger logger = LoggerFactory.getLogger(BQTestExtension.class);

    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(BQTestExtension.class);

    private static final String RUNNING_SHARED_RUNTIMES = "runningSharedRuntimes";

    @Override
    public void beforeAll(ExtensionContext context) {
        ExtensionContext.Store store = context.getStore(NAMESPACE);
        Class<?> testType = context.getRequiredTestClass();
        ReflectionUtils
                .findFields(testType, isRunnable(), ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .stream()
                .map(f -> getInstance(null, f))
                .forEach(r -> startAndRegisterForShutdown(r, store));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        shutdown(context.getStore(NAMESPACE));
    }

    protected void shutdown(ExtensionContext.Store store) {
        List<TestRuntime> running = store.get(RUNNING_SHARED_RUNTIMES, List.class);
        if (running != null) {
            running.forEach(TestRuntime::shutdown);
        }
    }

    protected TestRuntime getInstance(Object testInstance, Field field) {

        field.setAccessible(true);
        BQRuntime runtime;
        try {
            runtime = (BQRuntime) field.get(testInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error reading runtime field", e);
        }

        Preconditions.notNull(runtime, () -> "Runtime instance '" + field.getName() + "' must be initialized explicitly");
        return new TestRuntime(runtime, field.getName(), field.getAnnotation(BQApp.class));
    }

    protected void startAndRegisterForShutdown(TestRuntime runtime, ExtensionContext.Store store) {

        if (!runtime.skipRun()) {
            CommandOutcome out = runtime.run();
            assertTrue(out.isSuccess(), () -> "Runtime '" + runtime.name + " failed to start: " + out);
        }

        if (runtime.immediateShutdown()) {
            // should we warn of quick shutdown of daemon apps? Or apps that were not run?
            runtime.shutdown();
        } else {
            store.getOrComputeIfAbsent(RUNNING_SHARED_RUNTIMES, s -> new ArrayList<>(), List.class).add(runtime);
        }
    }

    protected Predicate<Field> isRunnable() {
        return f -> {

            // provide diagnostics for misapplied or missing annotations
            // TODO: will it be actually more useful to throw instead of print a warning?
            if (AnnotationSupport.isAnnotated(f, BQApp.class)) {

                if (!BQRuntime.class.isAssignableFrom(f.getType())) {
                    logger.warn(() -> "Field '" + f.getName() + "' is annotated with @BQRun but is not a BQRuntime. Ignoring...");
                    return false;
                }

                if (!ReflectionUtils.isStatic(f)) {
                    logger.warn(() -> "BQRuntime field '" + f.getName() + "' is annotated with @BQRun but is not static. Ignoring...");
                    return false;
                }

                return true;
            }

            return false;
        };
    }

    static class TestRuntime {

        private BQRuntime runtime;
        private String name;
        private BQApp config;

        public TestRuntime(BQRuntime runtime, String name, BQApp config) {
            this.runtime = runtime;
            this.name = name;
            this.config = config;
        }

        public boolean skipRun() {
            return config.skipRun();
        }

        public boolean immediateShutdown() {
            return config.immediateShutdown();
        }

        public CommandOutcome run() {
            logger.debug(() -> "Starting Bootique runtime '" + name + "'...");
            return runtime.run();
        }

        public void shutdown() {
            logger.debug(() -> "Stopping Bootique runtime '" + name + "'...");
            try {
                runtime.shutdown();
            } catch (Exception e) {
                // ignore...
            }
        }
    }
}

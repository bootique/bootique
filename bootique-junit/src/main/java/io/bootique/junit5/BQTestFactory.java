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
package io.bootique.junit5;

import io.bootique.junit5.scope.BQAfterScopeCallback;
import io.bootique.junit5.scope.BQBeforeScopeCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Allows to create one or more Bootique runtimes within a JUnit test, performing automatic shutdown of all created
 * runtimes at the end of the factory scope. Can be declared as a static or an instance variable in a test class
 * and annotated with {@link BQTestTool} (and the containing test class must be annotated with {@link BQTest}). Shutdown
 * of the managed runtimes is down based on the explicit (as defined in the {@link BQTestTool} annotation), or implied
 * scope of the factory.
 *
 * <pre>
 * public class MyTest {
 *
 *   // shuts down all runtimes created by the factory after running all tests in MyClass
 *   &#64;BQTestTool
 *   public static BQTestFactory classScopeFactory = new BQTestFactory();
 *
 *   // shuts down all runtimes created by the factory after each test method
 *   &#64;BQTestTool
 *   public BQTestFactory methodScopeFactory = new BQTestFactory();
 * }
 * </pre>
 *
 * @since 2.0
 */
public class BQTestFactory implements BQBeforeScopeCallback, BQAfterScopeCallback {

    private final TestRuntimesManager runtimes;
    private boolean autoLoadModules;

    public BQTestFactory() {
        this.runtimes = new TestRuntimesManager();
    }

    protected TestRuntimesManager getRuntimes() {
        return runtimes;
    }

    /**
     * Sets the default policy for this factory to auto-load modules for each app.
     */
    public BQTestFactory autoLoadModules() {
        this.autoLoadModules = true;
        return this;
    }

    /**
     * @param args a String vararg emulating shell arguments passed to a real app.
     * @return a new instance of builder for the test runtime stack.
     */
    public TestRuntumeBuilder app(String... args) {
        TestRuntumeBuilder builder = new TestRuntumeBuilder(runtimes, args);

        if (autoLoadModules) {
            builder.autoLoadModules();
        }

        return builder;
    }

    @Override
    public void beforeScope(BQTestScope scope, ExtensionContext context) {
        runtimes.reset();
    }

    @Override
    public void afterScope(BQTestScope scope, ExtensionContext context) {
        runtimes.shutdown();
    }
}

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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * A JUnit 5 extension that allows to create one or more Bootique runtimes within a JUnit test, performing automatic
 * shutdown of all created runtimes after the end of each test method. You would normally add this extension to the
 * test programmatically, either as a static or an instance variable annotated with
 * {@link org.junit.jupiter.api.extension.RegisterExtension}. E.g.:
 *
 * <pre>
 * public class MyTest {
 *
 *   &#64;RegisterExtension
 *   public static BQTestFactory testFactory = new BQTestFactory();
 * }
 * </pre>
 *
 * @see BQTestClassFactory
 * @since 2.0
 */
public class BQTestFactory implements BeforeEachCallback, AfterEachCallback {

    private TestRuntimesManager runtimes;
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
    public void beforeEach(ExtensionContext extensionContext) {
        runtimes.reset();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        runtimes.shutdown();
    }
}

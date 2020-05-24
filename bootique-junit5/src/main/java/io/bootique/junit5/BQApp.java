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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate static {@link io.bootique.BQRuntime} instances in a unit test to instruct JUnit to start these
 * runtimes, and shut them down when all the tests in the class are finished.
 *
 * @since 2.0
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BQApp {

    /**
     * If true, skips starting up the app. {@link io.bootique.BQRuntime} will still be shutdown at the end of
     * the test cycle to clean up any resources.
     */
    boolean skipRun() default false;

    /**
     * If true, shuts down the app {@link io.bootique.BQRuntime} immediately after the command exit. Usually
     * appropriate for non-"daemon" apps
     */
    boolean immediateShutdown() default false;
}

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

package io.bootique.test.junit;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import org.junit.Rule;
import org.junit.Test;

public class BQRuntimeCheckerTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    @Test
    public void testTestModulesLoaded() {
        final BQRuntime runtime = testFactory.app().createRuntime();
        BQRuntimeChecker.testModulesLoaded(runtime, BQCoreModule.class);
    }

    @Test(expected = AssertionError.class)
    public void testTestModulesNotLoaded() {
        final BQRuntime runtime = testFactory.app().createRuntime();
        BQRuntimeChecker.testModulesLoaded(runtime, NonLoadedModule.class);
    }

    static class NonLoadedModule implements Module {

        @Override
        public void configure(Binder binder) {
        }
    }
}

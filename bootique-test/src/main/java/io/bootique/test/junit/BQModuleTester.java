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

import io.bootique.BQModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A testing utility class for verifying auto-loading configuration, and other module aspects.
 *
 * @since 3.0
 * @deprecated as we are we phasing out JUnit 4 support in favor of JUnit 5.
 */
@Deprecated(since = "3.0", forRemoval = true)
public class BQModuleTester {

    private final Class<? extends BQModule> moduleType;

    /**
     * Creates a tester for the provided module type.
     */
    public static BQModuleTester of(Class<? extends BQModule> moduleType) {
        return new BQModuleTester(moduleType);
    }

    protected BQModuleTester(Class<? extends BQModule> moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * Verifies that the module type argument is auto-loadable in a Bootique app.
     */
    public BQModuleTester testAutoLoadable() {
        int c = autoLoadCount();
        switch (c) {
            case 0:
                fail("Module of type '" + moduleType.getName() + "' is not auto-loadable. To make it auto-loadable, add " +
                        "it to 'META-INF/services/io.bootique.BQModule' file on classpath");
                break;
            case 1:
                break;
            default:
                fail("Auto-loadable module of type '" + moduleType.getName() + "' is found more then once: " + c);
                break;
        }

        return this;
    }

    /**
     * Checks that declared configuration types for the Module don't have issues with annotations and can be loaded
     * without errors. Does not verify the actual configuration contents.
     */
    public BQModuleTester testConfig() {

        boolean autoLoadable = autoLoadCount() > 0;

        BQRuntime runtime;

        try {
            runtime = autoLoadable
                    ? Bootique.app().autoLoadModules().createRuntime()
                    : Bootique.app().module(moduleType).createRuntime();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Auto-loading test runtime creation failed");
            return this;
        }

        try {
            // loading metadata ensures that all BQ (and Jackson?) annotations are processed
            Optional<ModuleMetadata> moduleMetadata = runtime
                    .getInstance(ModulesMetadata.class)
                    .getModules()
                    .stream()
                    .filter(mmd -> moduleType == mmd.getType())
                    .findFirst();

            assertTrue("No metadata available for Module: '" + moduleType.getName() + "'", moduleMetadata.isPresent());
            moduleMetadata.get().getConfigs();

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not resolve metadata");
        } finally {
            runtime.shutdown();
        }

        return this;
    }

    private int autoLoadCount() {
        return (int) ServiceLoader.load(BQModule.class).stream().filter(p -> moduleType == p.type()).count();
    }
}

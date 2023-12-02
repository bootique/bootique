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

import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.meta.module.ModuleMetadata;
import io.bootique.meta.module.ModulesMetadata;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.counting;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A helper class to for writing generic tests for {@link BQModuleProvider} implementors. It allows to verify that the
 * provider and related classes are wired properly, there are no typos in service descriptors, etc. Same usage:
 * <pre>
 * &#64;Test
 * public void testAutoLoadable() {
 * 	   BQModuleProviderChecker.testAutoLoadable(MyModuleProvider.class);
 * }
 * </pre>
 *
 * @deprecated as we are we phasing out JUnit 4 support in favor of JUnit 5. Also, BQModuleProvider itself is deprecated.
 */
@Deprecated(since = "3.0", forRemoval = true)
public class BQModuleProviderChecker {

    private Class<? extends BQModuleProvider> provider;

    protected BQModuleProviderChecker(Class<? extends BQModuleProvider> provider) {
        this.provider = Objects.requireNonNull(provider);
    }

    /**
     * Verifies that the passed provider type is auto-loadable in a Bootique app.
     *
     * @param provider provider type being testing.
     */
    public static void testAutoLoadable(Class<? extends BQModuleProvider> provider) {
        new BQModuleProviderChecker(provider).testAutoLoadable();
    }

    /**
     * Checks that config metadata for the Module created by the tested provider can be loaded without errors. Does not
     * verify the actual metadata contents.
     *
     * @param provider provider type that we are testing.
     */
    public static void testMetadata(Class<? extends BQModuleProvider> provider) {
        new BQModuleProviderChecker(provider).testMetadata();
    }

    protected Stream<BQModuleProvider> matchingProviders() {
        return StreamSupport.stream(ServiceLoader.load(BQModuleProvider.class).spliterator(), false)
                .filter(p -> p != null && provider.equals(p.getClass()));
    }

    protected BQModuleProvider matchingProvider() {
        return matchingProviders().findFirst().get();
    }
    
    protected void testAutoLoadable() {

        Long c = matchingProviders().collect(counting());

        switch (c.intValue()) {
            case 0:
                fail("Expected provider '" + provider.getName() + "' is not found");
                break;
            case 1:
                break;
            default:
                fail("Expected provider '" + provider.getName() + "' is found more then once: " + c);
                break;
        }
    }

    protected void testMetadata() {

        testWithFactory(testFactory -> {
            // must auto-load modules to ensure all tested module dependencies are present...
            BQRuntime runtime = testFactory.app().autoLoadModules().createRuntime();
            String providerName = matchingProvider().name();

            // loading metadata ensures that all annotations are properly applied...
            Optional<ModuleMetadata> moduleMetadata = runtime
                    .getInstance(ModulesMetadata.class)
                    .getModules()
                    .stream()
                    .filter(mmd -> providerName.equals(mmd.getProviderName()))
                    .findFirst();

            assertTrue("No module metadata available for provider: '" + providerName + "'", moduleMetadata.isPresent());
            moduleMetadata.get().getConfigs();
        });
    }

    protected void testWithFactory(Consumer<BQTestFactory> test) {
        BQTestFactory testFactory = new BQTestFactory();
        try {
            testFactory.before();
            test.accept(testFactory);
        } finally {
            testFactory.after();
        }
    }
}

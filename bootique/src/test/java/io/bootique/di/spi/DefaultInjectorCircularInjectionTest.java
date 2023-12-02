
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

package io.bootique.di.spi;

import io.bootique.BQModule;
import io.bootique.di.DIBootstrap;
import io.bootique.di.DIRuntimeException;
import io.bootique.di.Injector;
import io.bootique.di.mock.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class DefaultInjectorCircularInjectionTest {

    @Test
    public void fieldInjection_CircularDependency() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1_DepOn2.class);
            binder.bind(MockInterface2.class).to(MockImplementation2.class);
        };

        Injector injector = DIBootstrap.injectorBuilder(module).disableProxyCreation().build();

        try {
            injector.getInstance(MockInterface1.class);
            fail("Circular dependency is not detected.");
        }
        catch (DIRuntimeException e) {
            // expected
        }
        catch (StackOverflowError e) {
            fail("Circular dependency is not detected, causing stack overflow");
        }
    }

    @Test
    public void providerInjection_CircularDependency() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(
                    MockImplementation1_DepOn2Provider.class);
            binder.bind(MockInterface2.class).to(MockImplementation2.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertEquals("MockImplementation2Name", service.getName());
    }

    @Test
    public void constructorInjection_CircularDependency() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(
                    MockImplementation1_DepOn2Constructor.class);
            binder.bind(MockInterface2.class).to(
                    MockImplementation2_Constructor.class);
        };

        Injector injector = DIBootstrap.injectorBuilder(module).disableProxyCreation().build();

        try {
            injector.getInstance(MockInterface1.class);
            fail("Circular dependency is not detected.");
        }
        catch (DIRuntimeException e) {
            // expected
        }
        catch (StackOverflowError e) {
            fail("Circular dependency is not detected, causing stack overflow");
        }
    }

    @Test
    public void constructorInjection_WithFieldInjectionDeps() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(
                    MockImplementation1_DepOn2Constructor.class);
            binder.bind(MockInterface2.class).to(
                    MockImplementation2_I3Dependency.class);
            binder.bind(MockInterface3.class).to(MockImplementation3.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        try {
            injector.getInstance(MockInterface1.class);
        }
        catch (DIRuntimeException e) {
            fail("Circular dependency is detected incorrectly: " + e.getMessage());
        }
    }
}

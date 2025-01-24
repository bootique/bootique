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
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.mock.MockImplementation1;
import io.bootique.di.mock.MockImplementation1Alt;
import io.bootique.di.mock.MockImplementation1Alt2;
import io.bootique.di.mock.MockInterface1;
import io.bootique.di.mock.MockInterface1Provider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultInjectorBindingTest {

    @Test
    public void classBinding() {

        BQModule module = binder -> binder.bind(MockInterface1.class).to(MockImplementation1.class);

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("MyName", service.getName());
    }

    @Test
    public void classNamedBinding() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(Key.get(MockInterface1.class, "abc")).to(
                    MockImplementation1Alt.class);
            binder.bind(Key.get(MockInterface1.class, "xyz")).to(
                    MockImplementation1Alt2.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 defaultObject = injector.getInstance(MockInterface1.class);
        assertNotNull(defaultObject);
        assertEquals("MyName", defaultObject.getName());

        MockInterface1 abcObject = injector.getInstance(Key.get(
                MockInterface1.class,
                "abc"));
        assertNotNull(abcObject);
        assertEquals("alt", abcObject.getName());

        MockInterface1 xyzObject = injector.getInstance(Key.get(
                MockInterface1.class,
                "xyz"));
        assertNotNull(xyzObject);
        assertEquals("alt2", xyzObject.getName());
    }

    @Test
    public void providerBinding() {
        BQModule module = binder -> binder
                .bind(MockInterface1.class)
                .toProvider(MockInterface1Provider.class);

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("MyName", service.getName());
    }

    @Test
    public void instanceBinding() {

        final MockImplementation1 instance = new MockImplementation1();

        BQModule module = binder -> binder.bind(MockInterface1.class).toInstance(instance);

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertSame(instance, service);
    }

    @Test
    public void keyBindingChain() {
        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(Key.get(MockImplementation1.class));
            binder.bind(MockImplementation1.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("MyName", service.getName());
    }

    @Test
    public void keyBindingSimple() {
        BQModule module = binder
                -> binder.bind(MockInterface1.class).to(Key.get(MockImplementation1.class));

        Injector injector = DIBootstrap.injectorBuilder(module).build();

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("MyName", service.getName());
    }

    @Test
    public void classReBinding() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.bind(MockInterface1.class).to(MockImplementation1Alt.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("alt", service.getName());
    }

    @Test
    public void directImplementationBinding() {
        BQModule module = binder -> {
            binder.bind(Implementation1.class).withoutScope();
            binder.bind(Implementation2.class).inSingletonScope();
        };
        Injector injector = DIBootstrap.injectorBuilder(module).build();

        Implementation1 impl1 = injector.getInstance(Implementation1.class);
        assertNotNull(impl1);
        assertNotNull(impl1.implementation2);

        Implementation1 impl2 = injector.getInstance(Implementation1.class);
        assertNotNull(impl2);
        assertNotNull(impl2.implementation2);
        assertNotSame(impl1, impl2);
        assertSame(impl1.implementation2, impl2.implementation2);

    }

    public static class Implementation1 {
        @Inject
        Implementation2 implementation2;
    }

    public static class Implementation2 {
    }

}

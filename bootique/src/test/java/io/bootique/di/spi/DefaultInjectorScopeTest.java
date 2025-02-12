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
import io.bootique.di.BeforeScopeEnd;
import io.bootique.di.DIBootstrap;
import io.bootique.di.Injector;
import io.bootique.di.mock.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultInjectorScopeTest {

    @Test
    public void noScope_ImplicitService() {
        Injector injector = DIBootstrap
                .injectorBuilder(b -> b.bind(MockInterface1.class).to(MockImplementation1.class).inSingletonScope())
                .build();

        MockImplementation2 undeclared1 = injector.getInstance(MockImplementation2.class);
        MockImplementation2 undeclared2 = injector.getInstance(MockImplementation2.class);

        assertNotSame(undeclared1, undeclared2, "Implicit service creation must follow Injector default scope rules");
        assertSame(undeclared1.getService(), undeclared2.getService(), "Injection in the implicit service must follow the scope rules");
    }

    @Test
    public void defaultSingletonScope_ImplicitService() {
        Injector injector = DIBootstrap
                .injectorBuilder(b -> b.bind(MockInterface1.class).to(MockImplementation1.class).inSingletonScope())
                .defaultSingletonScope()
                .build();

        MockImplementation2 undeclared1 = injector.getInstance(MockImplementation2.class);
        MockImplementation2 undeclared2 = injector.getInstance(MockImplementation2.class);

        assertSame(undeclared1, undeclared2, "Implicit service creation must follow Injector default scope rules");
        assertSame(undeclared1.getService(), undeclared2.getService(), "Injection in the implicit service must follow the scope rules");
    }

    @Test
    public void defaultScope_IsSingleton() {

        BQModule module = binder -> binder.bind(MockInterface1.class).to(MockImplementation1.class);

        Injector injector = DIBootstrap.injectorBuilder(module).defaultSingletonScope().build();

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance2 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance3 = injector.getInstance(MockInterface1.class);

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);

        assertSame(instance1, instance2);
        assertSame(instance2, instance3);
    }

    @Test
    public void noScope() {

        BQModule module = binder -> binder
                .bind(MockInterface1.class)
                .to(MockImplementation1.class)
                .withoutScope();

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance2 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance3 = injector.getInstance(MockInterface1.class);

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);

        assertNotSame(instance1, instance2);
        assertNotSame(instance2, instance3);
        assertNotSame(instance3, instance1);
    }

    @Test
    public void singletonScope() {

        BQModule module = binder -> binder
                .bind(MockInterface1.class)
                .to(MockImplementation1.class)
                .inSingletonScope();

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance2 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance3 = injector.getInstance(MockInterface1.class);

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);

        assertSame(instance1, instance2);
        assertSame(instance2, instance3);
    }

    @Test
    public void singletonScope_AnnotatedEvents() {

        MockImplementation1_EventAnnotations.reset();

        BQModule module = binder -> binder.bind(MockInterface1.class).to(
                MockImplementation1_EventAnnotations.class).inSingletonScope();

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        assertEquals("XuI", instance1.getName());

        assertFalse(MockImplementation1_EventAnnotations.shutdown1);
        assertFalse(MockImplementation1_EventAnnotations.shutdown2);
        assertFalse(MockImplementation1_EventAnnotations.shutdown3);

        injector.getSingletonScope().postScopeEvent(BeforeScopeEnd.class);

        assertTrue(MockImplementation1_EventAnnotations.shutdown1);
        assertTrue(MockImplementation1_EventAnnotations.shutdown2);
        assertTrue(MockImplementation1_EventAnnotations.shutdown3);
    }

    @Test
    public void singletonScope_WithProvider() {

        BQModule module = binder -> binder
                .bind(MockInterface1.class)
                .toJakartaProvider(MockImplementation1_Provider.class)
                .inSingletonScope();

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance2 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance3 = injector.getInstance(MockInterface1.class);

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);

        assertSame(instance1, instance2);
        assertSame(instance2, instance3);
    }

    @Test
    public void noScope_WithProvider() {

        BQModule module = binder -> binder
                .bind(MockInterface1.class)
                .toJakartaProvider(MockImplementation1_Provider.class).withoutScope();

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance2 = injector.getInstance(MockInterface1.class);
        MockInterface1 instance3 = injector.getInstance(MockInterface1.class);

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertNotNull(instance3);

        assertNotSame(instance1, instance2);
        assertNotSame(instance2, instance3);
    }
}

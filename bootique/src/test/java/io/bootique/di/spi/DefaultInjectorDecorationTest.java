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
import io.bootique.di.mock.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefaultInjectorDecorationTest {

    @Test
    public void singleDecorator_After() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.decorate(MockInterface1.class).after(MockInterface1_Decorator1.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("[MyName]", service.getName());
    }

    @Test
    public void singleDecorator_Before() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.decorate(MockInterface1.class).before(MockInterface1_Decorator1.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("[MyName]", service.getName());
    }

    @Test
    public void decoratorChain() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.decorate(MockInterface1.class).before(MockInterface1_Decorator1.class);
            binder.decorate(MockInterface1.class).before(MockInterface1_Decorator2.class);
            binder.decorate(MockInterface1.class).after(MockInterface1_Decorator3.class);

        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("<[{MyName}]>", service.getName());
    }

    @Test
    public void singleDecorator_Provider_ConstructorInjection() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.decorate(MockInterface1.class).before(MockInterface1_Decorator4.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("[4MyName4]", service.getName());
    }

    @Test
    public void singleDecorator_Provider_FieldInjection() {

        BQModule module = binder -> {
            binder.bind(MockInterface1.class).to(MockImplementation1.class);
            binder.decorate(MockInterface1.class).before(MockInterface1_Decorator5.class);
        };

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 service = injector.getInstance(MockInterface1.class);
        assertNotNull(service);
        assertEquals("[5MyName5]", service.getName());
    }
}

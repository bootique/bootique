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

package io.bootique.di;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InjectorOptionsIT {

    @Test
    public void dynamicBindingDisabled() {
        Injector injector = DIBootstrap.injectorBuilder(b -> b.bind(Service.class).to(Service_Impl1.class))
                .disableDynamicBindings()
                .build();

        // no binding of consumer, should throw
        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Consumer1.class));
    }

    @Test
    public void dynamicBindingEnabled() {
        Injector injector = DIBootstrap.injectorBuilder(b -> b.bind(Service.class).to(Service_Impl1.class))
                .build();

        // no binding of consumer, but dynamic is allowed
        Consumer1 consumer = injector.getInstance(Consumer1.class);
        assertInstanceOf(Service_Impl1.class, consumer.service);
    }

    @Test
    public void singletonScope() {
        Injector injector = DIBootstrap.injectorBuilder(b -> b.bind(Service.class).to(Service_Impl1.class))
                .defaultSingletonScope()
                .build();

        // no binding of consumer, but dynamic is allowed
        Service service1 = injector.getInstance(Service.class);
        assertInstanceOf(Service_Impl1.class, service1);

        Service service2 = injector.getInstance(Service.class);
        assertInstanceOf(Service_Impl1.class, service2);
        assertSame(service1, service2);
    }

    @Test
    public void noScopeScope() {
        Injector injector = DIBootstrap.injectorBuilder(b -> b.bind(Service.class).to(Service_Impl1.class))
                .build();

        // no binding of consumer, but dynamic is allowed
        Service service1 = injector.getInstance(Service.class);
        assertInstanceOf(Service_Impl1.class, service1);

        Service service2 = injector.getInstance(Service.class);
        assertInstanceOf(Service_Impl1.class, service2);
        assertNotSame(service1, service2);
    }

    interface Service {
        String doIt();
    }

    static class Service_Impl1 implements Service {
        @Override
        public String doIt() {
            return "impl1";
        }
    }

    static class Consumer1 {
        @Inject
        Service service;
    }
}

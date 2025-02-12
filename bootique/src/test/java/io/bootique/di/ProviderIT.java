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
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProviderIT {

    @Test
    void providerImplementationFieldInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ProviderWithFieldInjection.class);
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("field s2", instance.doIt());
    }

    @Test
    void providerInterfaceFieldInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ServiceProvider.class);
                    binder.bind(ServiceProvider.class).to(ProviderWithFieldInjection.class);
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("field s2", instance.doIt());
    }

    @Test
    void providerImplementationConstructorInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ProviderWithConstructorInjection.class);
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("constructor s2", instance.doIt());
    }

    @Test
    void providerInterfaceConstructorInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ServiceProvider.class);
                    binder.bind(ServiceProvider.class).to(ProviderWithConstructorInjection.class);
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("constructor s2", instance.doIt());
    }

    @Test
    void providerImplementationInstanceFieldInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProviderInstance(new ProviderWithFieldInjection());
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("field s2", instance.doIt());
    }

    @Test
    void providerInterfaceInstanceFieldInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ServiceProvider.class);
                    binder.bind(ServiceProvider.class).toInstance(new ProviderWithFieldInjection());
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("field s2", instance.doIt());
    }

    @Test
    void providerImplementationProvidedServiceFieldInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ProviderProvidedObjectFieldInjection.class);
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("s2 s3", instance.doIt());
    }

    @Test
    void providerImplementationInstanceProvidedServiceFieldInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProviderInstance(new ProviderProvidedObjectFieldInjection());
                    binder.bind(Service2.class).toInstance(() -> "s2");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("s2 s3", instance.doIt());
    }

    @Test
    void providerImplementationFullInjection() {
        Injector injector = DIBootstrap.createInjector(
                binder -> {
                    binder.bind(Service.class).toJakartaProvider(ProviderFullInjection.class);
                    binder.bind(Service2.class).toInstance(() -> "s2");
                    binder.bind(String.class).toInstance("str");
                }
        );

        Service instance = injector.getInstance(Service.class);
        assertEquals("s2 s3 str s2 s4", instance.doIt());
    }

    interface Service {
        String doIt();
    }

    interface Service2 extends Service {
    }

    interface ServiceProvider extends Provider<Service> {
    }

    static class ProviderWithFieldInjection implements ServiceProvider {

        @Inject
        Service2 service2;

        @Override
        public Service get() {
            return () -> "field " + service2.doIt();
        }
    }

    static class ProviderWithConstructorInjection implements ServiceProvider {

        private final Service2 service2;

        @Inject
        public ProviderWithConstructorInjection(Service2 service2) {
            this.service2 = service2;
        }

        @Override
        public Service get() {
            return () -> "constructor " + service2.doIt();
        }
    }

    static class Service3 implements Service {
        @Inject
        Service2 service2;

        @Override
        public String doIt() {
            return service2.doIt() + " s3";
        }
    }

    static class ProviderProvidedObjectFieldInjection implements ServiceProvider {
        @Override
        public Service get() {
            return new Service3();
        }
    }

    static class Service4 implements Service {
        @Inject
        Service3 service3;

        private final String s;

        Service4(String s) {
            this.s = s;
        }

        @Override
        public String doIt() {
            return service3.doIt() + " " + s + " s4";
        }
    }

    static class ProviderFullInjection implements ServiceProvider {

        private final String s;

        @Inject
        private Service2 service2;

        @Inject
        public ProviderFullInjection(String s) {
            this.s = s;
        }

        @Override
        public Service get() {
            return new Service4(s + " " + service2.doIt());
        }
    }
}

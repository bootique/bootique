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

import org.junit.jupiter.api.Test;
import io.bootique.BQModule;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QualifiersIT {

    @Test
    public void qualifiedInject() {
        Injector injector = DIBootstrap.createInjector(b -> {
            b.bind(Key.get(Service.class, CustomQualifier.class)).to(Service_Impl1.class);
            b.bind(Key.get(Service.class)).to(Service_Impl2.class);

            // Direct field injection
            b.bind(Consumer.class).to(Consumer_Impl1.class);
            b.bind(Key.get(Consumer.class, CustomQualifier.class)).to(Consumer_Impl2.class);
        });
        checkInjectionResult(injector);
    }

    @Test
    public void qualifiedProvider() {
        Injector injector = DIBootstrap.createInjector(b -> {
            // Direct field injection
            b.bind(Consumer.class).to(Consumer_Impl1.class);
            b.bind(Key.get(Consumer.class, CustomQualifier.class)).to(Consumer_Impl2.class);
        }, new ServiceModule2()); // module with @Provides annotated methods, can't use anonymous class
        checkInjectionResult(injector);
    }

    @Test
    public void qualifiedConstructorParameter() {
        Injector injector = DIBootstrap.createInjector(b -> {
            b.bind(Key.get(Service.class, CustomQualifier.class)).to(Service_Impl1.class);
            b.bind(Key.get(Service.class)).to(Service_Impl2.class);

            // Constructor injection
            b.bind(Consumer.class).to(Consumer_Impl3.class);
            b.bind(Key.get(Consumer.class, CustomQualifier.class)).to(Consumer_Impl4.class);
        });
        checkInjectionResult(injector);
    }

    @Test
    public void multipleQualifiers() {
        Injector injector = DIBootstrap.createInjector(b -> {
            b.bind(Key.get(Service.class, CustomQualifier.class)).to(Service_Impl1.class);
            b.bind(Key.get(Service.class)).to(Service_Impl2.class);

            // Constructor injection
            b.bind(Consumer.class).to(Consumer_Impl5.class);
        });

        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Consumer.class));
    }

    private void checkInjectionResult(Injector injector) {
        Consumer consumer1 = injector.getInstance(Consumer.class);
        assertInstanceOf(Service_Impl1.class, consumer1.getService());

        Consumer consumer2 = injector.getInstance(Key.get(Consumer.class, CustomQualifier.class));
        assertInstanceOf(Service_Impl2.class, consumer2.getService());
    }

    public static class ServiceModule2 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @CustomQualifier
        public Service createService() {
            return new Service_Impl1();
        }

        @Provides
        public Service createService2() {
            return new Service_Impl2();
        }
    }

    interface Service {}
    interface Consumer {
        Service getService();
    }

    private static class Service_Impl1 implements Service {
    }

    private static class Service_Impl2 implements Service {
    }

    private static class Consumer_Impl1 implements Consumer {
        @Inject
        @CustomQualifier
        private Service service;

        @Override
        public Service getService() {
            return service;
        }
    }

    private static class Consumer_Impl2 implements Consumer {
        @Inject
        private Service service;

        @Override
        public Service getService() {
            return service;
        }
    }

    private static class Consumer_Impl3 implements Consumer {
        private Service service;

        @Inject
        public Consumer_Impl3(@CustomQualifier Service service) {
            this.service = service;
        }

        @Override
        public Service getService() {
            return service;
        }
    }

    private static class Consumer_Impl4 implements Consumer {
        private Service service;

        @Inject
        public Consumer_Impl4(Service service) {
            this.service = service;
        }

        @Override
        public Service getService() {
            return service;
        }
    }

    private static class Consumer_Impl5 implements Consumer {

        @Inject
        @CustomQualifier
        @CustomQualifier2
        private Service service;

        @Override
        public Service getService() {
            return service;
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomQualifier {
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomQualifier2 {
    }
}

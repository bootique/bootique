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

import io.bootique.BQModule;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CircularDependencyIT {

    @Test
    public void proxyCreation() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Service1.class).to(Service1Impl1.class).inSingletonScope();
            binder.bind(Service2.class).to(Service2Impl1.class).inSingletonScope();
        }).build();

        Service2 service2 = injector.getInstance(Service2.class);
        assertEquals("service1 + service2.2.2", service2.exec());
    }

    @Test
    public void proxyCreationFailure() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Service3.class).inSingletonScope();
            binder.bind(Service4.class).inSingletonScope();
        }).build();

        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Service3.class));
    }

    @Test
    public void proxyCreationProviderMethodFailure() {
        Injector injector = DIBootstrap.injectorBuilder(new CircularModule()).build();

        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Service1.class));
    }

    static class CircularModule implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        Service1 createService1(Service2 service2) {
            service2.exec();
            return new Service1Impl1();
        }

        @Provides
        Service2 createService2(Service1 service1) {
            service1.info();
            return new Service2Impl1();
        }
    }

    interface Service1 {
        String info();

        String wrap(String arg);
    }

    interface Service2 extends Service1 {
        String exec();
    }

    static class Service1Impl1 implements Service1 {

        @Inject
        Service2 service2;

        @Override
        public String info() {
            return "service1 + " + service2.wrap(service2.info());
        }

        @Override
        public String wrap(String arg) {
            return arg + ".1";
        }
    }

    static class Service2Impl1 implements Service2 {

        @Inject
        Service1 service1;

        @Override
        public String exec() {
            return service1.info();
        }

        @Override
        public String info() {
            return wrap("service2");
        }

        @Override
        public String wrap(String arg) {
            return arg + ".2";
        }
    }

    static class Service3 {
        @Inject
        Service4 service4;
    }

    static class Service4 {
        @Inject
        Service3 service3;
    }
}

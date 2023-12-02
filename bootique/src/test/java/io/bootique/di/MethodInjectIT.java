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

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MethodInjectIT {

    @Test
    public void methodInjectDisabledByDefault() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Service.class).to(Service_Impl1.class);
            binder.bind(Consumer_Impl.class);
        }).build();

        Consumer_Impl consumer = injector.getInstance(Consumer_Impl.class);
        assertFalse(consumer.serviceSet);
    }

    @Test
    public void methodInject() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Service.class).to(Service_Impl1.class);
            binder.bind(Consumer_Impl.class);
        }).enableMethodInjection().build();

        Consumer_Impl consumer = injector.getInstance(Consumer_Impl.class);
        assertTrue(consumer.serviceSet);
    }

    @Test
    public void injectMembers() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Service.class).to(Service_Impl1.class);
            binder.bind(Consumer_Impl.class);
        }).enableMethodInjection().build();

        Consumer_Impl consumer = new Consumer_Impl();
        injector.injectMembers(consumer);
        assertTrue(consumer.serviceSet, "Method not injected");
    }

    @Test
    public void injectMethod_QualifiedParameter() {
        Injector injector = DIBootstrap.injectorBuilder(binder -> {
            binder.bind(Key.get(Service.class, CustomQualifier.class)).to(Service_Impl1.class);
            binder.bind(Consumer_Impl2.class);
        }).enableMethodInjection().build();

        Consumer_Impl2 consumer = injector.getInstance(Consumer_Impl2.class);
        assertTrue(consumer.serviceSet);
    }


    interface Service {
    }

    static class Service_Impl1 implements Service {
    }

    static class Consumer_Impl {

        private boolean serviceSet;

        @Inject
        void setService(Service service) {
            Objects.requireNonNull(service);
            serviceSet = true;
        }
    }

    static class Consumer_Impl2 {

        private boolean serviceSet;

        @Inject
        void setService(@CustomQualifier Service service) {
            Objects.requireNonNull(service);
            serviceSet = true;
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomQualifier {
    }


}

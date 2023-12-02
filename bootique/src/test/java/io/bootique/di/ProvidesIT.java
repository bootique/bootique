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
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProvidesIT {

    @Test
    public void provides_Standalone_Static() {
        Injector injector = DIBootstrap.createInjector(new TestModule_StandaloneService_Static());

        Service1 s1 = injector.getInstance(Service1.class);
        assertEquals("provideService1", s1.doIt());
    }

    @Test
    public void provides_Standalone_Instance() {
        Injector injector = DIBootstrap.createInjector(new TestModule_StandaloneService_Instance());

        Service1 s1 = injector.getInstance(Service1.class);
        assertEquals("provideService1", s1.doIt());
    }

    @Test
    public void provides_Standalone_AnonymousClass() {
        Injector injector = DIBootstrap.createInjector(new BQModule() {

            @Override
            public void configure(Binder binder) {
            }

            @Provides
            Service1 createService() {
                return () -> "provideService1";
            }
        });

        Service1 s1 = injector.getInstance(Service1.class);
        assertEquals("provideService1", s1.doIt());
    }

    @Test
    public void provides_Standalone_Named() {
        Injector injector = DIBootstrap.createInjector(new TestModule_NamedService());

        Service1 s1 = injector.getInstance(Key.get(Service1.class, "s1"));
        assertEquals("provideService1", s1.doIt());
    }

    @Test
    public void provides_Chain() {
        Injector injector = DIBootstrap.createInjector(new TestModule_ServiceChain());

        Service2 s2 = injector.getInstance(Service2.class);
        assertEquals("provideService2_provideService1", s2.doIt());
    }

    @Test
    public void provides_Chain_NamedParameter() {
        Injector injector = DIBootstrap.createInjector(new TestModule_NamedParameter());

        Service2 s2 = injector.getInstance(Service2.class);
        assertEquals("provideService2_provideService1", s2.doIt());
    }

    @Test
    public void provides_Chain_ProviderParameter() {
        Injector injector = DIBootstrap.createInjector(new TestModule_ServiceChain_ProviderParameter());

        Service2 s2 = injector.getInstance(Service2.class);
        assertEquals("provideService2_provideService1", s2.doIt());
    }

    @Test
    public void provides_Chain_QualifiedProviderParameter() {
        Injector injector = DIBootstrap.createInjector(new TestModule_QualifiedProviderParameter());

        Service2 s2 = injector.getInstance(Service2.class);
        assertEquals("provideService2_provideService1", s2.doIt());
    }

    @Test
    public void providesCycle() {
        Injector injector = DIBootstrap.injectorBuilder(new TestModule_CircularDependency())
                .disableProxyCreation().build();
        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Service1.class));
    }

    @Test
    public void provides_Invalid() {
        assertThrows(DIRuntimeException.class, () -> DIBootstrap.createInjector(new TestModule_InvalidProvider()));
    }

    @Test
    public void provides_InvalidQualifier() {
        assertThrows(DIRuntimeException.class, () -> DIBootstrap.createInjector(new TestModule_InvalidQualifier()));
    }

    @Test
    public void provides_CustomProvider() {
        Injector injector = DIBootstrap.createInjector(new TestModule_ProvidesProvider());

        Service2 s2 = injector.getInstance(Service2.class);
        assertEquals("service2 provider", s2.doIt());
    }

    interface Service1 {
        String doIt();
    }

    interface Service2 {
        String doIt();
    }

    public static class TestModule_StandaloneService_Static implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public static Service1 provideService1() {
            return () -> "provideService1";
        }
    }

    public static class TestModule_StandaloneService_Instance implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public Service1 provideService1() {
            return () -> "provideService1";
        }
    }

    public static class TestModule_ServiceChain implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public static Service1 provideService1() {
            return () -> "provideService1";
        }

        @Provides
        static Service2 provideService2(Service1 s1) {
            return () -> "provideService2_" + s1.doIt();
        }
    }

    public static class TestModule_ServiceChain_ProviderParameter implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public static Service1 provideService1() {
            return () -> "provideService1";
        }

        @Provides
        static Service2 provideService2(Provider<Service1> s1) {
            return () -> "provideService2_" + s1.get().doIt();
        }
    }

    public static class TestModule_InvalidProvider implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public void invalidProvides() {
        }
    }

    public static class TestModule_InvalidQualifier implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @TestQualifier
        @Named("s1")
        @Provides
        public Service1 invalidProvides() {
            return () -> "provideService1";
        }
    }

    public static class TestModule_NamedService implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Named("s1")
        @Provides
        public static Service1 provideService1() {
            return () -> "provideService1";
        }
    }

    public static class TestModule_NamedParameter implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public static Service1 provideUnnamedService1() {
            return () -> "provideService1_unnamed";
        }

        @Named("s1")
        @Provides
        public static Service1 provideService1() {
            return () -> "provideService1";
        }

        @Provides
        public Service2 provideService2(@Named("s1") Service1 s1) {
            return () -> "provideService2_" + s1.doIt();
        }
    }

    public static class TestModule_QualifiedProviderParameter implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public static Service1 provideUnnamedService1() {
            return () -> "provideService1_unqualified";
        }

        @TestQualifier
        @Provides
        public static Service1 provideService1() {
            return () -> "provideService1";
        }

        @Provides
        public Service2 provideService2(@TestQualifier Provider<Service1> s1) {
            return () -> "provideService2_" + s1.get().doIt();
        }
    }

    private static class TestModule_CircularDependency implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Provides
        Service1 createService1(Service2 service2) {
            return () -> "service1" + service2.doIt();
        }

        @Provides
        Service2 createService2(Service1 service1) {
            return () -> "service2" + service1.doIt();
        }
    }

    private static class TestModule_ProvidesProvider implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bind(Service2.class).toProvider(CustomService2Provider.class);
        }

        @Provides
        CustomService2Provider createService2Provider() {
            return new CustomService2Provider("provider");
        }
    }

    private static class CustomService2Provider implements Provider<Service2> {

        private final String name;

        private CustomService2Provider(String name) {
            this.name = name;
        }

        @Override
        public Service2 get() {
            return () -> "service2 " + name;
        }
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @interface TestQualifier {
    }
}




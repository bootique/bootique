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
import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class GenericTypesIT {

    @Test
    public void directAccess() {
        Injector injector = DIBootstrap.createInjector(new TestModule1());

        assertEquals(Arrays.asList(1,2,3), injector.getInstance(Key.get(new TypeLiteral<List<? extends Integer>>(){})));
        assertEquals(Arrays.asList(3,4,5), injector.getInstance(Key.get(new TypeLiteral<List<? super Integer>>(){})));

        Optional<String> optionalString = injector.getInstance(Key.get(new TypeLiteral<Optional<String>>(){}));
        assertEquals("test", optionalString.orElseThrow(NullPointerException::new));

        Optional<Integer> optionalInteger = injector.getInstance(Key.get(new TypeLiteral<Optional<Integer>>(){}));
        assertEquals(Integer.valueOf(42), optionalInteger.orElseThrow(NullPointerException::new));
    }

    @Test
    public void directInjection() {
        Injector injector = DIBootstrap.createInjector(new TestModule1(), new TestService1Module());

        Service1 service1 = injector.getInstance(Service1.class);
        assertInstanceOf(Service1_Impl1.class, service1);

        assertEquals(Arrays.asList(1,2,3), service1.getIntegers());
        assertEquals(Arrays.asList(3,4,5), service1.getObjects());
        assertEquals("test", service1.getOptionalString().orElseThrow(NullPointerException::new));
        assertEquals(Integer.valueOf(42), service1.getOptionalInteger().orElseThrow(NullPointerException::new));
    }

    @Test
    public void providerAccess() {
        Injector injector = DIBootstrap.createInjector(new TestModule1());

        Provider<List<? extends Integer>> integers = injector.getProvider(Key.get(new TypeLiteral<List<? extends Integer>>(){}));
        assertEquals(Arrays.asList(1,2,3), integers.get());

        Provider<List<? super Integer>> objects = injector.getProvider(Key.get(new TypeLiteral<List<? super Integer>>(){}));
        assertEquals(Arrays.asList(3,4,5), objects.get());

        Provider<Optional<String>> optionalString = injector.getProvider(Key.get(new TypeLiteral<Optional<String>>(){}));
        assertEquals("test", optionalString.get().orElseThrow(NullPointerException::new));

        Provider<Optional<Integer>> optionalInteger = injector.getProvider(Key.get(new TypeLiteral<Optional<Integer>>(){}));
        assertEquals(Integer.valueOf(42), optionalInteger.get().orElseThrow(NullPointerException::new));
    }

    @Test
    public void providerInjection() {
        Injector injector = DIBootstrap.createInjector(new TestModule1(), new TestService2Module());

        Service1 service1 = injector.getInstance(Service1.class);
        assertInstanceOf(Service1_Impl2.class, service1);

        assertEquals(Arrays.asList(1,2,3), service1.getIntegers());
        assertEquals(Arrays.asList(3,4,5), service1.getObjects());
        assertEquals("test", service1.getOptionalString().orElseThrow(NullPointerException::new));
        assertEquals(Integer.valueOf(42), service1.getOptionalInteger().orElseThrow(NullPointerException::new));
    }

    @Test
    public void directAccessFromProvidesMethods() {
        Injector injector = DIBootstrap.createInjector(new TestModule2());

        assertEquals(Arrays.asList(1,2,3), injector.getInstance(Key.get(new TypeLiteral<List<? extends Integer>>(){})));
        assertEquals(Arrays.asList(3,4,5), injector.getInstance(Key.get(new TypeLiteral<List<? super Integer>>(){})));

        Optional<String> optionalString = injector.getInstance(Key.get(new TypeLiteral<Optional<String>>(){}));
        assertEquals("test", optionalString.orElseThrow(NullPointerException::new));

        Optional<Integer> optionalInteger = injector.getInstance(Key.get(new TypeLiteral<Optional<Integer>>(){}));
        assertEquals(Integer.valueOf(42), optionalInteger.orElseThrow(NullPointerException::new));
    }

    @Test
    public void genericFieldInjection() {
        Injector injector = DIBootstrap.createInjector(new TestModule3());

        Service2_Impl<?> service = injector.getInstance(Service2_Impl.class);
        assertEquals("test", service.doSomething());

        Service2_Impl2 service2 = injector.getInstance(Service2_Impl2.class);
        service2.setTestField2(123);
        assertEquals("test123", service2.doSomething());
    }

    static class TestService1Module implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(Service1.class).to(Service1_Impl1.class);
        }
    }

    static class TestService2Module implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(Service1.class).to(Service1_Impl2.class);
        }
    }

    static class TestModule1 implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(Key.get(new TypeLiteral<List<? extends Integer>>(){})).toInstance(Arrays.asList(1,2,3));
            binder.bind(Key.get(new TypeLiteral<List<? super Integer>>(){})).toInstance(Arrays.asList(3,4,5));

            binder.bind(Key.get(new TypeLiteral<Optional<String>>(){})).toInstance(Optional.of("test"));
            binder.bind(Key.get(new TypeLiteral<Optional<Integer>>(){})).toInstance(Optional.of(42));
        }
    }

    public static class TestModule2 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public List<? extends Integer> createIntegerList() {
            return Arrays.asList(1,2,3);
        }

        @Provides
        public List<? super Integer> createObjectsList() {
            return Arrays.asList(3,4,5);
        }

        @Provides
        public Optional<Integer> createIntegerOptional() {
            return Optional.of(42);
        }

        @Provides
        public Optional<String> createStringOptional() {
            return Optional.of("test");
        }

    }

    public static class TestModule3 implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(String.class).toInstance("test");
            binder.bind(Service2_Impl.class).inSingletonScope();
            binder.bind(Service2_Impl2.class).inSingletonScope();
        }
    }

    interface Service1 {
        List<? extends Integer> getIntegers();

        List<? super Integer> getObjects();

        Optional<String> getOptionalString();

        Optional<Integer> getOptionalInteger();
    }

    static class Service1_Impl1 implements Service1 {

        @Inject
        private List<? extends Integer> integers;

        @Inject
        private List<? super Integer> objects;

        @Inject
        private Optional<String> optionalString;

        @Inject
        private Optional<Integer> optionalInteger;


        @Override
        public List<? extends Integer> getIntegers() {
            return integers;
        }

        @Override
        public List<? super Integer> getObjects() {
            return objects;
        }

        @Override
        public Optional<String> getOptionalString() {
            return optionalString;
        }

        @Override
        public Optional<Integer> getOptionalInteger() {
            return optionalInteger;
        }
    }

    static class Service1_Impl2 implements Service1 {

        @Inject
        private Provider<List<? extends Integer>> integers;

        @Inject
        private Provider<List<? super Integer>> objects;

        @Inject
        private Provider<Optional<String>> optionalString;

        @Inject
        private Provider<Optional<Integer>> optionalInteger;


        @Override
        public List<? extends Integer> getIntegers() {
            return integers.get();
        }

        @Override
        public List<? super Integer> getObjects() {
            return objects.get();
        }

        @Override
        public Optional<String> getOptionalString() {
            return optionalString.get();
        }

        @Override
        public Optional<Integer> getOptionalInteger() {
            return optionalInteger.get();
        }
    }

    static abstract class Service2<T> {
        @Inject
        T genericField;

        abstract String doSomething();
    }

    static class Service2_Impl<K> extends Service2<String> {
        K testField2;

        public void setTestField2(K testField2) {
            this.testField2 = testField2;
        }

        @Override
        String doSomething() {
            return genericField;
        }
    }

    static class Service2_Impl2 extends Service2_Impl<Integer> {
        @Override
        String doSomething() {
            return genericField + testField2;
        }
    }

}

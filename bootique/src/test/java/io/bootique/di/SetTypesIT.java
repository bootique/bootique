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
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class SetTypesIT {

    @Test
    public void byKeyAndValueTypeMapInjection() {
        Injector injector = DIBootstrap.createInjector(serviceModule1, b -> {
            b.bindSet(Integer.class).addInstance(1).addInstance(2);
            b.bindSet(String.class).addInstance("3").addInstance("4");
        });

        assertSetContent(injector);
    }

    @Test
    public void byGenericTypeMapInjection() {
        Injector injector = DIBootstrap.createInjector(serviceModule1, b -> {
            b.bindSet(TypeLiteral.of(Integer.class)).addInstance(1).addInstance(2);
            b.bindSet(TypeLiteral.of(String.class)).addInstance("3").addInstance("4");
        });

        assertSetContent(injector);
    }

    @Test
    public void directMapInjection() {
        Injector injector = DIBootstrap.createInjector(serviceModule1, b -> {
            Set<Integer> integerMap = new HashSet<>();
            integerMap.add(1);
            integerMap.add(2);

            Set<String> stringMap = new HashSet<>();
            stringMap.add("3");
            stringMap.add("4");

            b.bind(Key.get(new TypeLiteral<Set<Integer>>() {})).toInstance(integerMap);
            b.bind(Key.get(new TypeLiteral<Set<String>>() {})).toInstance(stringMap);
        });

        assertSetContent(injector);
    }

    @Test
    public void providerMapInjection() {
        Injector injector = DIBootstrap.createInjector(serviceModule1, new SetProviderModule());
        assertSetContent(injector);
    }

    @Test
    public void wildcardMapDirectInjection() {
        Injector injector = DIBootstrap.createInjector(b -> {
            b.bind(Service.class).to(Service_Impl2.class);
            b.bindSet(new TypeLiteral<List<? extends Number>>() {})
                    .addInstance(Arrays.asList(1, 2, 3));
        });

        Service service = injector.getInstance(Service.class);

        assertInstanceOf(Service_Impl2.class, service);

        Service_Impl2 impl = (Service_Impl2) service;
        assertTrue(impl.getSet().contains(asList(1, 2, 3)));
    }

    @Test
    public void wildcardMapProvider() {
        Injector injector = DIBootstrap.createInjector(new SetProviderModule(),
                b -> b.bind(Service.class).to(Service_Impl2.class));

        Service service = injector.getInstance(Service.class);

        assertInstanceOf(Service_Impl2.class, service);

        Service_Impl2 impl = (Service_Impl2) service;
        assertTrue(impl.getSet().contains(asList(1, 2, 3)));
    }

    @Test
    public void duplicateValue() {
        Injector injector = DIBootstrap.createInjector(b ->
                b.bindSet(TypeLiteral.of(Integer.class)).addInstance(1).addInstance(2).addInstance(2));

        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Key.get(TypeLiteral.setOf(Integer.class))));
    }

    @Test
    public void addType() {
        Injector injector = DIBootstrap.createInjector(
                new SetProviderModule(),
                b -> b.bindSet(Service.class).add(Service_Impl1.class).add(Service_Impl2.class)
        );
        Set<Service> services = injector.getInstance(Key.getSetOf(Service.class));
        Set<Class<?>> serviceTypes = services.stream().map(Object::getClass).collect(Collectors.toSet());
        assertEquals(new HashSet<>(asList(Service_Impl1.class, Service_Impl2.class)), serviceTypes);
    }

    @Test
    public void addAllBinding() {
        Injector injector = DIBootstrap.createInjector(b ->
                b.bindSet(TypeLiteral.of(Integer.class)).addInstance(1).addInstances(Arrays.asList(2,3,4)).addInstance(5));

        Set<Integer> set = injector.getInstance(Key.get(TypeLiteral.setOf(Integer.class)));
        assertEquals(new HashSet<>(asList(1, 2, 3, 4, 5)), set);
    }

    @Test
    public void continueBinding() {
        Injector injector = DIBootstrap.createInjector(
                b -> b.bindSet(TypeLiteral.of(Integer.class)).addInstance(1).addInstance(2),
                b -> b.bindSet(TypeLiteral.of(Integer.class)).addInstance(3).addInstance(4)
        );

        Set<Integer> set = injector.getInstance(Key.get(TypeLiteral.setOf(Integer.class)));
        assertEquals(new HashSet<>(asList(1, 2, 3, 4)), set);
    }

    @Test
    public void addKey() {
        Injector injector = DIBootstrap.createInjector(b -> {
            b.bind(Key.get(Integer.class, "1")).toInstance(1);
            b.bind(Key.get(Integer.class, "2")).toInstance(2);
            b.bindSet(Integer.class)
                    .add(Key.get(Integer.class, "1"))
                    .add(Key.get(Integer.class, "2"));
        });

        Set<Integer> set = injector.getInstance(Key.getSetOf(Integer.class));
        assertEquals(new HashSet<>(asList(1, 2)), set);
    }

    @Test
    public void duplicatedValue() {
        Injector injector = DIBootstrap.createInjector(
            b -> b.bindSet(Integer.class).addInstance(1).addInstance(2).addInstance(1)
        );

        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Key.getSetOf(Integer.class)));
    }

    @Test
    public void duplicateProviderMethod() {
        Injector injector = DIBootstrap.createInjector(new DuplicateValueModule());
        assertThrows(DIRuntimeException.class, () -> injector.getInstance(Key.getSetOf(Integer.class)));
    }

    @Test
    public void providerBinding() {
        Injector injector = DIBootstrap.createInjector(new ProviderTypeModule());
        Set<Integer> integers = injector.getInstance(Key.getSetOf(Integer.class));

        assertEquals(new HashSet<>(asList(1, 2, 3)), integers);
    }

    private void assertSetContent(Injector injector) {
        Service service = injector.getInstance(Service.class);

        assertInstanceOf(Service_Impl1.class, service);

        Service_Impl1 impl = (Service_Impl1) service;
        assertTrue(impl.getIntegerSet().contains(1));
        assertTrue(impl.getIntegerSet().contains(2));

        assertTrue(impl.getStringSet().contains("3"));
        assertTrue(impl.getStringSet().contains("4"));
    }

    private static final BQModule serviceModule1 = b -> b.bind(Service.class).to(Service_Impl1.class);

    interface Service {
    }

    private static class Service_Impl1 implements Service {
        @Inject
        private Set<String> stringSet;
        @Inject
        private Set<Integer> integerSet;

        Set<Integer> getIntegerSet() {
            return integerSet;
        }

        Set<String> getStringSet() {
            return stringSet;
        }
    }

    private static class Service_Impl2 implements Service {
        @Inject
        private Set<List<? extends Number>> set;

        Set<List<? extends Number>> getSet() {
            return set;
        }
    }

    public static class SetProviderModule implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        public Set<Integer> createIntegerSet() {
            Set<Integer> integerSet = new HashSet<>();
            integerSet.add(1);
            integerSet.add(2);
            return integerSet;
        }

        @Provides
        public Set<String> createStringSet() {
            Set<String> stringSet = new HashSet<>();
            stringSet.add("3");
            stringSet.add("4");
            return stringSet;
        }

        @Provides
        public Set<List<? extends Number>> createListSet() {
            Set<List<? extends Number>> listSet = new HashSet<>();
            listSet.add(Arrays.asList(1, 2, 3));
            return listSet;
        }
    }

    static class DuplicateValueModule implements BQModule  {

        @Override
        public void configure(Binder binder) {
            binder.bindSet(Integer.class)
                    .add(Key.get(Integer.class, "1"))
                    .add(Key.get(Integer.class, "2"))
                    .add(Key.get(Integer.class, "3"));
        }

        @Provides
        @Named("1")
        Integer getInt1() {
            return 1;
        }

        @Provides
        @Named("2")
        Integer getInt2() {
            return 2;
        }

        @Provides
        @Named("3")
        Integer getInt3() {
            return 1;
        }

    }

    static class MyIntegerProvider implements Provider<Integer> {
        @Override
        public Integer get() {
            return 3;
        }
    }

    interface IntegerProvider extends Provider<Integer> {
    }

    static class ProviderTypeModule implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bindSet(Integer.class)
                    .addProviderInstance(() -> 1)
                    .addProvider(IntegerProvider.class)
                    .addProvider(MyIntegerProvider.class);
        }

        @Provides
        IntegerProvider createProvider() {
            return () -> 2;
        }
    }

}

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

package io.bootique;

import io.bootique.di.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BootiqueTest {

    private Bootique bootique;

    @BeforeEach
    public void before() {
        this.bootique = Bootique.app();
    }

    @Test
    public void createInjector_Modules_Instances() {
        Injector i = bootique.modules(new TestModule1(), new TestModule2()).createInjector();
        Set<String> strings = i.getInstance(Key.get(new TypeLiteral<>(){}));

        assertEquals(2, strings.size());
        assertTrue(strings.contains("tm1"));
        assertTrue(strings.contains("tm2"));
    }

    @Test
    public void createInjector_Modules_Types() {
        Injector i = bootique.modules(TestModule1.class, TestModule2.class).createInjector();
        Set<String> strings = i.getInstance(Key.get(new TypeLiteral<>(){}));

        assertEquals(2, strings.size());
        assertTrue(strings.contains("tm1"));
        assertTrue(strings.contains("tm2"));
    }

    @Test
    public void mergeArrays() {
        assertArrayEquals(new String[]{}, Bootique.mergeArrays(new String[0], new String[0]));
        assertArrayEquals(new String[]{"a"}, Bootique.mergeArrays(new String[]{"a"}, new String[0]));
        assertArrayEquals(new String[]{"b"}, Bootique.mergeArrays(new String[0], new String[]{"b"}));
        assertArrayEquals(new String[]{"b", "c", "d"}, Bootique.mergeArrays(new String[]{"b", "c"}, new String[]{"d"}));
    }

    @Test
    public void moduleProviderDependencies() {
        BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        ModuleCrate m1 = mock(ModuleCrate.class);
        when(testModuleProvider1.moduleCrate()).thenReturn(m1);

        ModuleCrate m2 = mock(ModuleCrate.class);
        when(testModuleProvider2.moduleCrate()).thenReturn(m2);

        ModuleCrate m3 = mock(ModuleCrate.class);
        when(testModuleProvider3.moduleCrate()).thenReturn(m3);

        Collection<ModuleCrate> BuiltModule =
                Bootique.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertEquals(3, BuiltModule.size());
        assertTrue(BuiltModule.contains(m1));
        assertTrue(BuiltModule.contains(m2));
        assertTrue(BuiltModule.contains(m3));

        verify(testModuleProvider1, times(1)).dependencies();
        verify(testModuleProvider1, times(1)).moduleCrate();
        verify(testModuleProvider2, times(1)).dependencies();
        verify(testModuleProvider2, times(1)).moduleCrate();
        verify(testModuleProvider3, times(1)).dependencies();
        verify(testModuleProvider3, times(1)).moduleCrate();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    @Test
    public void moduleProviderDependenciesTwoLevels() {
        BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        ModuleCrate m1 = mock(ModuleCrate.class);
        when(testModuleProvider1.moduleCrate()).thenReturn(m1);

        ModuleCrate m2 = mock(ModuleCrate.class);
        when(testModuleProvider2.moduleCrate()).thenReturn(m2);

        ModuleCrate m3 = mock(ModuleCrate.class);
        when(testModuleProvider3.moduleCrate()).thenReturn(m3);

        Collection<ModuleCrate> BuiltModule =
                Bootique.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertEquals(3, BuiltModule.size());
        assertTrue(BuiltModule.contains(m1));
        assertTrue(BuiltModule.contains(m2));
        assertTrue(BuiltModule.contains(m3));

        verify(testModuleProvider1, times(1)).dependencies();
        verify(testModuleProvider1, times(1)).moduleCrate();
        verify(testModuleProvider2, times(1)).dependencies();
        verify(testModuleProvider2, times(1)).moduleCrate();
        verify(testModuleProvider3, times(1)).dependencies();
        verify(testModuleProvider3, times(1)).moduleCrate();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    @Test
    public void moduleProviderDependenciesCircular() {
        BQModuleProvider testModuleProvider1 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider2 = mock(BQModuleProvider.class);
        BQModuleProvider testModuleProvider3 = mock(BQModuleProvider.class);

        when(testModuleProvider1.dependencies()).thenReturn(asList(testModuleProvider2, testModuleProvider3));

        ModuleCrate m1 = mock(ModuleCrate.class);
        when(testModuleProvider1.moduleCrate()).thenReturn(m1);

        ModuleCrate m2 = mock(ModuleCrate.class);
        when(testModuleProvider2.moduleCrate()).thenReturn(m2);

        ModuleCrate m3 = mock(ModuleCrate.class);
        when(testModuleProvider3.moduleCrate()).thenReturn(m3);

        Collection<ModuleCrate> BuiltModule =
                Bootique.moduleProviderDependencies(singletonList(testModuleProvider1));

        assertEquals(3, BuiltModule.size());
        assertTrue(BuiltModule.contains(m1));
        assertTrue(BuiltModule.contains(m2));
        assertTrue(BuiltModule.contains(m3));

        verify(testModuleProvider1, times(1)).dependencies();
        verify(testModuleProvider1, times(1)).moduleCrate();
        verify(testModuleProvider2, times(1)).dependencies();
        verify(testModuleProvider2, times(1)).moduleCrate();
        verify(testModuleProvider3, times(1)).dependencies();
        verify(testModuleProvider3, times(1)).moduleCrate();

        verifyNoMoreInteractions(testModuleProvider1, testModuleProvider2, testModuleProvider3);
    }

    static class TestModule1 implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).addInstance("tm1");
        }
    }

    static class TestModule2 implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).addInstance("tm2");
        }
    }
}

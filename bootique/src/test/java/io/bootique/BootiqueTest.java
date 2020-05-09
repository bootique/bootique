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

import io.bootique.di.Binder;
import io.bootique.di.Injector;
import io.bootique.di.Key;
import io.bootique.di.BQModule;

import io.bootique.di.TypeLiteral;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BootiqueTest {

    private Bootique bootique;

    @BeforeEach
    public void before() {
        this.bootique = Bootique.app();
    }

    @Test
    public void testCreateInjector_Modules_Instances() {
        Injector i = bootique.modules(new TestModule1(), new TestModule2()).createInjector();
        Set<String> strings = i.getInstance(Key.get(new TypeLiteral<Set<String>>(){}));

        assertEquals(2, strings.size());
        assertTrue(strings.contains("tm1"));
        assertTrue(strings.contains("tm2"));
    }

    @Test
    public void testCreateInjector_Modules_Types() {
        Injector i = bootique.modules(TestModule1.class, TestModule2.class).createInjector();
        Set<String> strings = i.getInstance(Key.get(new TypeLiteral<Set<String>>(){}));

        assertEquals(2, strings.size());
        assertTrue(strings.contains("tm1"));
        assertTrue(strings.contains("tm2"));
    }

    static class TestModule1 implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).add("tm1");
        }
    }

    static class TestModule2 implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).add("tm2");
        }
    }
}

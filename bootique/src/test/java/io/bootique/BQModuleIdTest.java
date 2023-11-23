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

import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BQModuleIdTest {

    @Test
    public void innerStaticClass() {
        BQModuleId moduleId1 = BQModuleId.of(new MyStaticModule());
        BQModuleId moduleId2 = BQModuleId.of(new MyStaticModule());
        BQModuleId moduleId3 = BQModuleId.of(new MyStaticModule2());

        assertEquals(moduleId1, moduleId1);
        assertEquals(moduleId1.hashCode(), moduleId1.hashCode());
        assertEquals(moduleId2, moduleId2);
        assertEquals(moduleId2.hashCode(), moduleId2.hashCode());
        assertEquals(moduleId3, moduleId3);
        assertEquals(moduleId3.hashCode(), moduleId3.hashCode());

        assertEquals(moduleId1, moduleId2);
        assertEquals(moduleId1.hashCode(), moduleId2.hashCode());

        assertNotEquals(moduleId1, moduleId3);
        assertNotEquals(moduleId1.hashCode(), moduleId3.hashCode());
        assertNotEquals(moduleId2, moduleId3);
        assertNotEquals(moduleId2.hashCode(), moduleId3.hashCode());
    }

    @Test
    public void innerClass() {
        BQModuleId moduleId1 = BQModuleId.of(new MyModule());
        BQModuleId moduleId2 = BQModuleId.of(new MyModule());
        BQModuleId moduleId3 = BQModuleId.of(new MyModule2());

        assertEquals(moduleId1, moduleId1);
        assertEquals(moduleId1.hashCode(), moduleId1.hashCode());
        assertEquals(moduleId2, moduleId2);
        assertEquals(moduleId2.hashCode(), moduleId2.hashCode());
        assertEquals(moduleId3, moduleId3);
        assertEquals(moduleId3.hashCode(), moduleId3.hashCode());

        assertEquals(moduleId1, moduleId2);
        assertEquals(moduleId1.hashCode(), moduleId2.hashCode());

        assertNotEquals(moduleId1, moduleId3);
        assertNotEquals(moduleId1.hashCode(), moduleId3.hashCode());
        assertNotEquals(moduleId2, moduleId3);
        assertNotEquals(moduleId2.hashCode(), moduleId3.hashCode());
    }

    @Test
    public void methodRef() {
        BQModuleId moduleId1 = BQModuleId.of(BQModuleIdTest::configure);
        BQModuleId moduleId2 = BQModuleId.of(BQModuleIdTest::configure);
        BQModuleId moduleId3 = BQModuleId.of(BQModuleIdTest::configure2);

        assertEquals(moduleId1, moduleId1);
        assertEquals(moduleId1.hashCode(), moduleId1.hashCode());
        assertEquals(moduleId2, moduleId2);
        assertEquals(moduleId2.hashCode(), moduleId2.hashCode());
        assertEquals(moduleId3, moduleId3);
        assertEquals(moduleId3.hashCode(), moduleId3.hashCode());

        assertNotEquals(moduleId1, moduleId2);
        assertNotEquals(moduleId1.hashCode(), moduleId2.hashCode());
        assertNotEquals(moduleId1, moduleId3);
        assertNotEquals(moduleId1.hashCode(), moduleId3.hashCode());
        assertNotEquals(moduleId2, moduleId3);
        assertNotEquals(moduleId2.hashCode(), moduleId3.hashCode());
    }

    @Test
    public void lambda() {
        BQModuleId moduleId1 = BQModuleId.of(binder -> binder.bindSet(String.class).addInstance("lambda"));
        BQModuleId moduleId2 = BQModuleId.of(binder -> binder.bindSet(String.class).addInstance("lambda 2"));

        assertEquals(moduleId1, moduleId1);
        assertEquals(moduleId1.hashCode(), moduleId1.hashCode());
        assertEquals(moduleId2, moduleId2);
        assertEquals(moduleId2.hashCode(), moduleId2.hashCode());

        assertNotEquals(moduleId1, moduleId2);
        assertNotEquals(moduleId1.hashCode(), moduleId2.hashCode());
    }

    @Test
    public void lambdaFactory() {
        BQModuleId moduleId1 = BQModuleId.of(createModule("lambda factory"));
        BQModuleId moduleId2 = BQModuleId.of(createModule("lambda factory 2"));

        assertEquals(moduleId1, moduleId1);
        assertEquals(moduleId1.hashCode(), moduleId1.hashCode());
        assertEquals(moduleId2, moduleId2);
        assertEquals(moduleId2.hashCode(), moduleId2.hashCode());

        assertNotEquals(moduleId1, moduleId2);
        // here we'll get same hash codes but this still good for equals/hashCode
        assertEquals(moduleId1.hashCode(), moduleId2.hashCode());
    }

    @Test
    public void mix() {
        BQModuleId moduleId1 = BQModuleId.of(new MyModule());
        BQModuleId moduleId2 = BQModuleId.of(BQModuleIdTest::configure);
        BQModuleId moduleId3 = BQModuleId.of(createModule("lambda factory"));

        assertEquals(moduleId1, moduleId1);
        assertEquals(moduleId1.hashCode(), moduleId1.hashCode());
        assertEquals(moduleId2, moduleId2);
        assertEquals(moduleId2.hashCode(), moduleId2.hashCode());
        assertEquals(moduleId3, moduleId3);
        assertEquals(moduleId3.hashCode(), moduleId3.hashCode());

        assertNotEquals(moduleId1, moduleId2);
        assertNotEquals(moduleId1.hashCode(), moduleId2.hashCode());
        assertNotEquals(moduleId1, moduleId3);
        assertNotEquals(moduleId1.hashCode(), moduleId3.hashCode());
        assertNotEquals(moduleId2, moduleId3);
        assertNotEquals(moduleId2.hashCode(), moduleId3.hashCode());
    }

    static void configure(Binder binder) {
        binder.bindSet(String.class).addInstance("method ref");
    }

    private static void configure2(Binder binder) {
        binder.bindSet(String.class).addInstance("method ref 2");
    }

    public BQModule createModule(String name) {
        return binder -> binder.bindSet(String.class).addInstance(name);
    }

    public static class MyStaticModule implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).addInstance("static class");
        }
    }

    static class MyStaticModule2 implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).addInstance("static class 2");
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class MyModule implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).addInstance("nested class");
        }
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    private class MyModule2 implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bindSet(String.class).addInstance("nested class 2");
        }
    }
}
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
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import io.bootique.shutdown.DefaultShutdownManager;
import io.bootique.shutdown.ShutdownManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class BootiqueTest {

    static final BootLogger logger = new DefaultBootLogger(true);
    static final ShutdownManager shutdownManager = new DefaultShutdownManager(Duration.ofSeconds(1), logger);

    private Bootique bootique;

    @BeforeEach
    public void before() {
        this.bootique = Bootique.app();
    }

    @Test
    public void createInjector_Modules_Instances() {
        Injector i = bootique.modules(new TestModule1(), new TestModule2()).createInjector(shutdownManager, logger);
        Set<String> strings = i.getInstance(Key.get(new TypeLiteral<>(){}));

        assertEquals(2, strings.size());
        assertTrue(strings.contains("tm1"));
        assertTrue(strings.contains("tm2"));
    }

    @Test
    public void createInjector_Modules_Types() {
        Injector i = bootique.modules(TestModule1.class, TestModule2.class).createInjector(shutdownManager, logger);
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

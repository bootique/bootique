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
import io.bootique.log.DefaultBootLogger;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ModulesSorterTest {

    // using real logger to better understand what's going on in the tests
    final ModulesSorter sorter = new ModulesSorter(new DefaultBootLogger(true));
    final List<BQModule> testModules = List.of(new M0(), new M1(), new M2(), new M3(), new M4());
    final List<ModuleCrate> builtModules = testModules.stream().map(m -> ModuleCrate.of(m).build()).collect(Collectors.toList());

    @Test
    public void getModules_Empty() {
        assertEquals(0, sorter.uniqueCratesInLoadOrder(Collections.emptyList()).size());
    }

    @Test
    public void getModules_One() {
        List<ModuleCrate> modules = sorter.uniqueCratesInLoadOrder(List.of(builtModules.get(2)));

        assertEquals(1, modules.size());
        assertSame(testModules.get(2), modules.get(0).getModule());
    }

    @Test
    public void getModules_Two() {

        // the order is irrelevant, these modules don't depend on each other
        Set<BQModule> modules = sorter.uniqueCratesInLoadOrder(List.of(
                builtModules.get(2),
                builtModules.get(1))).stream().map(ModuleCrate::getModule).collect(Collectors.toSet());

        assertEquals(2, modules.size());

        assertTrue(modules.contains(testModules.get(2)));
        assertTrue(modules.contains(testModules.get(1)));
    }

    @Test
    public void getModules_Three_Dupes() {

        List<ModuleCrate> modules = sorter.uniqueCratesInLoadOrder(List.of(
                builtModules.get(2),
                builtModules.get(1),
                builtModules.get(2)));

        assertEquals(2, modules.size());
        assertSame(testModules.get(2), modules.get(0).getModule());
        assertSame(testModules.get(1), modules.get(1).getModule());
    }

    @Test
    public void getModules_Overrides() {

        ModuleCrate bm0 = ModuleCrate.of(testModules.get(0)).overrides(M3.class).build();
        List<ModuleCrate> modules = sorter.uniqueCratesInLoadOrder(List.of(
                bm0,
                builtModules.get(3)
        ));

        assertEquals(2, modules.size());
        assertSame(testModules.get(3), modules.get(0).getModule());
        assertSame(testModules.get(0), modules.get(1).getModule());
    }

    @Test
    public void getModules_Overrides_Chain() {

        ModuleCrate bm0 = ModuleCrate.of(testModules.get(0)).overrides(M3.class).build();
        ModuleCrate bm3 = ModuleCrate.of(testModules.get(3)).overrides(M4.class).build();

        List<ModuleCrate> modules = sorter.uniqueCratesInLoadOrder(List.of(
                builtModules.get(4),
                bm0,
                builtModules.get(1),
                bm3
        ));

        assertEquals(4, modules.size());
        assertSame(testModules.get(4), modules.get(0).getModule());
        assertSame(testModules.get(3), modules.get(1).getModule());
        assertSame(testModules.get(0), modules.get(2).getModule());
        assertSame(testModules.get(1), modules.get(3).getModule());
    }

    @Test
    public void getModules_OverrideCycle() {

        // 0 replaces 3 ; 3 replaces 0
        ModuleCrate bm0 = ModuleCrate.of(testModules.get(0)).overrides(M3.class).build();
        ModuleCrate bm3 = ModuleCrate.of(testModules.get(3)).overrides(M0.class).build();

        assertThrows(RuntimeException.class, () -> sorter.uniqueCratesInLoadOrder(List.of(bm0, bm3)));
    }

    @Test
    public void getModules_OverrideIndirectCycle() {

        // 0 replaces 3 ; 3 replaces 4 ; 4 replaces 0
        ModuleCrate bm0 = ModuleCrate.of(testModules.get(0)).overrides(M3.class).build();
        ModuleCrate bm3 = ModuleCrate.of(testModules.get(3)).overrides(M4.class).build();
        ModuleCrate bm4 = ModuleCrate.of(testModules.get(4)).overrides(M0.class).build();

        assertThrows(RuntimeException.class, () -> sorter.uniqueCratesInLoadOrder(List.of(bm0, bm4, bm3)));
    }

    @Test
    public void getModules_OverrideDupe() {

        // 0 overrides 3 ; 4 overrides 3
        ModuleCrate bm0 = ModuleCrate.of(testModules.get(0)).overrides(M3.class).build();
        ModuleCrate bm4 = ModuleCrate.of(testModules.get(4)).overrides(M3.class).build();

        assertThrows(RuntimeException.class, () -> sorter.uniqueCratesInLoadOrder(List.of(bm4, bm0, builtModules.get(3))));
    }

    class M0 implements BQModule {

        @Override
        public void configure(Binder binder) {
            // noop
        }
    }

    class M1 implements BQModule {

        @Override
        public void configure(Binder binder) {
            // noop
        }
    }

    class M2 implements BQModule {

        @Override
        public void configure(Binder binder) {
            // noop
        }
    }

    class M3 implements BQModule {

        @Override
        public void configure(Binder binder) {
            // noop
        }
    }

    class M4 implements BQModule {

        @Override
        public void configure(Binder binder) {
            // noop
        }
    }
}

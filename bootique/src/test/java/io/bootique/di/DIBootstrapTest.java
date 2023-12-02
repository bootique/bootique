
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
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class DIBootstrapTest {

    @Test
    public void createInjector_Empty() {
        Injector emptyInjector = DIBootstrap.injectorBuilder().build();
        assertNotNull(emptyInjector);
    }

    @Test
    public void createInjector_SingleModule() {
        final boolean[] configureCalled = new boolean[1];

        BQModule module = binder -> configureCalled[0] = true;

        Injector injector = DIBootstrap.injectorBuilder(module).build();
        assertNotNull(injector);

        assertTrue(configureCalled[0]);
    }

    @Test
    public void createInjector_MultiModule() {

        final boolean[] configureCalled = new boolean[2];

        BQModule module1 = binder -> configureCalled[0] = true;

        BQModule module2 = binder -> configureCalled[1] = true;

        Injector injector = DIBootstrap.injectorBuilder(module1, module2).build();
        assertNotNull(injector);

        assertTrue(configureCalled[0]);
        assertTrue(configureCalled[1]);
    }

    @Test
    public void createInjector_MultiModuleCollection() {

        final boolean[] configureCalled = new boolean[2];

        BQModule module1 = binder -> configureCalled[0] = true;

        BQModule module2 = binder -> configureCalled[1] = true;

        Injector injector = DIBootstrap.injectorBuilder(Arrays.asList(module1, module2)).build();
        assertNotNull(injector);

        assertTrue(configureCalled[0]);
        assertTrue(configureCalled[1]);
    }
}

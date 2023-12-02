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

package io.bootique.di.spi;

import io.bootique.BQModule;
import io.bootique.di.mock.MockImplementation1_EventAnnotations;
import io.bootique.di.mock.MockInterface1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultInjectorTest {

    @Test
    public void constructor_Empty() {
        new DefaultInjector();
        // no exceptions...
    }

    @Test
    public void constructor_SingleModule() {
        final boolean[] configureCalled = new boolean[1];

        BQModule module = binder -> configureCalled[0] = true;

        new DefaultInjector(module);
        assertTrue(configureCalled[0]);
    }

    @Test
    public void constructor_MultiModule() {

        final boolean[] configureCalled = new boolean[2];

        BQModule module1 = binder -> configureCalled[0] = true;

        BQModule module2 = binder -> configureCalled[1] = true;

        new DefaultInjector(module1, module2);
        assertTrue(configureCalled[0]);
        assertTrue(configureCalled[1]);
    }

    @Test
    public void shutdown() {

        MockImplementation1_EventAnnotations.reset();

        BQModule module = binder -> binder
                .bind(MockInterface1.class)
                .to(MockImplementation1_EventAnnotations.class)
                .inSingletonScope();

        DefaultInjector injector = new DefaultInjector(module);

        MockInterface1 instance1 = injector.getInstance(MockInterface1.class);
        assertEquals("XuI", instance1.getName());

        assertFalse(MockImplementation1_EventAnnotations.shutdown1);
        assertFalse(MockImplementation1_EventAnnotations.shutdown2);
        assertFalse(MockImplementation1_EventAnnotations.shutdown3);

        injector.shutdown();

        assertTrue(MockImplementation1_EventAnnotations.shutdown1);
        assertTrue(MockImplementation1_EventAnnotations.shutdown2);
        assertTrue(MockImplementation1_EventAnnotations.shutdown3);
    }

}

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

import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.DIBootstrap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleTypeProviderTest {

    @Test
    public void module() {
        BQModule m = new ModuleTypeProvider(M1.class).createModule();
        assertEquals("tm1", DIBootstrap.createInjector(m).getInstance(String.class));
    }

    @Test
    public void moduleBuilder() {
        BuiltModule md = new ModuleTypeProvider(M1.class).buildModule();
        assertEquals("M1", md.getModuleName());
        assertNull(md.getDescription());
        assertEquals("ModuleTypeProvider", md.getProviderName());
        assertFalse(md.isDeprecated());
    }

    @Test
    public void moduleBuilder_deprecatedAnnotation() {
        BuiltModule md = new ModuleTypeProvider(M2.class).buildModule();
        assertTrue(md.isDeprecated());
    }

    static class M1 implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bind(String.class).toInstance("tm1");
        }
    }

    // Note to future self: this is not a real deprecation. The annotation is used to test the generated module metadata
    @Deprecated(since = "33.0", forRemoval = true)
    static class M2 implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bind(String.class).toInstance("tm2");
        }
    }
}

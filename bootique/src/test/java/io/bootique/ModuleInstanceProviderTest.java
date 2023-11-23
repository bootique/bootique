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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleInstanceProviderTest {

    @Test
    public void buildModule() {
        M1 m1 = new M1();
        BuiltModule bm = new ModuleInstanceProvider(m1).buildModule();
        assertSame(m1, bm.getModule());
        assertEquals("M1", bm.getModuleName());
        assertNull(bm.getDescription());
        assertEquals("ModuleInstanceProvider", bm.getProviderName());
        assertFalse(bm.isDeprecated());
    }

    @Test
    public void buildModule_deprecatedAnnotation() {
        M2 m2 = new M2();
        BuiltModule bm = new ModuleInstanceProvider(m2).buildModule();
        assertTrue(bm.isDeprecated());
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

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
package io.bootique.junit5;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.di.Binder;
import io.bootique.di.DIRuntimeException;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.*;

@Deprecated
public class BQModuleTesterTest {

    @Test
    public void autoLoadableModule() {
        BQModuleTester.of(M1.class).testAutoLoadable();
    }

    @Test
    public void nonAutoLoadableModule() {

        try {
            BQModuleTester.of(M2.class).testAutoLoadable();
        } catch (AssertionFailedError e) {
            assertTrue(e.getMessage().startsWith("Module of type 'io.bootique.junit5.BQModuleTesterTest$M2' is not auto-loadable"));
            return;
        }

        fail("Exception expected, as M2 is not auto-loadable");
    }

    @Test
    public void goodConfig() {
        BQModuleTester.of(M3.class).testConfig();
    }

    @Test
    public void badConfig() {

        try {
            BQModuleTester.of(M4.class).testConfig();
        } catch (AssertionFailedError e) {
            assertTrue(e.getCause() instanceof DIRuntimeException);
            assertTrue(e.getCause().getMessage().startsWith("Error invoking provider method 'provideModulesMetadata()' of module 'io.bootique.BQCoreModule'"));
            return;
        }

        fail("Exception expected, as M4 has config issues");
    }

    public static class M1 implements BQModule {
        @Override
        public void configure(Binder binder) {
        }
    }

    public static class M2 implements BQModule {
        @Override
        public void configure(Binder binder) {
        }
    }

    public static class M3 implements BQModule {

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).config("a", C1.class).build();
        }

        @Override
        public void configure(Binder binder) {
        }
    }

    public static class M4 implements BQModule {

        @Override
        public ModuleCrate crate() {
            return ModuleCrate.of(this).config("a", C2.class).build();
        }

        @Override
        public void configure(Binder binder) {
        }
    }

    @BQConfig
    static class C1 {
        @BQConfigProperty
        public void setX(String x) {
        }
    }

    @BQConfig
    static class C2 {
        @BQConfigProperty
        public void setX() {
        }
    }
}

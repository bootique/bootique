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
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ScopeTest {

    @Test
    public void defaultSingletonScope() {
        Injector injector = DIBootstrap
                .injectorBuilder(binder -> binder.bind(TI.class).to(TC.class))
                .defaultSingletonScope()
                .build();
        assertSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void defaultSingletonScope_WithoutScope() {
        Injector injector = DIBootstrap
                .injectorBuilder(binder -> binder.bind(TI.class).to(TC.class).withoutScope())
                .defaultSingletonScope()
                .build();
        assertNotSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void defaultSingletonScope_NoScopeMethod() {
        Injector injector = DIBootstrap
                .injectorBuilder(new DefaultModule())
                .defaultSingletonScope()
                .build();
        assertSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void defaultSingletonScope_SingletonScopeMethod() {
        Injector injector = DIBootstrap
                .injectorBuilder(new SingletonModule())
                .defaultSingletonScope()
                .build();
        assertSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void implicitNoScope() {
        Injector injector = DIBootstrap
                .injectorBuilder(binder -> binder.bind(TI.class).to(TC.class))
                .build();
        assertNotSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void implicitNoScope_SingletonAnnotation() {
        Injector injector = DIBootstrap
                .injectorBuilder(binder -> binder.bind(TI.class).to(TCSingleton.class))
                .build();
        assertSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void implicitNoScope_SingletonAnnotationKey() {
        Injector injector = DIBootstrap
                .injectorBuilder(binder -> binder.bind(TI.class).to(Key.get(TCSingleton.class)))
                .build();
        assertSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void implicitNoScope_NoScopeMethod() {
        Injector injector = DIBootstrap
                .injectorBuilder(new DefaultModule())
                .build();
        assertNotSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    @Test
    public void implicitNoScope_SingletonMethod() {
        Injector injector = DIBootstrap
                .injectorBuilder(new SingletonModule())
                .build();
        assertSame(injector.getInstance(TI.class), injector.getInstance(TI.class));
    }

    public interface TI {

    }

    public static class TC implements TI {

    }

    @Singleton
    public static class TCSingleton implements TI {

    }

    public static class DefaultModule implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Provides
        TI create() {
            return new TC();
        }
    }

    public static class SingletonModule implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @Provides
        @Singleton
        TI create() {
            return new TC();
        }
    }
}

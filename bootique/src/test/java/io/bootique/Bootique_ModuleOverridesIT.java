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

import io.bootique.annotation.Args;
import io.bootique.di.*;
import org.junit.jupiter.api.Test;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class Bootique_ModuleOverridesIT {

    private String[] args = new String[]{"a", "b", "c"};

    @Test
    public void createInjector_Overrides() {
        Injector i = Bootique.app(args)
                .override(BQCoreModule.class)
                .with(M0.class)
                .createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(M0.ARGS, args);
    }

    @Test
    public void createInjector_Overrides_Multi_Level() {
        Injector i = Bootique.app(args)
                .override(BQCoreModule.class).with(M0.class)
                .override(M0.class).with(M1.class)
                .createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(M1.ARGS, args);
    }

    @Test
    public void createInjector_OverridesWithProvider() {
        BQModuleProvider provider = () -> ModuleCrate.of(new M0()).overrides(BQCoreModule.class).build();

        Injector i = Bootique.app(args).moduleProvider(provider).createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(M0.ARGS, args);
    }

    @Test
    public void createInjector_Override_TwoLevelTree() {

        M5.configCalls = 0;
        M3.configCalls = 0;
        M4.configCalls = 0;

        Injector i = Bootique.app()
                .module(M3.class)
                .module(M4.class)
                .override(M3.class, M4.class).with(M5.class)
                .createInjector();

        assertEquals(1, M5.configCalls, "Overriding module is expected to be called once and only once");
        assertEquals(1, M3.configCalls, "Overriding module is expected to be called once and only once");
        assertEquals(1, M4.configCalls, "Overriding module is expected to be called once and only once");

        String s1 = i.getInstance(Key.get(String.class, S1.class));
        assertEquals("m5_s1", s1);

        String s2 = i.getInstance(Key.get(String.class, S2.class));
        assertEquals("m5_s2", s2);
    }

    @Test
    public void createInjector_Override_ThreeLevelTree() {

        M3.configCalls = 0;
        M4.configCalls = 0;
        M5.configCalls = 0;
        M6.configCalls = 0;
        M7.configCalls = 0;
        M8.configCalls = 0;
        M9.configCalls = 0;

        Injector i = Bootique.app()
                .module(M6.class)
                .module(M7.class)
                .module(M8.class)
                .module(M9.class)
                .override(M3.class, M4.class).with(M5.class)
                .override(M6.class, M7.class).with(M3.class)
                .override(M8.class, M9.class).with(M4.class)
                .createInjector();

        assertEquals(1, M3.configCalls);
        assertEquals(1, M4.configCalls);
        assertEquals(1, M5.configCalls);
        assertEquals(1, M6.configCalls);
        assertEquals(1, M7.configCalls);
        assertEquals(1, M8.configCalls);
        assertEquals(1, M9.configCalls);

        String s1 = i.getInstance(Key.get(String.class, S1.class));
        assertEquals("m5_s1", s1);

        String s2 = i.getInstance(Key.get(String.class, S2.class));
        assertEquals("m5_s2", s2);

        String s3 = i.getInstance(Key.get(String.class, S3.class));
        assertEquals("m3_s3", s3);

        String s4 = i.getInstance(Key.get(String.class, S4.class));
        assertEquals("m4_s4", s4);
    }

    @Test
    public void createInjector_Overrides_OriginalModuleServices() {
        Injector i = Bootique.app()
                .module(M2.class)
                .override(M2.class).with(SubM2.class)
                .createInjector();

        String s2 = i.getInstance(Key.get(String.class, S2.class));
        assertEquals("sub_m2_s2_m2_s1", s2);
    }

    @Test
    public void createInjector_Overrides_Multi_Level_OriginalModuleServices() {
        Injector i = Bootique.app()
                .module(M2.class)
                .override(M2.class).with(SubM2.class)
                .override(SubM2.class).with(SubSubM2.class)
                .createInjector();

        String s2 = i.getInstance(Key.get(String.class, S2.class));
        assertEquals("sub_sub_m2_s2_m2_s1", s2);
    }


    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @interface S1 {

    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @interface S2 {

    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @interface S3 {

    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier
    @interface S4 {

    }

    static class M0 implements BQModule {

        static String[] ARGS = {"1", "2", "3"};

        @Override
        public void configure(Binder binder) {
            binder.bind(String[].class, Args.class).toInstance(ARGS);
        }
    }

    static class M1 implements BQModule {

        static String[] ARGS = {"x", "y", "z"};

        @Override
        public void configure(Binder binder) {
            binder.bind(String[].class, Args.class).toInstance(ARGS);
        }
    }

    static class M2 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @S1
        @Provides
        @Singleton
        String getS1() {
            return "m2_s1";
        }
    }

    static class SubM2 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @S2
        @Provides
        @Singleton
        String getS2(@S1 String s1) {
            return "sub_m2_s2_" + s1;
        }
    }

    static class SubSubM2 implements BQModule {

        @Override
        public void configure(Binder binder) {
        }

        @S2
        @Provides
        @Singleton
        String getS2(@S1 String s1) {
            return "sub_sub_m2_s2_" + s1;
        }
    }

    static class M3 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S1
        @Provides
        @Singleton
        String getS1() {
            return "m3_s1";
        }

        @S3
        @Provides
        @Singleton
        String getS3() {
            return "m3_s3";
        }
    }

    static class M4 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S2
        @Provides
        @Singleton
        String getS2() {
            return "m4_s2";
        }

        @S4
        @Provides
        @Singleton
        String getS4() {
            return "m4_s4";
        }
    }

    static class M5 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S1
        @Provides
        @Singleton
        String getS1() {
            return "m5_s1";
        }

        @S2
        @Provides
        @Singleton
        String getS2() {
            return "m5_s2";
        }
    }

    static class M6 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S1
        @Provides
        @Singleton
        String getS1() {
            return "m6_s1";
        }
    }

    static class M7 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S3
        @Provides
        @Singleton
        String getS3() {
            return "m7_s3";
        }
    }

    static class M8 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S2
        @Provides
        @Singleton
        String getS2() {
            return "m8_s2";
        }
    }

    static class M9 implements BQModule {

        static int configCalls;

        @Override
        public void configure(Binder binder) {
            configCalls++;
        }

        @S4
        @Provides
        @Singleton
        String getS4() {
            return "m9_s4";
        }
    }
}

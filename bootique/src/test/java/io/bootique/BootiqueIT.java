/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique;

import com.google.inject.Binder;
import com.google.inject.BindingAnnotation;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.annotation.Args;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.it.ItestModuleProvider;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class BootiqueIT {

    private String[] args = new String[]{"a", "b", "c"};

    @Test
    public void testExec() {
        CommandOutcome outcome = Bootique.app(args).exec();
        assertTrue(outcome.isSuccess());
    }

    @Test
    public void testExec_Failure() {
        CommandOutcome outcome = Bootique.app("-a").module(b ->
                BQCoreModule.extend(b).addCommand(new Command() {
                    @Override
                    public CommandOutcome run(Cli cli) {
                        return CommandOutcome.failed(-1, "it failed");
                    }

                    @Override
                    public CommandMetadata getMetadata() {
                        return CommandMetadata.builder("acommand").build();
                    }
                })
        ).exec();

        assertFalse(outcome.isSuccess());
        assertEquals(-1, outcome.getExitCode());
        assertEquals("it failed", outcome.getMessage());
    }

    @Test
    public void testExec_Exception() {
        CommandOutcome outcome = Bootique.app("-a").module(b ->
                BQCoreModule.extend(b).addCommand(new Command() {
                    @Override
                    public CommandOutcome run(Cli cli) {
                        throw new RuntimeException("test exception");
                    }

                    @Override
                    public CommandMetadata getMetadata() {
                        return CommandMetadata.builder("acommand").build();
                    }
                })
        ).exec();

        assertFalse(outcome.isSuccess());
        assertEquals(1, outcome.getExitCode());
        assertNotNull(outcome.getException());
        assertEquals("test exception", outcome.getException().getMessage());
    }

    @Test
    public void testAutoLoadedProviders() {
        Collection<BQModuleProvider> autoLoaded = Bootique.app(args).autoLoadedProviders();

        assertEquals(1, autoLoaded.size());
        autoLoaded.forEach(m -> assertTrue(m instanceof ItestModuleProvider));
    }

    @Test
    public void testCreateInjector() {
        Injector i = Bootique.app(args).createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(this.args, args);
    }

    @Test
    public void testApp_Collection() {
        Injector i = Bootique.app(asList(args)).createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertArrayEquals(this.args, args);
    }

    @Test
    public void testCreateInjector_Overrides() {
        Injector i = Bootique.app(args).override(BQCoreModule.class).with(M0.class).createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(M0.ARGS, args);
    }

    @Test
    public void testCreateInjector_Overrides_Multi_Level() {
        Injector i = Bootique.app(args)
                .override(BQCoreModule.class).with(M0.class)
                .override(M0.class).with(M1.class)
                .createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(M1.ARGS, args);
    }

    @Test
    public void testCreateInjector_Overrides_OriginalModuleServices() {
        Injector i = Bootique.app(args)
                .module(M2.class)
                .override(M2.class).with(SubM2.class)
                .createInjector();

        String s2 = i.getInstance(Key.get(String.class, S2.class));
        assertEquals("sub_m2_s2_m2_s1", s2);
    }

    @Test
    public void testCreateInjector_Overrides_Multi_Level_OriginalModuleServices() {
        Injector i = Bootique.app(args)
                .module(M2.class)
                .override(M2.class).with(SubM2.class)
                .override(SubM2.class).with(SubSubM2.class)
                .createInjector();

        String s2 = i.getInstance(Key.get(String.class, S2.class));
        assertEquals("sub_sub_m2_s2_m2_s1", s2);
    }

    @Test
    public void testCreateInjector_OverridesWithProvider() {
        BQModuleProvider provider = new BQModuleProvider() {

            @Override
            public Module module() {
                return new M0();
            }

            @Override
            public Collection<Class<? extends Module>> overrides() {
                return Collections.singleton(BQCoreModule.class);
            }
        };

        Injector i = Bootique.app(args).module(provider).createInjector();

        String[] args = i.getInstance(Key.get(String[].class, Args.class));
        assertSame(M0.ARGS, args);
    }

    static class M0 implements Module {

        static String[] ARGS = {"1", "2", "3"};

        @Override
        public void configure(Binder binder) {
            binder.bind(String[].class).annotatedWith(Args.class).toInstance(ARGS);
        }
    }

    static class M1 implements Module {

        static String[] ARGS = {"x", "y", "z"};

        @Override
        public void configure(Binder binder) {
            binder.bind(String[].class).annotatedWith(Args.class).toInstance(ARGS);
        }
    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    @interface S1 {

    }

    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @BindingAnnotation
    @interface S2 {

    }

    static class M2 implements Module {

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

    static class SubM2 implements Module {

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

    static class SubSubM2 implements Module {

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
}

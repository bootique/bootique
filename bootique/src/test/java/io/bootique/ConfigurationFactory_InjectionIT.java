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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Key;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.type.TypeRef;
import io.bootique.unit.TestAppManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigurationFactory_InjectionIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    private BQRuntime withModule(Class<? extends BQModule> moduleType) {
        return appManager.runtime(Bootique
                .app("-c", "classpath:io/bootique/ConfigurationFactory_InjectionIT.yml")
                .autoLoadModules()
                .module(moduleType));
    }

    @Test
    public void fieldInjection() {
        S1 s1 = withModule(M1FieldInjection.class).getInstance(S1.class);
        assertEquals("[S3]_S1A", s1.get());
    }

    @Test
    public void constructorInjectionBootique() {
        S2 s2 = withModule(M2ConstructorInjectionBootique.class).getInstance(S2.class);
        assertEquals("[S3]_S2A", s2.get());
    }

    @Test
    public void constructorInjectionBootique_NoParamsConstructor() {
        S3 s3 = withModule(M3ConstructorInjectionBootique.class).getInstance(S3.class);
        assertEquals("S3A", s3.get());
    }

    @Test
    public void constructorInjectionJackson_JsonCreatorConstructor() {
        S4 s4 = withModule(M4ConstructorInjectionJackson.class).getInstance(S4.class);
        assertEquals("S4A", s4.get());
    }

    @Test
    public void constructorInjectionJackson_JsonPropertyConstructor() {
        S5 s5 = withModule(M5ConstructorInjectionJackson.class).getInstance(S5.class);
        assertEquals("S5A", s5.get());
    }

    @Test
    public void constructorInjectionJackson_JsonCreatorConstructor_FieldInjection() {
        S9 s9 = withModule(M9ConstructorInjectionJacksonFieldInjection.class).getInstance(S9.class);
        assertEquals("[S0]_S9A", s9.get());
    }

    @Test
    public void typeRefInjection() {
        Map<String, S6> map = withModule(M6TypeRefInjection.class).getInstance(
                Key.get(TypeLiteral.of(M6TypeRefInjection.typeRef.getType())));
        assertEquals(Set.of("k1"), map.keySet());
        assertEquals("[S0]_S6A", map.get("k1").get());
    }

    @Test
    public void nestedInjection() {
        S7 s7 = withModule(M7NestedInjection.class).getInstance(S7.class);
        assertEquals("[S0]_[S0]_S8A", s7.get());
    }

    static class M1FieldInjection implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S3]"));
        }

        @Singleton
        @Provides
        S1 provideS1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S1.class, "s1");
        }
    }

    static class M2ConstructorInjectionBootique implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S3]"));
        }

        @Singleton
        @Provides
        S2 provideS2(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S2.class, "s2");
        }
    }

    static class M3ConstructorInjectionBootique implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Singleton
        @Provides
        S3 provideS3(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S3.class, "s3");
        }
    }

    static class M4ConstructorInjectionJackson implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Singleton
        @Provides
        S4 provideS4(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S4.class, "s4");
        }
    }

    static class M5ConstructorInjectionJackson implements BQModule {
        @Override
        public void configure(Binder binder) {
        }

        @Singleton
        @Provides
        S5 provideS5(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S5.class, "s5");
        }
    }

    static class M6TypeRefInjection implements BQModule {

        static final TypeRef<Map<String, S6>> typeRef = new TypeRef<>() {
        };

        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S0]"));
        }

        @Singleton
        @Provides
        Map<String, S6> provideS6Map(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(typeRef, "s6");
        }
    }

    static class M7NestedInjection implements BQModule {

        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S0]"));
        }

        @Singleton
        @Provides
        S7 provideS7(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S7.class, "s7");
        }
    }

    static class M9ConstructorInjectionJacksonFieldInjection implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S0]"));
        }

        @Singleton
        @Provides
        S9 provideS9(ConfigurationFactory configFactory) {
            return configFactory.config(S9.class, "s9");
        }
    }

    static class S0 {
        final String val;

        S0(String val) {
            this.val = val;
        }
    }

    static class S1 {

        @Inject
        private S0 s0;
        private String a;

        public void setA(String a) {
            this.a = a;
        }

        public String get() {
            assertNotNull(s0, "'s0' was not injected");
            return s0.val + "_" + a;
        }
    }

    static class S2 {

        private final S0 s0;
        private String a;

        @Inject
        public S2(S0 s0) {
            this.s0 = s0;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String get() {
            assertNotNull(s0, "'s0' was not injected");
            return s0.val + "_" + a;
        }
    }

    static class S3 {

        private String a;

        @Inject
        public S3() {
        }

        public void setA(String a) {
            this.a = a;
        }

        public String get() {
            return a;
        }
    }

    static class S4 {

        private final String a;

        @JsonCreator
        public S4(String a) {
            this.a = a;
        }

        public String get() {
            return a;
        }
    }

    static class S5 {

        private final String a;

        public S5(@JsonProperty("a") String a) {
            this.a = a;
        }

        public String get() {
            return a;
        }
    }

    // as a config object nested in type ref, the only way to enable injection is to annotate it with @BQConfig
    @BQConfig
    static class S6 {

        @Inject
        private S0 s0;
        private String a;

        public void setA(String a) {
            this.a = a;
        }

        public String get() {
            assertNotNull(s0, "'s0' was not injected");
            return s0.val + "_" + a;
        }
    }

    static class S7 {

        @Inject
        private S0 s0;
        private S8 s8;

        @BQConfigProperty
        public S7 setS8(S8 s8) {
            this.s8 = s8;
            return this;
        }

        public String get() {
            assertNotNull(s0, "'s0' was not injected");
            String suffix = s8 != null ? s8.get() : "";
            return s0.val + "_" + suffix;
        }
    }

    // as a nested config object, the only way to enable injection is to annotate it with @BQConfig
    @BQConfig
    static class S8 {

        @Inject
        private S0 s0;
        private String a;

        public void setA(String a) {
            this.a = a;
        }

        public String get() {
            assertNotNull(s0, "'s0' was not injected");
            return s0.val + "_" + a;
        }
    }

    static class S9 {

        @Inject
        private S0 s0;

        private final String a;

        @JsonCreator
        public S9(String a) {
            this.a = a;
        }

        public String get() {
            assertNotNull(s0, "'s0' was not injected");
            return s0.val + "_" + a;
        }
    }
}

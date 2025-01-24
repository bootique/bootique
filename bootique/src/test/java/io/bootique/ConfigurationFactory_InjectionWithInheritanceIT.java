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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.unit.TestAppManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationFactory_InjectionWithInheritanceIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    private BQRuntime withModule(Class<? extends BQModule> moduleType) {
        return appManager.runtime(Bootique
                .app("-c", "classpath:io/bootique/ConfigurationFactory_InjectionWithInheritanceIT.yml")
                .autoLoadModules()
                .module(moduleType));
    }

    @Test
    public void fieldInjectionSuper() {
        S1 s1 = withModule(M1Super.class).getInstance(S1.class);

        assertSame(S1.class, s1.getClass());
        assertEquals("[S0]_S1A", s1.get());
    }

    @Test
    public void fieldInjectionSub1() {
        S1 s1 = withModule(M1Sub1.class).getInstance(S1.class);

        assertSame(S1Sub1.class, s1.getClass());
        assertEquals("[S0]_S1Sub1A_sub1_[S2]_S1Sub1B", s1.get());
    }

    @Test
    public void fieldInjectionSub2() {
        S1 s1 = withModule(M1Sub2.class).getInstance(S1.class);

        assertSame(S1Sub2.class, s1.getClass());
        assertEquals("[S0]_S1Sub2A_sub2_S1Sub2C", s1.get());
    }

    @Test
    public void constructorInjectionSuper() {
        C1 c1 = withModule(M1Super.class).getInstance(C1.class);

        assertSame(C1.class, c1.getClass());
        assertEquals("[S0]_S1A", c1.get());
    }

    @Test
    public void constructorInjectionSub1() {
        C1 c1 = withModule(M1Sub1.class).getInstance(C1.class);

        assertSame(C1Sub1.class, c1.getClass());
        assertEquals("[S0]_S1Sub1A_sub1_[S2]_S1Sub1B", c1.get());
    }

    @Test
    public void constructorInjectionSub2() {
        C1 c1 = withModule(M1Sub2.class).getInstance(C1.class);

        assertSame(C1Sub2.class, c1.getClass());
        assertEquals("[S0]_S1Sub2A_sub2_S1Sub2C", c1.get());
    }

    @Test
    public void nestedInjectionSuper() {
        S1 s1 = withModule(M1Super.class).getInstance(S3.class).getS1();

        assertSame(S1.class, s1.getClass());
        assertEquals("[S0]_S1A", s1.get());
    }

    @Test
    public void nestedInjectionSub1() {
        S1 s1 = withModule(M1Sub1.class).getInstance(S3.class).getS1();

        assertSame(S1Sub1.class, s1.getClass());
        assertEquals("[S0]_S1Sub1A_sub1_[S2]_S1Sub1B", s1.get());
    }

    @Test
    public void nestedInjectionSub2() {
        S1 s1 = withModule(M1Sub2.class).getInstance(S3.class).getS1();

        assertSame(S1Sub2.class, s1.getClass());
        assertEquals("[S0]_S1Sub2A_sub2_S1Sub2C", s1.get());
    }

    @Test
    public void constructorInjectionSuper_Jackson_JsonCreatorConstructor() {
        S4 s4 = withModule(M1Super.class).getInstance(S4.class);

        assertSame(S4.class, s4.getClass());
        assertEquals("S4A", s4.get());
    }

    @Test
    public void constructorInjectionSub_Jackson_JsonCreatorConstructor() {
        S4 s4 = withModule(M1Sub1.class).getInstance(S4.class);

        assertSame(S4Sub.class, s4.getClass());
        assertEquals("S4A_s4sub_S4SubB", s4.get());
    }

    @Test
    public void constructorInjectionSuper_Jackson_JsonPropertyConstructor() {
        S5 s5 = withModule(M1Super.class).getInstance(S5.class);

        assertSame(S5.class, s5.getClass());
        assertEquals("S5A", s5.get());
    }

    @Test
    public void constructorInjectionSub_Jackson_JsonPropertyConstructor() {
        S5 s5 = withModule(M1Sub1.class).getInstance(S5.class);

        assertSame(S5Sub.class, s5.getClass());
        assertEquals("S5A_s5sub_S5SubB", s5.get());
    }

    static class M1Super implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S0]"));
        }

        @Singleton
        @Provides
        S1 provideS1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S1.class, "s1");
        }

        @Singleton
        @Provides
        C1 provideC1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(C1.class, "s1");
        }

        @Singleton
        @Provides
        S3 provideS3(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S3.class, "s3");
        }

        @Singleton
        @Provides
        S4 provideS4(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S4.class, "s4");
        }

        @Singleton
        @Provides
        S5 provideS5(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S5.class, "s5");
        }
    }

    static class M1Sub1 implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S0]"));
            binder.bind(S2.class).toInstance(new S2("[S2]"));
        }

        @Singleton
        @Provides
        S1 provideS1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S1.class, "s1sub1");
        }

        @Singleton
        @Provides
        C1 provideC1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(C1.class, "s1sub1");
        }

        @Singleton
        @Provides
        S3 provideS3(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S3.class, "s3sub1");
        }

        @Singleton
        @Provides
        S4 provideS4(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S4.class, "s4sub");
        }

        @Singleton
        @Provides
        S5 provideS5(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S5.class, "s5sub");
        }
    }

    static class M1Sub2 implements BQModule {
        @Override
        public void configure(Binder binder) {
            binder.bind(S0.class).toInstance(new S0("[S0]"));
        }

        @Singleton
        @Provides
        S1 provideS1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S1.class, "s1sub2");
        }

        @Singleton
        @Provides
        C1 provideC1(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(C1.class, "s1sub2");
        }

        @Singleton
        @Provides
        S3 provideS3(ConfigurationFactory configurationFactory) {
            return configurationFactory.config(S3.class, "s3sub2");
        }
    }

    static class S0 {
        final String val;

        S0(String val) {
            this.val = val;
        }
    }

    static class S2 {
        final String val;

        S2(String val) {
            this.val = val;
        }
    }

    static class S3 {

        S1 s1;

        public S3 setS1(S1 s1) {
            this.s1 = s1;
            return this;
        }

        public S1 getS1() {
            return Objects.requireNonNull(s1, "'s1' is not injected");
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = S1.class)
    static class S1 implements PolymorphicConfiguration {

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

    @JsonTypeName("sub1")
    static class S1Sub1 extends S1 {

        private String b;

        @Inject
        private S2 s2;

        public S1Sub1 setB(String b) {
            this.b = b;
            return this;
        }

        public String get() {
            assertNotNull(s2, "'s2' was not injected");
            return super.get() + "_sub1_" + s2.val + "_" + b;
        }
    }

    @JsonTypeName("sub2")
    static class S1Sub2 extends S1 {

        private String c;

        public S1Sub2 setC(String c) {
            this.c = c;
            return this;
        }

        public String get() {
            return super.get() + "_sub2_" + c;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = C1.class)
    static class C1 implements PolymorphicConfiguration {

        private final S0 s0;
        private String a;

        @Inject
        public C1(S0 s0) {
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

    @JsonTypeName("sub1")
    static class C1Sub1 extends C1 {

        private final S2 s2;
        private String b;

        @Inject
        public C1Sub1(S0 s0, S2 s2) {
            super(s0);
            this.s2 = s2;
        }

        public C1Sub1 setB(String b) {
            this.b = b;
            return this;
        }

        public String get() {
            assertNotNull(s2, "'s2' was not injected");
            return super.get() + "_sub1_" + s2.val + "_" + b;
        }
    }

    @JsonTypeName("sub2")
    static class C1Sub2 extends C1 {

        private String c;

        @Inject
        public C1Sub2(S0 s0) {
            super(s0);
        }

        public C1Sub2 setC(String c) {
            this.c = c;
            return this;
        }

        public String get() {
            return super.get() + "_sub2_" + c;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = S4.class)
    static class S4 implements PolymorphicConfiguration {

        private final String a;


        public S4(String a) {
            this.a = a;
        }

        public String get() {
            return a;
        }
    }

    @JsonTypeName("sub")
    static class S4Sub extends S4 {

        private final String b;

        @JsonCreator
        public S4Sub(@JsonProperty("a") String a, @JsonProperty("b") String b) {
            super(a);
            this.b = b;
        }

        @Override
        public String get() {
            return super.get() + "_s4sub_" + b;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = S5.class)
    static class S5 implements PolymorphicConfiguration {

        private final String a;

        public S5(@JsonProperty("a") String a) {
            this.a = a;
        }

        public String get() {
            return a;
        }
    }

    @JsonTypeName("sub")
    static class S5Sub extends S5 {

        private final String b;

        public S5Sub(@JsonProperty("a") String a, @JsonProperty("b") String b) {
            super(a);
            this.b = b;
        }

        @Override
        public String get() {
            return super.get() + "_s5sub_" + b;
        }
    }
}

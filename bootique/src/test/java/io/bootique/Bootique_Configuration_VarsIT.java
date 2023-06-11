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

import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class Bootique_Configuration_VarsIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    private BQRuntime app(Consumer<Binder> customizer) {
        Bootique app = Bootique.app()
                .autoLoadModules()
                .module(customizer::accept);
        return appManager.runtime(app);
    }

    @Test
    public void testOverrideValue() {
        BQRuntime runtime = app(b -> BQCoreModule.extend(b)
                .addConfig("classpath:io/bootique/Bootique_Configuration_VarsIT.yml")
                .declareVar("testOverrideValue.c.m.l", "MY_VAR")
                .setVar("MY_VAR", "myValue"));

        O1 b1 = runtime.getInstance(ConfigurationFactory.class).config(O1.class, "testOverrideValue");
        assertEquals("myValue", b1.c.m.l);
    }

    @Test
    public void testOverrideValueArray() {
        BQRuntime runtime = app(b -> BQCoreModule.extend(b)
                .addConfig("classpath:io/bootique/Bootique_Configuration_VarsIT.yml")
                .declareVar("testOverrideValueArray.h[1]", "MY_VAR")
                .setVar("MY_VAR", "J"));

        O5 b = runtime.getInstance(ConfigurationFactory.class).config(O5.class, "testOverrideValueArray");

        assertEquals("i", b.h.get(0));
        assertEquals("J", b.h.get(1));
        assertEquals("k", b.h.get(2));
    }

    @Test
    public void testDeclareVar_ConfigPathCaseSensitivity() {
        BQRuntime runtime = app(b -> BQCoreModule.extend(b)
                .declareVar("m.propCamelCase", "MY_VAR")
                .setVar("MY_VAR", "myValue"));

        O4 b4 = runtime.getInstance(ConfigurationFactory.class).config(O4.class, "");
        assertNotNull(b4.m, "Map did not resolve");
        assertEquals("myValue", b4.m.get("propCamelCase"), "Unexpected map contents: " + b4.m);
    }

    @Test
    public void testDeclareVar_NameCaseSensitivity() {
        BQRuntime runtime = app(b -> BQCoreModule.extend(b)
                .declareVar("m.propCamelCase", "MY_VAR")
                .setVar("my_var", "myValue"));

        O4 b4 = runtime.getInstance(ConfigurationFactory.class).config(O4.class, "");
        assertNull(b4.m);
    }

    @Test
    public void testDeclareVar_SameVarTwoPaths() {
        BQRuntime runtime = app(b -> BQCoreModule.extend(b)
                .declareVar("p1", "MY_VAR")
                .declareVar("p3", "MY_VAR")
                .setVar("MY_VAR", "myValue"));

        O6 o6 = runtime.getInstance(ConfigurationFactory.class).config(O6.class, "");

        assertEquals("myValue", o6.p1);
        assertNull(o6.p2);
        assertEquals("myValue", o6.p3);
    }

    @Test
    public void testDeclareVar_SameVarTwoIdenticalPaths() {
        BQRuntime runtime = app(b -> BQCoreModule.extend(b)
                .declareVar("p1", "MY_VAR")
                .declareVar("p1", "MY_VAR")
                .setVar("MY_VAR", "myValue"));

        O6 o6 = runtime.getInstance(ConfigurationFactory.class).config(O6.class, "");

        assertEquals("myValue", o6.p1);
        assertNull(o6.p2);
        assertNull(o6.p3);
    }

    static class O1 {
        private String a;
        private O2 c;

        public void setA(String a) {
            this.a = a;
        }

        public void setC(O2 c) {
            this.c = c;
        }
    }

    static class O2 {
        private O3 m;

        public void setM(O3 m) {
            this.m = m;
        }
    }

    static class O3 {
        private String k;
        private String f;
        private String l;

        public void setK(String k) {
            this.k = k;
        }

        public void setF(String f) {
            this.f = f;
        }

        public void setL(String l) {
            this.l = l;
        }
    }

    static class O4 {
        private Map<String, String> m;

        public void setM(Map<String, String> m) {
            this.m = m;
        }
    }

    static class O5 {
        private List<String> h;

        public void setH(List<String> h) {
            this.h = h;
        }
    }

    static class O6 {
        private String p1;
        private String p2;
        private String p3;


        public O6 setP1(String p1) {
            this.p1 = p1;
            return this;
        }

        public O6 setP2(String p2) {
            this.p2 = p2;
            return this;
        }

        public O6 setP3(String p3) {
            this.p3 = p3;
            return this;
        }
    }
}

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
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class Bootique_Configuration_PropertiesIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private BQInternalTestFactory.Builder app() {
        return testFactory.app("--config=classpath:io/bootique/Bootique_Configuration_PropertiesIT.yml");
    }

    @Test
    public void testOverride() {
        BQRuntime runtime = app()
                .property("bq.testOverride.c", "D")
                .createRuntime();

        TestOverrideBean b = runtime
                .getInstance(ConfigurationFactory.class)
                .config(TestOverrideBean.class, "testOverride");

        assertEquals("b", b.a);
        assertEquals("D", b.c);
    }

    @Test
    public void testOverrideNested() {
        BQRuntime runtime = app()
                .property("bq.testOverrideNested.m.z", "2")
                .createRuntime();

        TestOverrideNestedBean b = runtime.getInstance(ConfigurationFactory.class)
                .config(TestOverrideNestedBean.class, "testOverrideNested");

        assertEquals("y", b.m.x);
        assertEquals(2, b.m.z);
    }

    @Test
    public void testOverrideValueArray() {
        BQRuntime runtime = app()
                .property("bq.testOverrideValueArray.h[1]", "J")
                .createRuntime();

        TestOverrideValueArrayBean b = runtime.getInstance(ConfigurationFactory.class)
                .config(TestOverrideValueArrayBean.class, "testOverrideValueArray");

        assertEquals("i", b.h.get(0));
        assertEquals("J", b.h.get(1));
        assertEquals("k", b.h.get(2));
    }

    @Test
    public void testOverrideValueArray_Empty() {
        BQRuntime runtime = app()
                .module(b -> BQCoreModule.extend(b)
                        // calling 'setProperty' on BQCOreModule instead of BQInternalTestFactory,
                        // as the real ordering is important
                        .setProperty("bq.testOverrideValueArrayEmpty.h[0]", "J")
                        .setProperty("bq.testOverrideValueArrayEmpty.h[1]", "A")
                        .setProperty("bq.testOverrideValueArrayEmpty.h[2]", "Z"))
                .createRuntime();

        TestOverrideValueArrayBean b = runtime.getInstance(ConfigurationFactory.class)
                .config(TestOverrideValueArrayBean.class, "testOverrideValueArrayEmpty");

        assertEquals(3, b.h.size());
        assertEquals("J", b.h.get(0));
        assertEquals("A", b.h.get(1));
        assertEquals("Z", b.h.get(2));
    }

    @Test
    public void testOverrideObjectArray() {
        BQRuntime runtime = app()
                .property("bq.testOverrideObjectArray.d[1].e", "20")
                .createRuntime();

        TestOverrideObjectArrayBean b = runtime.getInstance(ConfigurationFactory.class)
                .config(TestOverrideObjectArrayBean.class, "testOverrideObjectArray");

        assertEquals(1, b.d.get(0).e);
        assertEquals(20, b.d.get(1).e);
    }

    @Test
    public void testOverrideObjectArray_AddValue() {
        BQRuntime runtime = app()
                // appending value at the end...
                .property("bq.testOverrideObjectArray.d[2].e", "3")
                .createRuntime();

        TestOverrideObjectArrayBean b = runtime.getInstance(ConfigurationFactory.class)
                .config(TestOverrideObjectArrayBean.class, "testOverrideObjectArray");

        assertEquals(1, b.d.get(0).e);
        assertEquals(2, b.d.get(1).e);
        assertEquals(3, b.d.get(2).e);
    }

    static class TestOverrideBean {
        private String a;
        private String c;


        public void setA(String a) {
            this.a = a;
        }

        public void setC(String c) {
            this.c = c;
        }
    }

    static class TestOverrideNestedBean {
        private TestOverrideNestedBeanM m;

        public void setM(TestOverrideNestedBeanM m) {
            this.m = m;
        }
    }

    static class TestOverrideNestedBeanM {
        private String x;
        private int z;

        public void setX(String x) {
            this.x = x;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }

    static class TestOverrideValueArrayBean {
        private List<String> h;

        public void setH(List<String> h) {
            this.h = h;
        }
    }

    static class TestOverrideObjectArrayBean {
        private List<TestOverrideObjectArrayBeanD> d;

        public void setD(List<TestOverrideObjectArrayBeanD> d) {
            this.d = d;
        }
    }

    static class TestOverrideObjectArrayBeanD {
        private int e;

        public void setE(int e) {
            this.e = e;
        }
    }
}

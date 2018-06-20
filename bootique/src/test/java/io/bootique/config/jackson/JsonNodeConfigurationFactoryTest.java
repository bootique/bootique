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

package io.bootique.config.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.resource.ResourceFactory;
import io.bootique.type.TypeRef;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonNodeConfigurationFactoryTest {

    private JsonNodeConfigurationFactory factory(String yaml) {
        JsonNode rootConfig = YamlReader.read(yaml);
        return new JsonNodeConfigurationFactory(rootConfig, new ObjectMapper());
    }

    @Test
    public void testConfig() {

        Bean1 b1 = factory("s: SS\ni: 55").config(Bean1.class, "");
        assertNotNull(b1);
        assertEquals("SS", b1.getS());
        assertEquals(55, b1.getI());
    }

    @Test
    public void testConfig_Nested() {
        Bean2 b2 = factory("b1:\n  s: SS\n  i: 55").config(Bean2.class, "");
        assertNotNull(b2);
        assertNotNull(b2.getB1());
        assertEquals("SS", b2.getB1().getS());
        assertEquals(55, b2.getB1().getI());
    }

    @Test
    public void testConfig_Subconfig() {
        Bean1 b1 = factory("b1:\n  s: SS\n  i: 55").config(Bean1.class, "b1");
        assertNotNull(b1);
        assertEquals("SS", b1.getS());
        assertEquals(55, b1.getI());
    }

    @Test
    public void testConfig_Subconfig_MultiLevel() {

        Bean1 b1 = factory("b0:\n  b1:\n    s: SS\n    i: 55").config(Bean1.class, "b0.b1");
        assertNotNull(b1);
        assertEquals("SS", b1.getS());
        assertEquals(55, b1.getI());
    }

    @Test
    public void testConfig_Subconfig_Missing() {
        Bean1 b1 = factory("b1:\n  s: SS\n  i: 55").config(Bean1.class, "no.such.path");
        assertNotNull(b1);
        assertEquals(null, b1.getS());
        assertEquals(0, b1.getI());
    }

    @Test
    public void testList_SingleLevel() {

        List<Object> l = factory("- SS\n- 55").config(new TypeRef<List<Object>>() {
        }, "");

        assertNotNull(l);
        assertEquals("SS", l.get(0));
        assertEquals(55, l.get(1));
    }

    @Test
    public void testList_MultiLevel() {

        List<List<Object>> l = factory("-\n  - SS\n  - 55\n-\n  - X")
                .config(new TypeRef<List<List<Object>>>() {
                }, "");

        assertNotNull(l);
        assertEquals(2, l.size());

        List<Object> sl1 = l.get(0);
        assertEquals(2, sl1.size());
        assertEquals("SS", sl1.get(0));
        assertEquals(55, sl1.get(1));

        List<Object> sl2 = l.get(1);
        assertEquals(1, sl2.size());
        assertEquals("X", sl2.get(0));
    }

    @Test
    public void testMap_SingleLevel() {
        Map<String, Object> m = factory("b1: SS\ni: 55").config(new TypeRef<Map<String, Object>>() {
        }, "");

        assertNotNull(m);
        assertEquals("SS", m.get("b1"));
        assertEquals(55, m.get("i"));
    }

    @Test
    public void testMap_MultiLevel() {

        Map<String, Map<String, Object>> m = factory("b1:\n  k1: SS\n  i: 55")
                .config(new TypeRef<Map<String, Map<String, Object>>>() {
                }, "");

        assertNotNull(m);
        Map<String, Object> subM = m.get("b1");
        assertNotNull(subM);

        assertEquals("SS", subM.get("k1"));
        assertEquals(55, subM.get("i"));
    }

    @Test
    public void testConfig_Polimorphic_Super() {

        BeanSuper b1 = factory("type: sup1").config(BeanSuper.class, "");
        assertEquals(BeanSuper.class, b1.getClass());
    }

    @Test
    public void testConfig_Polimorphic_Sub1() {

        BeanSuper b1 = factory("type: sub1\np1: p111").config(BeanSuper.class, "");
        assertEquals(BeanSub1.class, b1.getClass());
        assertEquals("p111", ((BeanSub1) b1).getP1());
    }

    @Test
    public void testConfig_Polimorphic_Sub2() {

        BeanSuper b1 = factory("type: sub2\np2: p222").config(BeanSuper.class, "");
        assertEquals(BeanSub2.class, b1.getClass());
        assertEquals("p222", ((BeanSub2) b1).getP2());
    }

    @Test
    public void testConfig_ResourceFactory() throws IOException {

        ResourceFactoryHolder rfh = factory("resourceFactory: classpath:io/bootique/config/resourcefactory.txt")
                .config(ResourceFactoryHolder.class, "");
        assertNotNull(rfh);
        assertNotNull(rfh.resourceFactory);

        try (Scanner scanner = new Scanner(rfh.resourceFactory.getUrl().openStream(), "UTF-8")) {
            assertEquals("resource factory worked!", scanner.useDelimiter("\\Z").nextLine());
        }
    }

    public static class Bean1 {

        private String s;
        private int i;

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }
    }

    public static class Bean2 {

        private Bean1 b1;

        public Bean1 getB1() {
            return b1;
        }

        public void setB1(Bean1 b1) {
            this.b1 = b1;
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonTypeName("sup1")
    @JsonSubTypes(value = {@JsonSubTypes.Type(value = BeanSub1.class), @JsonSubTypes.Type(value = BeanSub2.class)})
    public static class BeanSuper {

    }

    @JsonTypeName("sub1")
    public static class BeanSub1 extends BeanSuper {

        private String p1;

        public String getP1() {
            return p1;
        }

        public void setP1(String p1) {
            this.p1 = p1;
        }
    }

    @JsonTypeName("sub2")
    public static class BeanSub2 extends BeanSuper {

        private String p2;

        public void setP1(String p2) {
            this.p2 = p2;
        }

        public String getP2() {
            return p2;
        }
    }

    public static class ResourceFactoryHolder {
        private ResourceFactory resourceFactory;

        public void setResourceFactory(ResourceFactory resourceFactory) {
            this.resourceFactory = resourceFactory;
        }
    }
}

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

package io.bootique.meta.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.meta.MetadataNode;
import io.bootique.type.TypeRef;
import org.junit.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfigMetadataCompilerTest {

    private ConfigMetadataCompiler createCompiler() {
        return createCompiler(t -> Stream.empty());
    }

    private ConfigMetadataCompiler createCompiler(Function<Class<?>, Stream<Class<?>>> subclassProvider) {
        return new ConfigMetadataCompiler(subclassProvider, Collections.emptyMap());
    }

    @Test
    public void testCompile() {

        ConfigObjectMetadata md = (ConfigObjectMetadata) createCompiler().compile("prefix", Config1.class);
        assertNotNull(md);

        assertEquals("prefix", md.getName());
        assertEquals("Describes Config1", md.getDescription());
        assertEquals(Config1.class, md.getType());

        assertEquals(10, md.getProperties().size());

        Map<String, ConfigMetadataNode> propMap = md
                .getProperties()
                .stream()
                .collect(Collectors.toMap(MetadataNode::getName, Function.identity()));

        ConfigValueMetadata config1 = (ConfigValueMetadata) propMap.get("config1");
        assertEquals(String.class, config1.getType());
        assertEquals("constructor with params", config1.getDescription());

        ConfigValueMetadata p1 = (ConfigValueMetadata) propMap.get("p1");
        assertEquals(Integer.TYPE, p1.getType());
        assertNull(p1.getDescription());

        ConfigValueMetadata p2 = (ConfigValueMetadata) propMap.get("p2");
        assertEquals(BigDecimal.class, p2.getType());
        assertEquals("description of p2", p2.getDescription());

        ConfigMetadataNode p3 = propMap.get("p3");
        assertEquals(Config2.class, p3.getType());

        ConfigListMetadata p4 = (ConfigListMetadata) propMap.get("p4");
        assertEquals("java.util.List<java.lang.String>", p4.getType().getTypeName());
        assertEquals(String.class, p4.getElementType().getType());
        assertEquals(ConfigValueMetadata.class, p4.getElementType().getClass());

        ConfigListMetadata p5 = (ConfigListMetadata) propMap.get("p5");
        assertEquals("java.util.List<io.bootique.meta.config.ConfigMetadataCompilerTest$Config2>",
                p5.getType().getTypeName());
        assertEquals(Config2.class, p5.getElementType().getType());

        ConfigMapMetadata p6 = (ConfigMapMetadata) propMap.get("p6");
        assertEquals("java.util.Map<java.lang.String, java.math.BigDecimal>", p6.getType().getTypeName());
        assertEquals(String.class, p6.getKeysType());
        assertEquals(BigDecimal.class, p6.getValuesType().getType());
        assertEquals(ConfigValueMetadata.class, p6.getValuesType().getClass());

        ConfigMapMetadata p7 = (ConfigMapMetadata) propMap.get("p7");
        assertEquals("java.util.Map<java.lang.Integer, io.bootique.meta.config.ConfigMetadataCompilerTest$Config2>",
                p7.getType().getTypeName());
        assertEquals(Integer.class, p7.getKeysType());
        assertEquals(Config2.class, p7.getValuesType().getType());
    }

    @Test
    public void testCompile_Cycle() {

        ConfigObjectMetadata md = (ConfigObjectMetadata) createCompiler().compile("prefix", Config3.class);
        assertNotNull(md);

        assertEquals("prefix", md.getName());
        assertEquals(Config3.class, md.getType());

        assertEquals(1, md.getProperties().size());

        ConfigListMetadata p1 = (ConfigListMetadata) md.getProperties().iterator().next();
        assertEquals("java.util.Collection<io.bootique.meta.config.ConfigMetadataCompilerTest$Config4>", p1.getType().toString());
        assertEquals(Config4.class, p1.getElementType().getType());
    }

    @Test
    public void testCompile_Inheritance() {

        ConfigObjectMetadata c5 = (ConfigObjectMetadata) createCompiler(t -> {

            if (Config5.class.equals(t)) {
                return Stream.of(Config6.class, Config7.class);
            }

            if (Config6.class.equals(t)) {
                return Stream.of(Config8.class);
            }

            return Stream.empty();
        }).compile("prefix", Config5.class);

        assertNotNull(c5);
        assertEquals("prefix", c5.getName());
        assertEquals(Config5.class, c5.getType());
        assertTrue(c5.isAbstractType());

        Map<Type, ConfigMetadataNode> sc5 = c5.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigMetadataNode::getType, Function.identity()));
        assertEquals(2, sc5.size());

        ConfigObjectMetadata c6 = (ConfigObjectMetadata) sc5.get(Config6.class);
        assertNotNull(c6);
        assertEquals("c6", c6.getTypeLabel());
        assertFalse(c6.isAbstractType());
        Map<Type, ConfigMetadataNode> sc6 = c6.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigMetadataNode::getType, Function.identity()));
        assertEquals(1, sc6.size());

        ConfigObjectMetadata c8 = (ConfigObjectMetadata) sc6.get(Config8.class);
        assertNotNull(c8);
        assertEquals("c8", c8.getTypeLabel());
        assertFalse(c8.isAbstractType());
        assertEquals(0, c8.getSubConfigs().size());

        ConfigObjectMetadata c7 = (ConfigObjectMetadata) sc5.get(Config7.class);
        assertNotNull(c7);
        assertEquals("c7", c7.getTypeLabel());
        assertFalse(c7.isAbstractType());
        assertEquals(0, c7.getSubConfigs().size());
    }

    @Test
    public void testCompile_Map() {

        TypeRef<Map<String, BigDecimal>> tr1 = new TypeRef<Map<String, BigDecimal>>() {
        };

        ConfigMapMetadata md = (ConfigMapMetadata) createCompiler(t -> Stream.empty())
                .compile("prefix", tr1.getType());

        assertNotNull(md);
        assertEquals("prefix", md.getName());
        assertEquals(tr1.getType(), md.getType());

        assertEquals(String.class, md.getKeysType());
        assertEquals(BigDecimal.class, md.getValuesType().getType());
        assertEquals(ConfigValueMetadata.class, md.getValuesType().getClass());
    }

    @Test
    public void testCompile_Map_Inheritance() {

        TypeRef<Map<String, Config5>> tr1 = new TypeRef<Map<String, Config5>>() {
        };

        ConfigMapMetadata md = (ConfigMapMetadata) createCompiler(t -> {

            if (Config5.class.equals(t)) {
                return Stream.of(Config6.class, Config7.class);
            }

            if (Config6.class.equals(t)) {
                return Stream.of(Config8.class);
            }

            return Stream.empty();
        }).compile("prefix", tr1.getType());

        assertNotNull(md);
        assertEquals("prefix", md.getName());
        assertEquals(tr1.getType(), md.getType());

        assertEquals(String.class, md.getKeysType());
        assertEquals(Config5.class, md.getValuesType().getType());
        assertEquals(ConfigObjectMetadata.class, md.getValuesType().getClass());

        ConfigObjectMetadata c5 = (ConfigObjectMetadata) md.getValuesType();
        assertNotNull(c5);
        assertEquals(Config5.class, c5.getType());
        assertTrue(c5.isAbstractType());

        Map<Type, ConfigMetadataNode> sc5 = c5.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigMetadataNode::getType, Function.identity()));
        assertEquals(2, sc5.size());

        ConfigObjectMetadata c6 = (ConfigObjectMetadata) sc5.get(Config6.class);
        assertNotNull(c6);
        assertEquals("c6", c6.getTypeLabel());
        assertFalse(c6.isAbstractType());
        Map<Type, ConfigMetadataNode> sc6 = c6.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigMetadataNode::getType, Function.identity()));
        assertEquals(1, sc6.size());

        ConfigObjectMetadata c8 = (ConfigObjectMetadata) sc6.get(Config8.class);
        assertNotNull(c8);
        assertEquals("c8", c8.getTypeLabel());
        assertFalse(c8.isAbstractType());
        assertEquals(0, c8.getSubConfigs().size());

        ConfigObjectMetadata c7 = (ConfigObjectMetadata) sc5.get(Config7.class);
        assertNotNull(c7);
        assertEquals("c7", c7.getTypeLabel());
        assertFalse(c7.isAbstractType());
        assertEquals(0, c7.getSubConfigs().size());
    }

    @Test
    public void testCompile_InheritanceFromInterface() {

        ConfigObjectMetadata c9 = (ConfigObjectMetadata) createCompiler(t -> {

            if (Config9.class.equals(t)) {
                return Stream.of(Config10.class, Config11.class);
            }

            return Stream.empty();
        }).compile("prefix", Config9.class);

        assertNotNull(c9);
        assertEquals("prefix", c9.getName());
        assertEquals(Config9.class, c9.getType());
        assertTrue(c9.isAbstractType());

        Map<Type, ConfigMetadataNode> sc9 = c9.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigMetadataNode::getType, Function.identity()));
        assertEquals(2, sc9.size());

        ConfigObjectMetadata c10 = (ConfigObjectMetadata) sc9.get(Config10.class);
        assertNotNull(c10);
        assertEquals("c10", c10.getTypeLabel());
        assertFalse(c10.isAbstractType());
        assertEquals(0, c10.getSubConfigs().size());

        ConfigObjectMetadata c11 = (ConfigObjectMetadata) sc9.get(Config11.class);
        assertNotNull(c11);
        assertEquals("c11", c11.getTypeLabel());
        assertFalse(c11.isAbstractType());
        assertEquals(0, c11.getSubConfigs().size());
    }


    @BQConfig
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public static interface Config9 extends PolymorphicConfiguration {
    }

    @BQConfig("Describes Config1")
    public static class Config1 {

        @BQConfigProperty("constructor with params")
        public Config1(String s){
        }

        @BQConfigProperty
        public void setP1(int v) {
        }

        @BQConfigProperty("description of p2")
        public void setP2(BigDecimal v) {
        }

        @BQConfigProperty
        public void setP3(Config2 v) {
        }

        @BQConfigProperty
        public void setP4(List<String> v) {
        }

        @BQConfigProperty
        public void setP5(List<Config2> v) {
        }

        @BQConfigProperty
        public void setP6(Map<String, BigDecimal> v) {
        }

        @BQConfigProperty
        public void setP7(Map<Integer, Config2> v) {
        }

        @BQConfigProperty
        public Config1 setP8(String v) {
            return this;
        }

        @BQConfigProperty
        public String setP9(String v) {
            return v;
        }

        public void setNonP(String v) {
        }
    }

    @BQConfig("Describes Config2")
    public static class Config2 {

        @BQConfigProperty
        public void setP1(byte v) {
        }
    }

    @BQConfig
    public static class Config3 {

        @BQConfigProperty
        public void setC4s(Collection<Config4> v) {
        }
    }

    @BQConfig
    public static class Config4 {

        @BQConfigProperty
        public void setC3(Config3 v) {
        }
    }

    @BQConfig
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    public static abstract class Config5 implements PolymorphicConfiguration {

        @BQConfigProperty
        public void setP1(long v) {
        }
    }

    @BQConfig
    @JsonTypeName("c6")
    public static class Config6 extends Config5 {

        @BQConfigProperty
        public void setP2(String v) {
        }
    }

    @BQConfig
    @JsonTypeName("c7")
    public static class Config7 extends Config5 {

        @BQConfigProperty
        public void setP3(String v) {
        }
    }

    @BQConfig
    @JsonTypeName("c8")
    public static class Config8 extends Config6 {

        @BQConfigProperty
        public void setP4(BigDecimal v) {
        }
    }

    @BQConfig
    @JsonTypeName("c10")
    public static class Config10 implements Config9 {

        @BQConfigProperty
        public void setP2(String v) {
        }
    }

    @BQConfig
    @JsonTypeName("c11")
    public static class Config11 implements Config9 {

        @BQConfigProperty
        public void setP3(String v) {
        }
    }

}

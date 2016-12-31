package io.bootique.meta.config;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.meta.MetadataNode;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ConfigMetadataCompilerTest {

    private ConfigMetadataCompiler compiler;

    @Before
    public void before() {
        compiler = new ConfigMetadataCompiler();
    }

    @Test
    public void testCompile() {

        ConfigObjectMetadata md = (ConfigObjectMetadata) compiler.compile("prefix", Config1.class);
        assertNotNull(md);

        assertEquals("prefix", md.getName());
        assertEquals("Describes Config1", md.getDescription());
        assertEquals(Config1.class, md.getType());

        assertEquals(7, md.getProperties().size());

        Map<String, ConfigMetadataNode> propMap = md
                .getProperties()
                .stream()
                .collect(Collectors.toMap(MetadataNode::getName, Function.identity()));

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

        ConfigObjectMetadata md = (ConfigObjectMetadata) compiler.compile("prefix", Config3.class);
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

        ConfigObjectMetadata c5 = (ConfigObjectMetadata) compiler.compile("prefix", Config5.class);
        assertNotNull(c5);

        assertEquals("prefix", c5.getName());
        assertEquals(Config5.class, c5.getType());

        Map<Type, ConfigObjectMetadata> sc5 = c5.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigObjectMetadata::getType, Function.identity()));
        assertEquals(2, sc5.size());

        ConfigObjectMetadata c6 = sc5.get(Config6.class);
        assertNotNull(c6);
        Map<Type, ConfigObjectMetadata> sc6 = c6.getSubConfigs().stream()
                .collect(Collectors.toMap(ConfigObjectMetadata::getType, Function.identity()));
        assertEquals(1, sc6.size());

        ConfigObjectMetadata c8 = sc6.get(Config8.class);
        assertNotNull(c8);
        assertEquals(0, c8.getSubConfigs().size());

        ConfigObjectMetadata c7 = sc5.get(Config7.class);
        assertNotNull(c7);
        assertEquals(0, c7.getSubConfigs().size());
    }

    @BQConfig("Describes Config1")
    public static class Config1 {

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
    public static class Config5 implements PolymorphicConfiguration {

        @BQConfigProperty
        public void setP1(long v) {
        }
    }

    @BQConfig
    public static class Config6 extends Config5 {

        @BQConfigProperty
        public void setP2(String v) {
        }
    }

    @BQConfig
    public static class Config7 extends Config5 {

        @BQConfigProperty
        public void setP3(String v) {
        }
    }

    @BQConfig
    public static class Config8 extends Config6 {

        @BQConfigProperty
        public void setP4(BigDecimal v) {
        }
    }
}

package io.bootique.meta.config;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.meta.MetadataNode;
import org.junit.Before;
import org.junit.Test;

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
}

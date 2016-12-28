package io.bootique;

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DeferredModuleMetadataSupplierTest {

    @Test
    public void testToConfig() {

        ConfigObjectMetadata md = DeferredModuleMetadataSupplier.toConfig("prefix", Config1.class);
        assertNotNull(md);

        assertEquals("prefix", md.getName());
        assertEquals("Describes Config1", md.getDescription());
        assertEquals(Config1.class, md.getType());

        assertEquals(4, md.getProperties().size());

        Map<String, ConfigValueMetadata> propMap = md
                .getProperties()
                .stream()
                .collect(Collectors.toMap(ConfigValueMetadata::getName, Function.identity()));

        ConfigValueMetadata p1 = propMap.get("p1");
        assertNotNull(p1);
        assertEquals(Integer.TYPE, p1.getType());
        assertNull(p1.getDescription());

        ConfigValueMetadata p2 = propMap.get("p2");
        assertNotNull(p2);
        assertEquals(BigDecimal.class, p2.getType());
        assertEquals("description of p2", p2.getDescription());

        ConfigObjectMetadata p3 = (ConfigObjectMetadata) propMap.get("p3");
        assertNotNull(p3);
        assertEquals(Config2.class, p3.getType());
        assertEquals(1, p3.getProperties().size());

        ConfigListMetadata p4 = (ConfigListMetadata) propMap.get("p4");
        assertNotNull(p4);
        assertEquals(List.class, p4.getType());
        assertNotNull(p4.getElementType());
        assertEquals(String.class, p4.getElementType().getType());
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

        public void setNonP(String v) {
        }
    }

    @BQConfig("Describes Config2")
    public static class Config2 {

        @BQConfigProperty
        public void setP1(byte v) {
        }
    }
}

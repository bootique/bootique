package io.bootique.meta.module;

import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class ModuleMetadataTest {

    @Test
    public void testFindConfig() {

        ConfigObjectMetadata c1 = ConfigObjectMetadata.builder("r1")
                .description("r1 desc")
                .type(Object.class)
                .addProperty(ConfigValueMetadata.builder("r1_p1").description("r1_p1 desc").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("r1_p2").description("r1_p2 desc").type(Boolean.TYPE).build())
                .build();

        ConfigMapMetadata c2 = ConfigMapMetadata
                .builder("r2")
                .description("r2 desc")
                .keysType(String.class)
                .valuesType(c1).build();

        ConfigObjectMetadata c3 = ConfigObjectMetadata
                .builder("r3")
                .description("r3 desc")
                .type(Object.class)
                .addProperty(c2)
                .build();

        ModuleMetadata md = ModuleMetadata.builder("x").addConfig(c3).build();

        Optional<ConfigMetadataNode> missing1 = md.findConfig("r3.rX");
        assertFalse(missing1.isPresent());

        Optional<ConfigMetadataNode> missing2 = md.findConfig("r2");
        assertFalse(missing2.isPresent());

        Optional<ConfigMetadataNode> r1 = md.findConfig("r3.r2.somekey");
        assertTrue(r1.isPresent());
        assertEquals("r1", r1.get().getName());
        assertEquals("r1 desc", r1.get().getDescription());
        assertEquals(Object.class, r1.get().getType());

        Optional<ConfigMetadataNode> r1P2 = md.findConfig("r3.r2.somekey.r1_p2");
        assertTrue(r1P2.isPresent());
        assertEquals("r1_p2", r1P2.get().getName());
        assertEquals("r1_p2 desc", r1P2.get().getDescription());
        assertEquals(Boolean.TYPE, r1P2.get().getType());
    }
}

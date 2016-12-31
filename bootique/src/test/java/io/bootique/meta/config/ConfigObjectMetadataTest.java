package io.bootique.meta.config;

import org.junit.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConfigObjectMetadataTest {

    @Test
    public void testGetAllSubconfigs() {

        ConfigObjectMetadata o1 = ConfigObjectMetadata.builder().build();
        ConfigObjectMetadata o2 = ConfigObjectMetadata.builder().build();
        ConfigObjectMetadata o3 = ConfigObjectMetadata.builder().build();

        ConfigObjectMetadata o4 = ConfigObjectMetadata.builder().addSubConfig(o1).build();
        ConfigObjectMetadata o5 = ConfigObjectMetadata.builder().addSubConfig(o2).addSubConfig(o3).build();

        ConfigObjectMetadata o6 = ConfigObjectMetadata.builder().addSubConfig(o4).addSubConfig(o5).build();

        List<ConfigObjectMetadata> all6 = o6.getAllSubConfigs().collect(toList());
        assertEquals(6, all6.size());
        assertTrue(all6.contains(o1));
        assertTrue(all6.contains(o2));
        assertTrue(all6.contains(o3));
        assertTrue(all6.contains(o4));
        assertTrue(all6.contains(o5));
        assertTrue(all6.contains(o6));

        List<ConfigObjectMetadata> all4 = o4.getAllSubConfigs().collect(toList());
        assertEquals(2, all4.size());
        assertTrue(all4.contains(o1));
        assertTrue(all4.contains(o4));

        List<ConfigObjectMetadata> all1 = o1.getAllSubConfigs().collect(toList());
        assertEquals(1, all1.size());
        assertTrue(all1.contains(o1));
    }
}

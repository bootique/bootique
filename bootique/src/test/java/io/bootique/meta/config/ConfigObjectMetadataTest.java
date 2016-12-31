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

        List<ConfigObjectMetadata> all = o6.getAllSubConfigs().collect(toList());
        assertEquals(6, all.size());
        assertTrue(all.contains(o1));
        assertTrue(all.contains(o2));
        assertTrue(all.contains(o3));
        assertTrue(all.contains(o4));
        assertTrue(all.contains(o5));
        assertTrue(all.contains(o6));
    }
}

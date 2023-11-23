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

import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigObjectMetadataTest {

    @Test
    public void getAllSubconfigs() {

        ConfigObjectMetadata o1 = ConfigObjectMetadata.builder().build();
        ConfigObjectMetadata o2 = ConfigObjectMetadata.builder().build();
        ConfigObjectMetadata o3 = ConfigObjectMetadata.builder().build();

        ConfigObjectMetadata o4 = ConfigObjectMetadata.builder().addSubConfig(o1).build();
        ConfigObjectMetadata o5 = ConfigObjectMetadata.builder().addSubConfig(o2).addSubConfig(o3).build();

        ConfigObjectMetadata o6 = ConfigObjectMetadata.builder().addSubConfig(o4).addSubConfig(o5).build();

        List<ConfigMetadataNode> all6 = o6.getAllSubConfigs().collect(toList());
        assertEquals(6, all6.size());
        assertTrue(all6.contains(o1));
        assertTrue(all6.contains(o2));
        assertTrue(all6.contains(o3));
        assertTrue(all6.contains(o4));
        assertTrue(all6.contains(o5));
        assertTrue(all6.contains(o6));

        List<ConfigMetadataNode> all4 = o4.getAllSubConfigs().collect(toList());
        assertEquals(2, all4.size());
        assertTrue(all4.contains(o1));
        assertTrue(all4.contains(o4));

        List<ConfigMetadataNode> all1 = o1.getAllSubConfigs().collect(toList());
        assertEquals(1, all1.size());
        assertTrue(all1.contains(o1));
    }
}

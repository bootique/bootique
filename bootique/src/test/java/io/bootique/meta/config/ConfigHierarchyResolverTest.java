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

import io.bootique.config.PolymorphicConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class ConfigHierarchyResolverTest {

    @Test
    public void testCreate_Classes() {
        ConfigHierarchyResolver resolver = ConfigHierarchyResolver
                .create(asList(Config4.class, Config3.class, Config2.class, Config1.class));

        assertEquals(Collections.emptySet(), resolver.directSubclasses(Object.class).collect(toSet()));
        assertEquals(new HashSet<>(asList(Config2.class, Config3.class)),
                resolver.directSubclasses(Config1.class).collect(toSet()));
    }

    @Test
    public void testCreate_BaseInterface() {
        ConfigHierarchyResolver resolver = ConfigHierarchyResolver
                .create(asList(Config5.class, Config6.class, IConfig1.class));

        assertEquals(new HashSet<>(asList(Config5.class, Config6.class)),
                resolver.directSubclasses(IConfig1.class).collect(toSet()));
    }


    public static interface IConfig1 extends PolymorphicConfiguration {

    }

    public static class Config1 implements PolymorphicConfiguration {

    }

    public static class Config2 extends Config1 {

    }

    public static class Config3 extends Config1 {

    }

    public static class Config4 extends Config2 {

    }

    public static class Config5 implements IConfig1 {

    }

    public static class Config6 implements IConfig1 {

    }

}

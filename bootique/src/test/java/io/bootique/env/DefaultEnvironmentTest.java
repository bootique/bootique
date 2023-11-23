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
package io.bootique.env;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultEnvironmentTest {

    @Test
    public void testProperties() {
        Map<String, String> props = Map.of("a", "b", "bq.c", "d");
        DefaultEnvironment env = new DefaultEnvironment(props);
        assertEquals(props, env.properties());
    }

    @Test
    public void testFrameworkProperties() {
        DefaultEnvironment env = new DefaultEnvironment(Map.of(
                "a", "b",
                "bq.c", "d",
                "c", "e",
                "bq.x", "y"));

        assertEquals(Map.of("c", "d", "x", "y"), env.frameworkProperties());
    }
}

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
package io.bootique.junit5;

import io.bootique.BQModuleProvider;
import io.bootique.di.BQModule;
import io.bootique.junit5.BQModuleProviderChecker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BQModuleProviderCheckerTest {

    @Test
    public void matchingProvider() {
        BQModuleProvider p = new BQModuleProviderChecker(P1.class).matchingProvider();

        assertNotNull(p);
        assertTrue(p instanceof P1);
    }

    @Test
    public void metadata() {
        new BQModuleProviderChecker(P1.class).testMetadata();
    }

    public static class P1 implements BQModuleProvider {

        @Override
        public BQModule module() {
            return b -> {
            };
        }
    }

}

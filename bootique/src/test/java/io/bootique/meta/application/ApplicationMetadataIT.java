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

package io.bootique.meta.application;

import io.bootique.BQCoreModule;
import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;
import io.bootique.meta.config.ConfigValueMetadata;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationMetadataIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void basic() {
        BQRuntime runtime = appManager.runtime(Bootique.app());

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        // we really don't know what the generated name is. It varies depending on the unit test execution environment
        assertNotNull(md.getName());
        assertNull(md.getDescription());
        assertEquals(2, md.getCommands().size());
        assertEquals(1, md.getOptions().size());
    }

    @Test
    public void customDescription() {
        BQRuntime runtime = appManager.runtime(Bootique.app()
                .module(b -> BQCoreModule.extend(b).setApplicationDescription("app desc")));

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        // we really don't know what the generated name is. It varies depending on the unit test execution environment
        assertNotNull(md.getName());
        assertEquals("app desc", md.getDescription());
        assertEquals(2, md.getCommands().size());
        assertEquals(1, md.getOptions().size());
    }

    @Test
    public void unboundVar() {
        BQRuntime runtime = appManager.runtime(Bootique.app()
                .module(b -> BQCoreModule.extend(b).declareVar("x.p1", "UNBOUND_VAR")));

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        assertEquals(1, md.getVariables().size());
        ConfigValueMetadata varMd = md.getVariables().iterator().next();
        assertEquals("UNBOUND_VAR", varMd.getName());
        assertTrue(varMd.isUnbound());
    }

    @Test
    public void boundVar() {
        BQModule m = b -> {
        };
        BQModuleProvider provider = () -> BuiltModule.of(m).config("x", O1.class).build();

        BQRuntime runtime = appManager.runtime(Bootique.app()
                .moduleProvider(provider)
                .module(b -> BQCoreModule.extend(b).declareVar("x.p1", "BOUND_VAR")));

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        assertEquals(1, md.getVariables().size());
        ConfigValueMetadata varMd = md.getVariables().iterator().next();
        assertEquals("BOUND_VAR", varMd.getName());
        assertFalse(varMd.isUnbound());
    }

    @BQConfig
    static class O1 {
        private BigDecimal p1;

        @BQConfigProperty
        public O1 setP1(BigDecimal p1) {
            this.p1 = p1;
            return this;
        }
    }
}

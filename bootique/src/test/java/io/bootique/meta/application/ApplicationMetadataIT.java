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
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.unit.TestAppManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationMetadataIT {

    @RegisterExtension
    final TestAppManager appManager = new TestAppManager();

    @Test
    public void testDefault() {
        BQRuntime runtime = appManager.runtime(Bootique.app());

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        // we really don't know what the generated name is. It varies depending on the unit test execution environment
        assertNotNull(md.getName());
        assertNull(md.getDescription());
        assertEquals(2, md.getCommands().size());
        assertEquals(1, md.getOptions().size());
    }

    @Test
    public void testCustomDescription() {
        BQRuntime runtime = appManager.runtime(Bootique.app()
                .module(b -> BQCoreModule.extend(b).setApplicationDescription("app desc")));

        ApplicationMetadata md = runtime.getInstance(ApplicationMetadata.class);

        // we really don't know what the generated name is. It varies depending on the unit test execution environment
        assertNotNull(md.getName());
        assertEquals("app desc", md.getDescription());
        assertEquals(2, md.getCommands().size());
        assertEquals(1, md.getOptions().size());
    }
}

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

package io.bootique.shutdown;

import io.bootique.log.DefaultBootLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultShutdownManagerTest {

    private DefaultShutdownManager shutdownManager;


    @BeforeEach
    public void before() {
        this.shutdownManager = new DefaultShutdownManager(Duration.ofMillis(1000L), new DefaultBootLogger(true));
    }

    @Test
    public void shutdownAll_Empty() {
        shutdownManager.shutdownAll();
    }

    @Test
    public void shutdownAll() throws Exception {

        CloseableTracker c1 = new CloseableTracker();
        CloseableTracker c2 = new CloseableTracker();

        shutdownManager.onShutdown(c1);
        shutdownManager.onShutdown(c2);
        shutdownManager.shutdownAll();

        assertTrue(c1.wasClosed);
        assertTrue(c2.wasClosed);
    }

    static class CloseableTracker implements AutoCloseable {
        boolean wasClosed;

        @Override
        public void close() throws Exception {
            this.wasClosed = true;
        }
    }
}

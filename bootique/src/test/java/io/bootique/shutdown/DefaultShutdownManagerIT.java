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

import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultShutdownManagerIT {

    static final BootLogger logger = new DefaultBootLogger(true);

    @Test
    public void shutdown() throws Exception {
        Duration timeout = Duration.ofMillis(10000L);
        DefaultShutdownManager sm = new DefaultShutdownManager(timeout, logger);

        CloseableTracker c1 = new CloseableTracker();
        CloseableTracker c2 = new CloseableTracker();

        sm.onShutdown(c1);
        sm.onShutdown(c2);

        sm.shutdown();

        assertTrue(c1.wasClosed);
        assertTrue(c2.wasClosed);
    }

    @Test
    public void shutdown_Unresponsive_Timeout() {
        Duration timeout = Duration.ofMillis(500L);
        DefaultShutdownManager sm = new DefaultShutdownManager(timeout, logger);

        SpinningCloseableTracker c = new SpinningCloseableTracker();

        sm.onShutdown(c);

        long t0 = System.currentTimeMillis();
        sm.shutdown();

        long t1 = System.currentTimeMillis();

        assertTrue(c.wasClosed);
        assertTrue(t1 - t0 >= timeout.toMillis());

        // too optimistic??
        assertTrue(t1 - t0 < timeout.toMillis() + 1000);
    }

    static class CloseableTracker implements AutoCloseable {
        boolean wasClosed;

        @Override
        public void close() throws Exception {
            this.wasClosed = true;
        }
    }

    static class SpinningCloseableTracker implements AutoCloseable {
        boolean wasClosed;

        @Override
        public void close() throws Exception {
            this.wasClosed = true;
            while (true) {

                // spinning...
                try {
                    wait(1000);
                } catch (Throwable th) {
                }

                // TODO: bad .. this will keep spinning until all tests are run
            }
        }
    }
}

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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import io.bootique.shutdown.DefaultShutdownManager;
import org.junit.Before;
import org.junit.Test;

public class DefaultShutdownManagerIT {

	private AutoCloseable mockCloseable1;
	private AutoCloseable mockCloseable2;

	@Before
	public void before() {

		this.mockCloseable1 = mock(AutoCloseable.class);
		this.mockCloseable2 = mock(AutoCloseable.class);
	}

	@Test
	public void testShutdown() throws Exception {
		Duration timeout = Duration.ofMillis(10000l);
		DefaultShutdownManager shutdownManager = new DefaultShutdownManager(timeout);

		shutdownManager.addShutdownHook(mockCloseable1);
		shutdownManager.addShutdownHook(mockCloseable2);

		shutdownManager.shutdown();

		verify(mockCloseable1).close();
		verify(mockCloseable2).close();
	}

	@Test
	public void testShutdown_Unresponsive_Timeout() throws Exception {
		Duration timeout = Duration.ofMillis(500l);
		DefaultShutdownManager shutdownManager = new DefaultShutdownManager(timeout);

		doAnswer(i -> {
			while (true) {
				// spinning...
			}
		}).when(mockCloseable2).close();

		shutdownManager.addShutdownHook(mockCloseable2);

		long t0 = System.currentTimeMillis();
		shutdownManager.shutdown();

		long t1 = System.currentTimeMillis();

		verify(mockCloseable2).close();

		assertTrue(t1 - t0 >= timeout.toMillis());

		// too optimistic??
		assertTrue(t1 - t0 < timeout.toMillis() + 1000);
	}
}

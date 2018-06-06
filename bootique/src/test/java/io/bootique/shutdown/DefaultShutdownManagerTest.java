/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.shutdown;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Duration;

import io.bootique.shutdown.DefaultShutdownManager;
import org.junit.Before;
import org.junit.Test;

public class DefaultShutdownManagerTest {

	private DefaultShutdownManager shutdownManager;
	private AutoCloseable mockCloseable1;
	private AutoCloseable mockCloseable2;

	@Before
	public void before() {
		this.shutdownManager = new DefaultShutdownManager(Duration.ofMillis(1000l));

		this.mockCloseable1 = mock(AutoCloseable.class);
		this.mockCloseable2 = mock(AutoCloseable.class);
	}
	
	@Test
	public void testShutdownAll_Empty() throws Exception {
		shutdownManager.shutdownAll();
	}

	@Test
	public void testShutdownOne() throws Exception {
		shutdownManager.shutdownOne(mockCloseable1);
		verify(mockCloseable1).close();
	}

	@Test
	public void testShutdownAll() throws Exception {
		shutdownManager.addShutdownHook(mockCloseable1);
		shutdownManager.addShutdownHook(mockCloseable2);
		shutdownManager.shutdownAll();

		verify(mockCloseable1).close();
		verify(mockCloseable2).close();
	}
}

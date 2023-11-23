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

package io.bootique;

import io.bootique.bootstrap.BuiltModule;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeModuleMergerTest {

	private BootLogger bootLogger;

	private List<BuiltModule> mockBqModules;
	private List<BQModule> testModules;

	@BeforeEach
	public void before() {

		// using real logger to better understand what's going on in the tests
		this.bootLogger = new DefaultBootLogger(true);

		// module types are used as keys in Bootique, so lets' define a bunch of
		// distinct types without using mocks
		this.testModules = Arrays.asList(new M0(), new M1(), new M2(), new M3(), new M4());

		this.mockBqModules = new ArrayList<>();
		testModules.forEach(m -> mockBqModules.add(createBQModule(m)));
	}

	private void assertOverrideModule(BQModule m) {
		assertEquals("OverrideModule", m.getClass().getSimpleName());
	}

	@SafeVarargs
	private final BuiltModule createBQModule(BQModule m, Class<? extends BQModule>... overrides) {
		BuiltModule bqModuleMock = mock(BuiltModule.class);
		when(bqModuleMock.getModule()).thenReturn(m);
		when(bqModuleMock.getOverrides()).thenReturn(Arrays.asList(overrides));
		return bqModuleMock;
	}

	@Test
    public void getModules_Empty() {
		assertTrue(new RuntimeModuleMerger(bootLogger).toDIModules(Collections.emptyList()).isEmpty());
	}

	@Test
    public void getModules_One() {

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(2));

		Collection<BQModule> modules = new RuntimeModuleMerger(bootLogger).toDIModules(bqModules);
		assertEquals(1, modules.size());

		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
    public void getModules_Two() {

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(2), mockBqModules.get(1));

		Collection<BQModule> modules = new RuntimeModuleMerger(bootLogger).toDIModules(bqModules);
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
    public void getModules_Three_Dupes() {

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(2), mockBqModules.get(1), mockBqModules.get(2));

		Collection<BQModule> modules = new RuntimeModuleMerger(bootLogger).toDIModules(bqModules);
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
    public void getModules_Overrides() {

		// 0 overrides 3
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(3));
		Collection<BQModule> modules = new RuntimeModuleMerger(bootLogger).toDIModules(bqModules);
		assertEquals(2, modules.size());

//		assertOverrideModule(modules.iterator().next());
	}

	@Test
    public void getModules_Overrides_Chain() {

		// 0 overrides 3 ; 3 overrides 4
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(3, createBQModule(testModules.get(3), M4.class));

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(4), mockBqModules.get(0),
				mockBqModules.get(1), mockBqModules.get(3));
		Collection<BQModule> modules = new RuntimeModuleMerger(bootLogger).toDIModules(bqModules);
		assertEquals(4, modules.size());

		assertTrue(modules.contains(testModules.get(4)));
		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(0)));
		assertTrue(modules.contains(testModules.get(3)));
	}

	@Test
    public void getModules_OverrideCycle() {

		// 0 replaces 3 ; 3 replaces 0
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(3, createBQModule(testModules.get(3), M0.class));

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(3));
		assertThrows(RuntimeException.class, () -> new RuntimeModuleMerger(bootLogger).toDIModules(bqModules));
	}

	@Test
    public void getModules_OverrideIndirectCycle() {

		// 0 replaces 3 ; 3 replaces 4 ; 4 replaces 0
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(3, createBQModule(testModules.get(3), M4.class));
		mockBqModules.set(4, createBQModule(testModules.get(4), M0.class));

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(4),
				mockBqModules.get(3));
		assertThrows(RuntimeException.class, () -> new RuntimeModuleMerger(bootLogger).toDIModules(bqModules));
	}

	@Test
	@Disabled("TODO: what was this about? it was expected to throw?")
	public void getModules_OverrideDupe() {

		// 0 overrides 3 ; 4 overrides 3
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(4, createBQModule(testModules.get(4), M3.class));

		Collection<BuiltModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(4),
				mockBqModules.get(3));
		new RuntimeModuleMerger(bootLogger).toDIModules(bqModules);
	}

	class M0 implements BQModule {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M1 implements BQModule {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M2 implements BQModule {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M3 implements BQModule {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M4 implements BQModule {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}
}

package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import io.bootique.BQModuleProvider;
import io.bootique.ModuleMerger;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;

public class ModuleMergerTest {

	private BootLogger bootLogger;

	private List<BQModuleProvider> mockProviders;
	private List<Module> testModules;

	@Before
	public void before() {

		// using real logger to better understand what's going on in the tests
		this.bootLogger = new DefaultBootLogger(true);

		// module types are used as keys in Bootique, so lets' define a bunch of
		// distinct types without using mocks
		this.testModules = Arrays.asList(new M0(), new M1(), new M2(), new M3(), new M4());

		this.mockProviders = new ArrayList<>();
		testModules.forEach(m -> {
			mockProviders.add(createProvider(m));
		});
	}

	private void assertOverrideModule(Module m) {
		assertEquals("OverrideModule", m.getClass().getSimpleName());
	}

	@SafeVarargs
	private final BQModuleProvider createProvider(Module m, Class<? extends Module>... overrides) {
		BQModuleProvider providerMock = mock(BQModuleProvider.class);
		when(providerMock.module()).thenReturn(m);
		when(providerMock.overrides()).thenReturn(Arrays.asList(overrides));
		return providerMock;
	}

	@Test
	public void testGetModules_Empty() {
		assertTrue(new ModuleMerger(bootLogger).getModules(Collections.emptyList()).isEmpty());
	}

	@Test
	public void testGetModules_One() {

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(2));

		Collection<Module> modules = new ModuleMerger(bootLogger).getModules(providers);
		assertEquals(1, modules.size());

		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Two() {

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(2), mockProviders.get(1));

		Collection<Module> modules = new ModuleMerger(bootLogger).getModules(providers);
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Three_Dupes() {

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(2), mockProviders.get(1),
				mockProviders.get(2));

		Collection<Module> modules = new ModuleMerger(bootLogger).getModules(providers);
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Overrides() {

		// 0 overrides 3
		mockProviders.set(0, createProvider(testModules.get(0), M3.class));

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(0), mockProviders.get(3));
		List<Module> modules = new ModuleMerger(bootLogger).getModules(providers);
		assertEquals(1, modules.size());

		assertOverrideModule(modules.get(0));
	}

	@Test
	public void testGetModules_Overrides_Chain() {

		// 0 overrides 3 ; 3 overrides 4
		mockProviders.set(0, createProvider(testModules.get(0), M3.class));
		mockProviders.set(3, createProvider(testModules.get(3), M4.class));

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(4), mockProviders.get(0),
				mockProviders.get(1), mockProviders.get(3));
		Collection<Module> modules = new ModuleMerger(bootLogger).getModules(providers);
		assertEquals(2, modules.size());

		assertFalse(modules.contains(testModules.get(4)));
		assertTrue(modules.contains(testModules.get(1)));
		assertFalse(modules.contains(testModules.get(0)));
		assertFalse(modules.contains(testModules.get(3)));
	}

	@Test(expected = RuntimeException.class)
	public void testGetModules_OverrideCycle() {

		// 0 replaces 3 ; 3 replaces 0
		mockProviders.set(0, createProvider(testModules.get(0), M3.class));
		mockProviders.set(3, createProvider(testModules.get(3), M0.class));

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(0), mockProviders.get(3));
		new ModuleMerger(bootLogger).getModules(providers);
	}

	@Test(expected = RuntimeException.class)
	public void testGetModules_OverrideIndirectCycle() {

		// 0 replaces 3 ; 3 replaces 4 ; 4 replaces 0
		mockProviders.set(0, createProvider(testModules.get(0), M3.class));
		mockProviders.set(3, createProvider(testModules.get(3), M4.class));
		mockProviders.set(4, createProvider(testModules.get(4), M0.class));

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(0), mockProviders.get(4),
				mockProviders.get(3));
		new ModuleMerger(bootLogger).getModules(providers);
	}

	@Test(expected = RuntimeException.class)
	public void testGetModules_OverrideDupe() {

		// 0 overrides 3 ; 4 overrides 3
		mockProviders.set(0, createProvider(testModules.get(0), M3.class));
		mockProviders.set(4, createProvider(testModules.get(4), M3.class));

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(0), mockProviders.get(4),
				mockProviders.get(3));
		new ModuleMerger(bootLogger).getModules(providers);
	}

	class M0 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M1 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M2 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M3 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M4 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}
}

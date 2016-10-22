package io.bootique;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.bootique.log.BootLogger;
import io.bootique.log.DefaultBootLogger;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuntimeModuleMergerTest {

	private BootLogger bootLogger;

	private List<BQModule> mockBqModules;
	private List<Module> testModules;

	@Before
	public void before() {

		// using real logger to better understand what's going on in the tests
		this.bootLogger = new DefaultBootLogger(true);

		// module types are used as keys in Bootique, so lets' define a bunch of
		// distinct types without using mocks
		this.testModules = Arrays.asList(new M0(), new M1(), new M2(), new M3(), new M4());

		this.mockBqModules = new ArrayList<>();
		testModules.forEach(m -> {
			mockBqModules.add(createBQModule(m));
		});
	}

	private void assertOverrideModule(Module m) {
		assertEquals("OverrideModule", m.getClass().getSimpleName());
	}

	@SafeVarargs
	private final BQModule createBQModule(Module m, Class<? extends Module>... overrides) {
		BQModule bqModuleMock = mock(BQModule.class);
		when(bqModuleMock.getModule()).thenReturn(m);
		when(bqModuleMock.getOverrides()).thenReturn(Arrays.asList(overrides));
		return bqModuleMock;
	}

	@Test
	public void testGetModules_Empty() {
		assertTrue(new RuntimeModuleMerger(bootLogger).toGuiceModules(Collections.emptyList()).isEmpty());
	}

	@Test
	public void testGetModules_One() {

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(2));

		Collection<Module> modules = new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
		assertEquals(1, modules.size());

		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Two() {

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(2), mockBqModules.get(1));

		Collection<Module> modules = new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Three_Dupes() {

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(2), mockBqModules.get(1), mockBqModules.get(2));

		Collection<Module> modules = new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Overrides() {

		// 0 overrides 3
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(3));
		List<Module> modules = new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
		assertEquals(1, modules.size());

		assertOverrideModule(modules.get(0));
	}

	@Test
	public void testGetModules_Overrides_Chain() {

		// 0 overrides 3 ; 3 overrides 4
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(3, createBQModule(testModules.get(3), M4.class));

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(4), mockBqModules.get(0),
				mockBqModules.get(1), mockBqModules.get(3));
		Collection<Module> modules = new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
		assertEquals(2, modules.size());

		assertFalse(modules.contains(testModules.get(4)));
		assertTrue(modules.contains(testModules.get(1)));
		assertFalse(modules.contains(testModules.get(0)));
		assertFalse(modules.contains(testModules.get(3)));
	}

	@Test(expected = RuntimeException.class)
	public void testGetModules_OverrideCycle() {

		// 0 replaces 3 ; 3 replaces 0
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(3, createBQModule(testModules.get(3), M0.class));

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(3));
		new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
	}

	@Test(expected = RuntimeException.class)
	public void testGetModules_OverrideIndirectCycle() {

		// 0 replaces 3 ; 3 replaces 4 ; 4 replaces 0
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(3, createBQModule(testModules.get(3), M4.class));
		mockBqModules.set(4, createBQModule(testModules.get(4), M0.class));

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(4),
				mockBqModules.get(3));
		new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
	}

	@Test(expected = RuntimeException.class)
	public void testGetModules_OverrideDupe() {

		// 0 overrides 3 ; 4 overrides 3
		mockBqModules.set(0, createBQModule(testModules.get(0), M3.class));
		mockBqModules.set(4, createBQModule(testModules.get(4), M3.class));

		Collection<BQModule> bqModules = Arrays.asList(mockBqModules.get(0), mockBqModules.get(4),
				mockBqModules.get(3));
		new RuntimeModuleMerger(bootLogger).toGuiceModules(bqModules);
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

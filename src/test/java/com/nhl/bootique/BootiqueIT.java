package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Module;
import com.nhl.bootique.it.ItestModule;
import com.nhl.bootique.it.ItestModuleProvider;

public class BootiqueIT {

	@Before
	public void before() {
		ItestModule.MOCK_DELEGATE = mock(Module.class);
	}

	@After
	public void after() {
		ItestModuleProvider.REPLACES = null;
		ItestModule.MOCK_DELEGATE = null;
	}

	@Test
	public void testCreateAutoLoadModules() {
		Collection<Module> autoLoaded = Bootique.app(new String[0]).autoLoadModules()
				.createAutoLoadModules(Collections.emptyList());

		assertEquals(1, autoLoaded.size());
		autoLoaded.forEach(m -> assertTrue(m instanceof ItestModule));
	}

	@Test
	public void testCreateAutoLoadModules_ExplicitIgnored() {

		Collection<Module> existing = Arrays.asList(new ItestModule());
		assertEquals(0, Bootique.app(new String[0]).autoLoadModules().createAutoLoadModules(existing).size());
	}

	@Test
	public void testCreateAutoLoadModules_ReplaceAnotherModule() {

		ItestModuleProvider.REPLACES = BQCoreModule.class;

		Collection<Module> existing = Arrays.asList(new ItestModule());
		assertEquals(0, Bootique.app(new String[0]).autoLoadModules().createAutoLoadModules(existing).size());
	}
}

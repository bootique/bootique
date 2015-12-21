package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.google.inject.Module;
import com.nhl.bootique.it.ItestModule;

public class BootiqueIT {

	@Test
	public void testCreateAutoLoadModules_NoAutoLoad() {
		assertEquals(0, Bootique.app(new String[0]).createAutoLoadModules(Collections.emptyList()).size());
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
}

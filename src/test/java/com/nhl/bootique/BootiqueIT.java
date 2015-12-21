package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.nhl.bootique.it.ItestModuleProvider;

public class BootiqueIT {

	@Test
	public void testAutoLoadedProviders() {
		Collection<BQModuleProvider> autoLoaded = Bootique.app(new String[0]).autoLoadedProviders();

		assertEquals(1, autoLoaded.size());
		autoLoaded.forEach(m -> assertTrue(m instanceof ItestModuleProvider));
	}

}

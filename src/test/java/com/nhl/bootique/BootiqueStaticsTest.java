package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Module;

public class BootiqueStaticsTest {

	@Test
	public void testCreateBundleModule() {
		Module m = Bootique.createBundleModule(TestBundle.class, "some.config.path");
		assertEquals("some.config.path", Guice.createInjector(m).getInstance(String.class));
	}

	@Test
	public void testCreateBundleModule_RootConfig() {
		Module m = Bootique.createBundleModule(TestBundle.class, "");
		assertEquals("", Guice.createInjector(m).getInstance(String.class));
	}

	@Test
	public void testCreateBundleModule_DefaultConfig() {
		Module m = Bootique.createBundleModule(TestBundle.class, null);
		assertEquals("test", Guice.createInjector(m).getInstance(String.class));
	}

	static class TestBundle implements BQBundle {

		@Override
		public Module module(String configPrefix) {
			return b -> b.bind(String.class).toInstance(configPrefix);
		}
	}

}

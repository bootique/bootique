package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Module;

public class BootiqueStaticsTest {

	@Test
	public void testCreateModule() {
		Module m = Bootique.createModule(TestModule.class);
		assertEquals("tm1", Guice.createInjector(m).getInstance(String.class));
	}

	static class TestModule implements Module {

		@Override
		public void configure(Binder binder) {
			binder.bind(String.class).toInstance("tm1");
		}
	}

}

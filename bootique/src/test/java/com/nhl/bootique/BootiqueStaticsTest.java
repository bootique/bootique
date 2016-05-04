package com.nhl.bootique;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
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

	@Test
	public void testToArray() {
		assertArrayEquals(new String[] {}, Bootique.toArray(asList()));
		assertArrayEquals(new String[] { "a", "b", "c" }, Bootique.toArray(asList("a", "b", "c")));
	}

	@Test
	public void testMergeArrays() {
		assertArrayEquals(new String[] {}, Bootique.mergeArrays(new String[0], new String[0]));
		assertArrayEquals(new String[] { "a" }, Bootique.mergeArrays(new String[] { "a" }, new String[0]));
		assertArrayEquals(new String[] { "b" }, Bootique.mergeArrays(new String[0], new String[] { "b" }));
		assertArrayEquals(new String[] { "b", "c", "d" },
				Bootique.mergeArrays(new String[] { "b", "c" }, new String[] { "d" }));

	}

	static class TestModule implements Module {

		@Override
		public void configure(Binder binder) {
			binder.bind(String.class).toInstance("tm1");
		}
	}

}

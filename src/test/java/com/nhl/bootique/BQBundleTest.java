package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.Module;

public class BQBundleTest {

	@Test
	public void testConfigPrefix() {
		assertEquals("testxyz", new TestXyzBundle().configPrefix());
		assertEquals("xyz", new Xyz().configPrefix());
	}

	class TestXyzBundle implements BQBundle {
		@Override
		public Module module(String configPrefix) {
			throw new UnsupportedOperationException();
		}
	}

	class Xyz implements BQBundle {
		@Override
		public Module module(String configPrefix) {
			throw new UnsupportedOperationException();
		}
	}
}

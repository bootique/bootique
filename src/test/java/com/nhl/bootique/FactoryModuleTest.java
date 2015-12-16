package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FactoryModuleTest {

	@Test
	public void testDefaultConfigPrefix() {
		assertEquals("testxyz", new TestXyzModule().defaultConfigPrefix());
		assertEquals("xyz", new Xyz().defaultConfigPrefix());
	}
	
	@Test
	public void testConfigPrefix() {
		assertEquals("testxyz", new TestXyzModule().configPrefix);
		assertEquals("custom-prefix", new TestXyzModule("custom-prefix").configPrefix);
	}

	class TestXyzModule extends FactoryModule<Object> {

		public TestXyzModule() {
			super(Object.class);
		}
		
		public TestXyzModule(String configPrefix) {
			super(Object.class, configPrefix);
		}
	}

	class Xyz extends FactoryModule<Object> {
		public Xyz() {
			super(Object.class);
		}
	}
}

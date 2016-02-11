package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConfigModuleTest {

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

	class TestXyzModule extends ConfigModule {

		public TestXyzModule() {

		}

		public TestXyzModule(String configPrefix) {
			super(configPrefix);
		}
	}

	class Xyz extends ConfigModule {

	}
}

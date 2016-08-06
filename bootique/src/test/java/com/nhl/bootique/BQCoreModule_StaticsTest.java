package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import io.bootique.BQCoreModule;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.bootique.annotation.EnvironmentProperties;
import io.bootique.cli.CliOption;

public class BQCoreModule_StaticsTest {

	@Test
	public void testContributeProperties() {
		Injector i = Guice.createInjector(b -> {
			BQCoreModule.contributeProperties(b).addBinding("a").toInstance("b");
			BQCoreModule.contributeProperties(b).addBinding("c").toInstance("d");

			b.bind(MapInspector.class);
		});

		MapInspector inspector = i.getInstance(MapInspector.class);

		assertEquals("b", inspector.map.get("a"));
		assertEquals("d", inspector.map.get("c"));
	}

	@Test
	public void testContributeOptions() {
		CliOption o1 = CliOption.builder("o1").build();
		CliOption o2 = CliOption.builder("o2").build();

		Injector i = Guice.createInjector(b -> {
			BQCoreModule.contributeOptions(b).addBinding().toInstance(o1);
			BQCoreModule.contributeOptions(b).addBinding().toInstance(o2);

			b.bind(OptionsInspector.class);
		});

		OptionsInspector inspector = i.getInstance(OptionsInspector.class);
		assertEquals(2, inspector.options.size());
		assertTrue(inspector.options.contains(o1));
		assertTrue(inspector.options.contains(o2));
	}

	static class MapInspector {

		Map<String, String> map;

		@Inject
		public MapInspector(@EnvironmentProperties Map<String, String> map) {
			this.map = map;
		}
	}

	static class OptionsInspector {
		Set<CliOption> options;

		@Inject
		public OptionsInspector(Set<CliOption> options) {
			this.options = options;
		}
	}
}

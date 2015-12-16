package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.nhl.bootique.env.EnvironmentProperties;

public class BQContribBinderTest {

	@Test
	public void testBindProperty() {
		Injector i = Guice.createInjector(b -> {
			BQBinder.contributeTo(b).property("a", "b");
			BQBinder.contributeTo(b).property("c", "d");

			b.bind(MapInspector.class);
		});

		MapInspector inspector = i.getInstance(MapInspector.class);

		assertEquals("b", inspector.map.get("a"));
		assertEquals("d", inspector.map.get("c"));
	}

	static class MapInspector {

		Map<String, String> map;

		@Inject
		public MapInspector(@EnvironmentProperties Map<String, String> map) {
			this.map = map;
		}
	}
}

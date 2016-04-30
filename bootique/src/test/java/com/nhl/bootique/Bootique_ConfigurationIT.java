package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;

import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.type.TypeRef;
import com.nhl.bootique.unit.BQInternalTestFactory;

public class Bootique_ConfigurationIT {

	@ClassRule
	public static BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

	@Test
	public void testConfigConfig() {
		BQRuntime runtime = runtimeFactory.newRuntime().build("--config=src/test/resources/com/nhl/bootique/test1.yml",
				"--config=src/test/resources/com/nhl/bootique/test2.yml");

		Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
				.config(new TypeRef<Map<String, String>>() {
				}, "");
		assertEquals("{a=e, c=d}", config.toString());
	}

	@Test
	public void testConfigConfig_Reverse() {
		BQRuntime runtime = runtimeFactory.newRuntime().build("--config=src/test/resources/com/nhl/bootique/test2.yml",
				"--config=src/test/resources/com/nhl/bootique/test1.yml");

		Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
				.config(new TypeRef<Map<String, String>>() {
				}, "");
		assertEquals("{a=b, c=d}", config.toString());
	}
}

package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.resource.ResourceFactory;
import com.nhl.bootique.unit.BQInternalTestFactory;
import com.nhl.bootique.unit.BQInternalWebServerTestFactory;

public class JsonNodeConfigurationFactoryProviderIT {

	@ClassRule
	public static BQInternalWebServerTestFactory WEB_CONFIG_FACTORY = new BQInternalWebServerTestFactory();

	@BeforeClass
	public static void beforeClass() {
		WEB_CONFIG_FACTORY.newRuntime().resourceUrl(new ResourceFactory("classpath:com/nhl/bootique"))
				.build("--server");
	}

	@Rule
	public BQInternalTestFactory testFactory = new BQInternalTestFactory();

	@Test
	public void testLoadConfiguration_NoConfig() {

		BQRuntime runtime = testFactory.newRuntime().build();

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{}", config.toString());
	}

	@Test
	public void testLoadConfiguration_Yaml() {

		BQRuntime runtime = testFactory.newRuntime().build("--config=http://127.0.0.1:12025/test1.yml");

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{\"a\":\"b\"}", config.toString());
	}

	@Test
	public void testLoadConfiguration_Json() {

		BQRuntime runtime = testFactory.newRuntime().build("--config=http://127.0.0.1:12025/test1.json");

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{\"x\":1}", config.toString());
	}

	@Test
	public void testLoadConfiguration_JsonYaml() {

		BQRuntime runtime = testFactory.newRuntime().build("--config=http://127.0.0.1:12025/test1.json",
				"--config=http://127.0.0.1:12025/test1.yml");

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{\"x\":1,\"a\":\"b\"}", config.toString());
	}

}

package io.bootique;

import com.fasterxml.jackson.databind.JsonNode;
import io.bootique.config.ConfigurationSource;
import io.bootique.env.Environment;
import io.bootique.jackson.JacksonService;
import io.bootique.resource.ResourceFactory;
import io.bootique.unit.BQInternalTestFactory;
import io.bootique.unit.BQInternalWebServerTestFactory;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JsonNodeConfigurationFactoryProviderIT {

	@ClassRule
	public static BQInternalWebServerTestFactory WEB_CONFIG_FACTORY = new BQInternalWebServerTestFactory();

	@BeforeClass
	public static void beforeClass() {
		WEB_CONFIG_FACTORY.app("--server").resourceUrl(new ResourceFactory("classpath:io/bootique"))
				.createRuntime();
	}

	@Rule
	public BQInternalTestFactory testFactory = new BQInternalTestFactory();

	@Test
	public void testLoadConfiguration_NoConfig() {

		BQRuntime runtime = testFactory.app().createRuntime();

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{}", config.toString());
	}

	@Test
	public void testLoadConfiguration_Yaml() {

		BQRuntime runtime = testFactory.app("--config=http://127.0.0.1:12025/test1.yml").createRuntime();

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{\"a\":\"b\"}", config.toString());
	}

	@Test
	public void testLoadConfiguration_Json() {

		BQRuntime runtime = testFactory.app("--config=http://127.0.0.1:12025/test1.json").createRuntime();

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{\"x\":1}", config.toString());
	}

	@Test
	public void testLoadConfiguration_JsonYaml() {

		BQRuntime runtime = testFactory.app("--config=http://127.0.0.1:12025/test1.json",
				"--config=http://127.0.0.1:12025/test1.yml").createRuntime();

		JsonNodeConfigurationFactoryProvider provider = new JsonNodeConfigurationFactoryProvider(
				runtime.getInstance(ConfigurationSource.class), runtime.getInstance(Environment.class),
				runtime.getInstance(JacksonService.class), runtime.getBootLogger());

		JsonNode config = provider.loadConfiguration(Collections.emptyMap(), Collections.emptyMap());
		assertEquals("{\"x\":1,\"a\":\"b\"}", config.toString());
	}

}

package io.bootique.config;

import io.bootique.cli.Cli;
import io.bootique.log.BootLogger;
import io.bootique.resource.ResourceFactory;
import io.bootique.unit.BQInternalWebServerTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URL;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class CliConfigurationSource_WebConfigSourceIT {

	@Rule
	public BQInternalWebServerTestFactory testFactory = new BQInternalWebServerTestFactory();

	private BootLogger mockBootLogger;
	private Function<URL, String> configReader;

	@Before
	public void before() throws Exception {
		this.mockBootLogger = mock(BootLogger.class);
		this.configReader = CliConfigurationSourceTest.createConfigReader();
	}

	@Test
	public void testGet_HttpUrl() {

		testFactory.app("--server").resourceUrl(new ResourceFactory("classpath:io/bootique/config"))
				.createRuntime();

		String url = "http://localhost:12025/CliConfigurationSource_WebConfigSourceIT1.yml";
		Cli cli = CliConfigurationSourceTest.createCli(url);
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(";"));
		assertEquals("g: h", config);
	}
}

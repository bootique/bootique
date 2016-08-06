package io.bootique;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import io.bootique.BQCoreModule;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.bootique.cli.Cli;
import io.bootique.config.CliConfigurationSource;
import io.bootique.log.BootLogger;

public class BQCoreModule_DefaultCliOptionsIT {

	private BootLogger mockBootLogger;

	@Before
	public void before() {
		mockBootLogger = mock(BootLogger.class);
	}

	@Test
	public void testConfigOption() {
		Injector i = injector("--config=abc.yml");
		assertEquals(i.getInstance(Cli.class).optionStrings(CliConfigurationSource.CONFIG_OPTION), "abc.yml");
	}

	@Test
	public void testConfigOptions() {
		Injector i = injector("--config=abc.yml --config=xyz.yml");
		assertEquals(i.getInstance(Cli.class).optionStrings(CliConfigurationSource.CONFIG_OPTION), "abc.yml",
				"xyz.yml");
	}

	@Test
	public void testHelpOption() {
		Injector i = injector("--help");
		assertTrue(i.getInstance(Cli.class).hasOption("help"));
	}

	@Test
	public void testNoHelpOption() {
		Injector i = injector("a b");
		assertFalse(i.getInstance(Cli.class).hasOption("help"));
	}

	private void assertEquals(Collection<String> result, String... expected) {
		assertArrayEquals(expected, result.toArray());
	}

	private Injector injector(String args) {
		String[] argsArray = args.split(" ");
		BQCoreModule module = BQCoreModule.builder().args(argsArray).bootLogger(mockBootLogger).build();
		return Guice.createInjector(module);
	}
}

package com.nhl.bootique.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.nhl.bootique.command.ConfigCommand;
import com.nhl.bootique.jopt.Options;

public class CliConfigurationSource implements ConfigurationSource {

	// TODO: this logger is invoked before Logback is configured. Maybe use
	// STDOUT?
	private static final Logger LOGGER = LoggerFactory.getLogger(CliConfigurationSource.class);

	private String location;

	@Inject
	public CliConfigurationSource(Options options) {

		Collection<String> configs = options.stringsFor(ConfigCommand.CONFIG_OPTION);
		if (configs.isEmpty()) {
			LOGGER.info("No configuration options specified");
		} else if (configs.size() == 1) {
			this.location = configs.iterator().next();
			LOGGER.info("Using configuration at " + location);
		} else {
			this.location = configs.iterator().next();
			LOGGER.info("Ignoring multiple configurations. Using configuration at " + location);
		}
	}

	@Override
	public <T> T readConfig(Function<InputStream, T> processor) {
		if (location == null) {
			return null;
		}

		try {
			return doReadConfig(processor);
		} catch (IOException e) {
			throw new RuntimeException("Error reading config: " + location, e);
		}
	}

	private <T> T doReadConfig(Function<InputStream, T> processor) throws FileNotFoundException, IOException {
		try (InputStream in = new FileInputStream(location)) {
			return processor.apply(in);
		}
	}

}

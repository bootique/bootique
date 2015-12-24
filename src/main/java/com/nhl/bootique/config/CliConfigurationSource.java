package com.nhl.bootique.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Function;

import com.google.inject.Inject;
import com.nhl.bootique.command.ConfigCommand;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.log.BootLogger;

/**
 * A {@link ConfigurationSource} that locates configuration in a file specified
 * via command-line '--config' option.
 */
public class CliConfigurationSource implements ConfigurationSource {

	private String location;

	@Inject
	public CliConfigurationSource(Options options, BootLogger bootLogger) {

		Collection<String> configs = options.stringsFor(ConfigCommand.CONFIG_OPTION);
		if (configs.isEmpty()) {
			// we are likely in boot sequence... so be quiet
		} else if (configs.size() == 1) {
			this.location = configs.iterator().next();
			bootLogger.stdout("Using configuration at " + location);
		} else {
			this.location = configs.iterator().next();
			bootLogger.stdout("Ignoring multiple configurations. Using configuration at " + location);
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

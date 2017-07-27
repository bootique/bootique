package io.bootique.config;

import io.bootique.cli.Cli;
import io.bootique.log.BootLogger;
import io.bootique.resource.ResourceFactory;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link ConfigurationSource} that locates configuration in a resource
 * specified via command-line '--config' option.
 */
public class CliConfigurationSource implements ConfigurationSource {

	public static final String CONFIG_OPTION = "config";

	private List<String> locations;
	private BootLogger bootLogger;

    public CliConfigurationSource(Cli cli, BootLogger bootLogger) {
        this.locations = cli.optionStrings(CONFIG_OPTION);
        this.bootLogger = bootLogger;
    }

	@Override
	public Stream<URL> get() {
		return locations.stream().map(this::toURL);
	}

	protected URL toURL(String location) {
		bootLogger.trace(() -> "Reading configuration at " + location);
		return new ResourceFactory(location).getUrl();
	}
}

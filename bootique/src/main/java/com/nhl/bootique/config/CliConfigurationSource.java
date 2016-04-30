package com.nhl.bootique.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.resource.ResourceFactory;

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
	public Stream<InputStream> get() {

		Collection<InputStream> streamsToClose = new ArrayList<>();
		Runnable closeHandler = () -> streamsToClose.forEach(in -> {
			try {
				in.close();
			} catch (IOException e) {
				// ignoring...
			}
		});

		return locations.stream().map(location -> openStream(location, streamsToClose)).onClose(closeHandler);
	}

	protected InputStream openStream(String location, Collection<InputStream> streamsToClose) {

		bootLogger.stdout("Reading configuration at " + location);

		try {
			InputStream in = new ResourceFactory(location).getUrl().openStream();
			streamsToClose.add(in);
			return in;
		} catch (IOException e) {
			throw new RuntimeException("Error reading config: " + location, e);
		}
	}
}

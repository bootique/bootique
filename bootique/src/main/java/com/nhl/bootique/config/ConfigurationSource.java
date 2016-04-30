package com.nhl.bootique.config;

import java.io.InputStream;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A facade that presents configuration data as a stream to consumers.
 * Configuration can be stored in a file, etc. Configuration source is agnostic
 * to the media type of configuration (JSON, YAML, etc.)
 */
public interface ConfigurationSource extends Supplier<Stream<InputStream>> {

}

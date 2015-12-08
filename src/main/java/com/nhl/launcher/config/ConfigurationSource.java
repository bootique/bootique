package com.nhl.launcher.config;

import java.io.InputStream;
import java.util.function.Function;

public interface ConfigurationSource {

	<T> T readConfig(Function<InputStream, T> processor);
}

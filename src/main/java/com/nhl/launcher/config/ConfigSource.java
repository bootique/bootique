package com.nhl.launcher.config;

public interface ConfigSource {

	<T> T config(Class<T> type);

	<T> T subconfig(String prefix, Class<T> type);
}

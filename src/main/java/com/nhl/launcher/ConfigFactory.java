package com.nhl.launcher;

public interface ConfigFactory {

	<T> T config(Class<T> type);

	<T> T subconfig(String prefix, Class<T> type);
}

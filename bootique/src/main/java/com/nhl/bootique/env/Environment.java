package com.nhl.bootique.env;

import java.util.Map;

/**
 * Provides access to system properties and environment variables for the app.
 * Allows to filter properties by prefix to separate Bootique-specific values.
 */
public interface Environment {

	String FRAMEWORK_PROPERTIES_PREFIX = "bq";
	
	/**
	 * @since 0.17
	 */
	String FRAMEWORK_VARIABLES_PREFIX = "BQ_";

	String getProperty(String name);

	/**
	 * Returns all properties in this environment that start with a given prefix
	 * plus a dot separator. The prefix is stripped from the property name in
	 * the Map.
	 * 
	 * @param prefix
	 *            a prefix to qualify properties with.
	 * @return all properties in this environment that start with a given prefix
	 *         plus a dot separator.
	 */
	Map<String, String> subproperties(String prefix);

	/**
	 * An equivalent to calling {@link #subproperties(String)} with "bq" prefix
	 * argument.
	 * 
	 * @return a map of all properties that start with "bq." prefix.
	 */
	default Map<String, String> frameworkProperties() {
		return subproperties(FRAMEWORK_PROPERTIES_PREFIX);
	}

	/**
	 * Returns a value of the environment variable with a given name.
	 * 
	 * @since 0.17
	 * @param name
	 *            environment variable name.
	 * @return a value of the environment variable with a given name.
	 */
	String getVariable(String name);

	/**
	 * Returns a map of environment variables that start with a prefix. Prefix
	 * is stripped from the returned names.
	 * 
	 * @since 0.17
	 * @param prefix
	 *            a prefix to qualify variables with.
	 * @return a map of environment variables that start with a prefix.
	 */
	Map<String, String> variables(String prefix);

	/**
	 * Returns a map of all variables that start with "BQ_" prefix. Prefix is
	 * stripped from the returned names.
	 * 
	 * @since 0.17
	 * @return a map of all variables that start with "BQ_" prefix.
	 */
	default Map<String, String> frameworkVariables() {
		return variables(FRAMEWORK_VARIABLES_PREFIX);
	}
}

package io.bootique.env;

import java.util.Map;

/**
 * Provides access to system properties and environment variables for the app.
 * Allows to filter properties by prefix to separate Bootique-specific values.
 */
public interface Environment {

    String FRAMEWORK_PROPERTIES_PREFIX = "bq";

    String getProperty(String name);

    /**
     * Returns all properties in this environment that start with a given prefix
     * plus a dot separator. The prefix is stripped from the property name in
     * the Map.
     *
     * @param prefix a prefix to qualify properties with.
     * @return all properties in this environment that start with a given prefix
     * plus a dot separator.
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
}

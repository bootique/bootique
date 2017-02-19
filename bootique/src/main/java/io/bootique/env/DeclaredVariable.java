package io.bootique.env;

import java.util.Objects;

/**
 * An environment variable exposed in the app metadata that binds a value of certain configuration path.
 *
 * @since 0.22
 */
public class DeclaredVariable {

    private String configPath;
    private String name;
    private String canonicalName;

    public DeclaredVariable(String configPath, String name, String canonicalName) {
        this.configPath = Objects.requireNonNull(configPath);
        this.name = Objects.requireNonNull(name);
        this.canonicalName = Objects.requireNonNull(canonicalName);
    }

    public String getName() {
        return name;
    }

    public String getConfigPath() {
        return configPath;
    }

    /**
     * Returns the name of the variable that is derived from the configuration path.  E.g.
     * "jdbc.myds.password" config becomes "BQ_JDBC_MYDS_PASSWORD" canonical name.
     *
     * @return the name of the variable derived from the configuration path.
     */
    public String getCanonicalName() {
        return canonicalName;
    }

    /**
     * Returns a boolean indicating whether this variable is named using Bootique "canonical" form, matching the
     * config path.
     *
     * @return a boolean indicating whether this variable is named using Bootique "canonical" form, matching the
     * config path.
     */
    public boolean isCanonical() {
        return name.equals(canonicalName);
    }
}

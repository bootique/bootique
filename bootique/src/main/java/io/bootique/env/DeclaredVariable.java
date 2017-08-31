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

    public DeclaredVariable(String configPath, String name) {
        this.configPath = Objects.requireNonNull(configPath);
        this.name = Objects.requireNonNull(name);
    }

    public String getName() {
        return name;
    }

    public String getConfigPath() {
        return configPath;
    }
}

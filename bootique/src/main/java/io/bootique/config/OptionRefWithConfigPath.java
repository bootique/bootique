package io.bootique.config;

/**
 * @since 0.27
 */
public class OptionRefWithConfigPath {
    private String optionName;
    private String configPath;
    private String defaultValue;

    public OptionRefWithConfigPath(String optionName, String configPath, String defaultValue) {
        this.optionName = optionName;
        this.configPath = configPath;
        this.defaultValue = defaultValue;
    }

    public String getOptionName() {
        return optionName;
    }

    /**
     * @return a dot-separated "path" that navigates configuration tree to the property associated with this
     * option. E.g. "jdbc.myds.password".
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * @return the default value for the option. I.e. the value that will be used if the option is provided on
     * command line without an explicit value.
     */
    public String getDefaultValue() {
        return defaultValue;
    }
}

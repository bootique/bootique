package io.bootique.config;

import io.bootique.resource.ResourceFactory;

/**
 * @since 0.25
 */
public class OptionRefWithConfig {

    private String optionName;
    private String configResourceId;

    public OptionRefWithConfig(String optionName, String configResourceId) {
        this.optionName = optionName;
        this.configResourceId = configResourceId;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getConfigResourceId() {
        return configResourceId;
    }

    public ResourceFactory getConfigResource() {
        return new ResourceFactory(configResourceId);
    }
}

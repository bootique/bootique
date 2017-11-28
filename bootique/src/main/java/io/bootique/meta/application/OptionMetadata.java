package io.bootique.meta.application;

import io.bootique.meta.MetadataNode;
import io.bootique.resource.ResourceFactory;

import java.util.Objects;

/**
 * A descriptor of a command-line option.
 *
 * @since 0.20
 */
public class OptionMetadata implements MetadataNode {

    private String name;
    private String description;
    private String shortName;
    private OptionValueCardinality valueCardinality;
    private String valueName;
    private String configPath;
    private String defaultValue;

    @Deprecated
    private ResourceFactory configResource;

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @return option short name.
     * @since 0.21
     */
    public String getShortName() {
        return (shortName != null) ? shortName : name.substring(0, 1);
    }

    public OptionValueCardinality getValueCardinality() {
        return valueCardinality;
    }

    public String getValueName() {
        return valueName;
    }

    /**
     * Returns an optional configuration path associated with this option.
     *
     * @return null or a dot-separated "path" that navigates configuration tree to the property associated with this
     * option. E.g. "jdbc.myds.password".
     * @since 0.24
     */
    public String getConfigPath() {
        return configPath;
    }

    /**
     * Returns an optional resource associated with this option.
     *
     * @return an optional resource associated with this option.
     * @since 0.24
     * @deprecated since 0.25. The new way of adding an option associated with a config file is by separately declaring
     * an option and then associating it with one or more configs via
     * {@link io.bootique.BQCoreModuleExtender#addConfigOnOption(String, String)}.
     */
    @Deprecated
    public ResourceFactory getConfigResource() {
        return configResource;
    }

    /**
     * Returns the default value for this option. I.e. the value that will be used if the option is provided on
     * command line without an explicit value.
     *
     * @return the default value for this option.
     * @since 0.24
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    public static class Builder {

        private OptionMetadata option;

        protected Builder() {
            this.option = new OptionMetadata();
            this.option.valueCardinality = OptionValueCardinality.NONE;
        }

        public Builder name(String name) {
            this.option.name = validateName(name);
            return this;
        }

        public Builder shortName(String shortName) {
            option.shortName = validateShortName(shortName);
            return this;
        }

        public Builder shortName(char shortName) {
            option.shortName = String.valueOf(shortName);
            return this;
        }

        public Builder description(String description) {
            this.option.description = description;
            return this;
        }

        public Builder valueRequired() {
            return valueRequired("");
        }

        public Builder valueRequired(String valueName) {
            this.option.valueCardinality = OptionValueCardinality.REQUIRED;
            this.option.valueName = valueName;
            return this;
        }

        public Builder valueOptional() {
            return valueOptional("");
        }

        public Builder valueOptional(String valueName) {
            this.option.valueCardinality = OptionValueCardinality.OPTIONAL;
            this.option.valueName = valueName;
            return this;
        }

        /**
         * Sets the configuration property path that should be associated to this option value.
         *
         * @param configPath a dot-separated "path" that navigates configuration tree to the desired property. E.g.
         *                   "jdbc.myds.password".
         * @return this builder instance
         * @since 0.24
         */
        public Builder configPath(String configPath) {
            this.option.configPath = Objects.requireNonNull(configPath);
            this.option.configResource = null;
            return this;
        }

        /**
         * Sets the default value for this option.
         *
         * @param defaultValue a default value for the option.
         * @return this builder instance
         * @since 0.24
         */
        public Builder defaultValue(String defaultValue) {
            this.option.defaultValue = defaultValue;
            return this;
        }

        /**
         * Sets the config resource associated with this option.
         *
         * @param configResourceId a resource path compatible with {@link io.bootique.resource.ResourceFactory} denoting
         *                         a configuration source. E.g. "a/b/my.yml", or "classpath:com/foo/another.yml".
         * @return this builder instance
         * @since 0.24
         * @deprecated since 0.25. The new way of adding an option associated with a config file is by separately declaring
         * an option and then associating it with one or more configs via
         * {@link io.bootique.BQCoreModuleExtender#addConfigOnOption(String, String)}.
         */
        public Builder configResource(String configResourceId) {
            this.option.configResource = new ResourceFactory(configResourceId);
            this.option.configPath = null;
            return this;
        }

        public OptionMetadata build() {
            validateName(option.name);
            return option;
        }

        private String validateName(String name) {
            if (name == null) {
                throw new IllegalArgumentException("Null 'name'");
            }

            if (name.length() == 0) {
                throw new IllegalArgumentException("Empty 'name'");
            }

            return name;
        }

        private String validateShortName(String shortName) {
            if (shortName == null) {
                throw new IllegalArgumentException("Null 'shortName'");
            }

            if (shortName.length() != 1) {
                throw new IllegalArgumentException("'shortName' must be exactly one char long: " + shortName);
            }

            return shortName;
        }
    }

}

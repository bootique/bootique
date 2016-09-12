package io.bootique.application;

/**
 * A descriptor of a command-line option.
 *
 * @since 0.20
 */
public class OptionMetadata extends ApplicationMetadataNode {

    private OptionValueCardinality valueCardinality;
    private String valueName;

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    public OptionValueCardinality getValueCardinality() {
        return valueCardinality;
    }

    public String getValueName() {
        return valueName;
    }

    public static class Builder {

        private OptionMetadata option;

        private Builder() {
            this.option = new OptionMetadata();
            this.option.valueCardinality = OptionValueCardinality.NONE;
        }

        public Builder name(String name) {
            this.option.name = name;
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

        public OptionMetadata build() {
            return option;
        }
    }

}

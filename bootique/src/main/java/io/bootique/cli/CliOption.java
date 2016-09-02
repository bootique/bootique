package io.bootique.cli;

/**
 * A descriptor of a command-line option.
 *
 * @since 0.12
 */
public class CliOption {

    private String name;
    private CliOptionValueCardinality valueCardinality;
    private String description;
    private String valueDescription;

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return name.substring(0, 1);
    }

    public String getDescription() {
        return description;
    }

    public CliOptionValueCardinality getValueCardinality() {
        return valueCardinality;
    }

    public String getValueDescription() {
        return valueDescription;
    }

    public static class Builder {

        private CliOption option;

        private Builder() {
            this.option = new CliOption();
            this.option.valueCardinality = CliOptionValueCardinality.NONE;
        }

        public Builder name(String name) {
            this.option.name = validateName(name);
            return this;
        }

        public Builder description(String description) {
            this.option.description = description;
            return this;
        }

        public Builder valueRequired() {
            return valueRequired("");
        }

        public Builder valueRequired(String valueDescription) {
            this.option.valueCardinality = CliOptionValueCardinality.REQUIRED;
            this.option.valueDescription = valueDescription;
            return this;
        }

        public Builder valueOptional() {
            return valueOptional("");
        }

        public Builder valueOptional(String valueDescription) {
            this.option.valueCardinality = CliOptionValueCardinality.OPTIONAL;
            this.option.valueDescription = valueDescription;
            return this;
        }

        private String validateName(String name) {
            if (name == null) {
                throw new NullPointerException("Null option name");
            }

            if (name.length() < 1) {
                throw new IllegalStateException("Empty option name");
            }

            return name;
        }

        public CliOption build() {
            return option;
        }
    }

}

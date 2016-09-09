package io.bootique.cli.meta;

/**
 * A descriptor of a command-line option.
 *
 * @since 0.12
 */
public class CliOption extends CliNode {

    private CliOptionValueCardinality valueCardinality;
    private String valueName;

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    public CliOptionValueCardinality getValueCardinality() {
        return valueCardinality;
    }

    public String getValueName() {
        return valueName;
    }

    public static class Builder {

        private CliOption option;

        private Builder() {
            this.option = new CliOption();
            this.option.valueCardinality = CliOptionValueCardinality.NONE;
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
            this.option.valueCardinality = CliOptionValueCardinality.REQUIRED;
            this.option.valueName = valueName;
            return this;
        }

        public Builder valueOptional() {
            return valueOptional("");
        }

        public Builder valueOptional(String valueName) {
            this.option.valueCardinality = CliOptionValueCardinality.OPTIONAL;
            this.option.valueName = valueName;
            return this;
        }

        public CliOption build() {
            return option;
        }
    }

}

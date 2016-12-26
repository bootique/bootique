package io.bootique.application;

/**
 * A descriptor of a command-line option.
 *
 * @since 0.20
 */
public class OptionMetadata extends ApplicationMetadataNode {

    private String shortName;
    private OptionValueCardinality valueCardinality;
    private String valueName;

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }

    /**
     * @since 0.21
     * @return option short name.
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

    public static class Builder {

        private OptionMetadata option;

        private Builder() {
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

        public OptionMetadata build() {
            validateName(option.name);
            return option;
        }

        private String validateName(String name) {
            if(name == null) {
                throw new IllegalArgumentException("Null 'name'");
            }

            if(name.length() == 0) {
                throw new IllegalArgumentException("Empty 'name'");
            }

            return name;
        }

        private String validateShortName(String shortName) {
            if(shortName == null) {
                throw new IllegalArgumentException("Null 'shortName'");
            }

            if(shortName.length() != 1) {
                throw new IllegalArgumentException("'shortName' must be exactly one char long: " + shortName);
            }

            return shortName;
        }
    }

}

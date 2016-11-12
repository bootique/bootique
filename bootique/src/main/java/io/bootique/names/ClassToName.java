package io.bootique.names;

import java.util.Objects;

/**
 * A configurable strategy for converting Java class names to command names, configuration keys, etc.
 *
 * @since 0.21
 */
public class ClassToName {

    private String stripSuffix;
    private boolean convertToLowerCase;
    private String partsSeparator;

    private ClassToName() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toName(Class<?> type) {

        String name = type.getSimpleName();

        if (stripSuffix != null && name.endsWith(stripSuffix)) {
            name = name.substring(0, name.length() - stripSuffix.length());
        }

        if (partsSeparator != null) {

            StringBuilder transformed = new StringBuilder();

            for (char c : name.toCharArray()) {
                if(Character.isUpperCase(c) && transformed.length() > 0) {
                    transformed.append(partsSeparator);
                }

                transformed.append(c);
            }

            name = transformed.toString();
        }

        if (convertToLowerCase) {
            name = name.toLowerCase();
        }

        return name;
    }

    public static class Builder {
        private ClassToName strategy;

        private Builder() {
            this.strategy = new ClassToName();
        }

        public ClassToName build() {
            return strategy;
        }

        public Builder stripSuffix(String suffix) {
            if (Objects.requireNonNull(suffix).length() == 0) {
                throw new IllegalArgumentException("Empty suffix");
            }

            strategy.stripSuffix = suffix;
            return this;
        }

        public Builder convertToLowerCase() {
            strategy.convertToLowerCase = true;
            return this;
        }

        public Builder partsSeparator(String separator) {
            if (Objects.requireNonNull(separator).length() == 0) {
                throw new IllegalArgumentException("Empty separator");
            }

            strategy.partsSeparator = separator;
            return this;
        }
    }
}

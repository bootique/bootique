package io.bootique.names;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A configurable strategy for converting Java class names to command names, configuration keys, etc.
 *
 * @since 0.21
 */
public class ClassToName {

    private List<String> stripSuffixes;
    private boolean convertToLowerCase;
    private String partsSeparator;

    private ClassToName() {
        this.stripSuffixes = new ArrayList<>(2);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toName(Class<?> type) {

        String name = type.getSimpleName();

        name = applyStripSuffixes(name);
        name = applyPartsSeparator(name);
        name = applyCaseConversion(name);

        return name;
    }

    protected String applyStripSuffixes(String name) {
        
        // the order of suffixes matters... strip them in the same order they were added to the builder
        for (String suffix : stripSuffixes) {
            name = applyStripSuffix(name, suffix);
        }

        return name;
    }

    protected String applyStripSuffix(String name, String stripSuffix) {

        return stripSuffix != null && name.endsWith(stripSuffix)
                ? name.substring(0, name.length() - stripSuffix.length())
                : name;
    }

    protected String applyCaseConversion(String name) {
        return convertToLowerCase ? name.toLowerCase() : name;
    }

    protected String applyPartsSeparator(String name) {
        if (partsSeparator == null) {
            return name;
        }

        StringBuilder transformed = new StringBuilder();

        boolean wasUpper = false;
        for (char c : name.toCharArray()) {

            boolean isUpper = Character.isUpperCase(c);

            if (isUpper && transformed.length() > 0) {

                if (!wasUpper) {
                    transformed.append(partsSeparator);
                }
                wasUpper = true;
            } else {
                wasUpper = isUpper;
            }

            transformed.append(c);
        }

        return transformed.toString();
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

            strategy.stripSuffixes.add(suffix);
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

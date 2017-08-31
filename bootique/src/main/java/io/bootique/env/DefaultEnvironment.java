package io.bootique.env;

import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * An {@link Environment} implementation that reads properties and variables from the Map passed in constructor.
 */
public class DefaultEnvironment implements Environment {

    /**
     * If present, enables boot sequence tracing to STDERR.
     */
    public static final String TRACE_PROPERTY = "bq.trace";

    private Map<String, String> properties;
    private Map<String, String> variables;

    public static Builder withSystemPropertiesAndVariables(BootLogger logger) {
        return new Builder(logger).includeSystemProperties().includeSystemVariables();
    }

    protected DefaultEnvironment() {
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, String> subproperties(String prefix) {
        return filterByPrefix(properties, prefix, ".");
    }

    @Override
    public String getVariable(String name) {
        return variables.get(name);
    }

    @Override
    public Map<String, String> variables(String prefix) {
        return filterByPrefix(variables, prefix, "_");
    }

    protected Map<String, String> filterByPrefix(Map<String, String> unfiltered, String prefix, String separator) {
        String lPrefix = prefix.endsWith(separator) ? prefix : prefix + separator;
        int len = lPrefix.length();

        return unfiltered.entrySet().stream().filter(e -> e.getKey().startsWith(lPrefix))
                .collect(toMap(e -> e.getKey().substring(len), e -> e.getValue()));
    }

    public static class Builder {
        private Map<String, String> properties;
        private Map<String, String> diVariables;
        private Collection<DeclaredVariable> declaredVariables;
        private boolean includeSystemProperties;
        private boolean includeSystemVariables;
        private BootLogger logger;

        private Builder(BootLogger logger) {
            this.logger = logger;
        }

        public DefaultEnvironment build() {

            DefaultEnvironment env = new DefaultEnvironment();

            env.properties = buildProperties();
            env.variables = buildVariables();

            return env;
        }

        public Builder includeSystemProperties() {
            includeSystemProperties = true;
            return this;
        }

        public Builder includeSystemVariables() {
            includeSystemVariables = true;
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            this.properties = properties;
            return this;
        }

        public Builder diVariables(Map<String, String> diVariables) {
            this.diVariables = diVariables;
            return this;
        }

        public Builder declaredVariables(Collection<DeclaredVariable> declaredVariables) {
            this.declaredVariables = declaredVariables;
            return this;
        }

        protected Map<String, String> buildProperties() {

            Map<String, String> properties = new HashMap<>();
            if (this.properties != null) {
                properties.putAll(this.properties);
            }

            if (includeSystemProperties) {
                // override DI props from system...
                System.getProperties().forEach((k, v) -> properties.put((String) k, (String) v));
            }

            return properties;
        }

        protected Map<String, String> buildVariables() {

            Map<String, String> allVars = new HashMap<>();

            diVariables.keySet().forEach(this::warnOfDeprecatedVar);
            allVars.putAll(canonicalizeVariableNames(this.diVariables));

            if (includeSystemVariables) {
                Map<String, String> systemVars = System.getenv();

                // check for BQ_* shell vars
                systemVars.keySet().forEach(this::warnOfDeprecatedVar);
                allVars.putAll(canonicalizeVariableNames(systemVars));
            }

            return allVars;
        }

        protected Map<String, String> canonicalizeVariableNames(Map<String, String> vars) {

            if (declaredVariables == null || declaredVariables.isEmpty() || vars.isEmpty()) {
                return vars;
            }

            Map<String, String> canonical = new HashMap<>(vars);

            declaredVariables.forEach(dv -> {
                if (!dv.isCanonical()) {

                    String val = vars.get(dv.getName());
                    if (val != null) {
                        String existingVal = canonical.putIfAbsent(dv.getCanonicalName(), val);

                        // sanity check
                        if (existingVal != null && !existingVal.equals(val)) {
                            String message = String.format("Conflict canonicalizing var name. Public (%s) and " +
                                            "canonical (%s) names are bound to different values.",
                                    dv.getName(),
                                    dv.getCanonicalName());

                            throw new RuntimeException(message);
                        }
                    }
                }
            });

            return canonical;
        }

        //  Will go away when we stop supporting BQ_ vars completely.
        @Deprecated
        private void warnOfDeprecatedVar(String var) {

            if (var.startsWith(FRAMEWORK_VARIABLES_PREFIX)) {
                logger.stderr(("WARN: The use of BQ_* variables is deprecated. Consider declaring '"
                        + var
                        + "' explicitly using an app-specific name."));
            }
        }
    }
}

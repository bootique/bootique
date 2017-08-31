package io.bootique.env;

import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        private Map<String, String> variables;
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

        public Builder variables(Map<String, String> variables) {
            this.variables = variables;
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

            //warn if there are some variables overriding app configuration
            warnAboutNotDeclaredVars();

            Map<String, String> vars = new HashMap<>();
            vars.putAll(canonicalizeVariableNames(this.variables));

            if (includeSystemVariables) {
                vars.putAll(canonicalizeVariableNames(System.getenv()));
            }

            return vars;
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

        /**
         * Checks and prints warning if there are some undeclared vars.
         * This is the first step to remove BQ_* prefixed variables.
         * <p>
         * Currently BQ_* variables declared via {@link io.bootique.BQCoreModuleExtender#setVar(String, String)}
         * {@link io.bootique.BQCoreModuleExtender#setVars(Map)} are overridden with real BQ_* shell vars.
         * It can lead to some side effects whose cause is hard to be found (e.g. wrong values or extra vars in a env).
         * <p>
         * New approach: BQ_* shell variables and variables contributed in BQ are checked against vars declared via
         * {@link io.bootique.BQCoreModuleExtender#declareVar(String, String)} or
         * {@link io.bootique.BQCoreModuleExtender#declareVar(String)}.
         * So that user should explicitly control what vars must be used as a part of app configuration.
         * <p>
         */
        private void warnAboutNotDeclaredVars() {
            StringBuilder warn = new StringBuilder();

            //print BQ_* shell vars
            List<String> envVars = System.getenv().keySet().stream()
                    .filter(var -> var.startsWith(FRAMEWORK_VARIABLES_PREFIX))
                    .collect(Collectors.toList());
            if (!envVars.isEmpty()) {
                warn.append("WARNING: App JSON/YAML configuration will be overridden by ");
                warn.append(String.format("not app-controlled shell vars:\n%s\n", envVars));
            }

            //print BQ vars
            List<String> bqVars = variables.entrySet().stream()
                    .filter(var -> var.getKey().startsWith(FRAMEWORK_VARIABLES_PREFIX))
                    .map(var -> var.getKey())
                    .collect(Collectors.toList());

            if (!bqVars.isEmpty()) {
                warn.append(warn.length() == 0 ? "WARNING: App JSON/YAML configuration will be overridden by " : "And ");
                warn.append(String.format("BQ_* vars contributed into BQ:\n %s ", bqVars));
            }

            logger.stdout(warn.toString());
        }
    }
}

package io.bootique.env;

import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * An {@link Environment} implementation that reads properties from the Map
 * passed on constructor.
 */
public class DefaultEnvironment implements Environment {

    /**
     * If present, enables boot sequence tracing to STDERR.
     */
    public static final String TRACE_PROPERTY = "bq.trace";

    private Map<String, String> properties;
    private Map<String, String> variables;

    public DefaultEnvironment(Map<String, String> diProperties,
                              Map<String, String> diVariables,
                              Map<String, String> variableAliases) {

        this.properties = new HashMap<>(diProperties);
        this.variables = new HashMap<>(resolveAliases(diVariables, variableAliases));

        // override DI props from system...
        System.getProperties().forEach((k, v) -> properties.put((String) k, (String) v));
        resolveAliases(System.getenv(), variableAliases).forEach((k, v) -> variables.put(k, v));
    }

    protected static Map<String, String> resolveAliases(Map<String, String> in, Map<String, String> keyAliases) {

        if (keyAliases.isEmpty() || in.isEmpty()) {
            return in;
        }

        Map<String, String> resolved = new HashMap<>(in);

        keyAliases.forEach((k, v) -> {
            if (!k.equals(v)) {
                String aliasedVal = resolved.get(v);
                if (aliasedVal != null) {
                    String unaliasedVal = resolved.putIfAbsent(k, aliasedVal);
                    if (unaliasedVal != null && !unaliasedVal.equals(aliasedVal)) {
                        String message = String.format("Can't resolve aliases. Both aliased (%s) and unaliased (%s) " +
                                        "values are defined.",
                                k, v);
                        throw new RuntimeException(message);
                    }
                }
            }
        });

        return resolved;
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
}

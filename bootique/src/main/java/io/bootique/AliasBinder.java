package io.bootique;

import com.google.inject.multibindings.MapBinder;
import io.bootique.env.Environment;

/**
 * Provides fluent API for exposing application configuration variables. "Exposing" means inclusion in the help
 * "ENVIRONMENT" section. Variables can be exposed under their "canonical" names derived from Bootique configuration
 * structure (e.g. BQ_JDBC_DS1_PASSWORD), or be aliased to an arbitrary, presumably more application-centric name
 * (e.g. MYAPP_DB_PASSWORD).
 *
 * @since 0.22
 */
public class AliasBinder {

    private MapBinder<String, String> publicVarBinder;
    private String configPath;

    public AliasBinder(MapBinder<String, String> publicVarBinder, String configPath) {
        this.publicVarBinder = publicVarBinder;
        this.configPath = configPath;
    }

    /**
     * Exposes the variable under an alternative, presumably human-readable name.
     *
     * @param alias a variable name to be bound to a given property path.
     */
    public void as(String alias) {
        publicVarBinder.addBinding(alias).toInstance(configPath);
    }

    /**
     * Exposes the variable without renaming, using its canonical name.
     */
    public void asIs() {
        as(getCanonicalVariableName());
    }

    protected String getCanonicalVariableName() {

        StringBuilder varName = new StringBuilder(Environment.FRAMEWORK_VARIABLES_PREFIX);
        int dot;
        String subpath = this.configPath;
        while((dot = subpath.indexOf('.')) >= 0) {
            varName.append(subpath.substring(0, dot).toUpperCase()).append('_');
            subpath = subpath.substring(dot + 1);
        }

        varName.append(subpath.toUpperCase());

        return varName.toString();
    }
}

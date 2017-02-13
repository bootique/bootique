package io.bootique;

import com.google.inject.multibindings.MapBinder;

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
    private String canonicalName;

    public AliasBinder(MapBinder<String, String> publicVarBinder, String canonicalName) {
        this.publicVarBinder = publicVarBinder;
        this.canonicalName = canonicalName;
    }

    /**
     * Exposes the variable under an alternative, presumably human-readable name.
     *
     * @param alias
     */
    public void as(String alias) {
        publicVarBinder.addBinding(canonicalName).toInstance(alias);
    }

    /**
     * Exposes the variable without renaming, using its canonical name.
     */
    public void asIs() {
        as(canonicalName);
    }
}

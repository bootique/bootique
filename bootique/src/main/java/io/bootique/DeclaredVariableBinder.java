package io.bootique;

import com.google.inject.multibindings.Multibinder;
import io.bootique.env.DeclaredVariable;
import io.bootique.env.Environment;

/**
 * Provides fluent API for defining configuration variable declarations.
 *
 * @since 0.22
 */
class DeclaredVariableBinder {

    private Multibinder<DeclaredVariable> binder;
    private String configPath;

    DeclaredVariableBinder(Multibinder<DeclaredVariable> binder, String configPath) {
        this.binder = binder;
        this.configPath = configPath;
    }

    /**
     * Declares the variable with the user-specified name.
     *
     * @param name a variable name to be bound to a given property path.
     */
    public void withName(String name) {
        withNames(name, getCanonicalVariableName());
    }

    /**
     * Declares the variable with a "canonical" Bootique name calculated from this binder's config path. E.g.
     * "jdbc.myds.password" becomes "BQ_JDBC_MYDS_PASSWORD".
     */
    public void withCanonicalName() {
        String canonical = getCanonicalVariableName();
        withNames(canonical, canonical);
    }

    protected void withNames(String name, String canonicalName) {
        binder.addBinding().toInstance(new DeclaredVariable(configPath, name, canonicalName));
    }

    protected String getCanonicalVariableName() {

        StringBuilder varName = new StringBuilder(Environment.FRAMEWORK_VARIABLES_PREFIX);
        int dot;
        String subpath = this.configPath;
        while ((dot = subpath.indexOf('.')) >= 0) {
            varName.append(subpath.substring(0, dot).toUpperCase()).append('_');
            subpath = subpath.substring(dot + 1);
        }

        varName.append(subpath.toUpperCase());

        return varName.toString();
    }
}

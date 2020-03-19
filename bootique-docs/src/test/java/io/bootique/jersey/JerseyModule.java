package io.bootique.jersey;

import io.bootique.di.BaseBQModule;
import io.bootique.di.Binder;

/**
 * This is a stub for JerseyModule API for a documentation only,
 * because we can't use Jersey module here, as it will create circular dependency.
 */
public class JerseyModule extends BaseBQModule {
    public static Extender extend(Binder binder) {
        return new Extender();
    }
    public static class Extender {
        public void addResource(Class<?> resource) {
        }
    }
}

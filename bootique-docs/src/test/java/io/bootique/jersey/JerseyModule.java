package io.bootique.jersey;

import io.bootique.di.BaseBQModule;
import io.bootique.di.Binder;

// fake JerseyModule for docs
public class JerseyModule extends BaseBQModule {

    public static Extender extend(Binder binder) {
        return new Extender();
    }

    public static class Extender {
        public void addResource(Class<?> resource) {
        }
    }
}

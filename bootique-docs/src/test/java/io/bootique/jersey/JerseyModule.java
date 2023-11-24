package io.bootique.jersey;

import io.bootique.di.BQModule;
import io.bootique.di.Binder;

// fake JerseyModule for docs
public class JerseyModule implements BQModule {

    public static Extender extend(Binder binder) {
        return new Extender();
    }

    @Override
    public void configure(Binder binder) {
    }

    public static class Extender {
        public void addResource(Class<?> resource) {
        }
    }
}

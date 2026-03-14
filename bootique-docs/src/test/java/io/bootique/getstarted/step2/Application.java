package io.bootique.getstarted.step2;

// tag::all[

import io.bootique.Bootique;
import io.bootique.BQModule;
import io.bootique.di.Binder;

public class Application {

    public static void main(String[] args) {

        BQModule module = binder ->
                JerseyModule.extend(binder).addApiResource(HelloResource.class); // <1>

        Bootique
                .app(args)
                .module(module) // <2>
                .autoLoadModules()
                .exec()
                .exit();
    }
}
// end::all[]

// fake JerseyModule for docs
class JerseyModule implements BQModule {

    public static Extender extend(Binder binder) {
        return new Extender();
    }

    @Override
    public void configure(Binder binder) {
    }

    public static class Extender {
        public void addApiResource(Class<?> resource) {
        }
    }
}


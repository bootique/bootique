package io.bootique.docs.programming.configuration.yaml;

import io.bootique.BQModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import jakarta.inject.Singleton;


public class MyObjectModule implements BQModule {

    // tag::MyObject[]
    @Singleton
    @Provides
    public MyObject createMyService(ConfigurationFactory configFactory) {
        return configFactory.config(MyObject.class, "my");
    }
    // end::MyObject[]


    @Override
    public void configure(Binder binder) {
    }
}


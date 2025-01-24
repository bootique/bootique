package io.bootique.docs.programming.configuration.yaml;

import io.bootique.BQModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.docs.programming.configuration.MyService;
import jakarta.inject.Singleton;


public class MyFactoryModule implements BQModule {

    // tag::MyFactory[]
    @Singleton
    @Provides
    public MyService provideMyService(
            ConfigurationFactory configFactory,
            SomeOtherService service) {

        return configFactory.config(MyFactory.class, "my").createMyService(service);
    }
    // end::MyFactory[]

    @Override
    public void configure(Binder binder) {
    }
}


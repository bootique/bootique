package io.bootique.docs.programming.configuration.polymorphic.factory;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.PolymorphicConfiguration;
import io.bootique.di.Provides;
import io.bootique.docs.programming.configuration.polymorphic.BaseType;

// tag::polymorphic[]
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = ConcreteTypeFactory1.class)
public abstract class BaseTypeFactory implements PolymorphicConfiguration {

    public abstract BaseType create();
    // end::polymorphic[]

    // tag::polymorphicProvides[]
    @Provides
    public BaseType provideBaseType(ConfigurationFactory configFactory) {

        return configFactory
                .config(BaseTypeFactory.class, "my")
                .create();
    }
    // end::polymorphicProvides[]
    // tag::polymorphic[]
}

// end::polymorphic[]


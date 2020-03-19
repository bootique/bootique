package io.bootique.docs.programming.configuration.polymorphic.factory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.docs.programming.configuration.polymorphic.BaseType;
import io.bootique.docs.programming.configuration.polymorphic.ConcreteType2;

// tag::polymorphic[]
@JsonTypeName("type2")
public class ConcreteTypeFactory2 extends BaseTypeFactory {

    @Override
    public BaseType create() {
        return new ConcreteType2();
    }
}

// end::polymorphic[]


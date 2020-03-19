package io.bootique.docs.programming.configuration.polymorphic.factory;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.bootique.docs.programming.configuration.polymorphic.BaseType;
import io.bootique.docs.programming.configuration.polymorphic.ConcreteType1;

// tag::polymorphic[]
@JsonTypeName("type1")
public class ConcreteTypeFactory1 extends BaseTypeFactory {

    @Override
    public BaseType create() {
        return new ConcreteType1();
    }
}

// end::polymorphic[]


package io.bootique.docs.programming.configuration.yaml;


import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;

import javax.inject.Inject;

// tag::MyObject[]
@BQConfig // <1>
public class MyObject {

    final SomeOtherService soService;

    private int intProperty;
    private String stringProperty;

    @Inject
    public MyObject(SomeOtherService soService) {  // <2>
        this.soService = soService;
    }

    @BQConfigProperty // <3>
    public void setIntProperty(int i) {
        this.intProperty = i;
    }

    @BQConfigProperty // <4>
    public void setStringProperty(String s) {
        this.stringProperty = s;
    }

    public void doSomething() {
        // ..
    }
}
// end::MyObject[]



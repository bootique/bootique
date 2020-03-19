package io.bootique.docs.programming.configuration.yaml;


import io.bootique.docs.programming.configuration.MyService;

// tag::MyFactory[]
public class MyFactory {

    private int intProperty;
    private String stringProperty;

    public void setIntProperty(int i) {
        this.intProperty = i;
    }

    public void setStringProperty(String s) {
        this.stringProperty = s;
    }

    // factory method
    public MyService createMyService(SomeOtherService soService) {
        return new MyServiceImpl(soService, intProperty, stringProperty);
    }
}
// end::MyFactory[]



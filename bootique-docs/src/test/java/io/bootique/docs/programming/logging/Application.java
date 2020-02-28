package io.bootique.docs.programming.logging;

import io.bootique.Bootique;

// tag::Logging[]
public class Application {
    public static void main(String[] args) {
        Bootique.app(args).bootLogger(new MyBootLogger()).createRuntime().run();
    }
}
// end::Logging[]

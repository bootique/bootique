// tag::Application[]
// tag::Main[]
package io.bootique.getstarted.step1;

// tag::all[]
import io.bootique.Bootique;

public class Application {

    public static void main(String[] args) {
        Bootique
                .app(args)
                .autoLoadModules()
                .exec()
                .exit();
    }
}
// end::all[]

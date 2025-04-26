// tag::HelloResource[]
// tag::HelloInjectResource[]
package com.foo;

import io.bootique.annotation.Args;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

// end::HelloResource[]
// tag::HelloResource[]

@Path("/")
public class HelloResource {
    // end::HelloResource[]

    @Inject
    @Args
    private String[] args;
    // tag::HelloResource[]

    @GET
    public String hello() {
        // end::HelloResource[]
        String allArgs = String.join(" ", args);
        // end::HelloInjectResource[]
        if (allArgs.equals(" ")) {
        // tag::HelloResource[]
        return "Hello, world!";
        // end::HelloResource[]
        } else {
        // tag::HelloInjectResource[]
        return "Hello, world! The app was started with these args: " + allArgs;
        // end::HelloInjectResource[]
        }
        // tag::HelloInjectResource[]
        // tag::HelloResource[]
    }
}
// end::HelloResource[]
// end::HelloInjectResource[]
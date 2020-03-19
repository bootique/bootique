// tag::HelloResource[]
// tag::HelloInjectResource[]
package com.foo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
// end::HelloResource[]
import javax.inject.Inject;

import io.bootique.annotation.Args;
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
        return "Hello, world! The app was started with the following arguments: " + allArgs;
        // end::HelloInjectResource[]
        }
        // tag::HelloInjectResource[]
        // tag::HelloResource[]
    }
}
// end::HelloResource[]
// end::HelloInjectResource[]
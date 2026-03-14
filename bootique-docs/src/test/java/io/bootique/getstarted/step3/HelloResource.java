package io.bootique.getstarted.step3;

// tag::all[]
import io.bootique.annotation.Args;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HelloResource {

    @Inject
    @Args
    private String[] args;

    @GET
    public String hello() {
        String allArgs = String.join(" ", args);
        return "Hello, world! The app was started with these args: " + allArgs;
    }
}
// end::all[]
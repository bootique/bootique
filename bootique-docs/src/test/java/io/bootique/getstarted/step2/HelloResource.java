package io.bootique.getstarted.step2;

// tag::all[]
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HelloResource {

    @GET
    public String hello() {
        return "Hello, world!";
    }
}
// end::all[]
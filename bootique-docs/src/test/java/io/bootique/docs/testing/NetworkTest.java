package io.bootique.docs.testing;

import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BQTest
public class NetworkTest {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    @Disabled("No real Jersey module available")
    // tag::Testing[]
    @Test
    public void testServer() {

        testFactory.app("--server").autoLoadModules().run();

        // using JAX-RS client API
        WebTarget base = ClientBuilder.newClient().target("http://localhost:8080/");
        Response r1 = base.path("/somepath").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), r1.getStatus());
        assertEquals("{}", r1.readEntity(String.class));
    }
    // end::Testing[]
}

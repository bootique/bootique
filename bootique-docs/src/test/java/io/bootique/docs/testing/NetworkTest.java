package io.bootique.docs.testing;

import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class NetworkTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

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

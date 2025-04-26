package io.bootique.docs.testing;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.docs.FakeServerCommand;
import io.bootique.jetty.JettyTester;
import io.bootique.junit5.BQApp;
import io.bootique.junit5.BQTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@BQTest
public class NetworkTest {

    // tag::Testing[]
    // a tool from "bootique-jetty-junit5". Doesn't require @BQTestTool,
    // as it has no lifecycle of its own
    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("--server")
            .module(jetty.moduleReplacingConnectors())
            // end::Testing[]
            .module(b -> BQCoreModule.extend(b).addCommand(FakeServerCommand.class))
            // tag::Testing[]
            .createRuntime();

    // end::Testing[]
    @Disabled("No real Jersey module available")
    // tag::Testing[]
    @Test
    public void server() {
        Response response = jetty.getTarget().path("/somepath").request().get();
        JettyTester.assertOk(response);
    }
    // end::Testing[]
}

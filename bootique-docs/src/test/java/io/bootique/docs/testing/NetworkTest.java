package io.bootique.docs.testing;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.junit.BQApp;
import io.bootique.junit.BQTest;
import io.bootique.meta.application.CommandMetadata;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@BQTest
public class NetworkTest {

    // tag::all[]
    // a tool from "bootique-jetty-junit". Doesn't require @BQTestTool,
    // as it has no lifecycle of its own
    static final JettyTester jetty = JettyTester.create();

    @BQApp
    static final BQRuntime app = Bootique
            .app("--server")
            .module(jetty.moduleReplacingConnectors())
            // end::all[]
            .module(b -> BQCoreModule.extend(b).addCommand(ServerCommand.class))
            // tag::all[]
            .createRuntime();

    // end::all[]
    @Disabled("No real Jersey module available")
    // tag::all[]
    @Test
    public void server() {
        Response response = jetty.getTarget().path("/somepath").request().get();
        JettyTester.assertOk(response);
    }
    // end::all[]

    // fake JettyTester for docs
    static class JettyTester {

        public static JettyTester create() {
            return new JettyTester();
        }

        public static void assertOk(Response response) {
        }

        public WebTarget getTarget() {
            return null;
        }

        public BQModule moduleReplacingConnectors() {
            return b -> {
            };
        }
    }

    // fake ServerCommand
    static class ServerCommand extends CommandWithMetadata {

        public ServerCommand() {
            super(CommandMetadata.builder("server").build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }
}


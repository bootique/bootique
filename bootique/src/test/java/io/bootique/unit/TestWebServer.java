/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.unit;

import io.bootique.BQCoreModule;
import io.bootique.BQModule;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.resource.ResourceFactory;
import io.bootique.shutdown.ShutdownManager;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestWebServer implements BeforeAllCallback, AfterAllCallback {

    private final BQRuntime webServer;

    public TestWebServer(String resourceBase) {
        this.webServer = Bootique.app("--server")
                .module(new WebServerModule(resourceBase))
                .createRuntime();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        CommandOutcome run = webServer.run();
        assertTrue(run.isSuccess());
        assertTrue(run.forkedToBackground());
        assertTrue(webServer.getInstance(Server.class).isStarted());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        webServer.shutdown();
    }

    static class WebServerModule implements BQModule {

        final String resourceBase;

        WebServerModule(String resourceBase) {
            this.resourceBase = resourceBase;
        }

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(ServerCommand.class);
        }

        @Provides
        @Singleton
        Server provideServer(ShutdownManager shutdownManager) {
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);

            // TODO: dynamic port
            connector.setPort(12025);
            server.addConnector(connector);

            ServletContextHandler handler = new ServletContextHandler();
            handler.setContextPath("/");

            DefaultServlet servlet = new DefaultServlet();

            ServletHolder holder = new ServletHolder(servlet);
            handler.addServlet(holder, "/*");
            handler.setBaseResourceAsString(new ResourceFactory(resourceBase).getUrl().toExternalForm());

            server.setHandler(handler);

            shutdownManager.onShutdown(server::stop);

            return server;
        }

        static class ServerCommand implements Command {

            @Inject
            private Provider<Server> serverProvider;

            @Override
            public CommandOutcome run(Cli cli) {
                Server server = serverProvider.get();
                try {
                    server.start();
                } catch (Exception e) {
                    return CommandOutcome.failed(1, e);
                }

                return CommandOutcome.succeededAndForkedToBackground();
            }
        }
    }
}

package io.bootique.unit;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.env.Environment;
import io.bootique.resource.ResourceFactory;
import io.bootique.shutdown.ShutdownManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * A test factory that serves static resources out of "target"
 */
public class BQInternalWebServerTestFactory extends BQInternalDaemonTestFactory {

    @Override
    public Builder app(String... args) {
        Function<BQRuntime, Boolean> startupCheck = r -> r.getInstance(Server.class).isStarted();
        return new Builder(runtimes, executor, args)
                .startupCheck(startupCheck)
                .module(new InternalJettyModule());
    }

    public static class Builder extends BQInternalDaemonTestFactory.Builder<Builder> {

        private ResourceFactory resourceUrl;

        Builder(Collection<BQRuntime> runtimes, ExecutorService executor, String[] args) {
            super(runtimes, executor, args);
            this.resourceUrl = new ResourceFactory("classpath:");
        }

        public Builder resourceUrl(ResourceFactory resourceUrl) {
            this.resourceUrl = resourceUrl;
            return this;
        }

        @Override
        public BQRuntime createRuntime() {
            property("bq.internaljetty.base", resourceUrl.getUrl().toExternalForm());
            return super.createRuntime();
        }
    }

    static class InternalJettyModule implements Module {

        @Override
        public void configure(Binder binder) {
            BQCoreModule.extend(binder).addCommand(ServerCommand.class);
        }

        @Provides
        @Singleton
        Server provideServer(Environment env, ShutdownManager shutdownManager) {
            Server server = new Server();

            ServerConnector connector = new ServerConnector(server);
            connector.setPort(12025);
            server.addConnector(connector);

            ServletContextHandler handler = new ServletContextHandler();
            handler.setContextPath("/");

            DefaultServlet servlet = new DefaultServlet();

            ServletHolder holder = new ServletHolder(servlet);
            handler.addServlet(holder, "/*");
            handler.setResourceBase(env.getProperty("bq.internaljetty.base"));

            server.setHandler(handler);

            shutdownManager.addShutdownHook(() -> {
                server.stop();
            });

            return server;
        }
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

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                return CommandOutcome.failed(1, e);
            }

            return CommandOutcome.succeeded();
        }
    }
}

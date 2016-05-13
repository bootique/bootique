package com.nhl.bootique.unit;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.resource.ResourceFactory;
import com.nhl.bootique.shutdown.ShutdownManager;

/**
 * A test factory that serves static resources out of "target"
 */
public class BQInternalWebServerTestFactory extends BQInternalDaemonTestFactory {

	@Override
	public Builder newRuntime() {
		Function<BQRuntime, Boolean> startupCheck = r -> r.getInstance(Server.class).isStarted();
		return new Builder(runtimes, executor).startupCheck(startupCheck)
				.configurator(b -> b.module(new InternalJettyModule()));
	}

	public static class Builder extends BQInternalDaemonTestFactory.Builder {

		private ResourceFactory resourceUrl;

		Builder(Collection<BQRuntime> runtimes, ExecutorService executor) {
			super(runtimes, executor);
			this.resourceUrl = new ResourceFactory("classpath:");
		}

		public Builder resourceUrl(ResourceFactory resourceUrl) {
			this.resourceUrl = resourceUrl;
			return this;
		}

		@Override
		public Builder property(String key, String value) {
			return (Builder) super.property(key, value);
		}

		@Override
		public Builder configurator(Consumer<Bootique> configurator) {
			return (Builder) super.configurator(configurator);
		}

		@Override
		public Builder var(String key, String value) {
			return (Builder) super.var(key, value);
		}

		@Override
		public Builder startupCheck(Function<BQRuntime, Boolean> startupCheck) {
			return (Builder) super.startupCheck(startupCheck);
		}

		@Override
		public Builder startupTimeout(long timeout, TimeUnit unit) {
			return (Builder) super.startupTimeout(timeout, unit);
		}

		@Override
		public BQRuntime build(String... args) {
			property("bq.internaljetty.base", resourceUrl.getUrl().toExternalForm());
			return super.build(args);
		}
	}

	static class InternalJettyModule implements Module {

		@Override
		public void configure(Binder binder) {
			BQCoreModule.contributeCommands(binder).addBinding().to(ServerCommand.class);
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

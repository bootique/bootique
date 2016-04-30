package com.nhl.bootique.config;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.log.BootLogger;

public class CliConfigurationSource_WebConfigSourceIT {

	private Server jetty;
	private BootLogger mockBootLogger;
	private Function<InputStream, String> configReader;

	@Before
	public void before() throws Exception {
		this.mockBootLogger = mock(BootLogger.class);
		this.configReader = CliConfigurationSourceTest.createConfigReader();
		startServer();
	}

	@After
	public void after() throws Exception {
		jetty.stop();
	}

	private void startServer() throws Exception {
		this.jetty = new Server();

		ServerConnector connector = new ServerConnector(this.jetty);
		connector.setPort(12025);
		jetty.addConnector(connector);

		ServletContextHandler handler = new ServletContextHandler();
		handler.setContextPath("/");

		ServletHolder holder = new ServletHolder(new ConfigServlet());
		handler.addServlet(holder, "/*");
		jetty.setHandler(handler);

		jetty.start();
	}

	@Test
	public void testGet_HttpUrl() {

		String url = "http://localhost:12025/";
		Cli cli = CliConfigurationSourceTest.createCli(url);
		String config = new CliConfigurationSource(cli, mockBootLogger).get().map(configReader).collect(joining(";"));
		assertEquals("g: h", config);
	}

	static class ConfigServlet extends HttpServlet {
		private static final long serialVersionUID = -5746986231054267492L;

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().write("g: h");
		}
	}
}

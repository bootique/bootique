package io.bootique.resource;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ResourceFactory_WebConfigSourceIT {

    private Server jetty;

    @After
    public void after() throws Exception {
        jetty.stop();
    }

    @Before
    public void before() throws Exception {

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
    public void testReadConfig_HttpUrl() throws IOException {
        String url = "http://localhost:12025/";
        assertEquals("g: h", ResourceFactoryTest.resourceContents(url));
    }

    static class ConfigServlet extends HttpServlet {
        private static final long serialVersionUID = -5746986231054267492L;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().write("g: h");
        }
    }
}

package com.nhl.bootique.resource;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FolderResourceFactoryTest {

    static String resourceContents(String folder, String resourceId) throws IOException {
        URL url = new FolderResourceFactory(folder).getUrl(resourceId);

        assertNotNull(url);

        try (Scanner scanner = new Scanner(url.openStream(), "UTF-8")) {
            return scanner.useDelimiter("\\Z").nextLine();
        }
    }

    @Test
    public void testGetUrl_RootClasspathUrl_Slash() throws IOException {
        String url = new FolderResourceFactory("classpath:/").getUrl().toExternalForm();
        assertNotNull(url);
        assertTrue(url.endsWith("/"));
    }

    @Test
    public void testGetUrl_RootClasspathUrl_NoSlash() throws IOException {
        String url = new FolderResourceFactory("classpath:").getUrl().toExternalForm();
        assertNotNull(url);
        assertTrue(url.endsWith("/"));
    }

    @Test
    public void testGetUrl_Subresource_RootClasspathUrl() throws IOException {
        assertEquals("c: d", resourceContents("classpath:", "com/nhl/bootique/config/test2.yml"));
    }

    @Test
    public void testGetUrl_Subresource_ClasspathUrl() throws IOException {
        assertEquals("c: d", resourceContents("classpath:com/nhl/bootique/config", "test2.yml"));
    }
}

package io.bootique.resource;

import io.bootique.BootiqueException;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.*;

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
        assertEquals("c: d", resourceContents("classpath:", "io/bootique/config/test2.yml"));
    }

    @Test
    public void testGetUrl_Subresource_ClasspathUrl() throws IOException {
        assertEquals("c: d", resourceContents("classpath:io/bootique/config", "test2.yml"));
    }

    @Test
    public void testGetUrl_Subresource_FileProtocolUrl() throws IOException {
        String folder = FolderResourceFactoryTest.class.getResource("/io/bootique/config").getPath();
        assertEquals("c: d", resourceContents("file:" + folder, "test2.yml"));
        assertEquals("c: d", resourceContents("file://" + folder, "test2.yml"));
    }

    @Test
    public void testGetUrl_Exception_InvalidScheme() throws IOException {
        try {
            resourceContents("Z:/a/b/c", "test2.yml");
            fail("Expected exception was not thrown.");
        } catch (BootiqueException e) {
            assertEquals(1, e.getOutcome().getExitCode());
            assertEquals("Invalid config resource url: Z:/a/b/c/test2.yml", e.getMessage());
        }
    }

    @Test
    public void testGetUrl_Subresource_ReverseSlashes() throws IOException {
        try {
            resourceContents("\\a\\b\\c", "test2.yml");
            fail("Expected exception was not thrown.");
        } catch (BootiqueException e) {
            assertEquals(1, e.getOutcome().getExitCode());
            assertEquals("Invalid config resource url: \\a\\b\\c/test2.yml", e.getMessage());
        }
    }
}

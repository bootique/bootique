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

package io.bootique.resource;

import io.bootique.BootiqueException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

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

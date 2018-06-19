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

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Ruslan Ibragimov
 */
public class UrlResolverTest {

    static String resourceContents(URL url) throws IOException {
        assertNotNull(url);

        try (Scanner scanner = new Scanner(url.openStream(), "UTF-8")) {
            return scanner.useDelimiter("\\Z").nextLine();
        }
    }

    @Test
    public void testGetCanonicalFile() throws IOException {
        File file = UrlResolver.getCanonicalFile("./src/test/resources/io/bootique/config/test1.yml");
        File expected = new File(
                System.getProperty("user.dir") + "/src/test/resources/io/bootique/config/test1.yml"
        );
        assertEquals(expected, file);
    }

    @Test
    public void testResolveClasspathUrlSingle() throws IOException {
        final URL url = UrlResolver.resolveClasspathUrl(
                "classpath:io/bootique/config/test1.yml",
                false,
                UrlResolver.DEFAULT_RESOURCE_LOADER
        );

        assertEquals("a: b", resourceContents(url));
    }

    @Test(expected = IllegalStateException.class)
    public void testResolveClasspathUrlMultiple() throws IOException {
        final URL fiction = new URL("file://test.yml");

        UrlResolver.resolveClasspathUrl(
                "classpath:test.yml",
                true,
                path -> asList(fiction, fiction)
        );
    }
}

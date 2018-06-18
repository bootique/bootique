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

/**
 * Licensed to the ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * “License”); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique.resource;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ResourceFactoryTest {

    private static String fileUrl(String path) {
        try {
            return new File(path).toURI().toURL().toExternalForm();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String jarEntryUrl(String jarPath, String entryPath) {
        return String.format("jar:%s!/%s", fileUrl(jarPath), entryPath);
    }

    static String resourceContents(String resourceId) throws IOException {
        URL url = new ResourceFactory(resourceId).getUrl();

        assertNotNull(url);

        try (Scanner scanner = new Scanner(url.openStream(), "UTF-8")) {
            return scanner.useDelimiter("\\Z").nextLine();
        }
    }

    @Test
    public void testGetUrl_File() throws IOException {
        assertEquals("a: b", resourceContents("src/test/resources/io/bootique/config/test1.yml"));
    }

    @Test
    public void testGetUrl_File_DotSlash() throws IOException {
        assertEquals("a: b", resourceContents("./src/test/resources/io/bootique/config/test1.yml"));
    }

    @Test
    public void testGetUrl_FileUrl() throws IOException {
        String fileUrl = fileUrl("src/test/resources/io/bootique/config/test2.yml");
        assertEquals("c: d", resourceContents(fileUrl));
    }

    @Test
    public void testGetUrl_JarUrl() throws IOException {
        String jarUrl = jarEntryUrl("src/test/resources/io/bootique/config/test3.jar", "com/foo/test3.yml");
        assertEquals("e: f", resourceContents(jarUrl));
    }

    @Test
    public void testGetUrl_ClasspathUrl() throws IOException {
        String cpUrl = "classpath:io/bootique/config/test2.yml";
        assertEquals("c: d", resourceContents(cpUrl));
    }

    @Test(expected = RuntimeException.class)
    public void testGetUrl_ClasspathUrlWithSlash() throws IOException {
        String cpUrl = "classpath:/io/bootique/config/test2.yml";
        resourceContents(cpUrl);
    }
}

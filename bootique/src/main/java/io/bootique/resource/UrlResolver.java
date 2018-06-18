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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Returns a URL to access resource contents.
 *
 * @author Ruslan Ibragimov
 */
public final class UrlResolver {
    static final String CLASSPATH_URL_PREFIX = "classpath:";

    /**
     * Extract loading resources from classloader to function in testing purposes.
     * Now we can pass any resource loader to {@link UrlResolver#resolveClasspathUrl} method.
     */
    static final Function<String, List<URL>> DEFAULT_RESOURCE_LOADER = (String path) -> {
        try {
            return Collections.list(
                    UrlResolver.class.getClassLoader().getResources(path)
            );
        } catch (IOException e) {
            throw new RuntimeException("I/O errors occurs when reading classpath resource.", e);
        }
    };

    /**
     * Returns a URL to access resource contents.
     *
     * @param resourceId resource identifier
     * @param checkIsMultiple in case of classpath url there are
     *                        possible multiple resources under one path
     *                        so to avoid ambiguity
     * @return Returns a URL to access resource contents.
     */
    public static URL resolveUrl(String resourceId, boolean checkIsMultiple) {
        // resourceId can be either a file path or a URL or a classpath: URL
        if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {
            return resolveClasspathUrl(resourceId, checkIsMultiple, DEFAULT_RESOURCE_LOADER);
        } else {
            return resolveFilesystemUrl(resourceId);
        }
    }

    static URL resolveClasspathUrl(
            String resourceId,
            boolean checkIsMultiple,
            Function<String, List<URL>> resourceLoader
    ) {
        String path = resourceId.substring(CLASSPATH_URL_PREFIX.length());

        // classpath URLs must not start with a slash. This does not work
        // with ClassLoader.
        if (path.length() > 0 && path.charAt(0) == '/') {
            throw new RuntimeException(CLASSPATH_URL_PREFIX + " URLs must not start with a slash: " + resourceId);
        }

        final List<URL> urls = resourceLoader.apply(path);

        if (urls.size() == 0) {
            throw new IllegalArgumentException("Classpath URL not found: " + resourceId);
        }

        if (checkIsMultiple) {
            if (urls.size() > 1) {
                throw new IllegalStateException("More than one file found in classpath for path: " + resourceId);
            }
        }

        return urls.get(0);
    }

    static URL resolveFilesystemUrl(String resourceId) {
        URI uri;
        try {
            uri = URI.create(resourceId);
        } catch (IllegalArgumentException e) {
            throw new BootiqueException(1, "Invalid config resource url: " + resourceId, e);
        }
        try {
            return uri.isAbsolute() ? uri.toURL() : getCanonicalFile(resourceId).toURI().toURL();
        } catch (IOException e) {
            throw new BootiqueException(1, "Invalid config resource url: " + resourceId, e);
        }
    }

    /**
     * Converts resource ID to a canonical file.
     * <p>
     * Using canonical file avoids downstream bugs like this:
     * <a href="https://github.com/bootique/bootique-jetty/issues/29">bootique/bootique-jetty#29</a>
     *
     * @return canonical file produced from resource id.
     * @throws IOException if an I/O error occurs
     */
    static File getCanonicalFile(String resourceId) throws IOException {
        return new File(resourceId).getCanonicalFile();
    }
}

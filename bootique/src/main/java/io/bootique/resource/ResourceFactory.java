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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * A value object representing a resource URL. Supports the following resource representations:
 * <p>
 * <ul>
 * <li>resource as a URL using the protocols recognized by Java (http:, https:, jar:, file:, etc).</li>
 * <li>resource as URL with "classpath:" protocol that identifies resources on classpath in a portable manner.
 * E.g., the same URL would identify the resource regardless of whether it is packaged in a jar or resides in a
 * source folder in an IDE.</li>
 * <li>resource as absolute or relative file path.</li>
 * </ul>
 */
public class ResourceFactory {

    protected static final String CLASSPATH_URL_PREFIX = "classpath:";

    protected String resourceId;

    /**
     * Creates a ResourceFactory passing it a String resource identifier. It can be one of
     * <ul>
     * <li>a URL string using the protocols recognized by Java (http:, https:, jar:, file:, etc).</li>
     * <li>a URL string starting with "classpath:" protocol</li>
     * <li>an absolute or relative file path.</li>
     * </ul>
     *
     * @param resourceId a String identifier of the resource.
     */
    public ResourceFactory(String resourceId) {
        this.resourceId = Objects.requireNonNull(resourceId);
    }

    /**
     * Returns a URL to access resource contents.
     *
     * @return a URL to access resource contents.
     */
    public URL getUrl() {
        return resolveUrl(this.resourceId);
    }

    /**
     * Returns a collection of URLs that matched this resource id. Normally this will be the equivalent of {@link #getUrl()},
     * however sometimes "classpath:" URLs can have duplicates.
     *
     * @return a collection of URLs that matched this resource id.
     * @since 2.0
     */
    public Collection<URL> getUrls() {
        return resolveUrls(this.resourceId);
    }

    /**
     * Returns resource ID string used to initialize this ResourceFactory.
     *
     * @return resource ID string used to initialize this ResourceFactory.
     */
    public String getResourceId() {
        return resourceId;
    }

    protected Collection<URL> resolveUrls(String resourceId) {

        // resourceId can be either a file path or a URL or a classpath: URL
        if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {

            String path = resolveAsClasspath(resourceId);

            Enumeration<URL> cpUrls;
            try {
                cpUrls = ResourceFactory.class.getClassLoader().getResources(path);
            } catch (IOException e) {
                throw new RuntimeException("Can't resolve resources for path: " + path, e);
            }

            if (!cpUrls.hasMoreElements()) {
                throw new IllegalArgumentException("Classpath URL not found: " + resourceId);
            }

            List<URL> urls = new ArrayList<>(2);
            while (cpUrls.hasMoreElements()) {
                urls.add(cpUrls.nextElement());
            }

            return urls;
        }

        return Collections.singletonList(resolveAsUri(resourceId));
    }

    protected URL resolveUrl(String resourceId) {

        // resourceId can be either a file path or a URL or a classpath: URL
        if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {

            String path = resolveAsClasspath(resourceId);
            URL cpUrl = ResourceFactory.class.getClassLoader().getResource(path);
            if (cpUrl == null) {
                throw new IllegalArgumentException("Classpath URL not found: " + resourceId);
            }

            return cpUrl;
        }

        return resolveAsUri(resourceId);
    }

    protected String resolveAsClasspath(String resourceId) {
        String path = resourceId.substring(CLASSPATH_URL_PREFIX.length());

        // classpath URLs must not start with a slash. This does not work with ClassLoader.
        // TODO: should we silently strip the leading path?
        if (path.length() > 0 && path.charAt(0) == '/') {
            throw new RuntimeException(CLASSPATH_URL_PREFIX + " URLs must not start with a slash: " + resourceId);
        }

        return path;
    }

    protected URL resolveAsUri(String resourceId) {
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
     *
     * @return canonical file produced from resource id.
     * @throws IOException
     */
    // using canonical file avoids downstream bugs like this:
    // https://github.com/bootique/bootique-jetty/issues/29
    protected File getCanonicalFile(String resourceId) throws IOException {
        return new File(resourceId).getCanonicalFile();
    }

    @Override
    public String toString() {
        return "ResourceFactory:" + resourceId;
    }

}

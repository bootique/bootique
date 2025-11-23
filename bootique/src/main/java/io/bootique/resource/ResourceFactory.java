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

import java.net.URL;
import java.util.Collection;
import java.util.Objects;

/**
 * A value object representing a resource URL. Supports the following resource representations:
 * <p>
 * <ul>
 * <li>resource as a URL using the protocols recognized by Java (http:, https:, jar:, file:, etc).</li>
 * <li>resource as URL with "classpath:" protocol that identifies resources on classpath in a portable manner.
 * E.g., the same URL would identify the resource regardless of whether it is packaged in a jar or resides in a
 * source folder in an IDE.</li>
 * <li>resource as a URL with "stdin:" protocol, that allows to access standard input. Requires format specification,
 * e.g., "stdin:json", "stdin:yaml".</li>
 * <li>resource as absolute or relative file path.</li>
 * </ul>
 */
public class ResourceFactory {

    protected static final String CLASSPATH_URL_PREFIX = "classpath:";
    protected static final String STDIN_URL_PREFIX = "stdin:";

    protected String resourceId;

    /**
     * Creates a ResourceFactory passing it a String resource identifier. It can be one of
     * <ul>
     * <li>a URL string using the protocols recognized by Java (http:, https:, jar:, file:, etc).</li>
     * <li>a URL string starting with "classpath:" protocol</li>
     * <li>a URL string starting with "stdin:" protocol</li>
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

        if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {
            return ClasspathUrlResolver.resolveCollection(resourceId);
        } else if (resourceId.startsWith(STDIN_URL_PREFIX)) {
            return StdinUrlResolver.resolveCollection(resourceId);
        } else {
            return UrlOrFileResolver.resolveCollection(resourceId);
        }
    }

    protected URL resolveUrl(String resourceId) {

        if (resourceId.startsWith(CLASSPATH_URL_PREFIX)) {
            return ClasspathUrlResolver.resolveSingle(resourceId);
        } else if (resourceId.startsWith(STDIN_URL_PREFIX)) {
            return StdinUrlResolver.resolveSingle(resourceId);
        } else {
            return UrlOrFileResolver.resolveSingle(resourceId);
        }
    }

    @Override
    public String toString() {
        return "ResourceFactory:" + resourceId;
    }
}

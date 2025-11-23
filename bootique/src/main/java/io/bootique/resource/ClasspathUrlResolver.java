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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

class ClasspathUrlResolver {

    public static URL resolveSingle(String resourceId) {
        String path = resolveAsClasspath(resourceId);
        URL cpUrl = ResourceFactory.class.getClassLoader().getResource(path);
        if (cpUrl == null) {
            throw new IllegalArgumentException("Classpath URL not found: " + resourceId);
        }

        return cpUrl;
    }

    public static Collection<URL> resolveCollection(String resourceId) {

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

        List<URL> urls = new ArrayList<>(3);
        while (cpUrls.hasMoreElements()) {
            urls.add(cpUrls.nextElement());
        }

        return urls;
    }

    private static String resolveAsClasspath(String resourceId) {
        String path = resourceId.substring(ResourceFactory.CLASSPATH_URL_PREFIX.length());

        // classpath URLs must not start with a slash. This does not work with ClassLoader.
        // TODO: should we silently strip the leading path?
        if (path.length() > 0 && path.charAt(0) == '/') {
            throw new RuntimeException(ResourceFactory.CLASSPATH_URL_PREFIX + " URLs must not start with a slash: " + resourceId);
        }

        return path;
    }
}

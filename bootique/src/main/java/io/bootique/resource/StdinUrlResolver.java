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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

class StdinUrlResolver {

    public static URL resolveSingle(String resourceId) {
        URI uri;
        try {
            uri = URI.create(resourceId);
        } catch (IllegalArgumentException e) {
            throw new BootiqueException(1, "Invalid config resource url: " + resourceId, e);
        }

        String contentType = parseContentType(resourceId);

        try {
            return URL.of(uri, new StdinUrlStreamHandler(contentType));
        } catch (MalformedURLException e) {
            throw new BootiqueException(1, "Invalid config resource url: " + resourceId, e);
        }
    }

    static String parseContentType(String resourceId) {
        String format = resourceId.substring(ResourceFactory.STDIN_URL_PREFIX.length());
        return switch (format) {
            case "json" -> "application/json";
            case "yaml", "yml" -> "application/x-yaml";
            default -> "application/octet-stream";
        };
    }

    public static Collection<URL> resolveCollection(String resourceId) {
        return List.of(resolveSingle(resourceId));
    }

}

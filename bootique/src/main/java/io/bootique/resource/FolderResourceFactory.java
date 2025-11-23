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

import java.io.File;
import java.net.URL;
import java.util.Objects;

/**
 * A {@link ResourceFactory} that corresponds to a "folder". Cleans up the resource ID to correct missing trailing
 * slashes.
 */
public class FolderResourceFactory extends ResourceFactory {

    static String normalizeResourceId(String resourceId) {

        // folder resources must end with a slash. Otherwise, relative URLs won't resolve properly

        if (resourceId.length() == 0) {
            return normalizeResourceId(getUserDir());
        }

        if (resourceId.startsWith(ResourceFactory.CLASSPATH_URL_PREFIX)) {

            String classpath = resourceId.substring(ResourceFactory.CLASSPATH_URL_PREFIX.length());
            if (classpath.length() == 0) {
                return ResourceFactory.CLASSPATH_URL_PREFIX;
            }

            // classpath:/ is invalid
            if (classpath.equals("/")) {
                return ResourceFactory.CLASSPATH_URL_PREFIX;
            }
        }

        return resourceId.endsWith("/") ? resourceId : resourceId + "/";
    }

    /**
     * @return Absolute URI for current working directory (without trailing forward slash)
     */
    private static String getUserDir() {
        String userDir = System.getProperty("user.dir");
        if (!userDir.startsWith("/")) {
            userDir = "/" + userDir;
        }
        if (userDir.endsWith("/")) {
            userDir = userDir.substring(0, userDir.length() - 1);
        }
        return "file://" + slashify(userDir);
    }

    /**
     * Converts abstract pathname into URI path (replacing system-dependent name separators with forward slashes)
     */
    private static String slashify(String path) {
        return File.separatorChar != '/' ? Objects.requireNonNull(path).replace(File.separatorChar, '/') : path;
    }

    public FolderResourceFactory(String resourceId) {
        super(normalizeResourceId(Objects.requireNonNull(resourceId)));
    }

    /**
     * Returns a URL of a resource based on a path relative to this folder.
     *
     * @param subResourcePath a path relative to this folder that points to a resource.
     * @return a URL of the specified resource located within the folder.
     */
    public URL getUrl(String subResourcePath) {

        if (subResourcePath.startsWith("/")) {
            subResourcePath = subResourcePath.substring(1);
        }

        return resolveUrl(this.resourceId + subResourcePath);
    }

    @Override
    public String toString() {
        return "FolderResourceFactory:" + resourceId;
    }
}

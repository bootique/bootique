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
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.resource;

import java.net.URL;
import java.util.Objects;

/**
 * A value object representing a resource URL. Supports 3 common resource
 * representations:
 * <p>
 * <ul>
 * <li>resource as a URL using protocols recognized by Java (http:, https:,
 * jar:, file:, etc).</li>
 * <li>resource as URL with "classpath:" protocol that allows to identify
 * resources on classpath in a portable manner. E.g. the same URL would identify
 * the resource regardless of whether it is packaged in a jar or resides in a
 * source folder in an IDE.</li>
 * <li>resource as absolute or relative file path.</li>
 * </ul>
 *
 * @since 0.15
 */
public class ResourceFactory {

    protected String resourceId;

    /**
     * Creates a ResourceFactory passing it a String resource identifier. It can
     * be one of
     * <ul>
     * <li>resource as a URL using protocols recognized by Java (http:, https:,
     * jar:, file:, etc).</li>
     * <li>resource as URL with "classpath:" protocol that allows to identify
     * resources on classpath in a portable manner. E.g. the same URL would
     * identify the resource regardless of whether it is packaged in a jar or
     * resides in a source folder in an IDE.</li>
     * <li>resource as absolute or relative file path.</li>
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
        return UrlResolver.resolveUrl(this.resourceId, false);
    }

    /**
     * Returns resource ID string used to initialize this ResourceFactory.
     *
     * @return resource ID string used to initialize this ResourceFactory.
     * @since 0.21
     */
    public String getResourceId() {
        return resourceId;
    }

    @Override
    public String toString() {
        return "ResourceFactory:" + resourceId;
    }
}

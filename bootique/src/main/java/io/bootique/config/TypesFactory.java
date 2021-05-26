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

package io.bootique.config;

import io.bootique.log.BootLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Provides access to a set of types loaded from a given META-INF/services/ descriptor.
 */
public class TypesFactory<T> {

    private BootLogger logger;
    private ClassLoader classLoader;
    private Class<T> serviceType;

    public TypesFactory(ClassLoader classLoader, Class<T> serviceType, BootLogger logger) {
        this.logger = logger;
        this.classLoader = classLoader;
        this.serviceType = serviceType;
    }

    // TODO: presumably this is not called more than once in an app, but if it is, we
    // must start caching the types...
    public Collection<Class<? extends T>> getTypes() {

        Collection<Class<? extends T>> types;
        try {
            types = resolveTypes();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Couldn't load subclasses for type: " + serviceType.getName(), e);
        }

        return types;
    }

    protected Collection<Class<? extends T>> resolveTypes() throws IOException, ClassNotFoundException {

        // note that unlike java.util.ServiceLoader, ConfigTypesFactory can work with abstract supertypes
        // as they are not instantiated....

        Collection<Class<? extends T>> types = new ArrayList<>();

        String location = serviceLocation();
        Enumeration<URL> serviceLists = classLoader.getResources(location);
        while (serviceLists.hasMoreElements()) {
            appendTypes(serviceLists.nextElement(), types);
        }

        return types;
    }

    protected void appendTypes(
            URL url,
            Collection<Class<? extends T>> subclasses) throws IOException, ClassNotFoundException {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                subclasses.add(loadClass(line.trim()));
            }
        }
    }

    protected Class<? extends T> loadClass(String className) throws ClassNotFoundException {
        logger.trace(() -> "Loading config subtype: " + className);
        return (Class<? extends T>) classLoader.loadClass(className);
    }

    protected String serviceLocation() {
        return "META-INF/services/" + serviceType.getName();
    }
}
